package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.model.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.model.dto.response.ListingCartResponseDTO;

import java.util.List;

public interface ListingService {
    public List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord);
    public boolean createListing(CreateListingFormDTO createListingForm);
}
