package com.ukm.ukm_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukm.ukm_app.entity.Kegiatan;

public interface KegiatanRepository extends JpaRepository<Kegiatan, Long> {
    List<Kegiatan> findByUkmId(Long ukmId);
    List<Kegiatan> findByOrderByTanggalAsc();
}
