package project.swp.spring.sebt_platform.service;

import java.util.List;

import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;

public interface ListingService {

    List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord);

    boolean createListing(
            CreateListingFormDTO createListingForm,
            Long sellerId,
            List<Image> imageUrls,
            Image thumbnailUrl);

    List<ListingCartResponseDTO> getAllActiveListingCarts();

    ListingDetailResponseDTO getListingDetailById(Long listingId);

    List<ListingCartResponseDTO> getCarListingCarts();

    List<ListingCartResponseDTO> getPinListingCarts();

    List<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId);

    int deleteListingImages(List<String> publicIds);

}
