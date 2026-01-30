package com.ukm.ukm_app.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ukm.ukm_app.entity.Ukm;
import com.ukm.ukm_app.entity.User;
import com.ukm.ukm_app.repository.KegiatanRepository;
import com.ukm.ukm_app.repository.PendaftaranRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/ukm")
public class AdminUkmController {
    
    @Autowired
    private KegiatanRepository kegiatanRepository;
    
    @Autowired
    private PendaftaranRepository pendaftaranRepository;
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN_UKM".equals(user.getRole())) {
            return "redirect:/login";
        }
        
        // Ambil UKM yang dikelola admin ini
        Ukm ukm = user.getUkm();
        if (ukm == null) {
            return "redirect:/login?error=Admin tidak memiliki UKM";
        }
        
        // Data untuk dashboard
        model.addAttribute("user", user);
        model.addAttribute("ukm", ukm);
        model.addAttribute("pendaftaran", pendaftaranRepository.findByUkm(ukm));
        model.addAttribute("kegiatan", kegiatanRepository.findByUkmId(ukm.getId()));
        
        return "ukm/dashboard";
    }
}