package project.swp.spring.sebt_platform.dto.response;

public class PostListingCartResponseDTO {
    private Long requestId;
    private Long listingId;
    private String thumbnailUrl;
    private double price;
    private String title;
    private String status;

    public PostListingCartResponseDTO() {
    }

    public PostListingCartResponseDTO(Long requestId, Long listingId, String thumbnailUrl, double price, String title, String status) {
        this.requestId = requestId;
        this.listingId = listingId;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.title = title;
        this.status = status;
    }

    public Long getRequestId() {
        return requestId;
    }
    
    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
