package com.ukm.ukm_app.repository;

import com.ukm.ukm_app.entity.Pendaftaran;
import com.ukm.ukm_app.entity.Ukm;
import com.ukm.ukm_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PendaftaranRepository extends JpaRepository<Pendaftaran, Long> {
    Pendaftaran findByMahasiswaAndUkm(User mahasiswa, Ukm ukm);
    List<Pendaftaran> findByUkm(Ukm ukm);
    List<Pendaftaran> findByMahasiswa(User mahasiswa);
    List<Pendaftaran> findByStatus(String status);
}
