package com.ukm.ukm_app.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ukm.ukm_app.entity.Kegiatan;
import com.ukm.ukm_app.entity.Pendaftaran;
import com.ukm.ukm_app.entity.Ukm;
import com.ukm.ukm_app.entity.User;
import com.ukm.ukm_app.repository.KegiatanRepository;
import com.ukm.ukm_app.repository.PendaftaranRepository;
import com.ukm.ukm_app.repository.UkmRepository;
import com.ukm.ukm_app.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mahasiswa")
public class MahasiswaController {
    
    @Autowired
    private UkmRepository ukmRepository;
    
    @Autowired
    private KegiatanRepository kegiatanRepository;
    
    @Autowired
    private PendaftaranRepository pendaftaranRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // MIDDLEWARE CEK LOGIN
    private boolean checkAuth(HttpSession session, String requiredRole) {
        User user = (User) session.getAttribute("user");
        return user != null && requiredRole.equals(user.getRole());
    }
    
    // DASHBOARD MAHASISWA
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        
        // Data untuk dashboard
        List<Ukm> semuaUkm = ukmRepository.findAll();
        List<Kegiatan> kegiatanMingguan = kegiatanRepository.findByOrderByTanggalAsc();
        List<Pendaftaran> pendaftaranSaya = pendaftaranRepository.findByMahasiswa(user);
        
        model.addAttribute("user", user);
        model.addAttribute("semuaUkm", semuaUkm);
        model.addAttribute("kegiatan", kegiatanMingguan);
        model.addAttribute("pendaftaranSaya", pendaftaranSaya);
        
        return "mahasiswa/dashboard";
    }
    
    // DAFTAR KE UKM
    @PostMapping("/daftar")
    public String daftarUkm(@RequestParam Long ukmId, HttpSession session) {
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User mahasiswa = (User) session.getAttribute("user");
        Ukm ukm = ukmRepository.findById(ukmId).orElse(null);
        
        if (ukm == null) {
            return "redirect:/mahasiswa/dashboard?error=UKM tidak ditemukan";
        }
        
        // Cek apakah sudah mendaftar
        Pendaftaran existing = pendaftaranRepository.findByMahasiswaAndUkm(mahasiswa, ukm);
        if (existing != null) {
            return "redirect:/mahasiswa/dashboard?error=Anda sudah mendaftar ke UKM ini";
        }
        
        // Buat pendaftaran baru
        Pendaftaran pendaftaran = new Pendaftaran();
        pendaftaran.setMahasiswa(mahasiswa);
        pendaftaran.setUkm(ukm);
        pendaftaranRepository.save(pendaftaran);
        
        return "redirect:/mahasiswa/dashboard?success=Pendaftaran berhasil! Tunggu verifikasi admin UKM";
    }
    
    // BATALKAN PENDAFTARAN
    @PostMapping("/batalkan/{id}")
    public String batalkanPendaftaran(@PathVariable Long id, HttpSession session) {
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User mahasiswa = (User) session.getAttribute("user");
        Pendaftaran pendaftaran = pendaftaranRepository.findById(id).orElse(null);
        
        // Validasi: hanya bisa membatalkan pendaftaran sendiri yang statusnya PENDING
        if (pendaftaran != null && 
            pendaftaran.getMahasiswa().getId().equals(mahasiswa.getId()) &&
            "PENDING".equals(pendaftaran.getStatus())) {
            
            pendaftaranRepository.delete(pendaftaran);
            return "redirect:/mahasiswa/dashboard?success=Pendaftaran dibatalkan";
        }
        
        return "redirect:/mahasiswa/dashboard?error=Tidak dapat membatalkan pendaftaran";
    }
}
