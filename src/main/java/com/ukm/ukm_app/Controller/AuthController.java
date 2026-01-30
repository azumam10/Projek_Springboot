package com.ukm.ukm_app.Controller;

import com.ukm.ukm_app.entity.User;
import com.ukm.ukm_app.entity.Ukm;
import com.ukm.ukm_app.repository.UserRepository;
import com.ukm.ukm_app.repository.UkmRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UkmRepository ukmRepository;
    
    // ========== SIMPLE PASSWORD CHECK ==========
    private boolean checkPassword(String input, String stored) {
        // SEMENTARA: Password polos tanpa encryption
        return input.equals(stored);
    }
    
    // ========== HOME ==========
    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    // ========== LOGIN ==========
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, 
                           @RequestParam(required = false) String success,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Email atau password salah!");
        }
        if (success != null) {
            model.addAttribute("success", "Registrasi berhasil! Silakan login.");
        }
        return "login";
    }
    
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirect) {
        
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            System.out.println("User found: " + user.getNama());
            System.out.println("Stored password: " + user.getPassword());
            System.out.println("Input password: " + password);
            
            // SIMPLE CHECK - No BCrypt
            if (checkPassword(password, user.getPassword())) {
                System.out.println("Login SUCCESS for: " + user.getRole());
                session.setAttribute("user", user);
                
                // Redirect berdasarkan role
                String redirectUrl = switch (user.getRole()) {
                    case "ADMIN_KAMPUS" -> "/admin/dashboard";
                    case "ADMIN_UKM" -> "/ukm/dashboard";
                    case "MAHASISWA" -> "/mahasiswa/dashboard";
                    default -> "/login?error";
                };
                
                return "redirect:" + redirectUrl;
            } else {
                System.out.println("Password MISMATCH");
            }
        } else {
            System.out.println("User NOT FOUND with email: " + email);
        }
        
        return "redirect:/login?error";
    }
    
    // ========== REGISTER ==========
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("listUkm", ukmRepository.findAll());
        return "register";
    }
    
    @PostMapping("/register")
    public String register(
            @RequestParam String nim,
            @RequestParam String nama,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) Long ukmId,
            RedirectAttributes redirect) {
        
        // Validasi email unik
        if (userRepository.existsByEmail(email)) {
            return "redirect:/register?error=Email sudah terdaftar";
        }
        
        // Buat user baru - SIMPAN PASSWORD POLOS
        User user = new User();
        user.setNim(nim);
        user.setNama(nama);
        user.setEmail(email);
        user.setPassword(password); // Password polos, tanpa hash!
        user.setRole(role);
        
        // Jika admin UKM, assign UKM
        if ("ADMIN_UKM".equals(role) && ukmId != null) {
            Ukm ukm = ukmRepository.findById(ukmId).orElse(null);
            if (ukm != null) {
                user.setUkm(ukm);
            }
        }
        
        userRepository.save(user);
        
        System.out.println("=== REGISTER SUCCESS ===");
        System.out.println("User: " + email + " / " + password);
        
        return "redirect:/login?success";
    }
    
    // ========== CREATE DEFAULT USERS ==========
    @GetMapping("/init")
    public String initDatabase(RedirectAttributes redirect) {
        // Cek jika belum ada user
        if (userRepository.count() == 0) {
            // 1. Buat Admin Kampus
            User adminKampus = new User();
            adminKampus.setNim("ADMIN001");
            adminKampus.setNama("Admin Kampus");
            adminKampus.setEmail("admin@kampus.ac.id");
            adminKampus.setPassword("admin123"); // Password polos
            adminKampus.setRole("ADMIN_KAMPUS");
            userRepository.save(adminKampus);
            
            // 2. Buat UKM
            Ukm ukm1 = new Ukm();
            ukm1.setNama("UKM Programming");
            ukm1.setDeskripsi("UKM untuk belajar programming");
            ukmRepository.save(ukm1);
            
            Ukm ukm2 = new Ukm();
            ukm2.setNama("UKM Seni Musik");
            ukm2.setDeskripsi("UKM untuk bermain musik");
            ukmRepository.save(ukm2);
            
            // 3. Buat Admin UKM
            User adminUkm = new User();
            adminUkm.setNim("ADMIN002");
            adminUkm.setNama("Ketua Programming");
            adminUkm.setEmail("programming@kampus.ac.id");
            adminUkm.setPassword("admin123"); // Password polos
            adminUkm.setRole("ADMIN_UKM");
            adminUkm.setUkm(ukm1);
            userRepository.save(adminUkm);
            
            // 4. Buat Mahasiswa
            User mahasiswa = new User();
            mahasiswa.setNim("2023001");
            mahasiswa.setNama("Budi Santoso");
            mahasiswa.setEmail("budi@kampus.ac.id");
            mahasiswa.setPassword("mahasiswa123"); // Password polos
            mahasiswa.setRole("MAHASISWA");
            userRepository.save(mahasiswa);
            
            redirect.addFlashAttribute("success", "Database initialized!");
            System.out.println("=== DATABASE INITIALIZED ===");
        } else {
            redirect.addFlashAttribute("info", "Database already has data");
        }
        
        return "redirect:/login";
    }
    
    // ========== LOGOUT ==========
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    // ========== DASHBOARD REDIRECT ==========
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        switch (user.getRole()) {
            case "ADMIN_KAMPUS":
                return "redirect:/admin/dashboard";
            case "ADMIN_UKM":
                return "redirect:/ukm/dashboard";
            case "MAHASISWA":
                return "redirect:/mahasiswa/dashboard";
            default:
                return "redirect:/login";
        }
    }
}