package project.swp.spring.sebt_platform.dto.request;

import java.util.List;

import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.dto.object.ProductImage;
import project.swp.spring.sebt_platform.model.enums.ListingType;

public record CreateListingFormDTO(
        String title,
        Product product,
        ListingType listingType,
        String mainImage,
        List<ProductImage> listingImages,
        String description,
        Double price,
        String category,
        Location location,
        Long sellerId  // Thêm sellerId để tránh session issue
){}
