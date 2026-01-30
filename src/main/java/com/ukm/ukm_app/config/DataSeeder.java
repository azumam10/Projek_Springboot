package com.ukm.ukm_app.config;

import com.ukm.ukm_app.entity.Ukm;
import com.ukm.ukm_app.entity.User;
import com.ukm.ukm_app.repository.UkmRepository;
import com.ukm.ukm_app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final UkmRepository ukmRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public DataSeeder(UserRepository userRepository, UkmRepository ukmRepository) {
        this.userRepository = userRepository;
        this.ukmRepository = ukmRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Cek jika belum ada admin kampus
        if (userRepository.count() == 0) {
            // 1. Buat Admin Kampus
            User adminKampus = new User();
            adminKampus.setNim("ADMIN001");
            adminKampus.setNama("Admin Kampus");
            adminKampus.setEmail("admin@kampus.ac.id");
            adminKampus.setPassword(encoder.encode("admin123"));
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
            User adminUkm1 = new User();
            adminUkm1.setNim("ADMIN002");
            adminUkm1.setNama("Ketua Programming");
            adminUkm1.setEmail("programming@kampus.ac.id");
            adminUkm1.setPassword(encoder.encode("admin123"));
            adminUkm1.setRole("ADMIN_UKM");
            adminUkm1.setUkm(ukm1);
            userRepository.save(adminUkm1);
            
            // 4. Buat Mahasiswa
            User mahasiswa1 = new User();
            mahasiswa1.setNim("2023001");
            mahasiswa1.setNama("Budi Santoso");
            mahasiswa1.setEmail("budi@kampus.ac.id");
            mahasiswa1.setPassword(encoder.encode("mahasiswa123"));
            mahasiswa1.setRole("MAHASISWA");
            userRepository.save(mahasiswa1);
            
            User mahasiswa2 = new User();
            mahasiswa2.setNim("2023002");
            mahasiswa2.setNama("Siti Aminah");
            mahasiswa2.setEmail("siti@kampus.ac.id");
            mahasiswa2.setPassword(encoder.encode("mahasiswa123"));
            mahasiswa2.setRole("MAHASISWA");
            userRepository.save(mahasiswa2);
            
            System.out.println("========== DATA SEEDER BERHASIL ==========");
            System.out.println("Admin Kampus: admin@kampus.ac.id / admin123");
            System.out.println("Admin UKM: programming@kampus.ac.id / admin123");
            System.out.println("Mahasiswa: budi@kampus.ac.id / mahasiswa123");
        }
    }
}