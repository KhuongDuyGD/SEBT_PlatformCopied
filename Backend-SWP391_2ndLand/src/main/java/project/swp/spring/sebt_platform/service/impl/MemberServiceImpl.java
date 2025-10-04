package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.model.PostResponseEntity;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.repository.PostResponseRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.MemberService;

import java.time.LocalDateTime;

@Service
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;

    private final PostResponseRepository postResponseRepository;

    @Autowired
    public MemberServiceImpl(UserRepository userRepository, PostResponseRepository postResponseRepository) {
        this.userRepository = userRepository;
        this.postResponseRepository = postResponseRepository;
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
    public UserEntity findUserByUsernameCaseSensitive(String username) {
        try {
            return userRepository.findUserByUsername(username);
        } catch (Exception e) {
            System.err.println("Find user by username error: " + e.getMessage());
            return null;
        }
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
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Page<PostAnoucementResponseDTO> getPostAnoucementResponse(Long userId, Pageable pageable) {
        try {
            Page<PostResponseEntity> responses = postResponseRepository.findAllBySellerId(userId, pageable);

            Page<PostAnoucementResponseDTO> responseDTOs = responses.map(response -> {
                PostAnoucementResponseDTO dto = new PostAnoucementResponseDTO();
                dto.setPostResponseId(response.getPostRequest().getId());
                dto.setListingId(response.getPostRequest().getListing().getId());
                dto.setThumbnailUrl(response.getPostRequest().getListing().getThumbnailImage());
                dto.setTitle(response.getPostRequest().getListing().getTitle());
                dto.setApprovalStatus(response.getPostRequest().getStatus());
                dto.setAdminFeedback(response.getPostRequest().getAdminNotes());
                dto.setReviewedAt(response.getPostRequest().getReviewedAt());

                dto.setPaymentQrCodeUrl(response.getPaymentQRCodeUrl());
                dto.setPaymentStatus(response.getPaymentStatus());
                dto.setAmount(response.getPostRequest().getListing().getPrice().doubleValue());
                return dto;
            });
            return responseDTOs;
        } catch (Exception e) {
            System.err.println("Get post announcement response error: " + e.getMessage());
        }
        return null;
    }
}