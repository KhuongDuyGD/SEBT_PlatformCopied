package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;

public interface ListingService {

    Page<ListingCartResponseDTO> getListingsByKeyWord(String keyWord, Long userId, Pageable pageable);

    // ✅ THÊM METHOD MỚI CHO ADVANCED SEARCH VỚI SET MERGE
    Page<ListingCartResponseDTO> searchListingsAdvanced(
            String title, 
            String brand, 
            Integer year, 
            Long userId, 
            Pageable pageable);

    boolean createListing(
            CreateListingFormDTO createListingForm,
            Long sellerId);

    ListingDetailResponseDTO getListingDetailById(Long listingId);

    Page<ListingCartResponseDTO> getEvListingCarts(Long userId, Pageable pageable);

    Page<ListingCartResponseDTO> getBatteryListingCarts(Long userId, Pageable pageable);

    Page<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId, Pageable pageable);

    int deleteListingImages(List<String> publicIds);
}
