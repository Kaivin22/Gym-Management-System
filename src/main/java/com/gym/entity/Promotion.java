package com.gym.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "promo_code", nullable = false, unique = true, length = 50)
    private String promoCode;
    
    @Column(name = "promo_name", length = 200, nullable = false) // SỬA: promotion_name -> promo_name
    private String promoName = "Khuyến mãi"; // SỬA: promotionName -> promoName
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "promotion_type", length = 50)
    private String promotionType;
    
    @Column(name = "discount_type", length = 50)
    private String discountType;
    
    @Column(name = "discount_percent")
    private Double discountPercent;
    
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;
    
    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "max_usage")
    private Integer maxUsage;
    
    @Column(name = "current_usage")
    private Integer currentUsage = 0;
    
    @Column(name = "applicable_for", length = 50)
    private String applicableFor = "Tất cả";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Promotion() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentUsage = 0;
        this.isActive = true;
        this.promoName = "Khuyến mãi"; // SỬA
        this.discountValue = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { 
        this.promoCode = promoCode != null ? promoCode.trim() : null;
    }
    
    public String getPromoName() { // SỬA
        return promoName != null ? promoName : "Khuyến mãi"; 
    }
    public void setPromoName(String promoName) { // SỬA
        this.promoName = promoName != null ? promoName.trim() : "Khuyến mãi"; 
    }
    
    // Giữ getter/setter cũ cho tương thích
    public String getPromotionName() {
        return getPromoName();
    }
    public void setPromotionName(String promotionName) {
        setPromoName(promotionName);
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPromotionType() { return promotionType; }
    public void setPromotionType(String promotionType) { this.promotionType = promotionType; }
    
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    
    public Double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public BigDecimal getDiscountValue() { 
        return discountValue != null ? discountValue : BigDecimal.ZERO; 
    }
    public void setDiscountValue(BigDecimal discountValue) { 
        this.discountValue = discountValue != null ? discountValue : BigDecimal.ZERO; 
    }
    
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }
    
    public Integer getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(Integer currentUsage) { this.currentUsage = currentUsage; }
    
    public String getApplicableFor() { return applicableFor; }
    public void setApplicableFor(String applicableFor) { this.applicableFor = applicableFor; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Đảm bảo các giá trị mặc định
        if (currentUsage == null) currentUsage = 0;
        if (isActive == null) isActive = true;
        if (applicableFor == null) applicableFor = "Tất cả";
        if (promoName == null || promoName.trim().isEmpty()) {
            promoName = "Khuyến mãi";
        }
        if (discountValue == null) {
            discountValue = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}