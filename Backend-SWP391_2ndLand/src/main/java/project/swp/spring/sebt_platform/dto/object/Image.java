package project.swp.spring.sebt_platform.dto.object;

public class Image{

    private String url;
    private String publicId;

    public Image() {
    }
    public Image(String url, String publicId) {
        this.url = url;
        this.publicId = publicId;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}
