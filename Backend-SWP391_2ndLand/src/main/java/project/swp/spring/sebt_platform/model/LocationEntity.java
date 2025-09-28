package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "location")
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private ListingEntity listing;

    @Column(name = "province", length = 20, columnDefinition = "NVARCHAR(20)")
    private String province;

    @Column(name = "district", length = 20, columnDefinition = "NVARCHAR(20)")
    private String district;

    @Column(name = "details", length = 255, columnDefinition = "NVARCHAR(255)")
    private String details;

    // Constructors
    public LocationEntity() {}

    public LocationEntity(String province, String district, String details) {
        this.province = province;
        this.district = district;
        this.details = details;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Utility method to get full address
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (this.details != null && !this.details.isEmpty()) {
            fullAddress.append(this.details);
        }
        if (district != null && !district.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(district);
        }
        if (province != null && !province.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(province);
        }
        return fullAddress.toString();
    }
}
