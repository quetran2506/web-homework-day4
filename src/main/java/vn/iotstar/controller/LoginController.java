package vn.iotstar.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.entity.User;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.CookieUtil;

@WebServlet("/login")
public class LoginController extends HttpServlet {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,64}$");
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (currentUser(request) != null) {
            String destination = destination(request.getParameter("next"));
            response.sendRedirect(request.getContextPath() + destination);
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = value(request, "username");
        String password = request.getParameter("password");
        if (!USERNAME.matcher(username).matches() || password == null || password.isBlank() || password.length() < 8 || password.length() > 128) {
            request.setAttribute("error", "Tên đăng nhập cần 3-64 ký tự hợp lệ và mật khẩu cần từ 8 đến 128 ký tự.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userService.login(username, password);
            if (user == null) {
                request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu chưa đúng.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }
            if (!user.isActive()) {
                request.setAttribute("activationEmail", user.getEmail());
                request.setAttribute("error", "Tài khoản chưa được kích hoạt. Hãy xác minh mã OTP gửi qua email.");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }

            HttpSession previous = request.getSession(false);
            if (previous != null) {
                previous.invalidate();
            }
            request.getSession(true).setAttribute("account", user);

            String oldToken = CookieUtil.readRememberToken(request);
            if ("on".equals(request.getParameter("remember"))) {
                if (oldToken != null) {
                    userService.revokeRememberToken(oldToken);
                }
                CookieUtil.setRememberToken(request, response, userService.issueRememberToken(user));
            } else {
                if (oldToken != null) {
                    userService.revokeRememberToken(oldToken);
                }
                CookieUtil.clearRememberToken(request, response);
            }
            String destination = destination(request.getParameter("next"));
            response.sendRedirect(request.getContextPath() + destination);
        } catch (SQLException ex) {
            getServletContext().log("Login failed because the database is unavailable", ex);
            request.setAttribute("error", "Chưa kết nối được cơ sở dữ liệu. Hãy kiểm tra cấu hình SQL Server.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("account") instanceof User user ? user : null;
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private String destination(String next) {
        if ("category".equals(next)) return "/admin/category/list";
        if ("video".equals(next)) return "/video/manage";
        return "/home";
    }
}
