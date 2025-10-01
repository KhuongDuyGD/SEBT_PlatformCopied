package project.swp.spring.sebt_platform.service;

import java.util.List;

import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;

public interface ListingService {

    List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord);

    boolean createListing(
            CreateListingFormDTO createListingForm,
            Long sellerId,
            List<Image> imageUrls,
            Image thumbnailUrl);
//
//    List<ListingResponseDTO> getAllActiveListings();
//
//    ListingResponseDTO getListingById(Long listingId);
//
//    List<ListingResponseDTO> getCarListings();
//
//    List<ListingResponseDTO> getPinListings();
//
//    List<ListingResponseDTO> getListingsBySeller(Long sellerId);
//
//    void incrementViewCount(Long listingId);
//
//    int deleteListingImages(List<String> publicIds);
}
