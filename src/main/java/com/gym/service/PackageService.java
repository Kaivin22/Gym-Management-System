// ============= PackageService.java =============
package com.gym.service;

import com.gym.dao.PackageDAO;
import com.gym.entity.Package;
import java.util.List;
import java.util.Optional;

public class PackageService {
    private final PackageDAO packageDAO;
    
    public PackageService() {
        this.packageDAO = new PackageDAO();
    }
    
    public void createPackage(Package pkg) {
        if (pkg.getPackageCode() == null || pkg.getPackageCode().isEmpty()) {
            pkg.setPackageCode(packageDAO.generatePackageCode());
        }
        packageDAO.save(pkg);
    }
    
    public void updatePackage(Package pkg) {
        packageDAO.update(pkg);
    }
    
    public void deletePackage(Long id) {
        packageDAO.delete(id);
    }
    
    public Optional<Package> getPackageById(Long id) {
        return packageDAO.findById(id);
    }
    
    public Optional<Package> getPackageByCode(String code) {
        return packageDAO.findByPackageCode(code);
    }
    
    public List<Package> getAllPackages() {
        return packageDAO.findAll();
    }
    
    public List<Package> getActivePackages() {
        return packageDAO.findActivePackages();
    }
}