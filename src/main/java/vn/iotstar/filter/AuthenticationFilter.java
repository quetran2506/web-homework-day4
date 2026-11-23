package vn.iotstar.filter;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.entity.User;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.CookieUtil;

public class AuthenticationFilter implements Filter {
    private final UserService userService = new UserServiceImpl();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest rawRequest, jakarta.servlet.ServletResponse rawResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) rawRequest;
        HttpServletResponse response = (HttpServletResponse) rawResponse;
        String token = CookieUtil.readRememberToken(request);
        HttpSession session = request.getSession(false);

        if ((session == null || session.getAttribute("account") == null) && token != null) {
            try {
                User user = userService.findByRememberToken(token);
                if (user != null) {
                    request.getSession(true).setAttribute("account", user);
                } else {
                    userService.revokeRememberToken(token);
                    CookieUtil.clearRememberToken(request, response);
                }
            } catch (SQLException ex) {
                throw new ServletException("Could not restore the remembered login", ex);
            }
        }

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        if ((path.startsWith("/admin/category") || isVideoManagementPath(path)
                || path.equals("/profile")) && !isLoggedIn(request)) {
            String next = path.equals("/profile") ? "profile"
                    : path.startsWith("/video/") ? "video" : "category";
            response.sendRedirect(contextPath + "/login?next=" + next);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("account") instanceof User;
    }

    private boolean isVideoManagementPath(String path) {
        return path.equals("/video/manage") || path.equals("/video/add")
                || path.equals("/video/edit") || path.equals("/video/delete");
    }
}
