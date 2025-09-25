package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents",
    indexes = {
        @Index(name = "idx_documents_post_request_id", columnList = "post_request_id")
    }
)
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_request_id", nullable = false)
    private PostRequestEntity postRequest;

    @Column(length = 255, nullable = false)
    private String fileName;

    @Column(length = 50, nullable = false)
    private String fileType;

    @Column
    private Long fileSize;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String filePath;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // Note: Consider adding checksum (file_hash) for deduplication.

    // Constructors
    public DocumentEntity() {}

    public DocumentEntity(PostRequestEntity postRequest, String fileName, String fileType, String filePath) {
        this.postRequest = postRequest;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PostRequestEntity getPostRequest() {
        return postRequest;
    }

    public void setPostRequest(PostRequestEntity postRequest) {
        this.postRequest = postRequest;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
