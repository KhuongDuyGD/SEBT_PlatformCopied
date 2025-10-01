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
            System.out.println("🚀 [DEBUG] Bắt đầu xử lý create listing request với Cloudinary URLs...");
            
            // Bước 1: Kiểm tra session
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.out.println("❌ [DEBUG] Session null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                System.out.println("❌ [DEBUG] UserId null trong session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session. Please login again.");
            }
            System.out.println("✅ [DEBUG] Session hợp lệ, userId = " + userId);

            // Bước 2: Validate DTO (Spring đã parse JSON tự động)
            if (createListingFormDTO == null) {
                System.out.println("❌ [DEBUG] CreateListingFormDTO null");
                return ResponseEntity.badRequest().body("Create listing form is required");
            }
            
            System.out.println("✅ [DEBUG] Nhận được DTO:");
            System.out.println("📋 [DEBUG] - Title: " + createListingFormDTO.title());
            System.out.println("📋 [DEBUG] - Price: " + createListingFormDTO.price());
            System.out.println("📋 [DEBUG] - MainImage: " + createListingFormDTO.title());

            // Bước 3: Validate Cloudinary URLs có trong payload
            String mainImageUrl = createListingFormDTO.mainImageUrl();
            if (mainImageUrl == null || !mainImageUrl.startsWith("http")) {
                System.out.println("❌ [DEBUG] MainImage URL không hợp lệ: " + mainImageUrl);
                return ResponseEntity.badRequest().body("Ảnh chính không hợp lệ hoặc chưa được upload");
            }
            
            if (createListingFormDTO.imageUrls() == null || createListingFormDTO.imageUrls().isEmpty()) {
                System.out.println("❌ [DEBUG] ImageUrls danh sách trống");
                return ResponseEntity.badRequest().body("Cần ít nhất một ảnh chi tiết");
            }

            System.out.println("✅ [DEBUG] Validation passed, sử dụng ảnh từ Cloudinary");

            // Bước 4: Gọi service với null cho image params (vì đã có URL sẵn)
            System.out.println("🔄 [DEBUG] Gọi listingService.createListing với Cloudinary URLs...");
            boolean createResult = listingService.createListing(createListingFormDTO, userId, null, null);
            
            if (createResult) {
                System.out.println("✅ [DEBUG] CreateListing thành công!");
                return ResponseEntity.ok().body("Bài đăng đã được tạo thành công và đang chờ admin xét duyệt. Bạn sẽ nhận được thông báo khi bài đăng được phê duyệt.");
            } else {
                System.out.println("❌ [DEBUG] CreateListing thất bại!");
                return ResponseEntity.badRequest().body("Tạo bài đăng thất bại. Vui lòng thử lại sau.");
            }

        } catch (Exception e) {
            // Log chi tiết lỗi để debug
            System.err.println("❌ ERROR trong createListingRequest:");
            System.err.println("   - Message: " + e.getMessage());
            System.err.println("   - Class: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("   - Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            
            // Trả về error message chi tiết hơn cho frontend debug
            String errorMessage = "Lỗi server: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (Cause: " + e.getCause().getMessage() + ")";
            }
            
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}