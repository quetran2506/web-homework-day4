package vn.iotstar.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.entity.User;
import vn.iotstar.service.EmailChangeRequestStatus;
import vn.iotstar.service.OtpResult;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.CloudinaryStorage;

@WebServlet("/profile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5_242_880L,
        maxRequestSize = 6_291_456L)
public class ProfileController extends HttpServlet {
    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+(). -]{7,32}$");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final UserService userService = new UserServiceImpl();
    private final CloudinaryStorage cloudinary = new CloudinaryStorage();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showProfile(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = value(request, "action");
        try {
            switch (action) {
                case "updateProfile" -> updateProfile(request, response);
                case "requestEmailChange" -> requestEmailChange(request, response);
                case "confirmEmailChange" -> confirmEmailChange(request, response);
                default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác hồ sơ không hợp lệ.");
            }
        } catch (SQLException ex) {
            getServletContext().log("Profile update failed because of a database error", ex);
            request.setAttribute("error", "Không cập nhật được hồ sơ. Hãy kiểm tra kết nối cơ sở dữ liệu.");
            showProfile(request, response);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            showProfile(request, response);
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Ảnh hồ sơ cần nhỏ hơn 5 MB.");
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        User user = currentUser(request);
        String fullName = value(request, "fullName");
        String phone = value(request, "phone");
        validateProfile(fullName, phone);
        String normalizedPhone = phone.isBlank() ? null : phone;

        CloudinaryStorage.UploadedImage uploaded = null;
        Part avatar = request.getPart("avatar");
        String avatarFilename = null;
        if (avatar != null && avatar.getSize() > 0) {
            avatarFilename = validateAvatar(avatar);
            if (!cloudinary.isConfigured()) {
                request.setAttribute("error", "Chưa cấu hình CLOUDINARY_* trong terminal chạy Tomcat.");
                showProfile(request, response);
                return;
            }
            try (InputStream input = avatar.getInputStream()) {
                uploaded = cloudinary.upload(input.readAllBytes(), avatarFilename, user.getId());
            } catch (Exception ex) {
                getServletContext().log("Cloudinary avatar upload failed", ex);
                request.setAttribute("error", "Không tải được ảnh lên Cloudinary. Kiểm tra CLOUDINARY_* rồi thử lại.");
                showProfile(request, response);
                return;
            }
        }

        String previousPublicId;
        try {
            previousPublicId = userService.updateProfile(user.getId(), fullName, normalizedPhone,
                    uploaded == null ? null : uploaded.url(), uploaded == null ? null : uploaded.publicId());
        } catch (SQLException ex) {
            deleteUploaded(uploaded);
            throw ex;
        }

        user.setFullName(fullName);
        user.setPhone(normalizedPhone);
        if (uploaded != null) {
            user.setProfileImageUrl(uploaded.url());
            user.setCloudinaryPublicId(uploaded.publicId());
            if (previousPublicId != null && !previousPublicId.equals(uploaded.publicId())) {
                try {
                    cloudinary.delete(previousPublicId);
                } catch (Exception ex) {
                    getServletContext().log("Could not remove the previous Cloudinary profile image", ex);
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/profile?updated=1");
    }

    private void requestEmailChange(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        User user = currentUser(request);
        String newEmail = value(request, "newEmail").toLowerCase(Locale.ROOT);
        request.setAttribute("newEmail", newEmail);
        EmailChangeRequestStatus result = userService.requestEmailChange(user, newEmail);
        switch (result) {
            case SENT -> {
                user.setPendingEmail(newEmail);
                response.sendRedirect(request.getContextPath() + "/profile?emailOtpSent=1");
                return;
            }
            case EMAIL_FAILED -> {
                user.setPendingEmail(newEmail);
                request.setAttribute("error", "Đã tạo yêu cầu nhưng chưa gửi được email. Kiểm tra cấu hình MAIL_*.");
            }
            case RATE_LIMITED -> request.setAttribute("error", "Bạn vừa yêu cầu mã. Chờ ít nhất 60 giây rồi thử lại.");
            case EMAIL_TAKEN -> request.setAttribute("error", "Email này đã được tài khoản khác sử dụng.");
            case SAME_EMAIL -> request.setAttribute("error", "Email mới trùng với email hiện tại.");
            case INVALID_EMAIL -> request.setAttribute("error", "Email mới chưa đúng định dạng hoặc quá dài.");
            case NOT_FOUND -> request.setAttribute("error", "Không tìm thấy tài khoản đang đăng nhập.");
        }
        showProfile(request, response);
    }

    private void confirmEmailChange(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        User user = currentUser(request);
        String pendingEmail = user.getPendingEmail();
        OtpResult result = userService.confirmEmailChange(user, value(request, "code"));
        if (result == OtpResult.SUCCESS) {
            user.setEmail(pendingEmail);
            user.setPendingEmail(null);
            response.sendRedirect(request.getContextPath() + "/profile?emailChanged=1");
            return;
        }
        if (result == OtpResult.EMAIL_TAKEN) {
            user.setPendingEmail(null);
            request.setAttribute("error", "Email này hiện đã được dùng bởi tài khoản khác. Yêu cầu mã cho email khác.");
        } else {
            request.setAttribute("error", otpError(result));
        }
        showProfile(request, response);
    }

    private void validateProfile(String fullName, String phone) {
        if (fullName.isBlank() || fullName.length() > 120) {
            throw new IllegalArgumentException("Họ tên không được để trống và tối đa 120 ký tự.");
        }
        if (!phone.isBlank() && (!PHONE.matcher(phone).matches()
                || phone.replaceAll("\\D", "").length() < 7
                || phone.replaceAll("\\D", "").length() > 15)) {
            throw new IllegalArgumentException("Số điện thoại cần có 7-15 chữ số.");
        }
    }

    private String validateAvatar(Part part) throws IOException {
        if (part.getSize() > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Ảnh hồ sơ cần nhỏ hơn 5 MB.");
        }
        String submitted = part.getSubmittedFileName();
        String filename = submitted == null ? "" : java.nio.file.Path.of(submitted).getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String extension = dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Chọn ảnh JPG, PNG, WebP hoặc GIF.");
        }
        try (InputStream input = part.getInputStream()) {
            byte[] signature = input.readNBytes(12);
            if (!isImageSignature(signature)) {
                throw new IllegalArgumentException("Tệp đã chọn không phải ảnh hợp lệ.");
            }
        }
        return filename;
    }

    private boolean isImageSignature(byte[] bytes) {
        boolean png = bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G' && (bytes[4] & 0xff) == 0x0d
                && (bytes[5] & 0xff) == 0x0a && (bytes[6] & 0xff) == 0x1a && (bytes[7] & 0xff) == 0x0a;
        boolean jpeg = bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        boolean gif = bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a';
        boolean webp = bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        return png || jpeg || gif || webp;
    }

    private String otpError(OtpResult result) {
        return switch (result) {
            case EXPIRED -> "Mã OTP đã hết hạn. Hãy yêu cầu mã mới.";
            case TOO_MANY_ATTEMPTS -> "Bạn đã nhập sai quá số lần. Hãy yêu cầu mã mới.";
            case NOT_FOUND -> "Không có yêu cầu đổi email còn hiệu lực. Hãy gửi OTP mới.";
            case ALREADY_DONE -> "Mã OTP này đã được sử dụng.";
            default -> "Mã OTP chưa đúng.";
        };
    }

    private void deleteUploaded(CloudinaryStorage.UploadedImage uploaded) {
        if (uploaded == null) return;
        try {
            cloudinary.delete(uploaded.publicId());
        } catch (Exception ex) {
            getServletContext().log("Could not clean up an unused Cloudinary image", ex);
        }
    }

    private void showProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("user", user);
        if (request.getAttribute("profileFullName") == null) {
            String entered = request.getParameter("fullName");
            request.setAttribute("profileFullName", entered == null ? user.getFullName() : entered);
        }
        if (request.getAttribute("profilePhone") == null) {
            String entered = request.getParameter("phone");
            request.setAttribute("profilePhone", entered == null ? user.getPhone() : entered);
        }
        if (request.getParameter("updated") != null) request.setAttribute("message", "Đã cập nhật hồ sơ.");
        if (request.getParameter("emailOtpSent") != null) {
            request.setAttribute("message", "Đã gửi OTP đến email mới. Mã có hiệu lực 10 phút.");
        }
        if (request.getParameter("emailChanged") != null) request.setAttribute("message", "Đã xác nhận và đổi email thành công.");
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    private User currentUser(HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        Object account = session == null ? null : session.getAttribute("account");
        return account instanceof User user ? user : null;
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
