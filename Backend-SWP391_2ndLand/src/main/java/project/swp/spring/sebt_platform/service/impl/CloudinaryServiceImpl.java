package project.swp.spring.sebt_platform.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.service.CloudinaryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryServiceImpl.class);
    private static final Set<String> ALLOWED_FORMATS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Image uploadImage(MultipartFile file, String folder) {
        try {
            // Validate file
            validateFile(file);

            // Tạo unique filename
            String originalFilename = file.getOriginalFilename();
            assert originalFilename != null;
            String uniqueFilename = generateUniqueFilename(originalFilename);

            // Upload options
            Map uploadOptions = ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", uniqueFilename,
                    "resource_type", "image",
                    "quality", "auto:good",
                    "fetch_format", "auto"
            );

            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            // Tạo và trả về Image object
            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");

            return new Image(secureUrl, publicId);

        } catch (IOException e) {
            logger.error("Error uploading image to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage());
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    @Async("imageUploadExecutor")
    @Override
    public List<Image> uploadMultipleImages(List<MultipartFile> files, String folder) {
        List<Image> uploadedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Image image = uploadImage(file, folder);
                uploadedImages.add(image);
                logger.info("Successfully uploaded: {}", file.getOriginalFilename());
            } catch (Exception e) {
                logger.error("Failed to upload {}: {}", file.getOriginalFilename(), e.getMessage());
                // Continue with other files instead of failing completely
            }
        }

        return uploadedImages;
    }

    @Override
    public boolean deleteImage(String publicId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");

            boolean success = "ok".equals(result);
            if (success) {
                logger.info("Successfully deleted image: {}", publicId);
            } else {
                logger.warn("Failed to delete image {}: {}", publicId, result);
            }

            return success;
        } catch (Exception e) {
            logger.error("Error deleting image {}: {}", publicId, e.getMessage());
            return false;
        }
    }

    @Override
    public int deleteMultipleImages(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        for (String publicId : publicIds) {
            if (deleteImage(publicId)) {
                deletedCount++;
            }
        }

        logger.info("Deleted {} out of {} images", deletedCount, publicIds.size());
        return deletedCount;
    }

    @Override
    public String getTransformedImageUrl(String publicId, int width, int height) {
        try {
            return cloudinary.url()
                    .transformation(
                            new com.cloudinary.Transformation()
                                    .width(width)
                                    .height(height)
                                    .crop("fill")
                                    .quality("auto:good")
                                    .fetchFormat("auto")
                    )
                    .generate(publicId);
        } catch (Exception e) {
            logger.error("Error generating transformed URL for {}: {}", publicId, e.getMessage());
            return null;
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Check file format
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid name");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException("File format not supported. Allowed formats: " + ALLOWED_FORMATS);
        }

        // Check if it's actually an image by content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be a valid image");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Generate unique filename to avoid conflicts
     */
    private String generateUniqueFilename(String originalFilename) {
        String nameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf("."));

        // Clean filename - remove special characters
        String cleanName = nameWithoutExtension.replaceAll("[^a-zA-Z0-9_-]", "_");

        // Add timestamp for uniqueness
        long timestamp = System.currentTimeMillis();

        return String.format("%s_%d", cleanName, timestamp);
    }
}
