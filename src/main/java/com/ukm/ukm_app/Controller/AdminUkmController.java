package com.ukm.ukm_app.Controller;

import com.ukm.ukm_app.entity.*;
import com.ukm.ukm_app.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/ukm")
public class AdminUkmController {
    
    @Autowired
    private KegiatanRepository kegiatanRepository;
    
    @Autowired
    private PendaftaranRepository pendaftaranRepository;
    
    @Autowired
    private UkmRepository ukmRepository;
    
    // ========== MIDDLEWARE CEK LOGIN ==========
    private boolean checkAuth(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN_UKM".equals(user.getRole());
    }
    
    // ========== DASHBOARD ADMIN UKM ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        Ukm ukm = user.getUkm();
        
        if (ukm == null) {
            return "redirect:/login?error=Admin tidak memiliki UKM";
        }
        
        // Ambil data untuk dashboard
        List<Pendaftaran> semuaPendaftaran = pendaftaranRepository.findByUkm(ukm);
        List<Pendaftaran> pendaftaranPending = semuaPendaftaran.stream()
            .filter(p -> "PENDING".equals(p.getStatus()))
            .toList();
        List<Pendaftaran> pendaftaranDiterima = semuaPendaftaran.stream()
            .filter(p -> "DITERIMA".equals(p.getStatus()))
            .toList();
        
        List<Kegiatan> kegiatanList = kegiatanRepository.findByUkmId(ukm.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("ukm", ukm);
        model.addAttribute("pendaftaranPending", pendaftaranPending);
        model.addAttribute("pendaftaranDiterima", pendaftaranDiterima);
        model.addAttribute("kegiatan", kegiatanList);
        model.addAttribute("totalAnggota", pendaftaranDiterima.size());
        
        return "ukm/dashboard";
    }
    
    // ========== TERIMA PENDAFTARAN ==========
    @PostMapping("/terima/{id}")
    public String terimaPendaftaran(
            @PathVariable Long id, 
            HttpSession session, 
            RedirectAttributes redirect) {
        
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        Pendaftaran pendaftaran = pendaftaranRepository.findById(id).orElse(null);
        
        // Validasi: hanya bisa terima pendaftaran UKM sendiri
        if (pendaftaran == null) {
            redirect.addFlashAttribute("error", "Pendaftaran tidak ditemukan");
            return "redirect:/ukm/dashboard";
        }
        
        if (!pendaftaran.getUkm().getId().equals(user.getUkm().getId())) {
            redirect.addFlashAttribute("error", "Anda tidak memiliki akses untuk pendaftaran ini");
            return "redirect:/ukm/dashboard";
        }
        
        if (!"PENDING".equals(pendaftaran.getStatus())) {
            redirect.addFlashAttribute("error", "Pendaftaran sudah diproses sebelumnya");
            return "redirect:/ukm/dashboard";
        }
        
        // Update status
        pendaftaran.setStatus("DITERIMA");
        pendaftaranRepository.save(pendaftaran);
        
        redirect.addFlashAttribute("success", 
            "Pendaftaran " + pendaftaran.getMahasiswa().getNama() + " berhasil diterima!");
        
        return "redirect:/ukm/dashboard";
    }
    
    // ========== TOLAK PENDAFTARAN ==========
    @PostMapping("/tolak/{id}")
    public String tolakPendaftaran(
            @PathVariable Long id, 
            HttpSession session, 
            RedirectAttributes redirect) {
        
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        Pendaftaran pendaftaran = pendaftaranRepository.findById(id).orElse(null);
        
        // Validasi: hanya bisa tolak pendaftaran UKM sendiri
        if (pendaftaran == null) {
            redirect.addFlashAttribute("error", "Pendaftaran tidak ditemukan");
            return "redirect:/ukm/dashboard";
        }
        
        if (!pendaftaran.getUkm().getId().equals(user.getUkm().getId())) {
            redirect.addFlashAttribute("error", "Anda tidak memiliki akses untuk pendaftaran ini");
            return "redirect:/ukm/dashboard";
        }
        
        if (!"PENDING".equals(pendaftaran.getStatus())) {
            redirect.addFlashAttribute("error", "Pendaftaran sudah diproses sebelumnya");
            return "redirect:/ukm/dashboard";
        }
        
        // Update status
        pendaftaran.setStatus("DITOLAK");
        pendaftaranRepository.save(pendaftaran);
        
        redirect.addFlashAttribute("success", 
            "Pendaftaran " + pendaftaran.getMahasiswa().getNama() + " ditolak");
        
        return "redirect:/ukm/dashboard";
    }
    
    // ========== FORM TAMBAH KEGIATAN ==========
    @GetMapping("/kegiatan/tambah")
    public String formTambahKegiatan(HttpSession session, Model model) {
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("ukm", user.getUkm());
        
        return "ukm/tambah_kegiatan";
    }
    
    // ========== PROSES TAMBAH KEGIATAN ==========
    @PostMapping("/kegiatan/tambah")
    public String tambahKegiatan(
            @RequestParam String judul,
            @RequestParam String deskripsi,
            @RequestParam String tanggal,
            @RequestParam String lokasi,
            HttpSession session,
            RedirectAttributes redirect) {
        
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // Parse tanggal dari form (format: yyyy-MM-ddTHH:mm)
            LocalDateTime tanggalParsed = LocalDateTime.parse(tanggal);
            
            // Buat kegiatan baru
            Kegiatan kegiatan = new Kegiatan();
            kegiatan.setJudul(judul);
            kegiatan.setDeskripsi(deskripsi);
            kegiatan.setTanggal(tanggalParsed);
            kegiatan.setLokasi(lokasi);
            kegiatan.setUkm(user.getUkm());
            
            kegiatanRepository.save(kegiatan);
            
            redirect.addFlashAttribute("success", "Kegiatan berhasil ditambahkan!");
            
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Gagal menambahkan kegiatan: " + e.getMessage());
        }
        
        return "redirect:/ukm/dashboard";
    }
    
    // ========== HAPUS KEGIATAN ==========
    @PostMapping("/kegiatan/hapus/{id}")
    public String hapusKegiatan(
            @PathVariable Long id, 
            HttpSession session, 
            RedirectAttributes redirect) {
        
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        Kegiatan kegiatan = kegiatanRepository.findById(id).orElse(null);
        
        // Validasi: hanya bisa hapus kegiatan UKM sendiri
        if (kegiatan == null) {
            redirect.addFlashAttribute("error", "Kegiatan tidak ditemukan");
            return "redirect:/ukm/dashboard";
        }
        
        if (!kegiatan.getUkm().getId().equals(user.getUkm().getId())) {
            redirect.addFlashAttribute("error", "Anda tidak memiliki akses untuk kegiatan ini");
            return "redirect:/ukm/dashboard";
        }
        
        kegiatanRepository.delete(kegiatan);
        redirect.addFlashAttribute("success", "Kegiatan berhasil dihapus!");
        
        return "redirect:/ukm/dashboard";
    }
}