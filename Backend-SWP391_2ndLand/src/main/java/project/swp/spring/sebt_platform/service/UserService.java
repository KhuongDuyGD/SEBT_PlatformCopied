package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.dto.request.UpdateProfileDTO;
import project.swp.spring.sebt_platform.model.UserEntity;

import java.security.NoSuchAlgorithmException;


public interface UserService {
    public UserEntity findUserById(Long id);
    public UserEntity findUserByEmail(String email);
    public UserEntity findUserByUsernameCaseSensitive(String username);

    // Add new method for updating profile with DTO and userId
    public boolean updateProfile(UpdateProfileDTO updateProfileDTO, Long userId);
}
