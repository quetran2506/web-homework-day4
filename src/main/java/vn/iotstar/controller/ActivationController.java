package vn.iotstar.controller;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.service.OtpRequestStatus;
import vn.iotstar.service.OtpResult;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;

@WebServlet("/activation")
public class ActivationController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("email", value(request, "email"));
        if ("1".equals(request.getParameter("mailError"))) {
            request.setAttribute("error", "Chưa gửi được email OTP. Kiểm tra cấu hình MAIL_* rồi thử gửi lại.");
        } else if ("1".equals(request.getParameter("sent"))) {
            request.setAttribute("message", "Đã gửi OTP nếu cấu hình email hợp lệ. Mã có hiệu lực 10 phút.");
        }
        request.getRequestDispatcher("/WEB-INF/views/activation.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = value(request, "email").toLowerCase(java.util.Locale.ROOT);
        request.setAttribute("email", email);
        if (email.isBlank()) {
            request.setAttribute("error", "Nhập email đã dùng khi đăng ký.");
            request.getRequestDispatcher("/WEB-INF/views/activation.jsp").forward(request, response);
            return;
        }
        try {
            if ("resend".equals(request.getParameter("action"))) {
                OtpRequestStatus status = userService.requestActivation(email);
                switch (status) {
                    case SENT -> request.setAttribute("message", "Đã gửi mã OTP mới. Hãy kiểm tra email.");
                    case RATE_LIMITED -> request.setAttribute("error", "Bạn vừa yêu cầu mã. Chờ ít nhất 60 giây trước khi gửi lại.");
                    case EMAIL_FAILED -> request.setAttribute("error", "Không gửi được email. Kiểm tra cấu hình MAIL_*.");
                    case ALREADY_ACTIVE -> request.setAttribute("message", "Tài khoản đã kích hoạt. Bạn có thể đăng nhập.");
                    case NOT_FOUND -> request.setAttribute("error", "Không tìm thấy tài khoản cho email này.");
                }
            } else {
                OtpResult result = userService.activate(email, value(request, "code"));
                if (result == OtpResult.SUCCESS || result == OtpResult.ALREADY_DONE) {
                    response.sendRedirect(request.getContextPath() + "/login?activated=1");
                    return;
                }
                request.setAttribute("error", otpError(result));
            }
        } catch (SQLException ex) {
            getServletContext().log("Account activation failed", ex);
            request.setAttribute("error", "Không xác minh được mã do lỗi cơ sở dữ liệu.");
        }
        request.getRequestDispatcher("/WEB-INF/views/activation.jsp").forward(request, response);
    }

    private String otpError(OtpResult result) {
        return switch (result) {
            case EXPIRED -> "Mã OTP đã hết hạn. Hãy gửi mã mới.";
            case TOO_MANY_ATTEMPTS -> "Bạn đã nhập sai quá số lần cho phép. Hãy gửi mã mới.";
            case NOT_FOUND -> "Không có mã OTP còn hiệu lực cho email này. Hãy gửi mã mới.";
            default -> "Mã OTP chưa đúng.";
        };
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
