package project.swp.spring.sebt_platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.service.CloudinaryService;
import project.swp.spring.sebt_platform.service.ListingService;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PutMapping(value = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createListingRequest(@RequestPart("createListingForm") String createListingFormJson,
                                                  @RequestPart("listingImages") List<MultipartFile> listingImages,
                                                  @RequestPart("thumbnailImage") MultipartFile thumbnailImage,
                                                  HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session. Please login again.");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            CreateListingFormDTO createListingFormDTO = objectMapper.readValue(createListingFormJson, CreateListingFormDTO.class);

            // Validate input
            if (createListingFormDTO == null) {
                return ResponseEntity.badRequest().body("Create listing form is required");
            }

            if (listingImages == null || listingImages.isEmpty()) {
                return ResponseEntity.badRequest().body("At least one listing image is required");
            }

            if (thumbnailImage == null || thumbnailImage.isEmpty()) {
                return ResponseEntity.badRequest().body("Thumbnail image is required");
            }

            // Upload images to Cloudinary
            List<Image> imageList = cloudinaryService.uploadMultipleImages(listingImages, "listings");
            Image thumbnailImageResult = cloudinaryService.uploadImage(thumbnailImage, "thumbnails");

            // Create listing
            if (listingService.createListing(createListingFormDTO, userId, imageList, thumbnailImageResult)) {
                return ResponseEntity.ok().body("Create listing request successfully");
            } else {
                return ResponseEntity.badRequest().body("Create listing request failed");
            }

        } catch (Exception e) {
            System.err.println("Create listing error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}