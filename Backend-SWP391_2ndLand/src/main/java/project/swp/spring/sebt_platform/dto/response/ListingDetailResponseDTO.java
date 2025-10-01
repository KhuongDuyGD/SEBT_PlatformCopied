package project.swp.spring.sebt_platform.dto.response;

import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.dto.object.Seller;

import java.util.List;

public record ListingDetailResponseDTO (
    Long id,
    String title,
    String description,
    Double price,
    String category,
    String listingType,
    String status,
    String createdAt,
    String updatedAt,
    Product product,
    Location location,
    Seller seller,
    Image thumbnail,
    List<Image> images
){}
