package project.swp.spring.sebt_platform.model.dto.request;

public class UpdateProfileFormDTO {
    private String username;
    private String phone;
    private String avatar;

    // Constructors
    public UpdateProfileFormDTO() {}

    public UpdateProfileFormDTO(String username, String phone, String avatar) {
        this.username = username;
        this.phone = phone;
        this.avatar = avatar;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar){
        this.avatar = avatar;
    }
}
