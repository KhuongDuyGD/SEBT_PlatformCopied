package project.swp.spring.sebt_platform.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.model.enums.ListingType;

public class CreateListingFormDTO {
    private String title;
    private Product product;
    private ListingType listingType;
    private String description;
    private Double price;
    private String category;
    private Location location;
    private List<MultipartFile> images; // Danh sách file ảnh, ảnh đầu tiên sẽ là thumbnail

    // Constructors
    public CreateListingFormDTO() {}

    public CreateListingFormDTO(String title, Product product, ListingType listingType,
                                String description, Double price, String category,
                                Location location, List<MultipartFile> images) {
        this.title = title;
        this.product = product;
        this.listingType = listingType;
        this.description = description;
        this.price = price;
        this.category = category;
        this.location = location;
        this.images = images;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public ListingType getListingType() { return listingType; }
    public void setListingType(ListingType listingType) { this.listingType = listingType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}
