package vn.iotstar.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.iotstar.config.AppConfig;
import vn.iotstar.entity.Category;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.impl.CategoryServiceImpl;

@WebServlet("/admin/category/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 3 * 1024 * 1024,
        maxRequestSize = 4 * 1024 * 1024)
public class CategoryController extends HttpServlet {
    private static final long MAX_IMAGE_BYTES = 3L * 1024 * 1024;
    private static final int PAGE_SIZE = 8;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private final CategoryService categoryService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = action(request);
        try {
            switch (action) {
                case "/list" -> showList(request, response);
                case "/add" -> {
                    request.setAttribute("category", new Category());
                    request.setAttribute("formAction", "add");
                    request.getRequestDispatcher("/WEB-INF/views/category-form.jsp").forward(request, response);
                }
                case "/edit" -> showEdit(request, response);
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
        String currentAction = action(request);
        try {
            switch (currentAction) {
                case "/add" -> add(request, response);
                case "/edit" -> edit(request, response);
                case "/delete" -> delete(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException ex) {
            databaseError(request, response, ex);
        } catch (IllegalStateException ex) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Ảnh cần nhỏ hơn 3 MB.");
        } catch (IllegalArgumentException ex) {
            if ("/delete".equals(currentAction)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            } else {
                redisplayForm(request, response, currentAction, ex.getMessage());
            }
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String keyword = value(request, "q");
        int totalCount = keyword.isBlank() ? categoryService.count() : categoryService.count(keyword);
        int totalPages = Math.max(1, (totalCount + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = parsePage(request.getParameter("page"));
        if (page > totalPages) {
            page = totalPages;
        }

        request.setAttribute("keyword", keyword);
        request.setAttribute("categories", keyword.isBlank()
                ? categoryService.getAll(page - 1, PAGE_SIZE)
                : categoryService.search(keyword, page - 1, PAGE_SIZE));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.getRequestDispatcher("/WEB-INF/views/category-list.jsp").forward(request, response);
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = parseId(request.getParameter("id"));
        Category category = categoryService.get(id);
        if (category == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("category", category);
        request.setAttribute("formAction", "edit");
        request.getRequestDispatcher("/WEB-INF/views/category-form.jsp").forward(request, response);
    }

    private void add(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String name = value(request, "name");
        validateName(name);
        ensureUniqueName(name, 0);

        Category category = new Category();
        category.setName(name);
        category.setStatus(parseStatus(request.getParameter("status")));
        String image = chooseImage(request, null);
        category.setIcon(image);
        try {
            categoryService.insert(category);
        } catch (SQLException ex) {
            deleteImage(image);
            throw ex;
        }
        redirectList(request, response, "created");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        int id = parseId(request.getParameter("id"));
        Category category = categoryService.get(id);
        if (category == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String name = value(request, "name");
        validateName(name);
        ensureUniqueName(name, id);
        String previousImage = category.getIcon();
        String nextImage = chooseImage(request, previousImage);
        category.setName(name);
        category.setStatus(parseStatus(request.getParameter("status")));
        category.setIcon(nextImage);
        try {
            categoryService.edit(category);
        } catch (SQLException ex) {
            if (!java.util.Objects.equals(previousImage, nextImage)) {
                deleteImage(nextImage);
            }
            throw ex;
        }
        if (!java.util.Objects.equals(previousImage, nextImage)) {
            deleteImage(previousImage);
        }
        redirectList(request, response, "updated");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = parseId(request.getParameter("id"));
        Category category = categoryService.get(id);
        if (category == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        categoryService.delete(id);
        deleteImage(category.getIcon());
        redirectList(request, response, "deleted");
    }

    private String chooseImage(HttpServletRequest request, String previousImage)
            throws IOException, ServletException {
        String uploaded = saveImage(request.getPart("icon"));
        if (uploaded != null) {
            return uploaded;
        }
        String imageUrl = value(request, "imageUrl");
        if (!imageUrl.isBlank()) {
            return validateImageUrl(imageUrl);
        }
        return previousImage;
    }

    private String saveImage(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        if (part.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Ảnh cần nhỏ hơn 3 MB.");
        }
        String submittedName = part.getSubmittedFileName();
        String originalName = submittedName == null ? "" : Path.of(submittedName).getFileName().toString();
        int dot = originalName.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("Chỉ nhận ảnh PNG, JPG, JPEG, GIF hoặc WebP.");
        }
        String extension = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Chỉ nhận ảnh PNG, JPG, JPEG, GIF hoặc WebP.");
        }
        String fileName = UUID.randomUUID() + "." + extension;
        Path directory = AppConfig.uploadDirectory();
        Files.createDirectories(directory);
        try (java.io.InputStream input = part.getInputStream()) {
            Files.copy(input, directory.resolve(fileName));
        }
        return fileName;
    }

    private String validateImageUrl(String value) {
        if (value.length() > 500) {
            throw new IllegalArgumentException("Đường dẫn ảnh tối đa 500 ký tự.");
        }
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("Link ảnh phải bắt đầu bằng http:// hoặc https://.");
            }
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Link ảnh")) {
                throw ex;
            }
            throw new IllegalArgumentException("Link ảnh không hợp lệ.");
        }
        return value;
    }

    private void deleteImage(String image) {
        if (image == null || image.isBlank() || image.startsWith("http://") || image.startsWith("https://")) {
            return;
        }
        try {
            Path directory = AppConfig.uploadDirectory();
            Path file = directory.resolve(image).normalize();
            if (file.startsWith(directory)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ex) {
            getServletContext().log("Could not remove category image " + image, ex);
        }
    }

    private void ensureUniqueName(String name, int currentId) throws SQLException {
        Category existing = categoryService.get(name);
        if (existing != null && existing.getId() != currentId) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
        }
    }

    private void validateName(String name) {
        if (name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Tên danh mục không được rỗng và tối đa 255 ký tự.");
        }
    }

    private int parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank() || "1".equals(rawStatus)) {
            return 1;
        }
        if ("0".equals(rawStatus)) {
            return 0;
        }
        throw new IllegalArgumentException("Trạng thái danh mục không hợp lệ.");
    }

    private int parseId(String rawId) {
        try {
            int id = Integer.parseInt(rawId);
            if (id < 1) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã danh mục không hợp lệ.");
        }
    }

    private int parsePage(String rawPage) {
        try {
            int page = Integer.parseInt(rawPage);
            return page < 1 ? 1 : page;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String action(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.isBlank() ? "/list" : path;
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void redirectList(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/category/list?message=" + message);
    }

    private void redisplayForm(HttpServletRequest request, HttpServletResponse response,
                               String currentAction, String message)
            throws ServletException, IOException {
        boolean editing = "/edit".equals(currentAction);
        Category category;
        if (editing) {
            try {
                category = categoryService.get(parseId(request.getParameter("id")));
            } catch (IllegalArgumentException ex) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                return;
            } catch (SQLException ex) {
                databaseError(request, response, ex);
                return;
            }
            if (category == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else {
            category = new Category();
        }
        String enteredName = request.getParameter("name");
        if (enteredName != null) category.setName(enteredName.trim());
        String enteredImageUrl = request.getParameter("imageUrl");
        if (enteredImageUrl != null && !enteredImageUrl.isBlank()) category.setIcon(enteredImageUrl.trim());
        String enteredStatus = request.getParameter("status");
        if ("0".equals(enteredStatus)) category.setStatus(0);
        if ("1".equals(enteredStatus)) category.setStatus(1);
        request.setAttribute("category", category);
        request.setAttribute("formAction", editing ? "edit" : "add");
        request.setAttribute("error", message);
        request.getRequestDispatcher("/WEB-INF/views/category-form.jsp").forward(request, response);
    }

    private void databaseError(HttpServletRequest request, HttpServletResponse response, SQLException ex)
            throws ServletException, IOException {
        getServletContext().log("JPA category operation failed", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        request.setAttribute("error", "Không thao tác được với danh mục. Hãy kiểm tra SQL Server và cấu hình JPA.");
        request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
    }
}
