package project.swp.spring.sebt_platform.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.dto.response.SessionInfoResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
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
    @PutMapping("/favorite")
    public ResponseEntity<?> markFavorite(@RequestParam Long userId, @RequestParam Long listingId) {
        try {
            boolean result = memberService.markFavorite(userId, listingId);
            if (result) {
                return ResponseEntity.ok("Listing marked as favorite.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to mark listing as favorite.");
            }
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
    @DeleteMapping("/favorite")
    public ResponseEntity<?> unmarkFavorite(@RequestParam Long userId, @RequestParam Long listingId) {
        try {
            boolean result = memberService.unmarkFavorite(userId, listingId);
            if (result) {
                return ResponseEntity.ok("Listing unmarked as favorite.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to unmark listing as favorite.");
            }
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
            HttpSession session = request.getSession(false);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No active session. Please login first.");
            }

            // Get userId from session
            Long userId = (Long) session.getAttribute("userId");

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
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Admin access required.");
            }

            Long userId = (Long) session.getAttribute("userId");
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
            // Get current session - do not create new one if not exists
            HttpSession session = request.getSession(false);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No active session. Please login first.");
            }

            // Get userId from session
            Long userId = (Long) session.getAttribute("userId");

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

}
