package com.ukm.ukm_app.Controller;


import com.ukm.ukm_app.entity.User;
import com.ukm.ukm_app.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminKampusController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UkmRepository ukmRepository;
    
    @Autowired
    private KegiatanRepository kegiatanRepository;
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN_KAMPUS".equals(user.getRole())) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("totalUkm", ukmRepository.count());
        model.addAttribute("totalMahasiswa", 
            userRepository.countByRole("MAHASISWA"));
        model.addAttribute("totalAdminUkm", 
            userRepository.countByRole("ADMIN_UKM"));
        model.addAttribute("listUkm", ukmRepository.findAll());
        
        return "admin/dashboard";
    }
}