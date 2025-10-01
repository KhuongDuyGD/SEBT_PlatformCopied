package project.swp.spring.sebt_platform.dto.request;

import java.util.List;

import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.model.enums.ListingType;

public record CreateListingFormDTO(
        String title,
        Product product,
        ListingType listingType,
        String description,
        Double price,
        String category,
        Location location,
        // Cloudinary image URLs (đã được upload từ frontend)
        String mainImageUrl,
        List<String> imageUrls
){}
