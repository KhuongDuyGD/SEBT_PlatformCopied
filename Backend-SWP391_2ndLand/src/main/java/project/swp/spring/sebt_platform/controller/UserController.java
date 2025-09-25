package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.request.*;
import project.swp.spring.sebt_platform.dto.response.*;
import project.swp.spring.sebt_platform.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileDTO updateProfileDTO, HttpServletRequest request) {
        try {
            // Get current session - do not create new one if not exists
            HttpSession session = request.getSession(false);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("No active session. Please login first."));
            }

            // Get userId from session
            Long userId = (Long) session.getAttribute("userId");

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("Invalid session. Please login again."));
            }

            // Update profile
            boolean updateResult = userService.updateProfile(updateProfileDTO, userId);

            if (updateResult) {
                return ResponseEntity.ok(new SuccessResponseDTO("Profile updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Failed to update profile. User not found."));
            }

        } catch (Exception e) {
            System.err.println("Update profile error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            // Get current session - do not create new one if not exists
            HttpSession session = request.getSession(false);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("No active session. Please login first."));
            }

            // Get userId from session
            Long userId = (Long) session.getAttribute("userId");

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("Invalid session. Please login again."));
            }

            // Get user profile
            var userProfile = userService.findUserById(userId);

            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDTO("User profile not found."));
            }

            return ResponseEntity.ok(new SuccessResponseDTO("Profile retrieved successfully", userProfile));

        } catch (Exception e) {
            System.err.println("Get profile error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("No active session"));
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

            return ResponseEntity.ok(new SuccessResponseDTO("Session info retrieved", sessionInfo));

        } catch (Exception e) {
            System.err.println("Get session info error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to get session info"));
        }
    }


}
