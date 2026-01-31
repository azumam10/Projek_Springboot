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
        
        // Cek autentikasi
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User mahasiswa = (User) session.getAttribute("user");
        
        // ========== PERBAIKAN: TAMBAH TRY-CATCH ==========
        try {
            System.out.println("=== PROSES DAFTAR UKM ===");
            System.out.println("Mahasiswa: " + mahasiswa.getNama() + " (ID: " + mahasiswa.getId() + ")");
            System.out.println("UKM ID: " + ukmId);
            
            // Cari UKM
            Ukm ukm = ukmRepository.findById(ukmId).orElse(null);
            
            if (ukm == null) {
                System.out.println("ERROR: UKM tidak ditemukan dengan ID: " + ukmId);
                redirect.addFlashAttribute("error", "UKM tidak ditemukan");
                return "redirect:/mahasiswa/dashboard";
            }
            
            System.out.println("UKM ditemukan: " + ukm.getNama());
            
            // Validasi 1: Cek apakah sudah mendaftar ke UKM ini
            Pendaftaran existing = pendaftaranRepository.findByMahasiswaAndUkm(mahasiswa, ukm);
            if (existing != null) {
                System.out.println("ERROR: Mahasiswa sudah mendaftar ke UKM ini");
                redirect.addFlashAttribute("error", "Anda sudah mendaftar ke UKM ini");
                return "redirect:/mahasiswa/dashboard";
            }
            
            // Validasi 2: Cek jumlah UKM yang sudah DITERIMA (max 2)
            long jumlahUkmDiterima = pendaftaranRepository.findByMahasiswa(mahasiswa)
                .stream()
                .filter(p -> "DITERIMA".equals(p.getStatus()))
                .count();
            
            System.out.println("Jumlah UKM yang sudah diterima: " + jumlahUkmDiterima);
            
            if (jumlahUkmDiterima >= 2) {
                System.out.println("ERROR: Kuota UKM sudah penuh (2/2)");
                redirect.addFlashAttribute("error", "Kuota penuh! Maksimal 2 UKM per mahasiswa");
                return "redirect:/mahasiswa/dashboard";
            }
            
            // Buat pendaftaran baru
            Pendaftaran pendaftaran = new Pendaftaran();
            pendaftaran.setMahasiswa(mahasiswa);
            pendaftaran.setUkm(ukm);
            pendaftaran.setStatus("PENDING");
            
            // Simpan ke database
            pendaftaranRepository.save(pendaftaran);
            
            System.out.println("SUCCESS: Pendaftaran berhasil disimpan dengan ID: " + pendaftaran.getId());
            
            // ========== PERBAIKAN: PESAN SUKSES LEBIH PENDEK ==========
            redirect.addFlashAttribute("success", "Pendaftaran berhasil! Tunggu verifikasi admin");
            
        } catch (Exception e) {
            // ========== PERBAIKAN: HANDLE ERROR ==========
            System.err.println("========================================");
            System.err.println("ERROR SAAT DAFTAR UKM!");
            System.err.println("========================================");
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Stack Trace:");
            e.printStackTrace();
            System.err.println("========================================");
            
            redirect.addFlashAttribute("error", "Terjadi kesalahan sistem. Silakan coba lagi");
            return "redirect:/mahasiswa/dashboard";
        }
        
        return "redirect:/mahasiswa/dashboard";
    }
    
    // ========== BATALKAN PENDAFTARAN ==========
    @PostMapping("/batalkan/{id}")
    public String batalkanPendaftaran(
            @PathVariable Long id, 
            HttpSession session,
            RedirectAttributes redirect) {
        
        // Cek autentikasi
        if (!checkAuth(session, "MAHASISWA")) {
            return "redirect:/login";
        }
        
        User mahasiswa = (User) session.getAttribute("user");
        
        // ========== PERBAIKAN: TAMBAH TRY-CATCH ==========
        try {
            System.out.println("=== PROSES BATALKAN PENDAFTARAN ===");
            System.out.println("Mahasiswa: " + mahasiswa.getNama() + " (ID: " + mahasiswa.getId() + ")");
            System.out.println("Pendaftaran ID: " + id);
            
            // Cari pendaftaran
            Pendaftaran pendaftaran = pendaftaranRepository.findById(id).orElse(null);
            
            // Validasi 1: Pendaftaran harus ada
            if (pendaftaran == null) {
                System.out.println("ERROR: Pendaftaran tidak ditemukan dengan ID: " + id);
                redirect.addFlashAttribute("error", "Pendaftaran tidak ditemukan");
                return "redirect:/mahasiswa/dashboard";
            }
            
            System.out.println("Pendaftaran ditemukan: " + pendaftaran.getUkm().getNama());
            
            // Validasi 2: Hanya bisa membatalkan pendaftaran sendiri
            if (!pendaftaran.getMahasiswa().getId().equals(mahasiswa.getId())) {
                System.out.println("ERROR: Mahasiswa tidak memiliki akses");
                redirect.addFlashAttribute("error", "Akses ditolak");
                return "redirect:/mahasiswa/dashboard";
            }
            
            // Validasi 3: Hanya bisa batalkan jika status PENDING
            if (!"PENDING".equals(pendaftaran.getStatus())) {
                System.out.println("ERROR: Status bukan PENDING: " + pendaftaran.getStatus());
                redirect.addFlashAttribute("error", "Tidak dapat dibatalkan. Status: " + pendaftaran.getStatus());
                return "redirect:/mahasiswa/dashboard";
            }
            
            // Hapus pendaftaran
            String namaUkm = pendaftaran.getUkm().getNama();
            pendaftaranRepository.delete(pendaftaran);
            
            System.out.println("SUCCESS: Pendaftaran berhasil dihapus");
            
            redirect.addFlashAttribute("success", "Pendaftaran berhasil dibatalkan");
            
        } catch (Exception e) {
            // ========== PERBAIKAN: HANDLE ERROR ==========
            System.err.println("========================================");
            System.err.println("ERROR SAAT BATALKAN PENDAFTARAN!");
            System.err.println("========================================");
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Stack Trace:");
            e.printStackTrace();
            System.err.println("========================================");
            
            redirect.addFlashAttribute("error", "Terjadi kesalahan sistem. Silakan coba lagi");
            return "redirect:/mahasiswa/dashboard";
        }
        
        return "redirect:/mahasiswa/dashboard";
    }
}