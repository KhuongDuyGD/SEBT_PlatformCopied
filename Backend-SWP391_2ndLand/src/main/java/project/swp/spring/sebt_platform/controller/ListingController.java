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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.Part;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import project.swp.spring.sebt_platform.dto.object.*;
import project.swp.spring.sebt_platform.dto.request.BatteryFilterFormDTO;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.request.EvFilterFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.service.CloudinaryService;
import project.swp.spring.sebt_platform.service.ListingService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listing created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or image upload failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createListing(
            @RequestPart("data") CreateListingFormDTO dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imagesParam,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Vui lòng đăng nhập để tạo bài đăng");
        }
        // Gán images từ part nếu có
        if (imagesParam != null && !imagesParam.isEmpty()) {
            dto.setImages(imagesParam);
        }
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vui lòng tải lên ít nhất một ảnh cho bài đăng (thiếu field 'images')");
        }

        // Manual validation & normalization
        project.swp.spring.sebt_platform.validation.CreateListingValidator validator = new project.swp.spring.sebt_platform.validation.CreateListingValidator();
        var result = validator.validateAndNormalize(dto);
        if (result.hasErrors()) {
            throw new project.swp.spring.sebt_platform.exception.ValidationException("Validation failed", result.getErrors());
        }

        logger.info("[CREATE_LISTING_REQUEST] userId={} title='{}' category={} listingType={} evPresent={} batteryPresent={} images={}",
                userId,
                dto.getTitle(),
                dto.getCategory(),
                dto.getListingType(),
                dto.getProduct()!=null && dto.getProduct().getEv()!=null,
                dto.getProduct()!=null && dto.getProduct().getBattery()!=null,
                dto.getImages()!=null?dto.getImages().size():0);

        try {
            List<Image> images = cloudinaryService.uploadMultipleImages(dto.getImages(), "listings").get();
            if (images.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tải lên ảnh thất bại. Vui lòng thử lại");
            }

            boolean success = listingService.createListing(dto, userId, images);
            if (!success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Tạo bài đăng thất bại. Vui lòng kiểm tra lại thông tin");
            }
            return ResponseEntity.ok("Tạo bài đăng thành công");
        } catch (project.swp.spring.sebt_platform.exception.ValidationException ve) {
            throw ve; // will be handled by advice
        } catch (Exception e) {
            logger.error("Error creating listing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo bài đăng");
        }
    }

    /**
     * Ghi lại thông tin multipart parts để hỗ trợ debug khi danh sách ảnh bị null.
     * Sẽ không ảnh hưởng performance nhiều vì chỉ gọi khi lỗi thiếu ảnh.
     */
    private void logMultipartDiagnostics(HttpServletRequest request) {
        try {
            String ct = request.getContentType();
            if (ct == null || !ct.toLowerCase().startsWith("multipart/")) {
                logger.debug("[MULTIPART_DEBUG] contentType={} (không phải multipart?)", ct);
                return;
            }
            Collection<Part> parts = request.getParts();
            List<String> names = parts.stream().map(Part::getName).toList();
            logger.debug("[MULTIPART_DEBUG] partCount={} names={}", parts.size(), names);
            for (Part p : parts) {
                List<String> headerNames = new ArrayList<>();
                for (String h : p.getHeaderNames()) {
                    headerNames.add(h + ':' + p.getHeaders(h));
                }
                logger.debug("[MULTIPART_DEBUG] part name={} size={} ct={} headers={}",
                        p.getName(), p.getSize(), p.getContentType(), headerNames);
            }
        } catch (Exception ex) {
            logger.debug("[MULTIPART_DEBUG] Lỗi khi ghi thông tin multipart: {}", ex.toString());
        }
    }

    /**
     * GET /api/listings/evCart - Get all EV listings
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved EV listings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách EV");
        }
    }

    /**
     * GET /api/listings/batteryCart - Get all battery listings
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved battery listings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách pin");
        }
    }

    /**
     * GET /api/listings/detail/{id} - Get listing detail
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved listing detail",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid listing ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Listing not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/detail/{listingId}")
    public ResponseEntity<?> getListingDetail(
            @PathVariable Long listingId,
            HttpServletRequest request) {
        try {
            if (listingId == null || listingId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("ID bài đăng không hợp lệ");
            }

            ListingDetailResponseDTO detail = listingService.getListingDetailById(
                listingId, getUserId(request));

            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy bài đăng với ID đã cho");
            }

            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            logger.error("Error getting listing detail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy chi tiết bài đăng");
        }
    }

    /**
     * GET /api/listings/search - Search listings by keyword
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search keyword",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchListings(
            @RequestParam String keyword,
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vui lòng nhập từ khóa tìm kiếm");
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingsByKeyWord(
                keyword.trim(), getUserId(request), pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error searching listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi tìm kiếm bài đăng");
        }
    }

    /**
     * GET /api/listings/ev-filter - Filter EV listings
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered EV listings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/ev-filter")
    public ResponseEntity<?> filterEvListings(
            HttpServletRequest request,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minBatteryCapacity,
            @RequestParam(required = false) Integer maxBatteryCapacity,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            EvFilterFormDTO dto = new EvFilterFormDTO(
                    vehicleType,
                    year,
                    brand,
                    location,
                    minBatteryCapacity,
                    maxBatteryCapacity,
                    minPrice,
                    maxPrice
            );
            Page<ListingCartResponseDTO> results = listingService.filterEvListings(
                    dto, getUserId(request), pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error filtering EV listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lọc EV");
        }
    }

    /**
     * GET /api/listings/battery-filter - Filter battery listings
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered battery listings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/battery-filter")
    public ResponseEntity<?> filterBatteryListings(
            HttpServletRequest request,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String compatibility,
            @RequestParam(required = false) Integer minBatteryCapacity,
            @RequestParam(required = false) Integer maxBatteryCapacity,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            BatteryFilterFormDTO dto = new BatteryFilterFormDTO(
                    brand,
                    location,
                    compatibility,
                    minBatteryCapacity,
                    maxBatteryCapacity,
                    minPrice,
                    maxPrice
            );
            Page<ListingCartResponseDTO> results = listingService.filterBatteryListings(
                    dto, getUserId(request), pageable);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error filtering battery listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lọc pin");
        }
    }

    /**
     * GET /api/listings/my-listings - Get user's listings
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user's listings",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "User not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body("Vui lòng đăng nhập để xem bài đăng của bạn");
            }

            Pageable pageable = PageRequest.of(Math.max(0, page), validateSize(size));
            Page<ListingCartResponseDTO> results = listingService.getListingCartsBySeller(userId, pageable);

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error getting my listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách của bạn");
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
