package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.dto.response.PostListingCartResponseDTO;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.PostResponseEntity;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.model.enums.PaymentStatus;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.PostResponseRepository;
import project.swp.spring.sebt_platform.service.AdminService;

import java.time.LocalDateTime;

@Service
public class AdminServiceImpl implements AdminService {

    private final PostRequestRepository postRequestRepository;
    private final PostResponseRepository postResponseRepository;

    @Autowired
    public AdminServiceImpl(PostRequestRepository postRequestRepository, PostResponseRepository postResponseRepository) {
        this.postRequestRepository = postRequestRepository;
        this.postResponseRepository = postResponseRepository;
    }

    @Override
    public void performAdminTask() {
    }

    @Override
    public Page<PostListingCartResponseDTO> getPostListingCart(Pageable pageable) {
        try{
            Page<PostRequestEntity> res = postRequestRepository.findAllByStatusIs(ApprovalStatus.PENDING, pageable);

            Page<PostListingCartResponseDTO> response = res.map(postRequest -> {
                PostListingCartResponseDTO dto = new PostListingCartResponseDTO();
                dto.setRequestId(postRequest.getId());
                dto.setListingId(postRequest.getListing().getId());
                dto.setThumbnailUrl(postRequest.getListing().getThumbnailImage());
                dto.setPrice(postRequest.getListing().getPrice().doubleValue());
                dto.setTitle(postRequest.getListing().getTitle());
                dto.setStatus(postRequest.getStatus().name());
                return dto;
            });

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean approvePostListing(Long requestId) {
        try {
            postRequestRepository.approvePostRequest(requestId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rejectPostListing(Long requestId, String reason) {
        try {
            postRequestRepository.rejectPostRequest(requestId, reason);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addPostResponse(Long requestId) {
        try {
            PostRequestEntity postRequest = postRequestRepository.findById(requestId).orElse(null);
            if (postRequest == null) {
                return false;
            }

            // Tạo PostResponseEntity mới
            PostResponseEntity postResponse = new PostResponseEntity();
            postResponse.setPostRequest(postRequest);
            postResponse.setPaymentStatus(PaymentStatus.PENDING);
            postResponse.setUpdateAt(LocalDateTime.now());
            postResponseRepository.save(postResponse);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
