package project.swp.spring.sebt_platform.dto.response;

public class ListingCartResponseDTO {
    private Long listingId;
    private String title;
    private String thumbnailUrl;
    private double price;
    private int viewCount;
    private String sellerPhoneNumber;
    private boolean favorite;

    public ListingCartResponseDTO() {
    }

    public ListingCartResponseDTO(Long listingId, String title, String thumbnailUrl, Double price, int viewCount, String sellerPhoneNumber, boolean favorite) {
        this.listingId = listingId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.viewCount = viewCount;
        this.sellerPhoneNumber = sellerPhoneNumber;
        this.favorite = favorite;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getSellerPhoneNumber() {
        return sellerPhoneNumber;
    }

    public void setSellerPhoneNumber(String sellerPhoneNumber) {
        this.sellerPhoneNumber = sellerPhoneNumber;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
