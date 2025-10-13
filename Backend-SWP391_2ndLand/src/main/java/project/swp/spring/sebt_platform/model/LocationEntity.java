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

    @Column(name = "province", length = 30, columnDefinition = "NVARCHAR(30)")
    private String province;

    @Column(name = "district", length = 50, columnDefinition = "NVARCHAR(50)")
    private String district;

    @Column(name = "details", columnDefinition = "NVARCHAR(MAX)")
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
            if (!fullAddress.isEmpty()) fullAddress.append(", ");
            fullAddress.append(district);
        }
        if (province != null && !province.isEmpty()) {
            if (!fullAddress.isEmpty()) fullAddress.append(", ");
            fullAddress.append(province);
        }
        return fullAddress.toString();
    }
}
