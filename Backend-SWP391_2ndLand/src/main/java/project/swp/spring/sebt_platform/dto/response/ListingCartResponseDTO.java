package project.swp.spring.sebt_platform.dto.response;

public record ListingCartResponseDTO(
        Long listingId,
        String title,
        String thumbnailUrl,
        Double price,
        int viewCount,
        String sellerPhoneNumber,
        boolean favorite
){
    @Override
    public Long listingId() {
        return listingId;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String thumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public Double price() {
        return price;
    }

    @Override
    public int viewCount() {
        return viewCount;
    }

    @Override
    public String sellerPhoneNumber() {
        return sellerPhoneNumber;
    }

    @Override
    public boolean favorite() {
        return favorite;
    }
}
