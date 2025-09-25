package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileDTO;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.UserService;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity findUserById(Long id) {
        try {
            return userRepository.findById(id).orElse(null);
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

    @Override
    public boolean updateProfile(UpdateProfileDTO updateProfileDTO, Long userId) {
        try {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.err.println("User not found with ID: " + userId);
                return false;
            }

            // Update user fields from DTO - only update if values are provided
            if (updateProfileDTO.getUsername() != null && !updateProfileDTO.getUsername().trim().isEmpty()) {
                user.setUsername(updateProfileDTO.getUsername());
            }

            if (updateProfileDTO.getPhone() != null && !updateProfileDTO.getPhone().trim().isEmpty()) {
                user.setPhoneNumber(updateProfileDTO.getPhone());
            }

            if (updateProfileDTO.getAvatar() != null && !updateProfileDTO.getAvatar().trim().isEmpty()) {
                user.setAvatar(updateProfileDTO.getAvatar());
            }

            if (updateProfileDTO.getAddress() != null && !updateProfileDTO.getAddress().trim().isEmpty()) {
                user.setAddress(updateProfileDTO.getAddress());
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
}
