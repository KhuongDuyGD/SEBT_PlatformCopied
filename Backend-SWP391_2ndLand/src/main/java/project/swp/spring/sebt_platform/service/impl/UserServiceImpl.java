package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.dto.request.UpdateProfileFormDTO;
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
    public boolean updateProfile(UpdateProfileFormDTO updateProfileDTO, Long userId) {
        try {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.err.println("User not found with ID: " + userId);
                return false;
            }

            user = userRepository.findUserByUsername(user.getUsername());
            if (user != null) {
                System.err.println("username already exists: " + updateProfileDTO.username());
                return false;
            }

            // Update user fields from DTO - only update if values are provided
            if (updateProfileDTO.username() != null && !updateProfileDTO.username().trim().isEmpty()) {
                user.setUsername(updateProfileDTO.username());
            }

            if (updateProfileDTO.phoneNumber() != null && !updateProfileDTO.phoneNumber().trim().isEmpty()) {
                user.setPhoneNumber(updateProfileDTO.phoneNumber());
            }

            if (updateProfileDTO.avatarUrl() != null && !updateProfileDTO.avatarUrl().trim().isEmpty()) {
                user.setAvatar(updateProfileDTO.avatarUrl());
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
