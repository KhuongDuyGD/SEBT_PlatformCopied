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

    @Column(name = "stress_name", length = 20, columnDefinition = "NVARCHAR(20)")
    private String stressName;

    @Column(name = "extra", length = 50, columnDefinition = "NVARCHAR(50)")
    private String extra;

    // Constructors
    public LocationEntity() {}

    public LocationEntity(String province, String district, String stressName, String extra) {
        this.province = province;
        this.district = district;
        this.stressName = stressName;
        this.extra = extra;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStressName() {
        return stressName;
    }

    public void setStressName(String stressName) {
        this.stressName = stressName;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    // Utility method to get full address
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (stressName != null && !stressName.isEmpty()) {
            fullAddress.append(stressName);
        }
        if (district != null && !district.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(district);
        }
        if (province != null && !province.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(province);
        }
        if (extra != null && !extra.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" - ");
            fullAddress.append(extra);
        }
        return fullAddress.toString();
    }
}
