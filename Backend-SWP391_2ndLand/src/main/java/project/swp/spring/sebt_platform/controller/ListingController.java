package project.swp.spring.sebt_platform.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.service.ListingService;


@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

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

    @GetMapping("/evCart")
    public ResponseEntity<?> getEvListingCarts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {

            HttpSession session = request.getSession(false);
            Long userId = null;
            if (session != null) {
                userId = (Long) session.getAttribute("userId");
            }

            // Tạo Pageable object từ request parameters
            Pageable pageable = PageRequest.of(page, size);

            var listingCarts = listingService.getEvListingCarts(userId, pageable);
            return ResponseEntity.ok(listingCarts);
        } catch (Exception e) {
            System.err.println("Error in getEvListingCarts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/batteryCart")
    public ResponseEntity<?> getBatteryListingCarts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            HttpSession session = request.getSession(false);
            Long userId = null;
            if (session != null) {
                userId = (Long) session.getAttribute("userId");
            }

            Pageable pageable = PageRequest.of(page, size);
            var listingCarts = listingService.getBatteryListingCarts(userId, pageable);
            return ResponseEntity.ok(listingCarts);
        } catch (Exception e) {
            System.err.println("Error in getBatteryListingCarts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{listingId}")
    public ResponseEntity<?> getListingDetail(@PathVariable Long listingId) {
        try {
            if (listingId == null || listingId <= 0) {
                return ResponseEntity.badRequest().body("ID bài đăng không hợp lệ");
            }

            var listingDetail = listingService.getListingDetailById(listingId);
            if (listingDetail == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(listingDetail);
        } catch (Exception e) {
            System.err.println("Error in getListingDetail: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchListings(
            @RequestParam String keyword,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Từ khóa tìm kiếm không được để trống");
            }

            HttpSession session = request.getSession(false);
            Long userId = null;
            if (session != null) {
                userId = (Long) session.getAttribute("userId");
            }

            Pageable pageable = PageRequest.of(page, size);
            var searchResults = listingService.getListingsByKeyWord(keyword.trim(), userId, pageable);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            System.err.println("Error in searchListings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<?> advancedSearchListings(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            logger.info("Advanced search request - title: {}, brand: {}, year: {}, page: {}, size: {}",
                title, brand, year, page, size);

            // Validate page parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 12;

            // Lấy userId từ session (nếu có)
            HttpSession session = request.getSession(false);
            Long userId = null;
            if (session != null) {
                userId = (Long) session.getAttribute("userId");
            }

            // Tạo Pageable object
            Pageable pageable = PageRequest.of(page, size);

            // Gọi service method mới
            Page<ListingCartResponseDTO> results = listingService.searchListingsAdvanced(
                title, brand, year, userId, pageable);

            // Tạo response cho UI
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results.getContent());
            response.put("pagination", Map.of(
                "currentPage", results.getNumber(),
                "totalPages", results.getTotalPages(),
                "totalElements", results.getTotalElements(),
                "size", results.getSize(),
                "hasNext", results.hasNext(),
                "hasPrevious", results.hasPrevious(),
                "isFirst", results.isFirst(),
                "isLast", results.isLast()
            ));
            response.put("filters", Map.of(
                "title", title != null ? title : "",
                "brand", brand != null ? brand : "",
                "year", year != null ? year : 0
            ));
            response.put("message", String.format("Tìm thấy %d kết quả", results.getTotalElements()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in advancedSearchListings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Lỗi tìm kiếm: " + e.getMessage(),
                "data", Collections.emptyList(),
                "pagination", Map.of(
                    "currentPage", 0,
                    "totalPages", 0,
                    "totalElements", 0,
                    "size", size
                )
            ));
        }
    }

    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session. Please login again.");
            }

            Pageable pageable = PageRequest.of(page, size);
            var myListings = listingService.getListingCartsBySeller(userId, pageable);
            return ResponseEntity.ok(myListings);
        } catch (Exception e) {
            System.err.println("Error in getMyListings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
}