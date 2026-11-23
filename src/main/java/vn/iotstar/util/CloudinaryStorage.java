package vn.iotstar.util;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public final class CloudinaryStorage {
    private static final String FOLDER = "web-homework-day4/profile-images";

    public boolean isConfigured() {
        return !setting("CLOUDINARY_CLOUD_NAME").isBlank()
                && !setting("CLOUDINARY_API_KEY").isBlank()
                && !setting("CLOUDINARY_API_SECRET").isBlank();
    }

    public UploadedImage upload(byte[] image, String filename, int userId) throws Exception {
        Cloudinary cloudinary = client();
        String publicId = "user-" + userId + "-" + UUID.randomUUID();
        Map<?, ?> result = cloudinary.uploader().upload(image, ObjectUtils.asMap(
                "folder", FOLDER,
                "public_id", publicId,
                "filename", filename,
                "overwrite", false,
                "resource_type", "image"));
        Object secureUrl = result.get("secure_url");
        Object savedPublicId = result.get("public_id");
        if (secureUrl == null || savedPublicId == null) {
            throw new IOException("Cloudinary did not return the uploaded image URL.");
        }
        return new UploadedImage(secureUrl.toString(), savedPublicId.toString());
    }

    public void delete(String publicId) throws Exception {
        if (publicId == null || publicId.isBlank()) return;
        client().uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
    }

    private Cloudinary client() {
        String cloudName = setting("CLOUDINARY_CLOUD_NAME");
        String apiKey = setting("CLOUDINARY_API_KEY");
        String apiSecret = setting("CLOUDINARY_API_SECRET");
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET first.");
        }
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    private String setting(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) value = System.getProperty(name);
        return value == null ? "" : value.trim();
    }

    public record UploadedImage(String url, String publicId) {
    }
}
