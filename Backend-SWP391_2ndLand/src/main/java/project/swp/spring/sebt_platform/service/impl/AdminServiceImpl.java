package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.dto.response.FiguresAdminDashboardResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserProfileResponseDTO;
import project.swp.spring.sebt_platform.dto.response.UserSessionResponseDTO;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.SystemConfigEntity;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.*;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.SystemConfigRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.AdminService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminServiceImpl implements AdminService {

    private final PostRequestRepository postRequestRepository;
    private final ListingRepository listingRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;

    @Autowired
    public AdminServiceImpl(PostRequestRepository postRequestRepository,
                            ListingRepository listingRepository,
                            SystemConfigRepository systemConfigRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.postRequestRepository = postRequestRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void performAdminTask() {
    }

    @Override
    public Page<PostListingCartResponseDTO> getPostListingCart(Pageable pageable) {
        try{
            Page<PostRequestEntity> res = postRequestRepository.findAllByStatusIs(ApprovalStatus.PENDING, pageable);

            return res.map(postRequest -> {
                PostListingCartResponseDTO dto = new PostListingCartResponseDTO();
                dto.setRequestId(postRequest.getId());
                dto.setListingId(postRequest.getListing().getId());
                dto.setThumbnailUrl(postRequest.getListing().getThumbnailImage());
                dto.setPrice(postRequest.getListing().getPrice().doubleValue());
                dto.setTitle(postRequest.getListing().getTitle());
                dto.setStatus(postRequest.getStatus().name());
                return dto;
            });
        } catch (Exception e) {
            System.err.println("Exception in performAdminTask: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean approvePostListing(Long requestId) {
        try {
            postRequestRepository.approvePostRequest(requestId);
            ListingEntity listing = Objects.requireNonNull(postRequestRepository.findById(requestId).orElse(null)).getListing();
            listing.setStatus(ListingStatus.PAY_WAITING);
            listingRepository.save(listing);
            return true;
        } catch (Exception e) {
            System.err.println("Exception in approvePostListing: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean rejectPostListing(Long requestId, String reason) {
        try {
            postRequestRepository.rejectPostRequest(requestId, reason);
            ListingEntity listing = Objects.requireNonNull(postRequestRepository.findById(requestId).orElse(null)).getListing();
            listing.setStatus(ListingStatus.REMOVED);
            listingRepository.save(listing);
            return true;
        } catch (Exception e) {
            System.err.println("Exception in rejectPostListing: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean modifyPostListing(Long requestId, String reason) {
        try {
            postRequestRepository.requireChangeRequest(requestId, reason);
            ListingEntity listing = Objects.requireNonNull(postRequestRepository.findById(requestId).orElse(null)).getListing();
            listing.setStatus(ListingStatus.DRAFT);
            listingRepository.save(listing);
            return true;
        } catch (Exception e) {
            System.err.println("Exception in rejectPostListing: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addConfig(String key, String value, ConfigDataType type, String description) {
        try{
            if(systemConfigRepository.findByConfigKey(key) != null){
                return false;
            }

            SystemConfigEntity systemConfigEntity = new SystemConfigEntity();
            systemConfigEntity.setConfigKey(key);
            systemConfigEntity.setConfigValue(value);
            systemConfigEntity.setDataType(type);
            systemConfigEntity.setDescription(description);

            systemConfigRepository.save(systemConfigEntity);

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateConfig(String key, String value) {
        try{
            SystemConfigEntity systemConfigEntity = systemConfigRepository.findByConfigKey(key);

            systemConfigEntity.setConfigValue(value);

            systemConfigRepository.save(systemConfigEntity);

            return true;
        } catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getConfigValue(String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            SystemConfigEntity systemConfigEntity = systemConfigRepository.findByConfigKey(key);
            if (systemConfigEntity == null) {
                result.put("error", "Config not found");
                return result;
            }

            ConfigDataType configDataType = systemConfigEntity.getDataType();
            String value = systemConfigEntity.getConfigValue();

            switch (configDataType) {
                case STRING:
                    result.put("type", "STRING");
                    result.put("value", value);
                    break;
                case NUMBER:
                    result.put("type", "NUMBER");
                    result.put("value", Double.parseDouble(value));
                    break;
                case BOOLEAN:
                    result.put("type", "BOOLEAN");
                    result.put("value", Boolean.parseBoolean(value));
                    break;
                default:
                    result.put("type", "UNKNOWN");
                    result.put("value", value);
            }
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public Page<UserProfileResponseDTO> getAllMembers(Pageable pageable) {
        Page<UserEntity> users = userRepository.findAllMember(pageable);
        return users.map(user -> new UserProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAvatar(),
                user.getCreatedAt()
        ));
    }

    @Override
    public Page<UserProfileResponseDTO> getAllMembersByStatus(UserStatus status, Pageable pageable) {
            Page<UserEntity> users = userRepository.findMemberByStatus(status, pageable);
            if(users == null) return null;

            return users.map(user -> new UserProfileResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getAvatar(),
                        user.getCreatedAt()
            ));
    }

    @Override
    public Page<UserProfileResponseDTO> searchMembersByKeyword(String keyword, Pageable pageable) {
            Page<UserEntity> users = userRepository.finMemberByUsernameContainingOrEmail(keyword, pageable);
            if(users == null) return null;

            return users.map(user -> new UserProfileResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getAvatar(),
                        user.getCreatedAt()
            ));
    }

    @Override
    public FiguresAdminDashboardResponseDTO getFiguresForDashBoard() {

        return null;
    }

}
