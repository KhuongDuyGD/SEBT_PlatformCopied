package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.AuthService;
import project.swp.spring.sebt_platform.service.MailService;
import project.swp.spring.sebt_platform.util.Utils;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private final Utils utils;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final MailService mailService;


    public AuthServiceImpl(Utils utils, UserRepository userRepository, MailService mailService) {
        this.utils = utils;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Override
    public UserEntity login(String email, String password) {
        try{
            UserEntity user = userRepository.findUserByEmail(email);
            if(user == null) return null;

            password = utils.encript(password, user.getSalt());

            if(user.getPassword().equals(password)){
                return user;
            } else {
                return null;
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean register(String password, String email, UserRole role) {
        try{
            // create salt and hash password
            String salt = utils.generateSalt();
            String hashedPassword = utils.encript(password, salt);
            UserEntity newUser = new UserEntity( email.substring(0,email.indexOf("@")),hashedPassword, email, salt);
            newUser.setRole(role);

            userRepository.save(newUser);

            return true;
        } catch(Exception e){
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public String getCurrentUserName() {
        return "";
    }

    @Override
    public String getCurrentUserEmail() {
        return "";
    }

    @Override
    public Long getCurrentUserId() {
        return 0L;
    }
}
