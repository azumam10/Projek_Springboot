package com.ukm.ukm_app.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ukm")
public class Ukm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nama;
    
    @Column(columnDefinition = "TEXT")
    private String deskripsi;
    
    @OneToOne
    @JoinColumn(name = "ketua_id")
    private User ketua;
    
    // ========== GETTER SETTER ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public User getKetua() { return ketua; }
    public void setKetua(User ketua) { this.ketua = ketua; }
}