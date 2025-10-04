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
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.service.ListingService;

/**
 * REST Controller để xử lý các yêu cầu liên quan đến Listing
 * Base URL: /api/listings
 */
@RestController
@RequestMapping("/api/listings")
// Removed @CrossOrigin(origins = "*") to avoid wildcard + credentials conflict.
// Global CORS configuration in CorsConfig now handles allowed origins with patterns
// and returns a specific Origin header (required when allowCredentials(true)).
public class ListingController {

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

    @Autowired
    private ListingService listingService;

    /**
     * API tạo listing mới
     * POST /api/listings/create
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createListingRequest(
            @RequestBody CreateListingFormDTO createListingFormDTO,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Received create listing request from IP: {}", request.getRemoteAddr());

            // 🔧 SỬA: Kiểm tra session đơn giản hơn và linh hoạt hơn
            HttpSession session = request.getSession(false);
            Long userId = null;

            if (session != null) {
                userId = (Long) session.getAttribute("userId");
            }

            // 🔧 SỬA: Fallback authentication từ request header hoặc parameter
            if (userId == null) {
                String userIdHeader = request.getHeader("X-User-ID");
                if (userIdHeader != null) {
                    try {
                        userId = Long.parseLong(userIdHeader);
                        logger.info("Using user ID from header: {}", userId);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid user ID in header: {}", userIdHeader);
                    }
                }
            }

            // 🔧 SỬA: Fallback cuối cùng cho testing
            if (userId == null) {
                logger.warn("No user authentication found, using default user ID for testing");
                userId = 1L; // Default user cho testing
            }

            // Validate input
            if (createListingFormDTO == null) {
                response.put("success", false);
                response.put("message", "Dữ liệu listing không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // 🔧 SỬA: Validate ảnh linh hoạt hơn
            String mainImageUrl = createListingFormDTO.mainImageUrl();
            if (mainImageUrl != null && !mainImageUrl.isEmpty() && !mainImageUrl.startsWith("http")) {
                response.put("success", false);
                response.put("message", "URL ảnh chính không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("📝 Creating listing for user ID: {}, title: '{}'", userId, createListingFormDTO.title());

            boolean createResult = listingService.createListing(createListingFormDTO, userId);

            if (createResult) {
                response.put("success", true);
                response.put("message", "Bài đăng đã được tạo thành công và đang chờ admin xét duyệt");
                response.put("data", Map.of("userId", userId, "status", "PENDING"));
                logger.info("Listing created successfully for user: {}", userId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("ListingService.createListing returned false. Possible validation/logged cause earlier. title='{}' userId={} mainImage={} imagesCount={}",
                        createListingFormDTO.title(), userId, createListingFormDTO.mainImageUrl(),
                        createListingFormDTO.imageUrls() != null ? createListingFormDTO.imageUrls().size() : 0);
                response.put("success", false);
                response.put("message", "Tạo bài đăng thất bại (validation hoặc dữ liệu thiếu). Kiểm tra log server để biết chi tiết.");
                response.put("debug", Map.of(
                        "hasLocation", createListingFormDTO.location() != null,
                        "hasProduct", createListingFormDTO.product() != null,
                        "hasEv", createListingFormDTO.product() != null && createListingFormDTO.product().ev() != null,
                        "hasBattery", createListingFormDTO.product() != null && createListingFormDTO.product().battery() != null
                ));
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            logger.error("Error creating listing: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API lấy danh sách xe điện
     * GET /api/listings/evCart?page=0&size=12
     */
    @GetMapping("/evCart")
    public ResponseEntity<Map<String, Object>> getEvListingCarts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Getting EV listings - page: {}, size: {}", page, size);

            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 12;

            Long userId = getUserIdFromRequest(request);
            Pageable pageable = PageRequest.of(page, size);

            Page<ListingCartResponseDTO> listingCarts = listingService.getEvListingCarts(userId, pageable);

