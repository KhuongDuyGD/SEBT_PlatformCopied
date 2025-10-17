package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.model.*;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.repository.*;
import project.swp.spring.sebt_platform.service.MemberService;
import project.swp.spring.sebt_platform.util.Utils;

import java.math.BigDecimal;
import project.swp.spring.sebt_platform.dto.response.ListingFeePaymentResponseDTO;
import project.swp.spring.sebt_platform.service.WalletLedgerService;
import project.swp.spring.sebt_platform.service.FeePolicyService;
import java.time.LocalDateTime;

@Service
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;

    private final FavoriteRepository favoriteRepository;

    private final ListingRepository listingRepository;

    private final WalletRepository walletRepository;

    // Legacy repositories (postRequestRepository, systemConfigRepository) không còn dùng cho luồng thanh toán phí mới
    private final WalletLedgerService walletLedgerService;

    @Autowired
    public MemberServiceImpl(UserRepository userRepository,
                             FavoriteRepository favoriteRepository,
                             ListingRepository listingRepository,
                             WalletRepository walletRepository,
                             WalletLedgerService walletLedgerService) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
        this.walletRepository = walletRepository;
        this.walletLedgerService = walletLedgerService;

    }

    @Override
    public UserProfileResponseDTO getUserProfileById(Long id) {
        try {
            UserEntity user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return null;
            }
            return new UserProfileResponseDTO(user.getUsername(), user.getEmail(), user.getPhoneNumber(), user.getAvatar(), user.getCreatedAt());

        } catch (Exception e) {
            System.err.println("Find user by ID error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public UserEntity findUserByEmail(String email) {
        try {
            return userRepository.findUserByEmail(email);
        } catch (Exception e) {
            System.err.println("Find user by email error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean markFavorite(Long userId, Long listingId) {
        FavoriteEntity existing = favoriteRepository.findByUserIdAndListingId(userId, listingId);
        if (existing != null) {
            return true;
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        ListingEntity listing = listingRepository.findById(listingId).orElse(null);

        if (user != null && listing != null) {
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setUser(user);
            favorite.setListing(listing);
            favoriteRepository.save(favorite);
            return true;
        }
        return false;
    }

    @Override
    public boolean unmarkFavorite(Long userId, Long listingId) {
        FavoriteEntity existing = favoriteRepository.findByUserIdAndListingId(userId, listingId);
        if (existing != null) {
            favoriteRepository.delete(existing);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật profile.
     * Trả về true nếu có ít nhất một trường thay đổi; false nếu không đổi hoặc lỗi.
     */
    @Override
    @Transactional
    public boolean updateProfile(UpdateProfileFormDTO dto, Long userId) {
        try {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.err.println("User not found with ID: " + userId);
                return false;
            }

            boolean changed = false;

            // Username
            if (dto.username() != null && !dto.username().trim().isEmpty()
                    && !dto.username().equals(user.getUsername())) {

                UserEntity existed = userRepository.findUserByUsername(dto.username().trim());
                if (existed != null && !existed.getId().equals(user.getId())) {
                    System.err.println("Username already taken: " + dto.username());
                    return false; // hoặc ném exception custom
                }
                user.setUsername(dto.username().trim());
                changed = true;
            }

            // Phone number
            if (dto.phoneNumber() != null && !dto.phoneNumber().trim().isEmpty()
                    && !dto.phoneNumber().equals(user.getPhoneNumber())) {
                user.setPhoneNumber(dto.phoneNumber().trim());
                changed = true;
            }

            // Avatar URL
            if (dto.avatarUrl() != null && !dto.avatarUrl().trim().isEmpty()
                    && !dto.avatarUrl().equals(user.getAvatar())) { // giả sử field tên avatar
                user.setAvatar(dto.avatarUrl().trim());
                changed = true;
            }

            if (!changed) {
                System.out.println("No changes detected for user ID: " + userId);
                // Tùy ý: trả true (coi như OK) hoặc false (giữ nguyên). Ở đây trả false để giống logic cũ.
                return false;
            }

            userRepository.save(user);
            System.out.println("Profile updated successfully for user ID: " + userId);
            return true;

        } catch (Exception e) {
            System.err.println("Update profile service error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Page<PostAnoucementResponseDTO> getPostAnoucementResponse(Long userId, Pageable pageable) {
        try {
            return null;
        } catch (Exception e) {
            System.err.println("Get post announcement response error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public UserEntity getCurrentUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public boolean changePassword(Long userId,String oldPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        assert user != null;
        String salt = user.getSalt();
        if (user.getPassword().equals(Utils.encript(oldPassword, salt))) {
            user.setPassword(Utils.encript(newPassword, salt));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } else {
            System.err.println("Old password does not match for user ID: " + userId);
        }
        return false;
    }

    @Override
    public Page<ListingCartResponseDTO> getUserFavoriteCartListings(Long userId, Pageable pageable) {
        try {
            Page<FavoriteEntity> favorites = favoriteRepository.findByUserId(userId, pageable);
            return favorites.map(fav -> {
                ListingEntity listing = fav.getListing();
                ListingCartResponseDTO dto = new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        true
                );
                if (listing.getStatus() != null) dto.setStatus(listing.getStatus().name());
                return dto;
            });
        } catch (Exception e) {
            System.err.println("Get user favorite cart listings error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        WalletEntity wallet = walletRepository.findByUserId(userId);
        return wallet.getBalance();
    }

    @Override
    public Object getConfigValue(String key) {
        return null;
    }

    @Override
    public ListingFeePaymentResponseDTO payByBalance(Long userId, Long listingId, BigDecimal fee) {
        try {
            ListingEntity listing = listingRepository.findById(listingId).orElse(null);
            if (listing == null) {
                return new ListingFeePaymentResponseDTO(listingId, fee, false,
                        null, false, null,
                        "Listing not found");
            }
            if (!listing.getSeller().getId().equals(userId)) {
                return new ListingFeePaymentResponseDTO(listingId, fee, false,
                        listing.getStatus().name(), false,
                        null, "Listing does not belong to user");
            }
            if (listing.getStatus() != ListingStatus.PAY_WAITING) {
                // Idempotent: nếu đã ACTIVE thì coi như đã thanh toán
                if (listing.getStatus() == ListingStatus.ACTIVE) {
                    WalletEntity wallet = walletRepository.findByUserId(userId);
                    return new ListingFeePaymentResponseDTO(listingId, fee,
                            false, listing.getStatus().name(),
                            false, wallet.getBalance(),
                            "Already active");
                }
                return new ListingFeePaymentResponseDTO(listingId, fee, false,
                        listing.getStatus().name(), false,
                        null, "Listing not in PAY_WAITING state");
            }

            WalletEntity wallet = walletRepository.findByUserId(userId);
            if (wallet == null) {
                return new ListingFeePaymentResponseDTO(listingId, fee,
                        false, listing.getStatus().name(), false,
                        null, "Wallet not found");
            }

            if (wallet.getBalance().compareTo(fee) < 0) {
                return new ListingFeePaymentResponseDTO(listingId, fee,
                        false, listing.getStatus().name(),
                        true, wallet.getBalance(),
                        "Insufficient balance");
            }

            var tx = walletLedgerService.debitListingFee(userId, listingId, fee);
            if (tx == null) {
                return new ListingFeePaymentResponseDTO(listingId, fee,
                        false, listing.getStatus().name(),
                        false, wallet.getBalance(),
                        "Debit failed");
            }

            listing.setStatus(ListingStatus.ACTIVE);
            listingRepository.save(listing);
            WalletEntity updatedWallet = walletRepository.findByUserId(userId);
            return new ListingFeePaymentResponseDTO(listingId, fee,
                    true, listing.getStatus().name(),
                    false, updatedWallet.getBalance(),
                    "Listing activated");
        } catch (Exception e) {
            return new ListingFeePaymentResponseDTO(listingId, fee,
                    false, null,
                    false, null,
                    "Payment error: " + e.getMessage());
        }
    }

}