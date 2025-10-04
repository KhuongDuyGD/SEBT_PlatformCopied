package project.swp.spring.sebt_platform.dto.object;

public class Location {
    private String province;
    private String district;
    private String details;

    // Default constructor
    public Location() {}

    // Constructor with parameters
    public Location(String province, String district, String details) {
        this.province = province;
        this.district = district;
        this.details = details;
    }

    // Getters and Setters
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
