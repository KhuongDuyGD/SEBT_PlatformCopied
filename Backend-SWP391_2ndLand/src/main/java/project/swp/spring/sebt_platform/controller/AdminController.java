package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.response.PostListingCartResponseDTO;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

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

    @GetMapping("/approve-request/{post_request_id}")
    public  ResponseEntity<?> getApproveRequests(HttpServletRequest request, @PathVariable Long postRequestId, @RequestParam int page, @RequestParam int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }

                boolean result = adminService.approvePostListing(postRequestId);
                if (result) {
                    adminService.addPostResponse(postRequestId);
                    return ResponseEntity.ok("Post request approved successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to approve post request.");
                }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/reject-request/{postRequestId}")
    public  ResponseEntity<?> getRejectRequests(HttpServletRequest request, @PathVariable Long postRequestId, @RequestParam String reason, @RequestParam int page, @RequestParam int size) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("role") == null || !session.getAttribute("role").equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Admin access required.");
            }

                boolean result = adminService.rejectPostListing(postRequestId, reason);
                if (result) {
                    adminService.addPostResponse(postRequestId);
                    return ResponseEntity.ok("Post request rejected successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reject post request.");
                }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
