package project.swp.spring.sebt_platform.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.dto.response.SessionInfoResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingFeePaymentResponseDTO;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.service.FeePolicyService;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.service.MemberService;
import project.swp.spring.sebt_platform.util.Utils;

@RestController
@RequestMapping("/api/members")
public class MemberController {

        private final MemberService memberService;
        private final ListingRepository listingRepository;
        private final FeePolicyService feePolicyService;

    @Autowired
        public MemberController(MemberService memberService, ListingRepository listingRepository, FeePolicyService feePolicyService) {
                this.memberService = memberService;
                this.listingRepository = listingRepository;
                this.feePolicyService = feePolicyService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PutMapping("/favorites/{listingId}")
    public ResponseEntity<?> markFavoriteNew(HttpServletRequest request, @PathVariable Long listingId) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            boolean result = memberService.markFavorite(userId, listingId);
            if (result) {
                return ResponseEntity.ok(Map.of(
                        "listingId", listingId,
                        "favorited", true
                ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to mark listing as favorite.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @DeleteMapping("/favorites/{listingId}")
    public ResponseEntity<?> unmarkFavoriteNew(HttpServletRequest request, @PathVariable Long listingId) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            boolean result = memberService.unmarkFavorite(userId, listingId);
            if (result) {
                return ResponseEntity.ok(Map.of(
                        "listingId", listingId,
                        "favorited", false
                ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to unmark listing as favorite.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to unmark favorite");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileFormDTO updateProfileDTO, HttpServletRequest request) {
        try {
            // Get current session - do not create new one if not exists
            Long userId = Utils.getUserIdFromSession(request);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid session. Please login again.");
            }

            // Update profile
            boolean updateResult = memberService.updateProfile(updateProfileDTO, userId);

            if (updateResult) {
                return ResponseEntity.ok("Profile updated successfully");
            } else {
                return ResponseEntity.ok("Failed to update profile.");
            }

        } catch (Exception e) {
            System.err.println("Update profile error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post responses",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostAnoucementResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Admin access required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping(path = "/post-responses")
    public ResponseEntity<?> getPostResponse(HttpServletRequest request, @RequestParam int page, @RequestParam int size) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            // Call your service method to get the paginated data
            Pageable pageable = Pageable.ofSize(size).withPage(page);

            var postResponses =  memberService.getPostAnoucementResponse(userId, pageable);
            return ResponseEntity.ok(postResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Cannot get post responses");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {

            // Get userId from session
            Long userId = Utils.getUserIdFromSession(request);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid session. Please login again.");
            }

            // Get user profile
            UserProfileResponseDTO userProfile = memberService.getUserProfileById(userId);

            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User profile not found.");
            }

            return ResponseEntity.ok(userProfile);

        } catch (Exception e) {
            System.err.println("Get profile error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Can not get profile");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session info retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SessionInfoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No active session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No active session");
            }

            // Return session information
            var sessionInfo = new SessionInfoResponseDTO(
                session.getId(),
                (Long) session.getAttribute("userId"),
                (String) session.getAttribute("username"),
                (String) session.getAttribute("email"),
                session.getCreationTime(),
                session.getLastAccessedTime(),
                session.getMaxInactiveInterval()
            );

            return ResponseEntity.ok(sessionInfo);

        } catch (Exception e) {
            System.err.println("Get session info error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get session info");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session info retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SessionInfoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No active session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
        /**
         * POST /api/members/listings/{id}/pay-fee
         * Thanh toán phí đăng tin sau khi bài đăng đã được admin duyệt (status = PAY_WAITING).
         * Logic:
         *  - Kiểm tra session
         *  - Kiểm tra listing thuộc về user và đang ở trạng thái PAY_WAITING
         *  - Tính fee qua ListingFeePolicy
         *  - Gọi service debit (refactor trong MemberServiceImpl) -> chuyển listing sang ACTIVE nếu đủ tiền
         *  - Trả về JSON chi tiết (fee, trạng thái mới, số dư, thiếu tiền hay không)
         */
        @PostMapping("/listings/{listingId}/pay-fee")
        public ResponseEntity<?> payListingFee(@PathVariable Long listingId, HttpServletRequest request) {
                try {
                        HttpSession session = request.getSession(false);
                        if (session == null) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session");
                        }
                        Long userId = (Long) session.getAttribute("userId");
                        if (userId == null) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
                        }

                        ListingEntity listing = listingRepository.findById(listingId).orElse(null);
                        if (listing == null) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
                        }

                        if (!listing.getSeller().getId().equals(userId)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Listing does not belong to current user");
                        }

                        if (listing.getStatus() != ListingStatus.PAY_WAITING) {
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Listing is not waiting for payment");
                        }

                        var fee = feePolicyService.computeListingFee(listing.getProduct() != null && listing.getProduct().getEvVehicle() != null,
                                listing.getProduct() != null && listing.getProduct().getBattery() != null,
                                listing.getPrice().longValue());

                        ListingFeePaymentResponseDTO result = memberService.payByBalance(userId, listing.getId(), fee);
                        return ResponseEntity.status(result.insufficientBalance() ? HttpStatus.PAYMENT_REQUIRED : HttpStatus.OK).body(result);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment processing error: " + e.getMessage());
                }
        }

        /**
         * GET /api/members/listings/{listingId}/fee
         * Trả về số phí cần thanh toán cho listing đang ở trạng thái PAY_WAITING (hoặc APPROVED nhưng chưa chuyển trạng thái – phòng trường hợp future refactor).
         */
        @GetMapping("/listings/{listingId}/fee")
        public ResponseEntity<?> getListingFee(@PathVariable Long listingId, HttpServletRequest request) {
                try {
                        HttpSession session = request.getSession(false);
                        if (session == null) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session");
                        }
                        Long userId = (Long) session.getAttribute("userId");
                        if (userId == null) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
                        }
                        ListingEntity listing = listingRepository.findById(listingId).orElse(null);
                        if (listing == null) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found");
                        }
                        if (!listing.getSeller().getId().equals(userId)) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Listing does not belong to current user");
                        }
                        if (listing.getStatus() != ListingStatus.PAY_WAITING) {
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Listing not in PAY_WAITING status");
                        }

                        var fee = feePolicyService.computeListingFee(listing.getProduct() != null && listing.getProduct().getEvVehicle() != null,
                                listing.getProduct() != null && listing.getProduct().getBattery() != null,
                                listing.getPrice().longValue());
                        return ResponseEntity.ok(Map.of("fee", fee));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot compute fee: " + e.getMessage());
                }
        }

        /**
         * GET /api/members/listings/pending-payment-count
         * Đếm số bài đăng ở trạng thái PAY_WAITING của user hiện tại.
         */
        @GetMapping("/listings/pending-payment-count")
        public ResponseEntity<?> countPendingPaymentListings(HttpServletRequest request) {
                try {
                        Long userId = Utils.getUserIdFromSession(request);
                        if (userId == null) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
                        }
                        long count = listingRepository.countListingEntitiesBySellerIdAndStatus(userId, ListingStatus.PAY_WAITING);
                        return ResponseEntity.ok(Map.of("count", count));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot count pending payment listings");
                }
        }

        // Moved ra ngoài: GET /api/members/profile-completeness
        @GetMapping("/profile-completeness")
        public ResponseEntity<?> getProfileCompleteness(HttpServletRequest request) {
                Long userId = Utils.getUserIdFromSession(request);
                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
                }
                var user = memberService.getCurrentUser(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
                }
                boolean phonePresent = user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty();
                return ResponseEntity.ok(Map.of("phonePresent", phonePresent));
        }

}