package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Pageable;
import java.util.List;

import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;

public interface ListingService {

    List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord, Long userId, Pageable pageable);

    boolean createListing(
            CreateListingFormDTO createListingForm,
            Long sellerId);

    ListingDetailResponseDTO getListingDetailById(Long listingId);

    List<ListingCartResponseDTO> getEvListingCarts(Long userId, Pageable pageable);

    List<ListingCartResponseDTO> getBatteryListingCarts(Long userId, Pageable pageable);

    List<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId, Pageable pageable);

    int deleteListingImages(List<String> publicIds);
}
