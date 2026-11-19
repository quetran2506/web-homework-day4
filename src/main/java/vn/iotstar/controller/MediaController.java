package vn.iotstar.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.config.AppConfig;

@WebServlet("/media/*")
public class MediaController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getPathInfo();
        if (name == null || !name.matches("/[a-f0-9-]{36}\\.(png|jpg|jpeg|gif|webp)")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String fileName = name.substring(1);
        Path directory = AppConfig.uploadDirectory();
        Path file = directory.resolve(fileName).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String contentType = switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/png";
        };
        response.setContentType(contentType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.setContentLengthLong(Files.size(file));
        try (OutputStream output = response.getOutputStream()) {
            Files.copy(file, output);
        }
    }
}
