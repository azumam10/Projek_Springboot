package com.ukm.ukm_app.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "kegiatan")
public class Kegiatan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String judul;
    
    @Column(columnDefinition = "TEXT")
    private String deskripsi;
    
    @Column(nullable = false)
    private LocalDateTime tanggal;
    
    private String lokasi;
    
    @ManyToOne
    @JoinColumn(name = "ukm_id", nullable = false)
    private Ukm ukm;
    
    // ========== GETTER SETTER ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public LocalDateTime getTanggal() { return tanggal; }
    public void setTanggal(LocalDateTime tanggal) { this.tanggal = tanggal; }
    
    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    
    public Ukm getUkm() { return ukm; }
    public void setUkm(Ukm ukm) { this.ukm = ukm; }
}