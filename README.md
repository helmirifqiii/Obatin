## 💊 Obatin — Solusi Manajemen Kesehatan & Pengingat Obat

Aplikasi web modern yang dirancang untuk membantu pengguna mengelola jadwal minum obat, memantau riwayat kesehatan, dan mendapatkan informasi seputar obat-obatan. Dibangun menggunakan Fullstack JavaScript Tech Stack yang digunakan di dunia profesional.

Aplikasi ini melayani dua peran utama:

* **Pengguna (Pasien):** Mengatur jadwal dan menerima notifikasi.
* **Admin (Pengelola):** Memantau data umum dan mengelola informasi obat.

---

### ✨ Fitur Utama (Kelas Profesional)

#### 1. 🔑 Fitur Dasar & Identitas

| Fitur | Deskripsi | Status |
| :--- | :--- | :--- |
| **Login & Register** | Sistem autentikasi menggunakan **Supabase Auth** (email/password, Google Sign-in). | ✅ |
| **Profil Kesehatan** | Manajemen profil, input data medis dasar, dan personalisasi akun. | ✅ |
| **Notifikasi In-App** | Sistem lonceng untuk pesan pengingat obat, artikel baru, atau janji temu. | ✅ |
| **Integrasi Kontak Darurat** | Fitur cepat untuk menghubungi kontak darurat saat dibutuhkan. | ✅ |

#### 2. 📝 Logika Bisnis & Notifikasi

| Fitur | Deskripsi | Status |
| :--- | :--- | :--- |
| **Pengingat Obat Cerdas** | Jadwal minum obat harian yang dapat disesuaikan dan notifikasi realtime. | ✅ |
| **Database Obat Lokal** | Pencarian cepat dan informasi detail obat dari database internal. | ✅ |
| **Riwayat Konsumsi** | Pencatatan otomatis konsumsi obat dan riwayat kesehatan. | ✅ |
| **Laporan Kepatuhan** | Grafik visualisasi kepatuhan minum obat harian/mingguan. | ✅ |

#### 3. 🌐 Modul Edukasi & Komunitas

| Fitur | Deskripsi | Status |
| :--- | :--- | :--- |
| **Pencarian Artikel & Filter** | Search real-time untuk artikel kesehatan dan filter berdasarkan kategori. | ✅ |
| **Wishlist & Review** | Pengguna dapat menyimpan artikel favorit dan memberi rating + komentar. | ✅ |
| **Papan Diskusi** | Forum sederhana bagi pengguna untuk berbagi tips dan pengalaman. | ✅ |
| **Pagination & Skeleton** | Halaman katalog lebih ringan & estetis (loading halus). | ✅ |

#### 4. ⚙️ Dashboard Admin (Reporting & Control)

| Fitur | Deskripsi | Status |
| :--- | :--- | :--- |
| **Dashboard Analytics** | Grafik distribusi pengguna dan pemakaian fitur (Recharts/Chart.js). | ✅ |
| **Manajemen Database** | Tambah, edit, hapus data obat dan informasi kesehatan. | ✅ |
| **Export Data** | Download laporan pengguna aktif atau data obat (.xlsx). | ✅ |
| **Force Notification** | Admin bisa mengirim notifikasi mendesak ke semua pengguna. | ✅ |

---

### 🎨 Pengalaman Pengguna (UI/UX)

* 🌙 **Dark Mode** dengan penyimpanan `localStorage`
* ✨ Animasi Transisi Halus (Framer Motion)
* 📱 **Responsif Penuh** (Desktop, Tablet, Mobile)
* 🍔 Menu Hamburger untuk tampilan mobile
* ⚡ Interaksi cepat dengan React Hot Toast
* 🖼️ Tampilan modern berbasis **Tailwind CSS**

---

### 🛠️ Tech Stack

| Kategori | Teknologi | Fungsi |
| :--- | :--- | :--- |
| **Frontend** | React.js, Vite | Kerangka aplikasi modern & super cepat |
| **Backend/BaaS** | Supabase (Auth, PostgreSQL, Storage) | Database, autentikasi, penyimpanan file |
| **UI/Animasi** | Tailwind CSS, Framer Motion, Recharts | Styling, animasi, visualisasi data |
| **Utility** | React-Query/SWR, Axios | Manajemen state dan fetching data |

---

### 🚀 Cara Menjalankan Proyek (Lokal)

#### 1. Clone Repository & Instalasi

```bash
git clone [https://github.com/helmirifqiii/Obatin.git](https://github.com/helmirifqiii/Obatin.git)
cd Obatin
npm install
