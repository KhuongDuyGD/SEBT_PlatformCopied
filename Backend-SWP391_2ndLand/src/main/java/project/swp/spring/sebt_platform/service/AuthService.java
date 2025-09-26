package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.model.UserEntity;

public interface AuthService {
    public UserEntity login(String email, String password);
    public boolean register(String password, String email);
    public boolean logout();
    public String getCurrentUserName();
    public String getCurrentUserEmail();
    Long getCurrentUserId();
}
