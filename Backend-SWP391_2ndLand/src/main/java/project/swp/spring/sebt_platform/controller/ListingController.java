package project.swp.spring.sebt_platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingResponseDTO;
import project.swp.spring.sebt_platform.service.ListingService;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    /**
     * Tạo listing mới trực tiếp (không cần admin duyệt)
     * @param createListingFormDTO thông tin listing
     * @param request HTTP request để lấy session
     * @return response với thông báo kết quả
     */
    @PostMapping("/create")
    public ResponseEntity<?> createListingDirect(@RequestBody CreateListingFormDTO createListingFormDTO, 
                                                 HttpServletRequest request) {
        try {
            System.out.println("🔐 CREATE LISTING - Authentication check");
            System.out.println("📥 Request headers: " + request.getHeaderNames());
            System.out.println("🍪 Cookies: " + java.util.Arrays.toString(request.getCookies()));
            
            HttpSession session = request.getSession(false);
            System.out.println("🔍 Session exists: " + (session != null));
            if (session != null) {
                System.out.println("🔍 Session ID: " + session.getId());
                System.out.println("🔍 Session attributes: ");
                java.util.Enumeration<String> attrs = session.getAttributeNames();
                while (attrs.hasMoreElements()) {
                    String attr = attrs.nextElement();
                    System.out.println("   - " + attr + ": " + session.getAttribute(attr));
                }
            }
            
            if (session == null) {
                System.err.println("❌ No active session found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            System.out.println("👤 UserId from session: " + userId);
            
            // Sử dụng sellerId từ DTO hoặc fallback từ session
            Long sellerId = createListingFormDTO.sellerId();
            if (sellerId == null) {
                sellerId = userId;
            }
            System.out.println("👤 Final sellerId to use: " + sellerId);
            
            if (sellerId == null) {
                System.err.println("❌ No sellerId available from DTO or session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication failed. Please login again.");
            }

            // Tạo listing trực tiếp với status ACTIVE, không cần admin duyệt
            if (listingService.createListingDirect(createListingFormDTO, sellerId)) {
                return ResponseEntity.ok().body("Create listing successfully");
            } else {
                return ResponseEntity.badRequest().body("Create listing failed");
            }

        } catch (Exception e) {
            System.err.println("Create listing error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Tạo listing request (cần admin duyệt)
     * @param createListingFormDTO thông tin listing
     * @param request HTTP request để lấy session
     * @return response với thông báo kết quả
     */
    @PutMapping("/create")
    public ResponseEntity<?> createListingRequest(@RequestBody CreateListingFormDTO createListingFormDTO, 
                                                  HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid session. Please login again.");
            }

            if (listingService.createListing(createListingFormDTO, userId)) {
                return ResponseEntity.ok().body("Create listing request successfully");
            } else {
                return ResponseEntity.badRequest().body("Create listing request failed");
            }

        } catch (Exception e) {
            System.err.println("Create listing error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả listing đang active
     * @return danh sách listing
     */
    @GetMapping("/all")
    public ResponseEntity<List<ListingResponseDTO>> getAllActiveListings() {
        try {
            List<ListingResponseDTO> listings = listingService.getAllActiveListings();
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            System.err.println("Get all listings error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy thông tin chi tiết một listing theo ID
     * @param id ID của listing
     * @return thông tin chi tiết listing
     */
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponseDTO> getListingById(@PathVariable Long id) {
        try {
            ListingResponseDTO listing = listingService.getListingById(id);
            if (listing == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Tăng view count khi xem chi tiết listing
            listingService.incrementViewCount(id);
            
            return ResponseEntity.ok(listing);
        } catch (Exception e) {
            System.err.println("Get listing by ID error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách car listings (chỉ xe ô tô)
     * @return danh sách listing xe ô tô
     */
    @GetMapping("/cars")
    public ResponseEntity<List<ListingResponseDTO>> getCarListings() {
        try {
            List<ListingResponseDTO> carListings = listingService.getCarListings();
            return ResponseEntity.ok(carListings);
        } catch (Exception e) {
            System.err.println("Get car listings error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách pin listings (chỉ pin)
     * @return danh sách listing pin
     */
    @GetMapping("/pins")
    public ResponseEntity<List<ListingResponseDTO>> getPinListings() {
        try {
            List<ListingResponseDTO> pinListings = listingService.getPinListings();
            return ResponseEntity.ok(pinListings);
        } catch (Exception e) {
            System.err.println("Get pin listings error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy danh sách listing của người bán hiện tại
     * @param request HTTP request để lấy session
     * @return danh sách listing của người bán
     */
    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid session. Please login again.");
            }

            List<ListingResponseDTO> myListings = listingService.getListingsBySeller(userId);
            return ResponseEntity.ok(myListings);
        } catch (Exception e) {
            System.err.println("Get my listings error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách listing của một người bán theo ID
     * @param sellerId ID của người bán
     * @return danh sách listing của người bán
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ListingResponseDTO>> getListingsBySeller(@PathVariable Long sellerId) {
        try {
            List<ListingResponseDTO> listings = listingService.getListingsBySeller(sellerId);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            System.err.println("Get listings by seller error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
