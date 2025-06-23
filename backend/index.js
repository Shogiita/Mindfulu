// Mengimpor library yang diperlukan
const express = require("express");
const { Sequelize, Model, DataTypes } = require("sequelize");
// [FIX] Mengimpor 'node-fetch' yang diperlukan untuk panggilan API
const fetch = (...args) => import('node-fetch').then(({ default: fetch }) => fetch(...args));

const app = express();
const port = 3000;

// Middleware untuk membaca body dari request dalam format JSON
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// --- KONEKSI DATABASE (SEQUELIZE) ---
// Pastikan detail ini sesuai dengan konfigurasi database Anda
const sequelize = new Sequelize(
    "mdpceria", // Nama Database
    "root",      // User
    "",          // Password
    {
        host: "127.0.0.1", 
        port: 3306,
        dialect: "mysql",
        logging: console.log, // Aktifkan logging untuk melihat query SQL di konsol
    }
);

// --- DEFINISI MODEL ---
class Moods extends Model {}
Moods.init({
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    mood: { type: DataTypes.STRING, allowNull: false },
    date: { type: DataTypes.DATE, allowNull: false },
    reason: { type: DataTypes.STRING, allowNull: false }
}, {
    sequelize,
    modelName: 'Moods',
    tableName: 'moods',
    timestamps: false
});


// --- ENDPOINTS (RUTE) ---

// Endpoint untuk menangani POST request ke /mood
app.post("/mood", async (req, res) => {
    try {
        const { mood, reason } = req.body;
        if (!mood || !reason) {
            return res.status(400).json({ message: "Mood dan alasan wajib diisi" });
        }
        const today = new Date();
        const date = today.toISOString().split('T')[0];
        const newMoodRecord = await Moods.create({ mood, date, reason });

        const responseMood = {
            id: newMoodRecord.id,
            mood: newMoodRecord.mood,
            reason: newMoodRecord.reason,
            date: new Date(newMoodRecord.date).getTime()
        };

        res.status(201).json({ message: "Mood berhasil dicatat", mood: responseMood });
    } catch (error) {
        console.error("Mood Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server saat menyimpan mood" });
    }
});


// [FIX] Menggabungkan kedua endpoint /suggestions menjadi satu yang berfungsi
app.post("/suggestions", async (req, res) => {
    try {
        const { mood, reason } = req.body;
        if (!mood || !reason) {
            return res.status(400).json({ message: "Mood dan alasan wajib diisi" });
        }

        const GEMINI_API_KEY = "AIzaSyA6erWToNDb8G24MJlg9MrEB2d5SNzrvuQ";
        const YOUTUBE_API_KEY = "AIzaSyA4l8O7-EFJ41iqpV1hdq6VRvxo_WrPwjw";

        if (!GEMINI_API_KEY || !YOUTUBE_API_KEY) {
            return res.status(500).json({ message: "API Key belum dikonfigurasi." });
        }

        const timestamp = new Date().toISOString();
        const prompt = `
            Sebagai seorang teman AI yang suportif, tolong berikan saran untuk seseorang yang merasa '${mood}' karena '${reason}'.
            Permintaan ini dibuat pada: ${timestamp}.
            Berikan respons dalam format JSON yang valid dan unik. Jangan sertakan link video.
            Struktur JSON:
            {
              "saranMusik": {
                "judul": "Judul Lagu yang Spesifik dan Relevan",
                "artis": "Nama Artis",
                "alasan": "Penjelasan singkat mengapa lagu ini cocok dengan mood tersebut."
              },
              "saranKegiatan": [
                {"kegiatan": "Mendengarkan podcast", "deskripsi": "Cari podcast komedi untuk mengubah suasana hati."},
                {"kegiatan": "Jalan santai", "deskripsi": "Berjalan di taman selama 15 menit untuk menjernihkan pikiran."},
                {"kegiatan": "Menulis jurnal", "deskripsi": "Tuliskan apa yang kamu rasakan tanpa dihakimi."},
                {"kegiatan": "Menonton film", "deskripsi": "Pilih genre film favoritmu untuk ditonton."},
                {"kegiatan": "Meditasi singkat", "deskripsi": "Lakukan meditasi pernapasan selama 5 menit."}
              ]
            }
            Catatan Penting: Hindari saran yang terlalu umum. Jadilah spesifik dan berikan ide-ide yang segar.
        `;

        const geminiPayload = {
            contents: [{ role: "user", parts: [{ text: prompt }] }],
            generationConfig: { responseMimeType: "application/json", temperature: 1.0 }
        };

        const geminiApiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=${GEMINI_API_KEY}`;
        const geminiResponse = await fetch(geminiApiUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(geminiPayload)
        });

        if (!geminiResponse.ok) throw new Error("Gagal mendapatkan saran dari Gemini API.");

        const geminiResult = await geminiResponse.json();
        const suggestionsText = geminiResult.candidates?.[0]?.content?.parts?.[0]?.text;

        if (!suggestionsText) throw new Error("Respons dari Gemini API kosong atau tidak valid.");

        let suggestions;
        try {
            const jsonString = suggestionsText.substring(suggestionsText.indexOf('{'), suggestionsText.lastIndexOf('}') + 1);
            suggestions = JSON.parse(jsonString);
        } catch (parseError) {
            console.error("Gagal mem-parse JSON dari Gemini. Teks Mentah:", suggestionsText);
            throw new Error("Respons dari AI tidak dapat diproses (format JSON tidak valid).");
        }

        const { judul, artis } = suggestions.saranMusik;
        let validVideoLink = null;
        const searchQuery = encodeURIComponent(`${judul} ${artis}`);
        const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&q=${searchQuery}&key=${YOUTUBE_API_KEY}&maxResults=1&type=video&videoEmbeddable=true&order=relevance`;

        console.log("Mencari di YouTube dengan URL:", searchUrl);

        const searchResponse = await fetch(searchUrl);
        if (searchResponse.ok) {
            const searchResult = await searchResponse.json();
            if (searchResult.items && searchResult.items.length > 0) {
                const videoId = searchResult.items[0].id.videoId;
                validVideoLink = `https://www.youtube.com/watch?v=${videoId}`;
                console.log(`Video ditemukan: ${validVideoLink}`);
            } else {
                console.warn(`Tidak ditemukan video yang cocok untuk "${judul} - ${artis}".`);
            }
        } else {
            console.error("Panggilan API YouTube gagal:", searchResponse.status);
            const errorBody = await searchResponse.text();
            console.error("Isi Error:", errorBody);
        }

        suggestions.saranMusik.linkVideo = validVideoLink;

        res.status(200).json({ message: "Saran berhasil didapatkan", suggestions });

    } catch (error) {
        console.error("Suggestions Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server", error: error.message });
    }
});

// [FIX] Definisi fungsi startServer yang hilang, sekarang ditambahkan
const startServer = async () => {
    try {
        await sequelize.sync({ force: false });
        console.log("Koneksi database berhasil dan model telah disinkronkan.");
        app.listen(port, () => console.log(`Server berjalan di http://localhost:${port}`));
    } catch (error) {
        console.error("Tidak dapat terhubung ke database:", error);
    }
};

// [FIX] Memanggil fungsi startServer di akhir
startServer();
