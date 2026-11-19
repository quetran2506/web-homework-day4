package vn.iotstar.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.service.OtpRequestStatus;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;

@WebServlet("/forgot-password")
public class ForgotPasswordController extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = value(request, "email").toLowerCase(java.util.Locale.ROOT);
        request.setAttribute("email", email);
        if (email.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập email.");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            return;
        }
        if (email.length() > 255 || !EMAIL.matcher(email).matches()) {
            request.setAttribute("error", "Email chưa đúng định dạng.");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            return;
        }
        try {
            OtpRequestStatus status = userService.requestPasswordReset(email);
            if (status == OtpRequestStatus.SENT || status == OtpRequestStatus.NOT_FOUND) {
                request.setAttribute("sent", true);
                request.setAttribute("message", "Nếu email thuộc tài khoản đã kích hoạt, mã đặt lại mật khẩu sẽ được gửi. Hãy kiểm tra hộp thư.");
            } else if (status == OtpRequestStatus.RATE_LIMITED) {
                request.setAttribute("error", "Bạn vừa yêu cầu mã. Chờ ít nhất 60 giây trước khi thử lại.");
            } else {
                request.setAttribute("error", "Không gửi được email OTP. Hãy kiểm tra cấu hình MAIL_* rồi thử lại.");
            }
        } catch (SQLException ex) {
            getServletContext().log("Password reset request failed", ex);
            request.setAttribute("error", "Không tạo được yêu cầu do lỗi cơ sở dữ liệu.");
        }
        request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
