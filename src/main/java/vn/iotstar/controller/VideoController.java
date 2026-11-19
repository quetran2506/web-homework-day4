package vn.iotstar.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.config.AppConfig;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Video;
import vn.iotstar.service.VideoService;
import vn.iotstar.service.impl.VideoServiceImpl;

@WebServlet("/video/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 524_288_000L,
        maxRequestSize = 534_773_760L)
public class VideoController extends HttpServlet {
    private static final int PAGE_SIZE = 6;
    private static final long MAX_VIDEO_BYTES = 500L * 1024 * 1024;
    private static final Pattern VIDEO_ID = Pattern.compile("^[a-f0-9-]{36}$");
    private final VideoService videoService = new VideoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = action(request);
        try {
            switch (action) {
                case "/" -> showPublicList(request, response);
                case "/detail" -> showDetail(request, response);
                case "/play" -> play(request, response);
                case "/manage" -> showManage(request, response);
                case "/add" -> showForm(request, response, new Video(), "add");
                case "/edit" -> showEditForm(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            databaseError(request, response, ex);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = action(request);
        try {
            switch (action) {
                case "/add" -> add(request, response);
                case "/edit" -> edit(request, response);
                case "/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            databaseError(request, response, ex);
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Video cần nhỏ hơn 500 MB.");
        } catch (IllegalArgumentException ex) {
            redisplayForm(request, response, action, ex.getMessage());
        }
    }

    private void showPublicList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer userId = parseOptionalUserId(request.getParameter("userId"));
        int totalCount = videoService.countPublic(userId);
        int totalPages = Math.max(1, (totalCount + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = parsePage(request.getParameter("page"));
        if (page > totalPages) page = totalPages;
        request.setAttribute("videos", videoService.publicPage(userId, page, PAGE_SIZE));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("userId", userId);
        request.getRequestDispatcher("/WEB-INF/views/video-list.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Video video = videoService.published(parseVideoId(request.getParameter("id")));
        if (video == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("video", video);
        request.getRequestDispatcher("/WEB-INF/views/video-detail.jsp").forward(request, response);
    }

    private void play(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Video video = videoService.published(parseVideoId(request.getParameter("id")));
        if (video == null || video.getVideoFile() == null || video.getVideoFile().isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path directory = AppConfig.videoUploadDirectory();
        Path file = directory.resolve(video.getVideoFile()).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long size = Files.size(file);
        ByteRange range = parseRange(request.getHeader("Range"), size);
        if (range == null) {
            response.setHeader("Content-Range", "bytes */" + size);
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }
        response.setContentType("video/mp4");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, max-age=3600");
        response.setHeader("Content-Disposition", "inline; filename=\"video.mp4\"");
        long length = range.end() - range.start() + 1;
        if (range.partial()) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + range.start() + "-" + range.end() + "/" + size);
        }
        response.setContentLengthLong(length);
        try (InputStream input = Files.newInputStream(file); OutputStream output = response.getOutputStream()) {
            input.skipNBytes(range.start());
            byte[] buffer = new byte[64 * 1024];
            long remaining = length;
            while (remaining > 0) {
                int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) break;
                output.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    private void showManage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("videos", videoService.ownedBy(user.getId()));
        request.getRequestDispatcher("/WEB-INF/views/video-manage.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Video video = videoService.owned(parseVideoId(request.getParameter("id")), currentUser(request).getId());
        if (video == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        showForm(request, response, video, "edit");
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Video video, String formAction)
            throws ServletException, IOException {
        request.setAttribute("video", video);
        request.setAttribute("formAction", formAction);
        request.getRequestDispatcher("/WEB-INF/views/video-form.jsp").forward(request, response);
    }

    private void add(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String title = value(request, "title");
        String description = value(request, "description");
        String poster = validatePoster(value(request, "poster"));
        validateDetails(title, description);
        String fileName = saveVideo(request.getPart("videoUpload"), true);

        Video video = new Video();
        video.setVideoId(UUID.randomUUID().toString());
        video.setTitle(title);
        video.setDescription(description);
        video.setPoster(poster.isBlank() ? null : poster);
        video.setVideoFile(fileName);
        video.setActive(1);
        video.setViews(0);
        video.setUser(currentUser(request));
        try {
            videoService.create(video);
        } catch (SQLException ex) {
            deleteVideoFile(fileName);
            throw ex;
        }
        redirectManage(request, response, "created");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String id = parseVideoId(request.getParameter("id"));
        int ownerId = currentUser(request).getId();
        Video existing = videoService.owned(id, ownerId);
        if (existing == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String title = value(request, "title");
        String description = value(request, "description");
        String poster = validatePoster(value(request, "poster"));
        validateDetails(title, description);
        String previousFile = existing.getVideoFile();
        String nextFile = saveVideo(request.getPart("videoUpload"), false);
        if (nextFile == null) nextFile = previousFile;

        existing.setTitle(title);
        existing.setDescription(description);
        existing.setPoster(poster.isBlank() ? null : poster);
        existing.setVideoFile(nextFile);
        try {
            if (!videoService.update(existing, ownerId)) {
                if (!nextFile.equals(previousFile)) deleteVideoFile(nextFile);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (SQLException ex) {
            if (!nextFile.equals(previousFile)) deleteVideoFile(nextFile);
            throw ex;
        }
        if (!nextFile.equals(previousFile)) deleteVideoFile(previousFile);
        redirectManage(request, response, "updated");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String id = parseVideoId(request.getParameter("id"));
        int ownerId = currentUser(request).getId();
        Video video = videoService.owned(id, ownerId);
        if (video == null || !videoService.delete(id, ownerId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        deleteVideoFile(video.getVideoFile());
        redirectManage(request, response, "deleted");
    }

    private String saveVideo(Part part, boolean required) throws IOException {
        if (part == null || part.getSize() == 0) {
            if (required) throw new IllegalArgumentException("Vui lòng chọn file MP4.");
            return null;
        }
        if (part.getSize() > MAX_VIDEO_BYTES) throw new IllegalArgumentException("Video cần nhỏ hơn 500 MB.");
        String submitted = part.getSubmittedFileName();
        String name = submitted == null ? "" : Path.of(submitted).getFileName().toString();
        if (!name.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
            throw new IllegalArgumentException("Chỉ nhận file có đuôi .mp4.");
        }
        try (InputStream input = part.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (header.length < 12 || !"ftyp".equals(new String(header, 4, 4, StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("File đã chọn không có định dạng MP4 hợp lệ.");
            }
        }
        String savedName = UUID.randomUUID() + ".mp4";
        Path directory = AppConfig.videoUploadDirectory();
        Files.createDirectories(directory);
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, directory.resolve(savedName));
        }
        return savedName;
    }

    private String validatePoster(String poster) {
        if (poster.isBlank()) return "";
        if (poster.length() > 500) throw new IllegalArgumentException("Link ảnh bìa tối đa 500 ký tự.");
        try {
            java.net.URI uri = java.net.URI.create(poster);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("Ảnh bìa phải dùng link http:// hoặc https://.");
            }
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Ảnh bìa")) throw ex;
            throw new IllegalArgumentException("Link ảnh bìa không hợp lệ.");
        }
        return poster;
    }

    private void validateDetails(String title, String description) {
        if (title.isBlank() || title.length() > 500) throw new IllegalArgumentException("Tên video không được trống và tối đa 500 ký tự.");
        if (description.length() > 10000) throw new IllegalArgumentException("Mô tả tối đa 10.000 ký tự.");
    }

    private void redisplayForm(HttpServletRequest request, HttpServletResponse response, String action, String message)
            throws ServletException, IOException {
        boolean editing = "/edit".equals(action);
        Video video = new Video();
        if (editing) {
            try {
                video = videoService.owned(parseVideoId(request.getParameter("id")), currentUser(request).getId());
            } catch (IllegalArgumentException | SQLException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (video == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String poster = request.getParameter("poster");
        if (title != null) video.setTitle(title.trim());
        if (description != null) video.setDescription(description.trim());
        if (poster != null) video.setPoster(poster.trim());
        request.setAttribute("error", message);
        showForm(request, response, video, editing ? "edit" : "add");
    }

    private ByteRange parseRange(String header, long size) {
        if (size <= 0) return header == null ? new ByteRange(0, -1, false) : null;
        if (header == null || header.isBlank()) return new ByteRange(0, size - 1, false);
        if (!header.startsWith("bytes=") || header.indexOf(',') >= 0) return null;
        String[] parts = header.substring(6).split("-", -1);
        if (parts.length != 2) return null;
        try {
            long start;
            long end;
            if (parts[0].isBlank()) {
                long suffix = Long.parseLong(parts[1]);
                if (suffix <= 0) return null;
                start = Math.max(0, size - suffix);
                end = size - 1;
            } else {
                start = Long.parseLong(parts[0]);
                end = parts[1].isBlank() ? size - 1 : Long.parseLong(parts[1]);
                if (start < 0 || end < start || start >= size) return null;
                end = Math.min(end, size - 1);
            }
            return new ByteRange(start, end, true);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void deleteVideoFile(String name) {
        if (name == null || name.isBlank() || !name.matches("[a-f0-9-]{36}\\.mp4")) return;
        try {
            Path directory = AppConfig.videoUploadDirectory();
            Path file = directory.resolve(name).normalize();
            if (file.startsWith(directory)) Files.deleteIfExists(file);
        } catch (IOException ex) {
            getServletContext().log("Could not remove video file " + name, ex);
        }
    }

    private User currentUser(HttpServletRequest request) {
        Object account = request.getSession(false) == null ? null : request.getSession(false).getAttribute("account");
        return account instanceof User user ? user : null;
    }

    private String parseVideoId(String id) {
        if (id == null || !VIDEO_ID.matcher(id).matches()) throw new IllegalArgumentException("Mã video không hợp lệ.");
        return id;
    }

    private Integer parseOptionalUserId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int id = Integer.parseInt(raw);
            if (id < 1) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
    }

    private int parsePage(String raw) {
        try { return Math.max(1, Integer.parseInt(raw)); }
        catch (NumberFormatException ex) { return 1; }
    }

    private String action(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.isBlank() ? "/" : path;
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void redirectManage(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/video/manage?message=" + message);
    }

    private void databaseError(HttpServletRequest request, HttpServletResponse response, SQLException ex)
            throws ServletException, IOException {
        getServletContext().log("JPA video operation failed", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        request.setAttribute("error", "Không thao tác được với video. Hãy kiểm tra SQL Server và cấu hình JPA.");
        request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
    }

    private record ByteRange(long start, long end, boolean partial) { }
}
