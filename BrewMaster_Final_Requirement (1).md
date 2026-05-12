# BrewMaster App Technical Specification v1.0

Dokumen ini adalah baseline final yang mencakup seluruh requirement untuk aplikasi Android Brew Timer & Recipe Manager.

## 1. Parameter Input Utama
User akan memasukkan data melalui interface dengan parameter berikut:

| Parameter | Tipe Data / Opsi | Deskripsi |
| :--- | :--- | :--- |
| **Berat Kopi** | Double (gram) | Input numerik untuk dosis kopi. |
| **Grind Size** | Enum | Fine, Medium-Fine, Medium, Medium-Coarse, Coarse, Extra-Fine. |
| **Brew Mode** | Toggle | **Hot** (Panas) atau **Ice** (Es). |
| **Ice Weight** | Double (gram) | Muncul otomatis jika Brew Mode = Ice. |
| **Jenis Proses** | String / Database | Filter proses kopi (Aerob, Anaerob, Natural, Washed, Honey, dsb). kumpulkan terlebih dahulu dari google |

---

## 2. Struktur Database (Schema)

### Table: `coffee_processes`
Kumpulan data jenis proses kopi yang mempengaruhi parameter otomatis.
- `id` (PK)
- `process_name`: Anaerob, Aerob, Carbonic Maceration, dsb.
- `temp_recommendation`: Rekomendasi suhu default.

### Table: `personal_recipes`
Penyimpanan resep custom user.
- `id` (PK)
- `bean_name`: Nama biji kopi.
- `process_id`: Relasi ke tabel proses.
- `grind_size`: Pilihan grind size.
- `ratio`: Rasio air.
- `is_ice`: Boolean status hot/ice.

---

## 3. Logika Kalkulasi (Logic Engine)

### A. Perhitungan Air (Yield Logic)
- **Mode Hot:**
  `Total_Air_Panas = Berat_Kopi * Rasio`
- **Mode Ice:**
  `Total_Volume = Berat_Kopi * Rasio`
  `Total_Air_Panas = Total_Volume - Berat_Es_Batu`

### B. Penentuan Suhu Air (Auto-Temp)
- **Proses Anaerob / Darker:** Set ke 88°C - 90°C.
- **Proses Aerob / Washed / Light:** Set ke 92°C - 94°C.

### C. Step-by-Step Brewing (Dynamic Steps)
1. **Blooming:** 2x - 3x Berat Kopi (Waktu: 0 - 45 detik).
2. **Pour 1:** 40% dari sisa air panas (Waktu: 45 - 90 detik).
3. **Pour 2:** Sisa air panas (Waktu: 90 - 150 detik).

---

## 4. Fitur Interaktif Timer & Alert

- **Active Timer:** Counter-up yang berjalan di foreground service agar tidak mati saat layar mati.
- **Step Alert:** Getaran/Suara setiap perpindahan tahap penuangan (pouring).
- **Final Alert:** Notifikasi khusus **"STOP / ANGKAT V60"** saat target volume air panas telah tercapai atau timer menyentuh waktu limit (misal 3:00 menit).

---

## 5. UI/UX Requirement
- **Main Dashboard:** Menampilkan parameter dosis dan rasio secara berdampingan.
- **Quick Switch:** Tombol cepat untuk beralih Hot ke Ice yang langsung mengubah tabel kalkulasi air.
- **Visual Timer:** Progress bar melingkar yang menunjukkan progres volume air saat ini dibandingkan target.

### Ringkasan Parameter yang Dimasukkan:

1.  **Database Jenis Kopi:** Saya sudah menyertakan kolom `process_type` (Aerob, Anaerob, dsb.) yang nantinya akan menarik data dari database untuk menentukan rekomendasi **Suhu Air**.
2.  **Logika Ice/Hot:**
    * Jika **Hot**: Total volume air panas = $Berat Kopi \times Rasio$.
    * Jika **Ice**: Menambahkan parameter `ice_weight`. Total air panas dikurangi berat es agar hasil akhir (yield) tetap konsisten dengan rasio yang diinginkan.
3.  **Grind Size:** Menggunakan sistem *Enum* (Medium, Medium-Coarse, dsb.) agar user tinggal memilih lewat menu dropdown/spinner.
4.  **Output Terintegrasi:**
    * **Timer & Step-by-Step:** Urutan penuangan air yang menyesuaikan dengan sisa air panas (terutama pada mode Ice).
    * **Auto-Stop Alert:** Notifikasi visual/suara untuk "Angkat V60" saat target waktu atau volume tercapai.



**Saran Pengembangan Lanjutan:**
Untuk bagian "Jenis Kopi", karena Anda ingin mengumpulkan datanya dari Google, kita bisa menambahkan fitur **Web Scraper sederhana** atau **API Integration** yang akan mengisi tabel `coffee_beans` secara otomatis setiap kali Anda menemukan jenis proses baru.