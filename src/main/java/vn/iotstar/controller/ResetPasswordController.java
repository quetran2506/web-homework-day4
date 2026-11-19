package vn.iotstar.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.service.OtpResult;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;

@WebServlet("/reset-password")
public class ResetPasswordController extends HttpServlet {
    private static final Pattern CODE = Pattern.compile("^[0-9]{6}$");
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("email", value(request, "email"));
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = value(request, "email").toLowerCase(java.util.Locale.ROOT);
        String code = value(request, "code");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");
        request.setAttribute("email", email);

        if (!CODE.matcher(code).matches()) {
            request.setAttribute("error", "Mã OTP phải gồm 6 chữ số.");
        } else if (password == null || password.length() < 8 || password.length() > 128) {
            request.setAttribute("error", "Mật khẩu mới cần từ 8 đến 128 ký tự.");
        } else if (!password.equals(confirm)) {
            request.setAttribute("error", "Mật khẩu xác nhận chưa khớp.");
        } else {
            try {
                OtpResult result = userService.resetPassword(email, code, password);
                if (result == OtpResult.SUCCESS) {
                    response.sendRedirect(request.getContextPath() + "/login?passwordReset=1");
                    return;
                }
                request.setAttribute("error", switch (result) {
                    case EXPIRED -> "Mã OTP đã hết hạn. Hãy yêu cầu mã mới.";
                    case TOO_MANY_ATTEMPTS -> "Bạn đã nhập sai quá số lần cho phép. Hãy yêu cầu mã mới.";
                    case NOT_FOUND -> "Không tìm thấy OTP đặt lại mật khẩu cho email này.";
                    default -> "Mã OTP chưa đúng hoặc đã được sử dụng.";
                });
            } catch (SQLException ex) {
                getServletContext().log("Password reset failed", ex);
                request.setAttribute("error", "Không đổi được mật khẩu do lỗi cơ sở dữ liệu.");
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
