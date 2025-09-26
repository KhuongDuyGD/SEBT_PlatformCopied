package project.swp.spring.sebt_platform.model.dto.request;

import project.swp.spring.sebt_platform.model.ListingImageEntity;
import project.swp.spring.sebt_platform.model.LocationEntity;
import project.swp.spring.sebt_platform.model.ProductEntity;
import project.swp.spring.sebt_platform.model.enums.ListingType;

import java.util.List;

public record CreateListingFormDTO(
        String title,
        Long sell_Id,
        ProductEntity product,
        ListingType listingType,
        String mainImage,
        List<ListingImageEntity> listingImages,
        String description,
        Double price,
        String category,
        LocationEntity location
){}
