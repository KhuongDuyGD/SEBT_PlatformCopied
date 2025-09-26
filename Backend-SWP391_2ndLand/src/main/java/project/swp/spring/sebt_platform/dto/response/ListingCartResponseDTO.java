package project.swp.spring.sebt_platform.model.dto.response;

public record ListingCartResponseDTO(
        Long listingId,
        String title,
        String image,
        Double price,
        int viewCount,
        String sellerPhoneNumber,
        boolean favorite
){}
