package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
import project.swp.spring.sebt_platform.model.UserEntity;


public interface UserService {
    public UserEntity findUserById(Long id);
    public UserEntity findUserByEmail(String email);
    public UserEntity findUserByUsernameCaseSensitive(String username);

    // Add new method for updating profile with DTO and userId
    public boolean updateProfile(UpdateProfileFormDTO updateProfileDTO, Long userId);
}
