package project.swp.spring.sebt_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.config.AiConfig;
import project.swp.spring.sebt_platform.dto.request.PricingSuggestRequestDTO;
import jakarta.validation.Valid;
import project.swp.spring.sebt_platform.service.PricingService;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import project.swp.spring.sebt_platform.service.WalletLedgerService;
import project.swp.spring.sebt_platform.util.Utils;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);

    @Autowired
    private PricingService pricingService;

    @Autowired
    private WalletLedgerService walletLedgerService;

    @Autowired
    private AiConfig environment;

    /**
     * POST /api/pricing/suggest - Get price suggestion (Gemini or heuristic fallback)
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pricing suggestion returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PricingSuggestResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/suggest")
    public ResponseEntity<?> suggest(@Valid @RequestBody PricingSuggestRequestDTO dto, HttpServletRequest request) {
        try {
            Long userId = Utils.getUserIdFromSession(request);
            if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("yêu cầu đăng nhập");
            if (dto.getProduct() == null || dto.getProduct().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("thiếu thông tin sản phẩm");
            }

           /*if(walletLedgerService.pricingFee(userId) == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số dư của bạn không đủ để sử dụng dịch vụ");
            }*/ // tư bản time

            PricingSuggestResponseDTO result = pricingService.suggestPrice(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error suggesting price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    /**
     * GET /api/pricing/health - quick check for AI availability
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health check successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/health")
    public ResponseEntity<?> health(@RequestParam(value = "verbose", required = false, defaultValue = "false") boolean verbose) {
        try {
            String propKey = environment.getGeminiApiKey();
            boolean hasEnv = System.getenv("GEMINI_API_KEY") != null;
            boolean hasProp = propKey != null && !propKey.isBlank();
            String model = environment.getGeminiModel();
            if (!verbose) {
                return ResponseEntity.ok(java.util.Map.of(
                        "hasApiKey", hasEnv || hasProp,
                        "timestamp", Instant.now().toString()
                ));
            }
            return ResponseEntity.ok(java.util.Map.of(
                    "hasApiKeyEnv", hasEnv,
                    "hasApiKeyProperty", hasProp,
                    "effectiveModel", model,
                    "timestamp", Instant.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
