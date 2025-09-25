package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.request.*;
import project.swp.spring.sebt_platform.dto.response.ErrorResponseDTO;
import project.swp.spring.sebt_platform.dto.response.SuccessResponseDTO;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;
import project.swp.spring.sebt_platform.service.AuthService;
import project.swp.spring.sebt_platform.service.MailService;
import project.swp.spring.sebt_platform.service.UserService;
import project.swp.spring.sebt_platform.util.Utils;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private Utils utils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDTO user) {
        try {
            // Input validation
            if (user.password() == null || user.password().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Password is required"));
            }
            if (user.email() == null || user.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Email is required"));
            }
            
            // Email format validation
            if (!user.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Invalid email format"));
            }
            
            // Password strength validation
            if (user.password().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Password must be at least 6 characters"));
            }

            UserEntity userEntity = userService.findUserByEmail(user.email());
            if (userEntity != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponseDTO("Email already exists"));
            }
            
            // Register user
            String pins = utils.generatePins();
            boolean success = authService.register(user.password(), user.email(), pins);

            // Send verification email
            mailService.sendVerificationEmail(user.email(), pins);

            if (success) {
                return ResponseEntity.ok(new SuccessResponseDTO(
                    "Registration successful. Please check your email for verification."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO("Registration failed"));
            }
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Registration failed"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO user, HttpServletRequest request) {
        try {
            if (user.email() == null || user.password() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Email and password are required"));
            }
            
            UserEntity loggedInUser = authService.login(user.email(), user.password());
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("email or password is incorrect"));
            }
            
            // Check if user is activated
            if (loggedInUser.getStatus() != UserStatus.ACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseDTO("Account not activated. Please verify your email first."));
            }
            
            // Tạo session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", loggedInUser.getId());
            session.setAttribute("username", loggedInUser.getUsername());
            session.setAttribute("email", loggedInUser.getEmail());
            session.setAttribute("role", loggedInUser.getRole());
            session.setAttribute("status", loggedInUser.getStatus());
            
            // Tạo DTO response
            UserSessionDTO sessionDTO = new UserSessionDTO(
                loggedInUser.getId(),
                loggedInUser.getUsername(),
                loggedInUser.getEmail(),
                loggedInUser.getRole(),
                loggedInUser.getStatus(),
                session.getId()
            );

            return ResponseEntity.ok(sessionDTO);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Login failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok(new SuccessResponseDTO("Logout successful"));
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Logout failed"));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody UserVerifyEmailDTO user) {
        try {
            if (user.pins() == null || user.pins().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("PIN is required"));
            }
            if (user.email() == null || user.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Email is required"));
            }
            
            boolean success = authService.verifyEmail(user.email(), user.pins());
            if (success) {
                return ResponseEntity.ok(new SuccessResponseDTO("Email verified successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDTO("Invalid PIN or email"));
            }
        } catch (Exception e) {
            System.err.println("Email verification error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Verification failed"));
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("No active session"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            String email = (String) session.getAttribute("email");
            UserRole role = (UserRole) session.getAttribute("role");
            UserStatus status = (UserStatus) session.getAttribute("status");
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO("Invalid session"));
            }
            
            UserSessionDTO sessionDTO = new UserSessionDTO(
                userId, username, email, role, status, session.getId()
            );
            
            return ResponseEntity.ok(sessionDTO);
        } catch (Exception e) {
            System.err.println("Get current user error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("Failed to get user information"));
        }
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.ok(new SessionCheckResponse(false, "No session"));
            }
            
            Long userId = (Long) session.getAttribute("userId");
            boolean isValid = userId != null;
            
            return ResponseEntity.ok(new SessionCheckResponse(isValid, 
                isValid ? "Valid session" : "Invalid session"));
        } catch (Exception e) {
            System.err.println("Check session error: " + e.getMessage());
            return ResponseEntity.ok(new SessionCheckResponse(false, "Session check failed"));
        }
    }

    public static class SessionCheckResponse {
        private boolean valid;
        private String message;

        public SessionCheckResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }

}
