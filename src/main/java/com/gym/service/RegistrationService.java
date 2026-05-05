// ============= RegistrationService.java =============
package com.gym.service;

import com.gym.dao.RegistrationDAO;
import com.gym.entity.Member;
import com.gym.entity.Package;
import com.gym.entity.Registration;
import com.gym.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RegistrationService {
    private final RegistrationDAO registrationDAO;
    private final PromotionService promotionService = new PromotionService();
    
    public RegistrationService() {
        this.registrationDAO = new RegistrationDAO();
    }
    
    public void createRegistration(Member member, Package pkg, LocalDate startDate, User user) {
        Registration registration = new Registration();
        registration.setRegistrationCode(registrationDAO.generateRegistrationCode());
        registration.setMember(member);
        registration.setPkg(pkg);
        registration.setStartDate(startDate);
        
        // Tính ngày kết thúc
        LocalDate endDate = startDate.plusDays(pkg.getDurationDays());
        registration.setEndDate(endDate);
        registration.setOriginalEndDate(endDate);
        
        // Tính toán số tiền
        registration.setTotalAmount(pkg.getPrice());
        
        BigDecimal discountAmount = pkg.getPrice()
            .multiply(pkg.getDiscountPercent())
            .divide(new BigDecimal(100));
        registration.setDiscountAmount(discountAmount);
        
        BigDecimal finalAmount = pkg.getPrice().subtract(discountAmount);
        registration.setFinalAmount(finalAmount);
        
        registration.setStatus(Registration.RegistrationStatus.ACTIVE);
        registration.setPaymentStatus(Registration.PaymentStatus.PENDING);
        registration.setRegisteredBy(user);
        
        registrationDAO.save(registration);
    }
    
    public void updateRegistration(Registration registration) {
        registrationDAO.update(registration);
    }
    
    public void deleteRegistration(Long id) {
        registrationDAO.delete(id);
    }
    
    public Optional<Registration> getRegistrationById(Long id) {
        return registrationDAO.findById(id);
    }
    
    public Optional<Registration> getRegistrationByCode(String code) {
        return registrationDAO.findByRegistrationCode(code);
    }
    
    public List<Registration> getAllRegistrations() {
        return registrationDAO.findAll();
    }
    
    public List<Registration> getRegistrationsByMember(Member member) {
        return registrationDAO.findByMember(member);
    }
    
    public Optional<Registration> getActiveRegistration(Long memberId) {
        return registrationDAO.findActiveRegistrationByMember(memberId);
    }
    
    public List<Registration> getExpiringRegistrations(int days) {
        return registrationDAO.findExpiringRegistrations(days);
    }
    
    public void updatePaymentStatus(Registration registration, BigDecimal paidAmount) {
        BigDecimal totalPaid = registration.getPaidAmount().add(paidAmount);
        registration.setPaidAmount(totalPaid);
        
        if (totalPaid.compareTo(registration.getFinalAmount()) >= 0) {
            registration.setPaymentStatus(Registration.PaymentStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            registration.setPaymentStatus(Registration.PaymentStatus.PARTIAL);
        }
        
        registrationDAO.update(registration);
    }
    
    public void freezeRegistration(Registration registration, int freezeDays) {
        if (freezeDays + registration.getFreezeDaysUsed() > registration.getPkg().getMaxFreezeDays()) {
            throw new IllegalArgumentException("Vượt quá số ngày đóng băng cho phép!");
        }
        
        registration.setFreezeDaysUsed(registration.getFreezeDaysUsed() + freezeDays);
        registration.setEndDate(registration.getEndDate().plusDays(freezeDays));
        registration.setStatus(Registration.RegistrationStatus.FROZEN);
        
        registrationDAO.update(registration);
    }
    
    public void unfreezeRegistration(Registration registration) {
        registration.setStatus(Registration.RegistrationStatus.ACTIVE);
        registrationDAO.update(registration);
    }
}