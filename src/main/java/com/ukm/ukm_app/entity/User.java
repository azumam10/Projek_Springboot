package com.ukm.ukm_app.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nim;
    
    @Column(nullable = false)
    private String nama;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role; // ADMIN_KAMPUS, ADMIN_UKM, MAHASISWA
    
    @ManyToOne
    @JoinColumn(name = "ukm_id")
    private Ukm ukm;
    
    // ========== GETTER SETTER ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Ukm getUkm() { return ukm; }
    public void setUkm(Ukm ukm) { this.ukm = ukm; }
}