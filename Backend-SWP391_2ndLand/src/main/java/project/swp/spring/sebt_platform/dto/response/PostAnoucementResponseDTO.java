package project.swp.spring.sebt_platform.dto.response;

import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.model.enums.PaymentStatus;

import java.time.LocalDateTime;

public class PostAnoucementResponseDTO {
    private Long postResponseId;
    private Long listingId;
    private String title;
    private ApprovalStatus approvalStatus;
    private String thumbnailUrl;
    private String adminFeedback;
    private LocalDateTime reviewedAt;

    private String paymentQrCodeUrl;
    private PaymentStatus paymentStatus;
    private double Amount;
    private LocalDateTime paymentDueDate;
    private LocalDateTime paymentCompletedAt;

    public PostAnoucementResponseDTO(Long postResponseId,Long listingId, String title, ApprovalStatus approvalStatus, String thumbnailUrl, String adminFeedback, LocalDateTime reviewedAt, String paymentQrCodeUrl, PaymentStatus paymentStatus, double amount, LocalDateTime paymentDueDate, LocalDateTime paymentCompletedAt) {
        this.postResponseId = postResponseId;
        this.listingId = listingId;
        this.title = title;
        this.approvalStatus = approvalStatus;
        this.thumbnailUrl = thumbnailUrl;
        this.adminFeedback = adminFeedback;
        this.reviewedAt = reviewedAt;
        this.paymentQrCodeUrl = paymentQrCodeUrl;
        this.paymentStatus = paymentStatus;
        Amount = amount;
        this.paymentDueDate = paymentDueDate;
        this.paymentCompletedAt = paymentCompletedAt;
    }

    public PostAnoucementResponseDTO() {
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Long getPostResponseId() {
        return postResponseId;
    }

    public void setPostResponseId(Long postResponseId) {
        this.postResponseId = postResponseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getPaymentQrCodeUrl() {
        return paymentQrCodeUrl;
    }

    public void setPaymentQrCodeUrl(String paymentQrCodeUrl) {
        this.paymentQrCodeUrl = paymentQrCodeUrl;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public LocalDateTime getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(LocalDateTime paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    public LocalDateTime getPaymentCompletedAt() {
        return paymentCompletedAt;
    }

    public void setPaymentCompletedAt(LocalDateTime paymentCompletedAt) {
        this.paymentCompletedAt = paymentCompletedAt;
    }
}
