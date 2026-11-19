package vn.iotstar.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import vn.iotstar.service.VideoService;
import vn.iotstar.service.impl.VideoServiceImpl;

@WebServlet("/home")
public class HomeController extends HttpServlet {
    private final VideoService videoService = new VideoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("latestVideos", videoService.latest(10));
        } catch (SQLException ex) {
            getServletContext().log("Could not load newest videos", ex);
            request.setAttribute("videoError", "Chưa tải được danh sách video. Hãy kiểm tra SQL Server.");
        }
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
