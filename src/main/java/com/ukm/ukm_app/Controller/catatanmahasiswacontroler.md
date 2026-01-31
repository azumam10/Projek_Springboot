# Catatan Kode: AdminKampusController (Spring Boot MVC)

Dokumen ini menjelaskan fungsi dan alur kerja dari file:

```
AdminKampusController.java
```

Controller ini digunakan untuk **mengelola dashboard Admin Kampus** pada sistem informasi UKM berbasis **Spring Boot MVC + Thymeleaf**.

---

## 1. Package dan Import

```java
package com.ukm.ukm_app.Controller;
```

Menentukan lokasi class di dalam struktur project.

### Import utama

```java
import com.ukm.ukm_app.entity.*;
import com.ukm.ukm_app.repository.*;
```

* Mengimpor **Entity** (User, Ukm, Kegiatan, Pendaftaran)
* Mengimpor **Repository** untuk akses database (JPA)

```java
import jakarta.servlet.http.HttpSession;
```

Digunakan untuk **manajemen session login**.

```java
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
```

Digunakan untuk:

* MVC Controller
* Mapping URL
* Mengirim data ke view (HTML)

---

## 2. Anotasi Class

```java
@Controller
@RequestMapping("/admin")
```

Penjelasan:

* `@Controller` → menandakan class ini adalah **MVC Controller** (return HTML)
* `@RequestMapping("/admin")` → semua URL di controller ini diawali `/admin`

Contoh URL:

* `/admin/dashboard`
* `/admin/ukm/{id}`

---

## 3. Dependency Injection (Repository)

```java
@Autowired
private UkmRepository ukmRepository;
```

Repository yang digunakan:

* `UkmRepository` → data UKM
* `KegiatanRepository` → data kegiatan
* `PendaftaranRepository` → data pendaftaran anggota
* `UserRepository` → data user (mahasiswa, admin)

`@Autowired` digunakan agar Spring **otomatis menginject object repository**.

---

## 4. Method checkAuth (Middleware Login)

```java
private boolean checkAuth(HttpSession session) {
    User user = (User) session.getAttribute("user");
    return user != null && "ADMIN_KAMPUS".equals(user.getRole());
}
```

### Fungsi:

* Mengecek apakah user:

  * Sudah login
  * Memiliki role **ADMIN_KAMPUS**

### Cara kerja:

* Mengambil object `user` dari session
* Jika tidak ada → belum login
* Jika role bukan ADMIN_KAMPUS → akses ditolak

Digunakan sebagai **pengganti middleware/security sederhana**.

---

## 5. Dashboard Admin Kampus

```java
@GetMapping("/dashboard")
public String dashboard(HttpSession session, Model model)
```

### Fungsi:

Menampilkan **dashboard utama Admin Kampus**.

### Alur Kerja:

1. Cek login dengan `checkAuth`
2. Ambil data user dari session
3. Ambil seluruh data UKM dan Kegiatan
4. Hitung statistik
5. Kirim data ke view

---

### a. Ambil Semua UKM dan Kegiatan

```java
List<Ukm> semuaUkm = ukmRepository.findAll();
List<Kegiatan> semuaKegiatan = kegiatanRepository.findByOrderByTanggalAsc();
```

Digunakan untuk:

* Menampilkan daftar UKM
* Menampilkan daftar kegiatan terurut tanggal

---

### b. Hitung Jumlah Anggota per UKM

```java
Map<Long, Long> jumlahAnggotaPerUkm = new HashMap<>();
```

Logika:

* Loop setiap UKM
* Ambil data pendaftaran berdasarkan UKM
* Filter status `DITERIMA`
* Hitung jumlah anggota

Tujuan:

* Menampilkan jumlah anggota tiap UKM di dashboard

---

### c. Hitung Statistik Global

```java
long totalMahasiswa = userRepository.countByRole("MAHASISWA");
long totalAdminUkm = userRepository.countByRole("ADMIN_UKM");
long totalUkm = semuaUkm.size();
long totalKegiatan = semuaKegiatan.size();
```

Digunakan untuk:

* Statistik ringkasan di dashboard
* Card / summary info

---

### d. Kirim Data ke View

```java
model.addAttribute("user", user);
model.addAttribute("semuaUkm", semuaUkm);
model.addAttribute("totalMahasiswa", totalMahasiswa);
```

`Model` berfungsi untuk:

* Mengirim data ke file HTML (Thymeleaf)

---

### e. Return View

```java
return "admin/dashboard";
```

Artinya Spring akan membuka:

```
resources/templates/admin/dashboard.html
```

---

## 6. Detail UKM

```java
@GetMapping("/ukm/{id}")
public String detailUkm(@PathVariable Long id, HttpSession session, Model model)
```

### Fungsi:

Menampilkan **detail lengkap satu UKM**.

---

### a. Ambil UKM Berdasarkan ID

```java
Ukm ukm = ukmRepository.findById(id).orElse(null);
```

Jika UKM tidak ditemukan:

```java
return "redirect:/admin/dashboard?error=UKM tidak ditemukan";
```

---

### b. Ambil Anggota UKM

```java
List<Pendaftaran> anggota = pendaftaranRepository.findByUkm(ukm)
    .stream()
    .filter(p -> "DITERIMA".equals(p.getStatus()))
    .toList();
```

Hanya menampilkan:

* Anggota dengan status **DITERIMA**

---

### c. Ambil Kegiatan UKM

```java
List<Kegiatan> kegiatan = kegiatanRepository.findByUkmId(ukm.getId());
```

Digunakan untuk:

* Menampilkan agenda / kegiatan UKM tersebut

---

### d. Return View Detail

```java
return "admin/detail_ukm";
```

Akan membuka file:

```
resources/templates/admin/detail_ukm.html
```

---

## 7. Kesimpulan

Controller ini bertugas untuk:

* Mengelola **Dashboard Admin Kampus**
* Monitoring UKM, anggota, dan kegiatan
* Menyediakan statistik global
* Menampilkan detail UKM

### Catatan Pengembangan Lanjutan:

* Disarankan menggunakan **Spring Security**
* Session manual bisa diganti JWT / Authentication
* Optimasi query (count langsung dari database)

---

✍️ Catatan ini cocok untuk:

* Laporan tugas akhir
* Dokumentasi proyek
* Presentasi sistem
