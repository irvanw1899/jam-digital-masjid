# Jam Digital Masjid — WebView App (Android TV)

App WebView native pengganti Fully Kiosk Browser. Kelebihan utamanya
dibanding browser/kiosk app pihak ketiga: kode ini punya
`onRenderProcessGone()` yang mendeteksi kalau proses render WebView
benar-benar crash/freeze, lalu otomatis membuat ulang WebView-nya —
inilah akar penyebab "hang" yang tidak bisa dibereskan browser biasa.

## Yang sudah disiapkan di sini
- `MainActivity.kt` — WebView fullscreen, keep-screen-on, auto-recover saat
  render crash, dan reload otomatis terjadwal jam 03:30 (jaring pengaman
  tambahan, bukan pengganti recovery crash).
- `BootReceiver.kt` — auto-buka app begitu TV selesai boot.
- `AndroidManifest.xml` — sudah didaftarkan agar muncul di home launcher
  Android TV (kategori `LEANBACK_LAUNCHER`) + banner TV.
- Ikon launcher & banner TV (gaya sama seperti web app-nya).

## Langkah pemasangan (dari nol, di Android Studio)

1. **Buat project baru**
   Android Studio → New Project → pilih template **"No Activity"** (bukan
   Empty Views Activity, supaya tidak ada file bawaan yang bentrok) →
   Language: **Kotlin** → Package name: `com.masjid.jamdigital` →
   Minimum SDK: **API 23 (Android 6.0)** cukup untuk hampir semua Android TV
   box. Klik Finish dan tunggu Gradle sync pertama selesai.

2. **Tambahkan MainActivity.kt & BootReceiver.kt**
   Copy dua file itu ke `app/src/main/java/com/masjid/jamdigital/`
   (path package harus sama persis).

3. **Ganti AndroidManifest.xml**
   Replace isi `app/src/main/AndroidManifest.xml` dengan punya saya, atau
   gabungkan kalau template sudah menghasilkan isi lain.

4. **Salin resource**
   - `themes.xml` dan `strings.xml` → `app/src/main/res/values/`
   - Semua folder `mipmap-*` → `app/src/main/res/`
   - `drawable/banner.png` → `app/src/main/res/drawable/`

5. **Pastikan dependency AppCompat ada**
   Di `app/build.gradle.kts` (atau `.gradle`), pastikan ada baris:
   ```
   implementation("androidx.appcompat:appcompat:1.7.0")
   ```
   (Android Studio biasanya sudah menambahkannya otomatis untuk project
   Kotlin baru; kalau belum, tambahkan lalu Sync Gradle.)

6. **Sesuaikan URL**
   Di `MainActivity.kt`, cek konstanta `HOME_URL` — sudah saya isi dengan
   link jam masjid kamu (`?masjid=basmatuiliman-001`). Ganti kalau perlu.

7. **Build & jalankan**
   Hubungkan Android TV lewat ADB (`adb connect <ip-tv>:5555`) atau lewat
   kabel, lalu klik Run ▶ di Android Studio. Atau build APK release
   (Build → Generate Signed Bundle/APK) dan sideload manual pakai app
   "Downloader" / `adb install`.

8. **Izinkan auto-start setelah boot**
   Di beberapa TV box (terutama merek Xiaomi/Android TV custom), fitur
   "start on boot" perlu diizinkan manual di Settings → Apps → Jam Digital
   Masjid → izinkan "Autostart"/"Start on boot". Ini di luar kendali app,
   tergantung ROM box masing-masing.

## Kenapa ini bisa lebih stabil dari Fully Kiosk
- Tidak ada lapisan app pihak ketiga dengan fitur tambahan (iklan, remote
  admin, dsb.) yang ikut memakai memori.
- `onRenderProcessGone` = pemulihan otomatis saat proses render benar-benar
  crash — sesuatu yang browser/kiosk app biasa tidak lakukan untuk kamu.
- Kamu kontrol penuh: gampang menambah watchdog lain nanti (mis. cek
  koneksi internet, restart WebView tiap sekian jam, dsb.).

## Yang TIDAK bisa dibereskan oleh app ini
Kalau yang terjadi adalah **OS Android TV-nya sendiri** yang freeze (bukan
cuma WebView), tidak ada app yang bisa menyelamatkan dirinya sendiri dari
itu. Untuk itu tetap disarankan reboot terjadwal di level device (lihat
saran sebelumnya: scheduled reboot bawaan TV box, atau smart plug timer).
