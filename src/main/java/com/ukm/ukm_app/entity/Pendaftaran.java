package com.ukm.ukm_app.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "pendaftaran")
public class Pendaftaran {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "mahasiswa_id", nullable = false)
    private User mahasiswa;
    
    @ManyToOne
    @JoinColumn(name = "ukm_id", nullable = false)
    private Ukm ukm;
    
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, DITERIMA, DITOLAK
    
    @Column(name = "tanggal_daftar", nullable = false)
    private LocalDateTime tanggalDaftar = LocalDateTime.now();
    
    // ========== CONSTRUCTOR ==========
    public Pendaftaran() {}
    
    public Pendaftaran(User mahasiswa, Ukm ukm) {
        this.mahasiswa = mahasiswa;
        this.ukm = ukm;
    }
    
    // ========== GETTER SETTER ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getMahasiswa() { return mahasiswa; }
    public void setMahasiswa(User mahasiswa) { this.mahasiswa = mahasiswa; }
    
    public Ukm getUkm() { return ukm; }
    public void setUkm(Ukm ukm) { this.ukm = ukm; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getTanggalDaftar() { return tanggalDaftar; }
    public void setTanggalDaftar(LocalDateTime tanggalDaftar) { 
        this.tanggalDaftar = tanggalDaftar; 
    }
}