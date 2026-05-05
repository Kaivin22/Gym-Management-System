package com.gym.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
public class Package {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "package_code", unique = true, nullable = false, length = 20)
    private String packageCode;
    
    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;
    
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
   
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
    
    @Column(name = "max_freeze_days")
    private Integer maxFreezeDays = 0;
    
    @Column(columnDefinition = "TEXT")
    private String features;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "pkg", fetch = FetchType.LAZY)
    private List<Registration> registrations = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Tính giá cuối cùng sau khi áp dụng khuyến mãi
     */
    public BigDecimal getFinalPrice() {
        if (promotion == null || !promotion.getIsActive()) {
            return price;
        }
        
        // Áp dụng khuyến mãi
        if (promotion.getDiscountPercent() != null) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(promotion.getDiscountPercent()))
                .divide(new BigDecimal(100));
            return price.subtract(discount);
        }
        
        if (promotion.getDiscountAmount() != null) {
            return price.subtract(promotion.getDiscountAmount());
        }
        
        return price;
    }
    
    /**
     * Lấy phần trăm giảm giá từ promotion
     */
    public BigDecimal getDiscountPercent() {
        if (promotion == null || !promotion.getIsActive()) {
            return BigDecimal.ZERO;
        }
        
        if (promotion.getDiscountPercent() != null) {
            return BigDecimal.valueOf(promotion.getDiscountPercent());
        }
        
        // Tính % từ discount amount
        if (promotion.getDiscountAmount() != null && price.compareTo(BigDecimal.ZERO) > 0) {
            return promotion.getDiscountAmount()
                .divide(price, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        }
        
        return BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }
    
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    
    public Integer getMaxFreezeDays() { return maxFreezeDays; }
    public void setMaxFreezeDays(Integer maxFreezeDays) { this.maxFreezeDays = maxFreezeDays; }
    
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public List<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(List<Registration> registrations) { this.registrations = registrations; }
}