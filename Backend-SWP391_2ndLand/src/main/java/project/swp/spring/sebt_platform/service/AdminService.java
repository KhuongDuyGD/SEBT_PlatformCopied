package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.swp.spring.sebt_platform.dto.response.PostListingCartResponseDTO;

public interface AdminService {

    void performAdminTask();

    public Page<PostListingCartResponseDTO> getPostListingCart(Pageable pageable);

    public boolean approvePostListing(Long requestId);

    public boolean rejectPostListing(Long requestId, String reason);

    public boolean addPostResponse(Long requestId);
}
