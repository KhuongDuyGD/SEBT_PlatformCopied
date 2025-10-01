package project.swp.spring.sebt_platform.service;

import java.util.List;

import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingResponseDTO;

public interface ListingService {
    
    /**
     * Tìm kiếm listing theo từ khóa
     * @param keyWord từ khóa tìm kiếm
     * @return danh sách listing theo format cart
     */
    List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord);
    
    /**
     * Tạo listing mới (tạo request cần admin duyệt)
     * @param createListingForm dữ liệu listing
     * @param sellerId ID người bán
     * @return true nếu tạo thành công, false nếu thất bại
     */
    boolean createListing(CreateListingFormDTO createListingForm, Long sellerId);
    
    /**
     * Tạo listing trực tiếp (không cần admin duyệt)
     * @param createListingForm dữ liệu listing
     * @param sellerId ID người bán
     * @return true nếu tạo thành công, false nếu thất bại
     */
    boolean createListingDirect(CreateListingFormDTO createListingForm, Long sellerId);
    
    /**
     * Lấy tất cả listing đang active
     * @return danh sách listing
     */
    List<ListingResponseDTO> getAllActiveListings();
    
    /**
     * Lấy listing theo ID
     * @param listingId ID của listing
     * @return thông tin chi tiết listing
     */
    ListingResponseDTO getListingById(Long listingId);
    
    /**
     * Lấy danh sách car listings (chỉ xe ô tô)
     * @return danh sách listing xe ô tô
     */
    List<ListingResponseDTO> getCarListings();
    
    /**
     * Lấy danh sách pin listings (chỉ pin)
     * @return danh sách listing pin
     */
    List<ListingResponseDTO> getPinListings();
    
    /**
     * Lấy listing của một người bán
     * @param sellerId ID người bán
     * @return danh sách listing của người bán
     */
    List<ListingResponseDTO> getListingsBySeller(Long sellerId);
    
    /**
     * Tăng view count cho listing
     * @param listingId ID của listing
     */
    void incrementViewCount(Long listingId);
}
