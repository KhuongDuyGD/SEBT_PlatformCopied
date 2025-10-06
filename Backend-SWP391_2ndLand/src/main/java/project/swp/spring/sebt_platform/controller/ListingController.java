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
    public ResponseEntity<?> createListing(
            @ModelAttribute CreateListingFormDTO dto,
            HttpServletRequest request) {

        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để tạo bài đăng"
                ));
            }

            if (dto.getImages() == null || dto.getImages().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Vui lòng tải lên ít nhất một ảnh cho bài đăng"
                ));
            }

            logger.info("Creating listing - userId: {}, title: '{}', images: {}",
                userId, dto.getTitle(), dto.getImages().size());

            List<Image> images = cloudinaryService.uploadMultipleImages(dto.getImages(), "listings");
            if (images.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Tải lên ảnh thất bại. Vui lòng thử lại"
                ));
            }

            boolean success = listingService.createListing(dto, userId, images);
            if (!success) {
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Tạo bài đăng thất bại. Vui lòng kiểm tra lại thông tin"
               ));
            }

            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Tạo bài đăng thành công"
            ));

        } catch (Exception e) {
            logger.error("Error creating listing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi tạo bài đăng"
            ));
        }
    }

    /**
     * GET /api/listings/evCart - Get all EV listings
     */
    @GetMapping("/evCart")
    public ResponseEntity<?> getEvListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getEvListingCarts(
                getUserId(request), pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error getting EV listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy danh sách EV"
            ));
        }
    }

    /**
     * GET /api/listings/batteryCart - Get all battery listings
     */
    @GetMapping("/batteryCart")
    public ResponseEntity<?> getBatteryListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getBatteryListingCarts(
                getUserId(request), pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error getting battery listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy danh sách pin"
            ));
        }
    }

    /**
     * GET /api/listings/detail/{id} - Get listing detail
     */
    @GetMapping("/detail/{listingId}")
    public ResponseEntity<?> getListingDetail(
            @PathVariable Long listingId,
            HttpServletRequest request) {

        try {
            if (listingId == null || listingId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "ID bài đăng không hợp lệ"
                ));
            }

            ListingDetailResponseDTO detail = listingService.getListingDetailById(
                listingId, getUserId(request));

            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy bài đăng với ID đã cho"
                ));
            }

            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            logger.error("Error getting listing detail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy chi tiết bài đăng"
            ));
        }
    }

    /**
     * GET /api/listings/search - Search listings by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchListings(
            @RequestParam String keyword,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Vui lòng nhập từ khóa tìm kiếm"
                ));
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingsByKeyWord(
                keyword.trim(), getUserId(request), pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error searching listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi tìm kiếm bài đăng"
            ));
        }
    }

    /**
     * GET /api/listings/ev-filter - Filter EV listings
     */
    @GetMapping("/ev-filter")
    public ResponseEntity<?> filterEvListings(
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

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error filtering EV listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi lọc EV"
            ));
        }
    }

    /**
     * GET /api/listings/battery-filter - Filter battery listings
     */
    @GetMapping("/battery-filter")
    public ResponseEntity<?> filterBatteryListings(
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

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error filtering battery listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi lọc pin"
            ));
        }
    }

    /**
     * GET /api/listings/my-listings - Get user's listings
     */
    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để xem bài đăng của bạn"
                ));
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingCartsBySeller(userId, pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error getting my listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Lỗi khi lấy danh sách của bạn"
            ));
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

}
