package project.swp.spring.sebt_platform.dto.response;

import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.dto.object.Seller;
import project.swp.spring.sebt_platform.model.enums.ListingType;

import java.math.BigDecimal;
import java.util.List;

public class ListingDetailResponseDTO {
    Long id;
    String title;
    String description;
    double price;
    ListingType listingType;
    String status;
    String createdAt;
    String updatedAt;
    Product product;
    Location location;
    Seller seller;
    String thumbnail;
    List<String> images;

    public ListingDetailResponseDTO() {
    }

    public ListingDetailResponseDTO(Long id,
                                    String title,
                                    String description,
                                    double price,
                                    ListingType listingType,
                                    String status, String createdAt,
                                    String updatedAt, Product product,
                                    Location location, Seller seller,
                                    String thumbnail, List<String> images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.listingType = listingType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.product = product;
        this.location = location;
        this.seller = seller;
        this.thumbnail = thumbnail;
        this.images = images;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Seller getSeller() {
        return seller;
    }


    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
