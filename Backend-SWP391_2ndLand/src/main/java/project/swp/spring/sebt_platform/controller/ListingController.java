package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.service.ListingService;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;


    @PutMapping("/create")
    public ResponseEntity<?> createListingRequest(@RequestBody CreateListingFormDTO createListingFormDTO, HttpServletRequest request) {
        try{
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session. Please login first.");
            }

            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session. Please login again.");
            }

            if(listingService.createListing(createListingFormDTO)){
                return ResponseEntity.ok().body("Create listing request successfully");
            } else {
                return ResponseEntity.badRequest().body("Create listing request failed");
            }

        } catch (Exception e){
            System.err.println("Get listings for cart error: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
