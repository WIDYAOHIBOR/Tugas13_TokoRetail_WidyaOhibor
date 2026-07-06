import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Aplikasi CLI Sederhana - Toko Retail
 * -------------------------------------
 * Menu:
 *   1. Tampil Semua Data
 *   2. Tambah Data
 *   3. Cari Data
 *   4. Ubah Data
 *   5. Hapus Data
 *   0. Keluar
 *
 * Database : toko_retail (MySQL)
 * Tabel    : barang (kode, nama_barang, harga, stok)
 *
 * PERSIAPAN SEBELUM MENJALANKAN:
 * 1. Jalankan script toko_retail.sql di MySQL (lewat MySQL Workbench / phpMyAdmin / cmd).
 * 2. Download MySQL Connector/J (file .jar) dari https://dev.mysql.com/downloads/connector/j/
 * 3. Sesuaikan variabel URL, USER, PASSWORD di bawah dengan konfigurasi MySQL kamu.
 * 4. Compile & jalankan dengan menyertakan connector jar di classpath, contoh:
 *
 *    javac TokoRetailCLI.java
 *    java -cp .;mysql-connector-j-9.x.x.jar TokoRetailCLI      (Windows)
 *    java -cp .:mysql-connector-j-9.x.x.jar TokoRetailCLI      (Mac/Linux)
 */
public class TokoRetailCLI {

    // ==== Konfigurasi koneksi database, sesuaikan jika perlu ====
    private static final String URL = "jdbc:mysql://localhost:3306/toko_retail?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
   private static final String PASSWORD = "admin123"; // isi sesuai password MySQL kamu

    private static Connection conn;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Berhasil terhubung ke database toko_retail.\n");
        } catch (SQLException e) {
            System.out.println("Gagal terhubung ke database: " + e.getMessage());
            return;
        }

        boolean jalan = true;
        while (jalan) {
            tampilkanMenu();
            System.out.print("Pilihan : ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1":
                    tampilSemuaData();
                    break;
                case "2":
                    tambahData();
                    break;
                case "3":
                    cariData();
                    break;
                case "4":
                    ubahData();
                    break;
                case "5":
                    hapusData();
                    break;
                case "0":
                    jalan = false;
                    System.out.println("Terima kasih, program selesai.");
                    break;
                default:
                    System.out.println("Pilihan tidak valid, coba lagi.\n");
            }
        }

        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            // abaikan
        }
        scanner.close();
    }

    private static void tampilkanMenu() {
        System.out.println("=======================================");
        System.out.println("           MENU TOKO RETAIL");
        System.out.println("=======================================");
        System.out.println("1. Tampil Semua Data");
        System.out.println("2. Tambah Data");
        System.out.println("3. Cari Data");
        System.out.println("4. Ubah Data");
        System.out.println("5. Hapus Data");
        System.out.println("0. Keluar");
        System.out.println("=======================================");
    }

    private static void tampilSemuaData() {
        String sql = "SELECT * FROM barang ORDER BY kode";
        System.out.println("\n=====================================================");
        System.out.println("              DAFTAR BARANG TOKO RETAIL");
        System.out.println("=====================================================");
        System.out.printf("%-3s %-8s %-20s %-10s %-6s%n", "#", "Kode", "Nama Barang", "Harga", "Stok");

        int no = 1;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%-3d %-8s %-20s %-10d %-6d%n",
                        no++,
                        rs.getString("kode"),
                        rs.getString("nama_barang"),
                        rs.getInt("harga"),
                        rs.getInt("stok"));
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat mengambil data: " + e.getMessage());
        }

        System.out.println("=====================================================");
        System.out.println("Total: " + (no - 1) + " barang\n");
    }

    private static void tambahData() {
        System.out.println("\n--- Tambah Data Barang ---");
        System.out.print("Kode         : ");
        String kode = scanner.nextLine().trim();
        System.out.print("Nama Barang  : ");
        String nama = scanner.nextLine().trim();
        System.out.print("Harga        : ");
        int harga = bacaAngka();
        System.out.print("Stok         : ");
        int stok = bacaAngka();

        String sql = "INSERT INTO barang (kode, nama_barang, harga, stok) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            ps.setString(2, nama);
            ps.setInt(3, harga);
            ps.setInt(4, stok);
            ps.executeUpdate();
            System.out.println("Data berhasil ditambahkan.\n");
        } catch (SQLException e) {
            System.out.println("Gagal menambah data: " + e.getMessage() + "\n");
        }
    }

    private static void cariData() {
        System.out.println("\n--- Cari Data Barang ---");
        System.out.print("Masukkan kode atau nama barang : ");
        String keyword = scanner.nextLine().trim();

        String sql = "SELECT * FROM barang WHERE kode = ? OR nama_barang LIKE ?";
        System.out.printf("%-8s %-20s %-10s %-6s%n", "Kode", "Nama Barang", "Harga", "Stok");
        boolean ada = false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ada = true;
                    System.out.printf("%-8s %-20s %-10d %-6d%n",
                            rs.getString("kode"),
                            rs.getString("nama_barang"),
                            rs.getInt("harga"),
                            rs.getInt("stok"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal mencari data: " + e.getMessage());
        }

        if (!ada) {
            System.out.println("Data tidak ditemukan.");
        }
        System.out.println();
    }

    private static void ubahData() {
        System.out.println("\n--- Ubah Data Barang ---");
        System.out.print("Masukkan kode barang yang ingin diubah : ");
        String kode = scanner.nextLine().trim();

        if (!dataAda(kode)) {
            System.out.println("Data dengan kode tersebut tidak ditemukan.\n");
            return;
        }

        System.out.print("Nama Barang baru : ");
        String nama = scanner.nextLine().trim();
        System.out.print("Harga baru       : ");
        int harga = bacaAngka();
        System.out.print("Stok baru        : ");
        int stok = bacaAngka();

        String sql = "UPDATE barang SET nama_barang = ?, harga = ?, stok = ? WHERE kode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setInt(2, harga);
            ps.setInt(3, stok);
            ps.setString(4, kode);
            int jumlah = ps.executeUpdate();
            if (jumlah > 0) {
                System.out.println("Data berhasil diubah.\n");
            } else {
                System.out.println("Data tidak ditemukan.\n");
            }
        } catch (SQLException e) {
            System.out.println("Gagal mengubah data: " + e.getMessage() + "\n");
        }
    }

    private static void hapusData() {
        System.out.println("\n--- Hapus Data Barang ---");
        System.out.print("Masukkan kode barang yang ingin dihapus : ");
        String kode = scanner.nextLine().trim();

        if (!dataAda(kode)) {
            System.out.println("Data dengan kode tersebut tidak ditemukan.\n");
            return;
        }

        System.out.print("Yakin ingin menghapus data ini? (y/n) : ");
        String konfirmasi = scanner.nextLine().trim();
        if (!konfirmasi.equalsIgnoreCase("y")) {
            System.out.println("Penghapusan dibatalkan.\n");
            return;
        }

        String sql = "DELETE FROM barang WHERE kode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            int jumlah = ps.executeUpdate();
            if (jumlah > 0) {
                System.out.println("Data berhasil dihapus.\n");
            } else {
                System.out.println("Data tidak ditemukan.\n");
            }
        } catch (SQLException e) {
            System.out.println("Gagal menghapus data: " + e.getMessage() + "\n");
        }
    }

    private static boolean dataAda(String kode) {
        String sql = "SELECT kode FROM barang WHERE kode = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
            return false;
        }
    }

    private static int bacaAngka() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Masukkan angka yang valid : ");
            }
        }
    }
}
