package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.dto.request.PricingSuggestRequestDTO;
import project.swp.spring.sebt_platform.dto.response.PricingSuggestResponseDTO;

public interface PricingService {
    PricingSuggestResponseDTO suggestPrice(PricingSuggestRequestDTO request);
}
