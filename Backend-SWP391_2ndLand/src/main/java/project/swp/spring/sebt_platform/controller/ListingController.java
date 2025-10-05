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
 * REST Controller for Listings - Base URL: /api/listings
 */
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

    @Autowired
    private ListingService listingService;

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * POST /api/listings/create - Create new listing with images
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createListing(
            @ModelAttribute CreateListingFormDTO dto,
            HttpServletRequest request) {

        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return error("Vui lòng đăng nhập để tạo bài đăng", HttpStatus.UNAUTHORIZED);
            }

            if (dto.getImages() == null || dto.getImages().isEmpty()) {
                return error("Vui lòng upload ít nhất một ảnh", HttpStatus.BAD_REQUEST);
            }

            logger.info("Creating listing - userId: {}, title: '{}', images: {}",
                userId, dto.getTitle(), dto.getImages().size());

            List<Image> images = cloudinaryService.uploadMultipleImages(dto.getImages(), "listings");
            if (images.isEmpty()) {
                return error("Upload ảnh thất bại", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            boolean success = listingService.createListing(dto, userId, images);
            if (!success) {
                return error("Tạo bài đăng thất bại. Vui lòng kiểm tra lại thông tin", HttpStatus.BAD_REQUEST);
            }

            return success(Map.of(
                "userId", userId,
                "status", "PENDING",
                "imagesUploaded", images.size()
            ), "Bài đăng đã được tạo thành công và đang chờ admin xét duyệt");

        } catch (Exception e) {
            logger.error("Error creating listing: {}", e.getMessage(), e);
            return error("Lỗi server: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/evCart - Get all EV listings
     */
    @GetMapping("/evCart")
    public ResponseEntity<Map<String, Object>> getEvListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getEvListingCarts(
                getUserId(request), pageable);

            return successPage(results, "Tìm thấy " + results.getTotalElements() + " EV");
        } catch (Exception e) {
            logger.error("Error getting EV listings: {}", e.getMessage(), e);
            return error("Lỗi khi lấy danh sách EV", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/batteryCart - Get all battery listings
     */
    @GetMapping("/batteryCart")
    public ResponseEntity<Map<String, Object>> getBatteryListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getBatteryListingCarts(
                getUserId(request), pageable);

            return successPage(results, "Tìm thấy " + results.getTotalElements() + " pin");
        } catch (Exception e) {
            logger.error("Error getting battery listings: {}", e.getMessage(), e);
            return error("Lỗi khi lấy danh sách pin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/detail/{id} - Get listing detail
     */
    @GetMapping("/detail/{listingId}")
    public ResponseEntity<Map<String, Object>> getListingDetail(
            @PathVariable Long listingId,
            HttpServletRequest request) {

        try {
            if (listingId == null || listingId <= 0) {
                return error("ID bài đăng không hợp lệ", HttpStatus.BAD_REQUEST);
            }

            ListingDetailResponseDTO detail = listingService.getListingDetailById(
                listingId, getUserId(request));

            if (detail == null) {
                return error("Không tìm thấy bài đăng", HttpStatus.NOT_FOUND);
            }

            return success(detail, "Lấy chi tiết listing thành công");
        } catch (Exception e) {
            logger.error("Error getting listing detail: {}", e.getMessage(), e);
            return error("Lỗi khi lấy chi tiết listing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/search - Search listings by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchListings(
            @RequestParam String keyword,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return error("Từ khóa tìm kiếm không được để trống", HttpStatus.BAD_REQUEST);
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingsByKeyWord(
                keyword.trim(), getUserId(request), pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results.getContent());
            response.put("pagination", pagination(results));
            response.put("keyword", keyword.trim());
            response.put("message", String.format("Tìm thấy %d kết quả cho '%s'",
                results.getTotalElements(), keyword.trim()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching listings: {}", e.getMessage(), e);
            return error("Lỗi khi tìm kiếm", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/ev-filter - Filter EV listings
     */
    @GetMapping("/ev-filter")
    public ResponseEntity<Map<String, Object>> filterEvListings(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.filterEvListings(
                year, vehicleType, minPrice, maxPrice, getUserId(request), pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results.getContent());
            response.put("pagination", pagination(results));
            response.put("filters", Map.of(
                "year", year != null ? year : 0,
                "vehicleType", vehicleType != null ? vehicleType.toString() : "",
                "minPrice", minPrice != null ? minPrice : 0,
                "maxPrice", maxPrice != null ? maxPrice : 0
            ));
            response.put("message", "Tìm thấy " + results.getTotalElements() + " kết quả");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering EV listings: {}", e.getMessage(), e);
            return error("Lỗi khi lọc EV", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/battery-filter - Filter battery listings
     */
    @GetMapping("/battery-filter")
    public ResponseEntity<Map<String, Object>> filterBatteryListings(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer year,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.filterBatteryListings(
                year, minPrice, maxPrice, getUserId(request), pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results.getContent());
            response.put("pagination", pagination(results));
            response.put("filters", Map.of(
                "year", year != null ? year : 0,
                "minPrice", minPrice != null ? minPrice : 0,
                "maxPrice", maxPrice != null ? maxPrice : 0
            ));
            response.put("message", "Tìm thấy " + results.getTotalElements() + " kết quả");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering battery listings: {}", e.getMessage(), e);
            return error("Lỗi khi lọc pin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/listings/my-listings - Get user's listings
     */
    @GetMapping("/my-listings")
    public ResponseEntity<Map<String, Object>> getMyListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return error("Cần đăng nhập để xem danh sách của bạn", HttpStatus.UNAUTHORIZED);
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingCartsBySeller(userId, pageable);

            return successPage(results, "Bạn có " + results.getTotalElements() + " bài đăng");
        } catch (Exception e) {
            logger.error("Error getting my listings: {}", e.getMessage(), e);
            return error("Lỗi khi lấy danh sách của bạn", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========== HELPER METHODS ==========

    private Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) return userId;
        }

        String header = request.getHeader("X-User-ID");
        if (header != null) {
            try {
                return Long.parseLong(header);
            } catch (NumberFormatException e) {
                logger.warn("Invalid X-User-ID header: {}", header);
            }
        }
        return null;
    }

    private int validateSize(int size) {
        return (size <= 0 || size > 100) ? 12 : size;
    }

    private Map<String, Object> pagination(Page<?> page) {
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

    private ResponseEntity<Map<String, Object>> success(Object data, String message) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", data,
            "message", message
        ));
    }

    private ResponseEntity<Map<String, Object>> successPage(Page<ListingCartResponseDTO> page, String message) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", page.getContent(),
            "pagination", pagination(page),
            "message", message
        ));
    }

    private ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
            "success", false,
            "message", message
        ));
    }
}
