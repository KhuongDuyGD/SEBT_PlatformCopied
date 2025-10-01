package project.swp.spring.sebt_platform.dto.object;

public record Image(
    String url,
    String publicId
) {
    public String getUrl() {
        return url;
    }

    public String getPublicId() {
        return publicId;
    }
}
