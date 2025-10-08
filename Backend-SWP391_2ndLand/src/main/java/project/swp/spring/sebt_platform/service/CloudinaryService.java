package project.swp.spring.sebt_platform.service;

import org.springframework.web.multipart.MultipartFile;
import project.swp.spring.sebt_platform.dto.object.Image;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CloudinaryService {

    Image uploadImage(MultipartFile file, String folder);

    CompletableFuture<List<Image>> uploadMultipleImages(List<MultipartFile> files, String folder);

    boolean deleteImage(String publicId);

    int deleteMultipleImages(List<String> publicIds);

    String getTransformedImageUrl(String publicId, int width, int height);
}
