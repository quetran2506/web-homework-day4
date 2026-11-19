package vn.iotstar.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieUtil {
    public static final String REMEMBER_COOKIE = "rememberToken";

    private CookieUtil() {
    }

    public static String readRememberToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (REMEMBER_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void setRememberToken(HttpServletRequest request, HttpServletResponse response, String token) {
        String path = request.getContextPath().isBlank() ? "/" : request.getContextPath();
        String header = REMEMBER_COOKIE + "=" + token + "; Path=" + path
                + "; Max-Age=" + (30 * 24 * 60 * 60) + "; HttpOnly; SameSite=Lax";
        if (request.isSecure()) {
            header += "; Secure";
        }
        response.addHeader("Set-Cookie", header);
    }

    public static void clearRememberToken(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getContextPath().isBlank() ? "/" : request.getContextPath();
        String header = REMEMBER_COOKIE + "=; Path=" + path
                + "; Max-Age=0; HttpOnly; SameSite=Lax";
        if (request.isSecure()) {
            header += "; Secure";
        }
        response.addHeader("Set-Cookie", header);
    }
}
