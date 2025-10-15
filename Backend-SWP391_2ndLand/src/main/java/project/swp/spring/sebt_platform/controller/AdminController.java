package project.swp.spring.sebt_platform.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;
import project.swp.spring.sebt_platform.service.AdminService;
import project.swp.spring.sebt_platform.service.ListingService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ListingService listingService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post requests",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostListingCartResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/post-request")
    public ResponseEntity<?> getPostRequests(HttpServletRequest request, @RequestParam int page, @RequestParam int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }
            // Call your service method to get the paginated data
             Page<PostListingCartResponseDTO> postRequests = adminService.getPostListingCart(PageRequest.of(page, size));
             return ResponseEntity.ok(postRequests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post request approved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Failed to approve post request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/approve-request/{postRequestId}")
    public ResponseEntity<?> getApproveRequests(HttpServletRequest request, @PathVariable Long postRequestId) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }

                boolean result = adminService.approvePostListing(postRequestId);
                if (result) {
                    return ResponseEntity.ok("Post request approved successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to approve post request.");
                }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post request rejected successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Failed to reject post request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/reject-request/{postRequestId}")
    public ResponseEntity<?> getRejectRequests(HttpServletRequest request, @PathVariable Long postRequestId, @RequestParam String reason) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }

                boolean result = adminService.rejectPostListing(postRequestId, reason);
                if (result) {
                    return ResponseEntity.ok("Post request rejected successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reject post request.");
                }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved listing detail",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListingDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid listing ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Listing not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/listing-detail/{listingId}")
    public ResponseEntity<?> getListingDetailForAdmin(HttpServletRequest request, @PathVariable Long listingId) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }

            if (listingId == null || listingId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid listing ID");
            }

            ListingDetailResponseDTO detail = listingService.getListingDetailByIdForAdmin(listingId);

            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
            }

            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/members")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request, @RequestParam int page, @RequestParam int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }
            // Call your service method to get the paginated data
             Page<UserProfileResponseDTO> users = adminService.getAllMembers(PageRequest.of(page, size));
             return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members by status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/members/status")
    public ResponseEntity<?> getUsersByStatus(HttpServletRequest request, @RequestParam UserStatus status, @RequestParam int page, @RequestParam int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }
            // Call your service method to get the paginated data
            Page<UserProfileResponseDTO> users = adminService.getAllMembersByStatus(status, PageRequest.of(page, size));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved members by keyword",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/members/search")
    public ResponseEntity<?> searchUsersByKeyword(HttpServletRequest request, @RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals(UserRole.ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }
            // Call your service method to get the paginated data
            Page<UserProfileResponseDTO> users = adminService.searchMembersByKeyword(keyword, PageRequest.of(page, size));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
