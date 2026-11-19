package vn.iotstar.config;

import java.nio.file.Path;

public final class AppConfig {
    private AppConfig() {
    }

    public static Path uploadDirectory() {
        String configured = System.getenv("APP_UPLOAD_DIR");
        if (configured == null || configured.isBlank()) {
            configured = System.getProperty("APP_UPLOAD_DIR");
        }
        return (configured == null || configured.isBlank()
                ? Path.of(System.getProperty("user.dir"), "uploads")
                : Path.of(configured))
                .toAbsolutePath().normalize();
    }

    public static Path videoUploadDirectory() {
        return uploadDirectory().resolve("videos").toAbsolutePath().normalize();
    }
}
