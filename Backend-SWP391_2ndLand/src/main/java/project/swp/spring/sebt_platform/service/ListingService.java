package project.swp.spring.sebt_platform.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.request.BatteryFilterFormDTO;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.request.EvFilterFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;

public interface ListingService {

    Page<ListingCartResponseDTO> getListingsByKeyWord(String keyWord, Long userId, Pageable pageable);

    Page<ListingCartResponseDTO> filterEvListings(
            EvFilterFormDTO evFilterFormDTO,
            Long userId,
            Pageable pageable);

    Page<ListingCartResponseDTO> filterBatteryListings(
            BatteryFilterFormDTO batteryFilterFormDTO,
            Long userId,
            Pageable pageable);

    boolean createListing(
            CreateListingFormDTO createListingForm,
            Long sellerId,
            List<Image> listingImages);

    ListingDetailResponseDTO getListingDetailById(Long listingId, Long userId);

    Page<ListingCartResponseDTO> getEvListingCarts(Long userId, Pageable pageable);

    Page<ListingCartResponseDTO> getBatteryListingCarts(Long userId, Pageable pageable);

    Page<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId, Pageable pageable);

    int deleteListingImages(List<String> publicIds);

    // Filter data endpoints - Cung cấp dữ liệu cho filter UI
    List<String> getAllProvinces();
    
    List<String> getAllDistricts(String province);
    
    List<String> getAllEvBrands();
    
    List<String> getAllBatteryBrands();
    
    List<String> getAllBatteryNames();
    
    List<String> getAllCompatibleVehicles();
    
    List<Integer> getAllEvYears();
    
    List<Integer> getAllBatteryYears();
}
