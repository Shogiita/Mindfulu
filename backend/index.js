// Import library yang diperlukan
const express = require("express");
const { Sequelize, Model, DataTypes } = require("sequelize");

const app = express();
const port = 3000; // Port diatur langsung

// Middleware untuk parsing body dari request
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// --- KONEKSI DATABASE (SEQUELIZE) ---
const sequelize = new Sequelize(
    "mdpjancok", 
    "root", 
    "", 
    {
        host: "localhost",
        port: 3306,
        dialect: "mysql",
        logging: false,
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

// --- ENDPOINTS (ROUTES) ---

// Endpoint untuk mencatat mood secara anonim
app.post("/mood", async (req, res) => {
    try {
        const { mood, reason } = req.body;
        if (!mood || !reason) {
            return res.status(400).json({ message: "Mood dan alasan wajib diisi" });
        }
        const today = new Date();
        const date = today.toISOString().split('T')[0];
        const newMood = await Moods.create({ mood, date, reason });
        res.status(201).json({ message: "Mood berhasil dicatat", mood: newMood });
    } catch (error) {
        console.error("Mood Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server" });
    }
});

// Endpoint untuk mendapatkan saran dari Gemini DAN mencari video di YouTube
app.post("/suggestions", async (req, res) => {
    try {
        const { mood, reason } = req.body;
        if (!mood || !reason) {
            return res.status(400).json({ message: "Mood dan alasan wajib diisi" });
        }
        
        // --- API KEYS ---
        // Cukup letakkan API Key Anda langsung di sini.
        const GEMINI_API_KEY = "AIzaSyA6erWToNDb8G24MJlg9MrEB2d5SNzrvuQ"; // <-- PASTIKAN INI KEY ANDA
        const YOUTUBE_API_KEY = "AIzaSyA4l8O7-EFJ41iqpV1hdq6VRvxo_WrPwjw"; // <-- PASTIKAN INI KEY ANDA

        // Pengecekan sederhana jika key kosong
        if (!GEMINI_API_KEY) {
            return res.status(500).json({ message: "Konfigurasi server tidak lengkap: API Key Gemini belum diatur." });
        }
        if (!YOUTUBE_API_KEY) {
            return res.status(500).json({ message: "Konfigurasi server tidak lengkap: API Key YouTube belum diatur." });
        }

        // --- Langkah 1: Minta saran dari Gemini ---
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
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."}
              ]
            }
            Catatan Penting: Hindari saran yang terlalu umum. Jadilah spesifik dan berikan ide-ide yang segar.`;

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
            suggestions = JSON.parse(suggestionsText);
        } catch (parseError) {
            console.error("Gagal mem-parse JSON dari Gemini. Teks Mentah:", suggestionsText);
            throw new Error("Respons dari AI tidak dapat diproses (format JSON tidak valid).");
        }

        const { judul, artis } = suggestions.saranMusik;

        // --- Langkah 2: Cari Video di YouTube (Versi Efisien dan Benar) ---
        let validVideoLink = null;
        const searchQuery = encodeURIComponent(`${judul} ${artis}`);
        
        // Menggunakan 1 API call yang efisien untuk mencari video publik yang bisa diputar
        const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&q=${searchQuery}&key=${YOUTUBE_API_KEY}&maxResults=1&type=video&videoEmbeddable=true&order=relevance`;

        console.log("Mencari di YouTube dengan URL:", searchUrl);
        
        const searchResponse = await fetch(searchUrl);

        if (searchResponse.ok) {
            const searchResult = await searchResponse.json();
            if (searchResult.items && searchResult.items.length > 0) {
                const videoId = searchResult.items[0].id.videoId;
                
                // INILAH PERBAIKAN UTAMANYA
                // 1. Menggunakan backtick `...` untuk bisa memasukkan variabel ${videoId}
                // 2. Menggunakan format link YouTube yang benar
                validVideoLink = `https://www.youtube.com/watch?v=${videoId}`;

                console.log(`Video ditemukan: ${validVideoLink}`);
            } else {
                console.warn(`Tidak ditemukan video yang dapat diputar untuk "${judul} - ${artis}".`);
            }
        } else {
            console.error("Panggilan API Pencarian YouTube GAGAL. Status:", searchResponse.status);
            const errorBody = await searchResponse.text();
            console.error("Badan Error:", errorBody);
        }
        
        suggestions.saranMusik.linkVideo = validVideoLink;

        // --- Langkah 3: Kirim respons gabungan ke user ---
        res.status(200).json({ message: "Saran berhasil didapatkan", suggestions });

    } catch (error) {
        console.error("Suggestions Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server", error: error.message });
    }
});


// Sinkronisasi model dengan database dan jalankan server
const startServer = async () => {
    try {
        await sequelize.authenticate();
        console.log('Koneksi ke database berhasil.');
        await sequelize.sync({ alter: true }); 
        console.log('Semua model berhasil disinkronkan.');
        app.listen(port, () => console.log(`Server berjalan di http://localhost:${port}`));
    } catch (error) {
        console.error('Tidak dapat terhubung ke database:', error);
    }
};

startServer();
