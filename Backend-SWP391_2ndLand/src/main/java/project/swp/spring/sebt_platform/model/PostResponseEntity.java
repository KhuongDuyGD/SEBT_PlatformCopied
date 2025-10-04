package project.swp.spring.sebt_platform.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import project.swp.spring.sebt_platform.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_responses")
public class PostResponseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "post request id", nullable = false ,referencedColumnName = "id")
    private PostRequestEntity postRequest;

    @Column(name = "payment qr code_url",length = 255, columnDefinition = "VARCHAR(MAX)")
    private String paymentQRCodeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment status", length = 30, columnDefinition = "NVARCHAR(30)")
    private PaymentStatus paymentStatus;

    @Column(name = "payment order id", length = 255, columnDefinition = "VARCHAR(MAX)")
    private String paymentOrderId;

    @Column(name = "payment amount", precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payed at", columnDefinition = "DATETIME2")
    private LocalDateTime payedAt;

    @CreationTimestamp
    @Column(name = "created at", columnDefinition = "DATETIME2")
    private LocalDateTime createAt;

    @Column(name = "updated at", columnDefinition = "DATETIME2")
    private LocalDateTime updateAt;

    // Getters and Setters

    public PostResponseEntity() {
    }

    public Long getId() {
        return id;
    }

    public PostRequestEntity getPostRequest() {
        return postRequest;
    }

    public void setPostRequest(PostRequestEntity postRequest) {
        this.postRequest = postRequest;
    }

    public String getPaymentQRCodeUrl() {
        return paymentQRCodeUrl;
    }

    public void setPaymentQRCodeUrl(String paymentQRCodeUrl) {
        this.paymentQRCodeUrl = paymentQRCodeUrl;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public LocalDateTime getPayedAt() {
        return payedAt;
    }

    public void setPayedAt(LocalDateTime payedAt) {
        this.payedAt = payedAt;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
