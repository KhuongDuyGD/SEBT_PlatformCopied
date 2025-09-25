package project.swp.spring.sebt_platform.dto.request;

public class UpdateProfileDTO {
    private String username;
    private String phone;
    private String avatar;
    private String address;

    // Constructors
    public UpdateProfileDTO() {}

    public UpdateProfileDTO(String username, String phone, String avatar,String address) {
        this.username = username;
        this.phone = phone;
        this.avatar = avatar;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setFullName(String username) {
        this.username = username;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
