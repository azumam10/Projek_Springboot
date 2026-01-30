package com.ukm.ukm_app.Controller;

import com.ukm.ukm_app.entity.*;
import com.ukm.ukm_app.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminKampusController {
    
   
    @Autowired
    private UkmRepository ukmRepository;
    
    @Autowired
    private KegiatanRepository kegiatanRepository;
    
    @Autowired
    private PendaftaranRepository pendaftaranRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ========== MIDDLEWARE CEK LOGIN ==========
    private boolean checkAuth(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN_KAMPUS".equals(user.getRole());
    }
    
    // ========== DASHBOARD ADMIN KAMPUS ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        
        // Ambil semua UKM
        List<Ukm> semuaUkm = ukmRepository.findAll();
        
        // Ambil semua kegiatan
        List<Kegiatan> semuaKegiatan = kegiatanRepository.findByOrderByTanggalAsc();
        
        // Hitung jumlah anggota per UKM
        Map<Long, Long> jumlahAnggotaPerUkm = new HashMap<>();
        for (Ukm ukm : semuaUkm) {
            long jumlahAnggota = pendaftaranRepository.findByUkm(ukm)
                .stream()
                .filter(p -> "DITERIMA".equals(p.getStatus()))
                .count();
            jumlahAnggotaPerUkm.put(ukm.getId(), jumlahAnggota);
        }
        
        // Hitung total mahasiswa, admin UKM, dll
        long totalMahasiswa = userRepository.countByRole("MAHASISWA");
        long totalAdminUkm = userRepository.countByRole("ADMIN_UKM");
        long totalUkm = semuaUkm.size();
        long totalKegiatan = semuaKegiatan.size();
        
        model.addAttribute("user", user);
        model.addAttribute("semuaUkm", semuaUkm);
        model.addAttribute("semuaKegiatan", semuaKegiatan);
        model.addAttribute("jumlahAnggotaPerUkm", jumlahAnggotaPerUkm);
        model.addAttribute("totalMahasiswa", totalMahasiswa);
        model.addAttribute("totalAdminUkm", totalAdminUkm);
        model.addAttribute("totalUkm", totalUkm);
        model.addAttribute("totalKegiatan", totalKegiatan);
        
        return "admin/dashboard";
    }
    
    // ========== DETAIL UKM ==========
    @GetMapping("/ukm/{id}")
    public String detailUkm(@PathVariable Long id, HttpSession session, Model model) {
        if (!checkAuth(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        Ukm ukm = ukmRepository.findById(id).orElse(null);
        
        if (ukm == null) {
            return "redirect:/admin/dashboard?error=UKM tidak ditemukan";
        }
        
        // Ambil data detail UKM
        List<Pendaftaran> anggota = pendaftaranRepository.findByUkm(ukm)
            .stream()
            .filter(p -> "DITERIMA".equals(p.getStatus()))
            .toList();
        
        List<Kegiatan> kegiatan = kegiatanRepository.findByUkmId(ukm.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("ukm", ukm);
        model.addAttribute("anggota", anggota);
        model.addAttribute("kegiatan", kegiatan);
        
        return "admin/detail_ukm";
    }
}