package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.model.UserEntity;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

public interface MemberService {
    public UserProfileResponseDTO getUserProfileById(Long id);
    public UserEntity findUserByEmail(String email);

    public boolean markFavorite(Long userId, Long listingId);
    public boolean unmarkFavorite(Long userId, Long listingId);

    // Add new method for updating profile with DTO and userId
    public boolean updateProfile(UpdateProfileFormDTO updateProfileDTO, Long userId);

    Page<PostAnoucementResponseDTO> getPostAnoucementResponse(Long userId, Pageable pageable);

    UserEntity getCurrentUser(Long userId);

    public boolean changePassword(Long userId, String oldPassword, String newPassword);

    Page<ListingCartResponseDTO> getUserFavoriteCartListings(Long userId, Pageable pageable);

    BigDecimal getBalance(Long userId);

    public Object getConfigValue(String key);

    public boolean payByBalance(Long userId,Long requestId);
}
