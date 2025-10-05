package project.swp.spring.sebt_platform.controller;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import project.swp.spring.sebt_platform.dto.object.*;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.service.CloudinaryService;
import project.swp.spring.sebt_platform.service.ListingService;

/**
 * REST Controller cho Listing - Base URL: /api/listings
 */
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

    @Autowired
    private ListingService listingService;
    @Autowired
    private CloudinaryService cloudinaryService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowNestedPaths(true);
        binder.initDirectFieldAccess();
    }

    /**
     * POST /api/listings/create - Tạo listing mới với multipart/form-data
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createListing(
            @ModelAttribute CreateListingFormDTO dto,
            HttpServletRequest request) {

        try {
            logger.info("Create listing request from IP: {}", request.getRemoteAddr());

            // Get user ID from session/header or fallback
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                logger.warn("No user authentication, using default user ID");
                userId = DEFAULT_USER_ID;
            }

            // Validate
            if (dto == null) {
                return buildErrorResponse("Dữ liệu listing không được để trống");
            }

            List<MultipartFile> images = dto.getImages();
            if (images == null || images.isEmpty()) {
                return buildErrorResponse("Vui lòng upload ít nhất một ảnh");
            }

            logger.info("Creating listing - userId: {}, title: '{}', images: {}",
                    userId, dto.getTitle(), images.size());

            // Upload images to Cloudinary
            List<Image> imageList = cloudinaryService.uploadMultipleImages(images, "listings");
            if (imageList.isEmpty()) {
                return buildErrorResponse("Upload ảnh thất bại");
            }

            logger.info("Uploaded {} images to Cloudinary (first image as thumbnail)", imageList.size());

            // Create listing
            boolean success = listingService.createListing(dto, userId, imageList);

            if (success) {
                logger.info("Listing created successfully for user: {}", userId);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Bài đăng đã được tạo thành công và đang chờ admin xét duyệt",
                        "data", Map.of(
                                "userId", userId,
                                "status", "PENDING",
                                "imagesUploaded", imageList.size()
                        )
                ));
            } else {
                logger.warn("createListing returned false for user: {}", userId);
                return buildErrorResponse("Tạo bài đăng thất bại. Vui lòng kiểm tra lại thông tin");
            }

        } catch (Exception e) {
            logger.error("Error creating listing: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Lỗi server: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * GET /api/listings/evCart
     */
    @GetMapping("/evCart")
    public ResponseEntity<Map<String, Object>> getEvListingCarts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            logger.info("Getting EV listings - page: {}, size: {}", page, size);
            return getListingCarts(request, page, size, "EV",
                    () -> listingService.getEvListingCarts(getUserIdFromRequest(request),
                            PageRequest.of(validatePage(page), validateSize(size))));
        } catch (Exception e) {
            return handleError("Error getting EV listings", e);
        }
    }

    /**
     * GET /api/listings/batteryCart
     */
    @GetMapping("/batteryCart")
    public ResponseEntity<Map<String, Object>> getBatteryListingCarts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            logger.info("Getting battery listings - page: {}, size: {}", page, size);
            return getListingCarts(request, page, size, "pin",
                    () -> listingService.getBatteryListingCarts(getUserIdFromRequest(request),
                            PageRequest.of(validatePage(page), validateSize(size))));
        } catch (Exception e) {
            return handleError("Error getting battery listings", e);
        }
    }

    /**
     * GET /api/listings/detail/{id}
     */
    @GetMapping("/detail/{listingId}")
    public ResponseEntity<Map<String, Object>> getListingDetail(
            @PathVariable Long listingId,
            HttpServletRequest request) {

        try {
            logger.info("Getting listing detail for ID: {}", listingId);

            if (listingId == null || listingId <= 0) {
                return buildErrorResponse("ID bài đăng không hợp lệ");
            }

            ListingDetailResponseDTO detail = listingService.getListingDetailById(
                    listingId, getUserIdFromRequest(request));

            if (detail == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", detail,
                    "message", "Lấy chi tiết listing thành công"
            ));

        } catch (Exception e) {
            return handleError("Error getting listing detail", e);
        }
    }

    /**
     * GET /api/listings/search
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchListings(
            @RequestParam String keyword,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            logger.info("Searching listings: '{}', page: {}, size: {}", keyword, page, size);

            if (keyword == null || keyword.trim().isEmpty()) {
                return buildErrorResponse("Từ khóa tìm kiếm không được để trống");
            }

            Pageable pageable = PageRequest.of(validatePage(page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingsByKeyWord(
                    keyword.trim(), getUserIdFromRequest(request), pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", results.getContent(),
                    "pagination", createPaginationInfo(results),
                    "keyword", keyword.trim(),
                    "message", String.format("Tìm thấy %d kết quả cho '%s'",
                            results.getTotalElements(), keyword.trim())
            ));

        } catch (Exception e) {
            return handleError("Error searching listings", e);
        }
    }

    /**
     * GET /api/listings/ev-filter
     */
    @GetMapping("/ev-filter")
    public ResponseEntity<Map<String, Object>> filterEvlistingCart(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            logger.info("Advanced search - year: {}, type: {}, price: {}-{}",
                    year, vehicleType, minPrice, maxPrice);

            Pageable pageable = PageRequest.of(validatePage(page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.filterEvListings(
                    year, vehicleType, minPrice, maxPrice,
                    getUserIdFromRequest(request), pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", results.getContent(),
                    "pagination", createPaginationInfo(results),
                    "filters", Map.of(
                            "year", year != null ? year : 0,
                            "vehicleType", vehicleType != null ? vehicleType.toString() : "",
                            "minPrice", minPrice != null ? minPrice : 0,
                            "maxPrice", maxPrice != null ? maxPrice : 0
                    ),
                    "message", String.format("Tìm thấy %d kết quả", results.getTotalElements())
            ));

        } catch (Exception e) {
            return handleError("Error in advanced search", e);
        }
    }

    /**
     * GET /api/listings/battery-filter
     */
    @GetMapping("/battery-filter")
    public ResponseEntity<Map<String, Object>> filterBatteryListingCart(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            logger.info("Advanced search - year: {}, price: {}-{}",
                    year, minPrice, maxPrice);

            Pageable pageable = PageRequest.of(validatePage(page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.filterBatteryListings(
                    year, minPrice, maxPrice,
                    getUserIdFromRequest(request), pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", results.getContent(),
                    "pagination", createPaginationInfo(results),
                    "filters", Map.of(
                            "year", year != null ? year : 0,
                            "minPrice", minPrice != null ? minPrice : 0,
                            "maxPrice", maxPrice != null ? maxPrice : 0
                    ),
                    "message", String.format("Tìm thấy %d kết quả", results.getTotalElements())
            ));

        } catch (Exception e) {
            return handleError("Error in advanced search", e);
        }
    }

    /**
     * GET /api/listings/my-listings - Danh sách listing của user
     */
    @GetMapping("/my-listings")
    public ResponseEntity<Map<String, Object>> getMyListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            logger.info("Getting my listings - page: {}, size: {}", page, size);

            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Cần đăng nhập để xem danh sách của bạn"
                ));
            }

            Pageable pageable = PageRequest.of(validatePage(page), validateSize(size));
            Page<ListingCartResponseDTO> myListings = listingService.getListingCartsBySeller(userId, pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", myListings.getContent(),
                    "pagination", createPaginationInfo(myListings),
                    "message", String.format("Bạn có %d bài đăng", myListings.getTotalElements())
            ));

        } catch (Exception e) {
            return handleError("Error getting my listings", e);
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Generic method để lấy listing carts với pagination
     */
    private ResponseEntity<Map<String, Object>> getListingCarts(
            HttpServletRequest request, int page, int size,
            String type, ListingSupplier supplier) {

        Page<ListingCartResponseDTO> carts = supplier.get();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", carts.getContent(),
                "pagination", createPaginationInfo(carts),
                "message", String.format("Tìm thấy %d %s", carts.getTotalElements(), type)
        ));
    }

    /**
     * Lấy user ID từ session/header
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) return userId;
        }

        String userIdHeader = request.getHeader("X-User-ID");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                logger.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }

        return null;
    }

    /**
     * Tạo pagination info
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

    /**
     * Build error response
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", message
        ));
    }

    /**
     * Handle error với logging
     */
    private ResponseEntity<Map<String, Object>> handleError(String logMessage, Exception e) {
        logger.error("{}: {}", logMessage, e.getMessage(), e);
        return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Lỗi server: " + e.getMessage(),
                "data", Collections.emptyList()
        ));
    }

    /**
     * Validate page number
     */
    private int validatePage(int page) {
        return page < 0 ? 0 : page;
    }

    /**
     * Validate page size
     */
    private int validateSize(int size) {
        return (size <= 0 || size > 100) ? 12 : size;
    }

    /**
     * Functional interface cho listing supplier
     */
    @FunctionalInterface
    private interface ListingSupplier {
        Page<ListingCartResponseDTO> get();
    }
}
