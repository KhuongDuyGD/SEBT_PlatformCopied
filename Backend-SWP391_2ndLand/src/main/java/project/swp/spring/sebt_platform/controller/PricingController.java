package project.swp.spring.sebt_platform.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.swp.spring.sebt_platform.dto.request.PricingSuggestRequestDTO;
import project.swp.spring.sebt_platform.service.PricingService;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);

    @Autowired
    private PricingService pricingService;

    @Autowired
    private Environment environment;

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
    public ResponseEntity<?> suggest(@RequestBody PricingSuggestRequestDTO dto) {
        try {
            if (dto.getProduct() == null || dto.getProduct().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thiếu thông tin sản phẩm để gợi ý giá");
            }
            PricingSuggestResponseDTO result = pricingService.suggestPrice(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error suggesting price: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi gợi ý giá");
        }
    }

    /**
     * GET /api/pricing/health - quick check for AI availability
     */
    @GetMapping("/health")
    public ResponseEntity<?> health(@RequestParam(value = "verbose", required = false, defaultValue = "false") boolean verbose) {
        try {
            String propKey = environment.getProperty("app.ai.gemini.apiKey");
            boolean hasEnv = System.getenv("GEMINI_API_KEY") != null;
            boolean hasProp = propKey != null && !propKey.isBlank();
            String model = environment.getProperty("app.ai.gemini.model", "(unset)");
            if (!verbose) {
                return ResponseEntity.ok(java.util.Map.of(
                        "hasApiKey", hasEnv || hasProp,
                        "timestamp", java.time.Instant.now().toString()
                ));
            }
            return ResponseEntity.ok(java.util.Map.of(
                    "hasApiKeyEnv", hasEnv,
                    "hasApiKeyProperty", hasProp,
                    "effectiveModel", model,
                    "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
