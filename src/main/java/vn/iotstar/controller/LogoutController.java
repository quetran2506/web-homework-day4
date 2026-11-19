package vn.iotstar.controller;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.service.UserService;
import vn.iotstar.service.impl.UserServiceImpl;
import vn.iotstar.util.CookieUtil;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = CookieUtil.readRememberToken(request);
        try {
            userService.revokeRememberToken(token);
        } catch (SQLException ex) {
            getServletContext().log("Could not revoke the remember-me token", ex);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        CookieUtil.clearRememberToken(request, response);
        response.sendRedirect(request.getContextPath() + "/login?loggedOut=1");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
