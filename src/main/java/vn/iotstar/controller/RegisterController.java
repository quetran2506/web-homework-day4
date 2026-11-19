package vn.iotstar.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.service.RegistrationStatus;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;

@WebServlet("/register")
public class RegisterController extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,64}$");
    private static final Pattern PHONE = Pattern.compile("^[+0-9().\\s-]{7,32}$");
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = value(request, "fullName");
        String email = value(request, "email");
        String username = value(request, "username");
        String password = request.getParameter("password");
        String phone = value(request, "phone");
        String confirmPassword = request.getParameter("confirmPassword");

        if (fullName.isBlank() || fullName.length() > 120) {
            showError(request, response, "Họ tên không được để trống và tối đa 120 ký tự.");
            return;
        }
        if (!EMAIL.matcher(email).matches() || email.length() > 255) {
            showError(request, response, "Email chưa đúng định dạng.");
            return;
        }
        if (!USERNAME.matcher(username).matches()) {
            showError(request, response, "Tên đăng nhập cần 3-64 ký tự: chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
            return;
        }
        if (password == null || password.length() < 8 || password.length() > 128) {
            showError(request, response, "Mật khẩu cần từ 8 đến 128 ký tự.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(request, response, "Mật khẩu xác nhận chưa khớp.");
            return;
        }
        long phoneDigits = phone.chars().filter(Character::isDigit).count();
        if (!phone.isBlank() && (!PHONE.matcher(phone).matches() || phoneDigits < 7 || phoneDigits > 15)) {
            showError(request, response, "Số điện thoại cần có 7-15 chữ số; chỉ nhập số và ký tự + ( ) . -.");
            return;
        }

        try {
            RegistrationStatus result = userService.register(fullName, email, username, password, phone);
            if (result == RegistrationStatus.USERNAME_TAKEN) {
                showError(request, response, "Tên đăng nhập đã được sử dụng.");
            } else if (result == RegistrationStatus.EMAIL_TAKEN) {
                showError(request, response, "Email đã được đăng ký.");
            } else if (result == RegistrationStatus.ACTIVATION_EMAIL_FAILED) {
                response.sendRedirect(request.getContextPath() + "/activation?email="
                        + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&mailError=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/activation?email="
                        + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&sent=1");
            }
        } catch (SQLException ex) {
            getServletContext().log("Registration failed", ex);
            showError(request, response, "Không lưu được tài khoản. Hãy kiểm tra kết nối cơ sở dữ liệu.");
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
