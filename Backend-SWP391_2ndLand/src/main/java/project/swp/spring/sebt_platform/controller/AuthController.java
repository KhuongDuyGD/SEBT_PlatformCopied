package project.swp.spring.sebt_platform.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import project.swp.spring.sebt_platform.dto.request.ResendOtpRequestDTO;
import project.swp.spring.sebt_platform.dto.request.UserLoginFormDTO;
import project.swp.spring.sebt_platform.dto.request.UserRegisterFormDTO;
import project.swp.spring.sebt_platform.dto.request.UserVerifyEmailFormDTO;
import project.swp.spring.sebt_platform.dto.response.UserSessionResponseDTO;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;
import project.swp.spring.sebt_platform.service.AuthService;
import project.swp.spring.sebt_platform.service.MailService;
import project.swp.spring.sebt_platform.service.MemberService;
import project.swp.spring.sebt_platform.util.Utils;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailService mailService;

    @Autowired
    private Utils utils;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration verification email sent",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterFormDTO user, HttpServletRequest request) {
        try {
            // Input validation
            if (user.password() == null || user.password().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password is required");
            }
            if (user.email() == null || user.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
            }
            
            // Email format validation
            if (!user.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid email format");
            }
            
            // Password strength validation
            if (user.password().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password must be at least 6 characters");
            }

            // Check if email already exists
            if (memberService.findUserByEmail(user.email()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email is already registered");
            }

            // Register user
            String pins = utils.generatePins();
            HttpSession session = request.getSession(true);
            session.setAttribute("pins", pins);
            session.setAttribute("password", user.password());
            session.setAttribute("email", user.email());

            // Send verification email
            mailService.sendVerificationEmail(user.email(), pins);
                return ResponseEntity.ok(
                    "Please check your email for verification.");
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Registration failed");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSessionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Account not activated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginFormDTO user, HttpServletRequest request) {
        try {
            if (user.email() == null || user.password() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email and password are required");
            }
            
            UserEntity loggedInUser = authService.login(user.email(), user.password());
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("email or password is incorrect");
            }
            
            // Check if user is activated
            if (loggedInUser.getStatus() != UserStatus.ACTIVE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Account not activated. Please verify your email first.");
            }
            
            // Tạo session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", loggedInUser.getId());
            session.setAttribute("username", loggedInUser.getUsername());
            session.setAttribute("email", loggedInUser.getEmail());
            session.setAttribute("role", loggedInUser.getRole());
            session.setAttribute("status", loggedInUser.getStatus());
            
            // Tạo DTO response
            UserSessionResponseDTO sessionDTO = new UserSessionResponseDTO(
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
                .body("Login failed");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or verification failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody UserVerifyEmailFormDTO user, HttpServletRequest request) {
        try {
            if (user.pins() == null || user.pins().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("PIN is required");
            }
            if (user.email() == null || user.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
            }

            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No verification process found. Please register again.");
            }

            String sessionPins = (String) session.getAttribute("pins");
            String sessionEmail = (String) session.getAttribute("email");
            String sessionPassword = (String) session.getAttribute("password");

            if (sessionPins == null || sessionEmail == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No verification process found. Please register again.");
            }

            if (!sessionPins.equals(user.pins())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("OTP does not match the registered email.");
            }

            boolean success = authService.register(sessionPassword,sessionEmail);

            if (success) {
                session.invalidate();
                return ResponseEntity.ok("Email verified, register successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid PIN or email");
            }
        } catch (Exception e) {
            System.err.println("Email verification error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Verification failed");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP resent successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or session not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequestDTO request, HttpServletRequest httpRequest) {
        try {
            if (request.email() == null || request.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
            }

            HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No verification process found. Please register again.");
            }

            String sessionEmail = (String) session.getAttribute("email");
            if (sessionEmail == null || !sessionEmail.equals(request.email())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email does not match the registration session.");
            }

            // Generate new OTP
            String newPins = utils.generatePins();
            
            // Update session with new OTP
            session.setAttribute("pins", newPins);
            
            // Send new OTP email
            mailService.sendVerificationEmail(request.email(), newPins);
            
            return ResponseEntity.ok("New OTP has been sent to your email");
            
        } catch (Exception e) {
            System.err.println("Resend OTP error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to resend OTP");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserSessionResponseDTO.class))),
            @ApiResponse(responseCode = "404",content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "500",content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseEntity.class)))
    })
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No active session");
            }
            
            Long userId = (Long) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            String email = (String) session.getAttribute("email");
            UserRole role = (UserRole) session.getAttribute("role");
            UserStatus status = (UserStatus) session.getAttribute("status");
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid session");
            }
            
            UserSessionResponseDTO sessionDTO = new UserSessionResponseDTO(
                userId, username, email, role, status, session.getId()
            );
            
            return ResponseEntity.ok(sessionDTO);
        } catch (Exception e) {
            System.err.println("Get current user error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get user information");
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SessionCheckResponse.class))),
            @ApiResponse(responseCode = "404",content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "500",content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseEntity.class)))
    })
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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                // Log thông tin logout để debug
                String sessionId = session.getId();
                Long userId = (Long) session.getAttribute("userId");
                System.out.println("Logout: User " + userId + " with session " + sessionId);

                session.invalidate();
                System.out.println("Session invalidated successfully");
            }

            // Primary cookie name is configured as SEBT_SESSION
            ResponseCookie deleteSebtSession = ResponseCookie.from("SEBT_SESSION", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();

            response.addHeader("Set-Cookie", deleteSebtSession.toString());
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
            e.printStackTrace(); // In stack trace để debug tốt hơn
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }
}
