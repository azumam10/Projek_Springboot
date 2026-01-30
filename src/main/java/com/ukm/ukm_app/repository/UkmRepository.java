package com.ukm.ukm_app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ukm.ukm_app.entity.Ukm;

public interface UkmRepository extends JpaRepository<Ukm, Long> {
    List<Ukm> findByNamaContaining(String keyword);
    // Ga usah ada karakter aneh di sini!
}