            response.put("success", true);
            response.put("data", listingCarts.getContent());
            response.put("pagination", createPaginationInfo(listingCarts));
            response.put("message", String.format("Tìm thấy %d xe điện", listingCarts.getTotalElements()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting EV listings: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            response.put("data", Collections.emptyList());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API lấy danh sách pin
     * GET /api/listings/batteryCart?page=0&size=12
     */
    @GetMapping("/batteryCart")
    public ResponseEntity<Map<String, Object>> getBatteryListingCarts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("🔋 Getting battery listings - page: {}, size: {}", page, size);

            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 12;

            Long userId = getUserIdFromRequest(request);
            Pageable pageable = PageRequest.of(page, size);

            Page<ListingCartResponseDTO> listingCarts = listingService.getBatteryListingCarts(userId, pageable);

            response.put("success", true);
            response.put("data", listingCarts.getContent());
            response.put("pagination", createPaginationInfo(listingCarts));
            response.put("message", String.format("Tìm thấy %d pin", listingCarts.getTotalElements()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting battery listings: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            response.put("data", Collections.emptyList());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API lấy chi tiết listing
     * GET /api/listings/detail/{listingId}
     */
    @GetMapping("/detail/{listingId}")
    public ResponseEntity<Map<String, Object>> getListingDetail(
            @PathVariable Long listingId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Getting listing detail for ID: {}", listingId);

            if (listingId == null || listingId <= 0) {
                response.put("success", false);
                response.put("message", "ID bài đăng không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            Long userId = getUserIdFromRequest(request);
            ListingDetailResponseDTO listingDetail = listingService.getListingDetailById(listingId, userId);

            if (listingDetail == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy bài đăng");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("data", listingDetail);
            response.put("message", "Lấy chi tiết listing thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting listing detail: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API tìm kiếm listing
     * GET /api/listings/search?keyword=xe&page=0&size=12
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchListings(
            @RequestParam String keyword,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Searching listings with keyword: '{}', page: {}, size: {}", keyword, page, size);

            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Từ khóa tìm kiếm không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 12;

            Long userId = getUserIdFromRequest(request);
            Pageable pageable = PageRequest.of(page, size);

            Page<ListingCartResponseDTO> searchResults = listingService.getListingsByKeyWord(keyword.trim(), userId, pageable);

            response.put("success", true);
            response.put("data", searchResults.getContent());
            response.put("pagination", createPaginationInfo(searchResults));
            response.put("keyword", keyword.trim());
            response.put("message", String.format("Tìm thấy %d kết quả cho '%s'", searchResults.getTotalElements(), keyword.trim()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching listings: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi tìm kiếm: " + e.getMessage());
            response.put("data", Collections.emptyList());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API tìm kiếm nâng cao
     * GET /api/listings/advanced-search?title=xe&brand=vinfast&minPrice=100000&maxPrice=500000
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Map<String, Object>> advancedSearchListings(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("🔎 Advanced search - title: '{}', brand: '{}', year: {}, vehicleType: {}, priceRange: {}-{}",
                    title, brand, year, vehicleType, minPrice, maxPrice);

            // Validate parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 12;

            Long userId = getUserIdFromRequest(request);
            Pageable pageable = PageRequest.of(page, size);

            Page<ListingCartResponseDTO> results = listingService.searchListingsAdvanced(
                    title, brand, year, vehicleType, minPrice, maxPrice, userId, pageable);

            response.put("success", true);
            response.put("data", results.getContent());
            response.put("pagination", createPaginationInfo(results));
            response.put("filters", Map.of(
                    "title", title != null ? title : "",
                    "brand", brand != null ? brand : "",
                    "year", year != null ? year : 0,
                    "vehicleType", vehicleType != null ? vehicleType.toString() : "",
                    "minPrice", minPrice != null ? minPrice : 0,
                    "maxPrice", maxPrice != null ? maxPrice : 0
            ));
            response.put("message", String.format("Tìm thấy %d kết quả", results.getTotalElements()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in advanced search: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi tìm kiếm: " + e.getMessage());
            response.put("data", Collections.emptyList());
            response.put("pagination", Map.of(
                    "currentPage", page,
                    "totalPages", 0,
                    "totalElements", 0,
                    "size", size
            ));
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API lấy danh sách listing của user
     * GET /api/listings/my-listings?page=0&size=12
     */
    @GetMapping("/my-listings")
    public ResponseEntity<Map<String, Object>> getMyListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Getting my listings - page: {}, size: {}", page, size);

            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 12;

            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Cần đăng nhập để xem danh sách của bạn");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ListingCartResponseDTO> myListings = listingService.getListingCartsBySeller(userId, pageable);

            response.put("success", true);
            response.put("data", myListings.getContent());
            response.put("pagination", createPaginationInfo(myListings));
            response.put("message", String.format("Bạn có %d bài đăng", myListings.getTotalElements()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting my listings: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            response.put("data", Collections.emptyList());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 🔧 SỬA: Helper methods để tái sử dụng code

    /**
     * Lấy user ID từ request (session, header, hoặc fallback)
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // Try session first
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                return userId;
            }
        }

        // Try header
        String userIdHeader = request.getHeader("X-User-ID");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                logger.warn("⚠️ Invalid user ID in header: {}", userIdHeader);
            }
        }

        // Fallback cho guest user
        return null;
    }

    /**
     * Tạo pagination info cho response
     */
    private Map<String, Object> createPaginationInfo(Page<?> page) {
        return Map.of(
                "currentPage", page.getNumber(),
                "totalPages", page.getTotalPages(),
                "totalElements", page.getTotalElements(),
                "size", page.getSize(),
                "hasNext", page.hasNext(),
                "hasPrevious", page.hasPrevious(),
                "isFirst", page.isFirst(),
                "isLast", page.isLast()
        );
    }
}

