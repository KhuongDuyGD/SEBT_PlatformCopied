package project.swp.spring.sebt_platform.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.swp.spring.sebt_platform.service.VnpayService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
public class VnpayController {

    @Autowired
    private VnpayService vnpayService;


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
    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestParam double amount,
                                           HttpServletRequest request) {
        try{
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("login required");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("login required");
            }

            if (amount <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("amount must be positive");
            }

            // NOTE: Prefer using POST /api/wallet/topups (WalletController) for new integrations.
            String paymentUrl = vnpayService.createPaymentUrl(amount,userId,request);
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid amount");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HttpServletResponse.class))),
            @ApiResponse(responseCode = "401", description = "No active session or invalid session",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HttpServletResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HttpServletResponse.class)))
    })
    @GetMapping("/return")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        try {
            boolean valid = vnpayService.validateReturn(params);
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            if (valid && "00".equals(responseCode)) {
                // TODO: (Hardening) validate that vnp_Amount matches pending transaction amount before crediting.
                vnpayService.updateTransactionStatus(txnRef, true);
                response.sendRedirect("http://localhost:5173/payment-success?orderId="+txnRef);
            } else {
                vnpayService.updateTransactionStatus(txnRef, false);
                response.sendRedirect("http://localhost:5173/payment-failed?orderId="+txnRef);
            }
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/payment-failed");
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
    @GetMapping("/refund")
    public ResponseEntity<?> refund(@RequestParam Long userId,
                                    @RequestParam Double amount,
                                    HttpServletRequest request ) throws UnsupportedEncodingException {
      try{


          return null;
      } catch(Exception e){
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
      }
    }
}
