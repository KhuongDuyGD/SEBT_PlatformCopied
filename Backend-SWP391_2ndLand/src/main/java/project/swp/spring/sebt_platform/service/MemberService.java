package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.model.UserEntity;


public interface MemberService {
    public UserProfileResponseDTO getUserProfileById(Long id);
    public UserEntity findUserByEmail(String email);

    public boolean markFavorite(Long userId, Long listingId);
    public boolean unmarkFavorite(Long userId, Long listingId);

    // Add new method for updating profile with DTO and userId
    public boolean updateProfile(UpdateProfileFormDTO updateProfileDTO, Long userId);

    Page<PostAnoucementResponseDTO> getPostAnoucementResponse(Long userId, Pageable pageable);

    public boolean changePassword(Long userId, String oldPassword, String newPassword);

    Page<ListingCartResponseDTO> getUserFavoriteCartListings(Long userId, Pageable pageable);
}
