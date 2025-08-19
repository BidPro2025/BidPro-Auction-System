package com.bidpro.service;

import com.bidpro.dao.AdminDao;
import com.bidpro.dto.AdminDTO;
import com.bidpro.dto.LoginDTO;
import com.bidpro.entities.Admin;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public AdminDTO createAdmin(AdminDTO adminDTO, String createdBy) {
        if (adminDao.existsByEmail(adminDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setFirstName(adminDTO.getFirstName());
        admin.setLastName(adminDTO.getLastName());
        admin.setEmail(adminDTO.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(adminDTO.getPassword()));
        admin.setCreatedBy(createdBy);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        admin = adminDao.save(admin);
        return mapToAdminDTO(admin);
    }

    public AdminDTO getAdminById(String adminID) {
        Admin admin = adminDao.findById(adminID)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));
        return mapToAdminDTO(admin);
    }

    public AdminDTO getAdminByEmail(String email) {
        Admin admin = adminDao.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));
        return mapToAdminDTO(admin);
    }

    @Transactional
    public AdminDTO updateAdmin(String adminID, AdminDTO adminDTO, String updatedBy) {
        Admin admin = adminDao.findById(adminID)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        adminDao.findByEmail(adminDTO.getEmail())
        .ifPresent(existing -> {
            if (!existing.getAdminId().equals(adminID)) {  // Changed to getAdminId()
                throw new IllegalArgumentException("Email already in use by another admin");
            }
        });

        admin.setFirstName(adminDTO.getFirstName());
        admin.setLastName(adminDTO.getLastName());
        admin.setEmail(adminDTO.getEmail());
        
        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
            admin.setPasswordHash(passwordEncoder.encode(adminDTO.getPassword()));
        }
        
        admin.setUpdatedBy(updatedBy);
        admin.setUpdatedAt(LocalDateTime.now());

        admin = adminDao.save(admin);
        return mapToAdminDTO(admin);
    }

    public AdminDTO login(LoginDTO loginDTO) {
        Admin admin = adminDao.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPasswordHash())) {
            throw new EntityNotFoundException("Invalid credentials");
        }

        return mapToAdminDTO(admin);
    }

    private AdminDTO mapToAdminDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setAdminId(admin.getAdminId());
        dto.setFirstName(admin.getFirstName());
        dto.setLastName(admin.getLastName());
        dto.setEmail(admin.getEmail());
        return dto;
    }
}