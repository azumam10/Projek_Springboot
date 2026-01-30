package com.ukm.ukm_app.Controller;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ukm.ukm_app.entity.Kegiatan;
import com.ukm.ukm_app.entity.Pendaftaran;
import com.ukm.ukm_app.entity.Ukm;
import com.ukm.ukm_app.entity.User;
import com.ukm.ukm_app.repository.KegiatanRepository;
import com.ukm.ukm_app.repository.PendaftaranRepository;
import com.ukm.ukm_app.repository.UkmRepository;


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
    
    // ========== MIDDLEWARE CEK LOGIN ==========
    private boolean checkAuth(HttpSession session, String requiredRole) {
        User user = (User) session.getAttribute("user");
        return user != null && requiredRole.equals(user.getRole());
    }
    
    // ========== DASHBOARD MAHASISWA ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        
        // Ambil semua UKM
        List<Ukm> semuaUkm = ukmRepository.findAll();
        
        // Ambil pendaftaran mahasiswa ini
        List<Pendaftaran> pendaftaranSaya = pendaftaranRepository.findByMahasiswa(user);
        
        // Ambil kegiatan dari UKM yang sudah diterima
        List<Long> ukmIdDiterima = pendaftaranSaya.stream()
            .filter(p -> "DITERIMA".equals(p.getStatus()))
            .map(p -> p.getUkm().getId())
            .toList();
        
        List<Kegiatan> kegiatanSaya = kegiatanRepository.findByOrderByTanggalAsc()
            .stream()
            .filter(k -> ukmIdDiterima.contains(k.getUkm().getId()))
            .toList();
        
        // Buat Set ID UKM yang sudah didaftar (untuk helper di Thymeleaf)
        Set<Long> ukmIdTerdaftar = pendaftaranSaya.stream()
            .map(p -> p.getUkm().getId())
            .collect(Collectors.toSet());
        
        // Hitung jumlah UKM yang sudah DITERIMA
        long jumlahUkmDiterima = pendaftaranSaya.stream()
            .filter(p -> "DITERIMA".equals(p.getStatus()))
            .count();
        
        model.addAttribute("user", user);
        model.addAttribute("semuaUkm", semuaUkm);
        model.addAttribute("pendaftaranSaya", pendaftaranSaya);
        model.addAttribute("kegiatanSaya", kegiatanSaya);
        model.addAttribute("ukmIdTerdaftar", ukmIdTerdaftar);
        model.addAttribute("jumlahUkmDiterima", jumlahUkmDiterima);
        
        return "mahasiswa/dashboard";
    }
    
    // ========== DAFTAR KE UKM ==========
    @PostMapping("/daftar")
    public String daftarUkm(
            @RequestParam Long ukmId, 
            HttpSession session,
            RedirectAttributes redirect) {
        
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User mahasiswa = (User) session.getAttribute("user");
        Ukm ukm = ukmRepository.findById(ukmId).orElse(null);
        
        if (ukm == null) {
            redirect.addFlashAttribute("error", "UKM tidak ditemukan");
            return "redirect:/mahasiswa/dashboard";
        }
        
        // Validasi 1: Cek apakah sudah mendaftar ke UKM ini
        Pendaftaran existing = pendaftaranRepository.findByMahasiswaAndUkm(mahasiswa, ukm);
        if (existing != null) {
            redirect.addFlashAttribute("error", "Anda sudah mendaftar ke UKM ini");
            return "redirect:/mahasiswa/dashboard";
        }
        
        // Validasi 2: Cek jumlah UKM yang sudah DITERIMA (max 2)
        long jumlahUkmDiterima = pendaftaranRepository.findByMahasiswa(mahasiswa)
            .stream()
            .filter(p -> "DITERIMA".equals(p.getStatus()))
            .count();
        
        if (jumlahUkmDiterima >= 2) {
            redirect.addFlashAttribute("error", 
                "Anda sudah bergabung di 2 UKM. Maksimal hanya 2 UKM per mahasiswa.");
            return "redirect:/mahasiswa/dashboard";
        }
        
        // Buat pendaftaran baru
        Pendaftaran pendaftaran = new Pendaftaran();
        pendaftaran.setMahasiswa(mahasiswa);
        pendaftaran.setUkm(ukm);
        pendaftaran.setStatus("PENDING");
        pendaftaranRepository.save(pendaftaran);
        
        redirect.addFlashAttribute("success", 
            "Pendaftaran ke " + ukm.getNama() + " berhasil! Tunggu verifikasi admin UKM.");
        
        return "redirect:/mahasiswa/dashboard";
    }
    
    // ========== BATALKAN PENDAFTARAN ==========
    @PostMapping("/batalkan/{id}")
    public String batalkanPendaftaran(
            @PathVariable Long id, 
            HttpSession session,
            RedirectAttributes redirect) {
        
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User mahasiswa = (User) session.getAttribute("user");
        Pendaftaran pendaftaran = pendaftaranRepository.findById(id).orElse(null);
        
        // Validasi 1: Pendaftaran harus ada
        if (pendaftaran == null) {
            redirect.addFlashAttribute("error", "Pendaftaran tidak ditemukan");
            return "redirect:/mahasiswa/dashboard";
        }
        
        // Validasi 2: Hanya bisa membatalkan pendaftaran sendiri
        if (!pendaftaran.getMahasiswa().getId().equals(mahasiswa.getId())) {
            redirect.addFlashAttribute("error", "Anda tidak memiliki akses untuk membatalkan pendaftaran ini");
            return "redirect:/mahasiswa/dashboard";
        }
        
        // Validasi 3: Hanya bisa batalkan jika status PENDING
        if (!"PENDING".equals(pendaftaran.getStatus())) {
            redirect.addFlashAttribute("error", 
                "Tidak dapat membatalkan pendaftaran. Status: " + pendaftaran.getStatus());
            return "redirect:/mahasiswa/dashboard";
        }
        
        // Hapus pendaftaran
        String namaUkm = pendaftaran.getUkm().getNama();
        pendaftaranRepository.delete(pendaftaran);
        
        redirect.addFlashAttribute("success", "Pendaftaran ke " + namaUkm + " berhasil dibatalkan");
        
        return "redirect:/mahasiswa/dashboard";
    }
}