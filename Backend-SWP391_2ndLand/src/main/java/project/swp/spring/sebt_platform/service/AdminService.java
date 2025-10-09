package project.swp.spring.sebt_platform.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.swp.spring.sebt_platform.dto.response.FiguresAdminDashboardResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostListingCartResponseDTO;
import project.swp.spring.sebt_platform.model.enums.ConfigDataType;

public interface AdminService {

    void performAdminTask();

    public Page<PostListingCartResponseDTO> getPostListingCart(Pageable pageable);

    public boolean approvePostListing(Long requestId);

    public boolean rejectPostListing(Long requestId, String reason);

    public boolean addConfig(String key, String value, ConfigDataType type, String description);

    public boolean updateConfig(String key, String value);

    public Object getConfigValue(String key);

    public FiguresAdminDashboardResponseDTO getFiguresForDashBoard();

}
