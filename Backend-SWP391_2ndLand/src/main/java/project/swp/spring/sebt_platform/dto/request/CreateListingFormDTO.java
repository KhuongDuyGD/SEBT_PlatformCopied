package project.swp.spring.sebt_platform.dto.request;

import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.dto.object.ProductImage;
import project.swp.spring.sebt_platform.model.enums.ListingType;

import java.util.List;

public record CreateListingFormDTO(
        String title,
        Product product,
        ListingType listingType,
        String mainImage,
        List<ProductImage> listingImages,
        String description,
        Double price,
        String category,
        Location location
){}
