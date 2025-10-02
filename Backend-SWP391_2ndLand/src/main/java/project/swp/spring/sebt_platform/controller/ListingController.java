package project.swp.spring.sebt_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.service.ListingService;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    // API endpoint để đăng bài mới - bài sẽ được gửi đến admin để xét duyệt
    // Nhận JSON payload vì ảnh đã được upload lên Cloudinary trước đó (không cần multipart)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createListingRequest(@RequestBody CreateListingFormDTO createListingFormDTO,
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

            if (createListingFormDTO == null) {
                return ResponseEntity.badRequest().body("Create listing form is required");
            }

            String mainImageUrl = createListingFormDTO.mainImageUrl();
            if (mainImageUrl == null || !mainImageUrl.startsWith("http")) {
                return ResponseEntity.badRequest().body("Ảnh chính không hợp lệ hoặc chưa được upload");
            }
            
            if (createListingFormDTO.imageUrls() == null || createListingFormDTO.imageUrls().isEmpty()) {
                return ResponseEntity.badRequest().body("Cần ít nhất một ảnh chi tiết");
            }

            boolean createResult = listingService.createListing(createListingFormDTO, userId);
            
            if (createResult) {
                return ResponseEntity.ok().body("Bài đăng đã được tạo thành công và đang chờ admin xét duyệt. Bạn sẽ nhận được thông báo khi bài đăng được phê duyệt.");
            } else {
                return ResponseEntity.badRequest().body("Tạo bài đăng thất bại. Vui lòng thử lại sau.");
            }

        } catch (Exception e) {
            if (e.getCause() != null) {
                System.err.println("   - Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();

            String errorMessage = "Lỗi server: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (Cause: " + e.getCause().getMessage() + ")";
            }
            
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}