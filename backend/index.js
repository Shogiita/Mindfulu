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
        // PERINGATAN: Jangan bagikan file ini jika API Key sudah terisi.
        const GEMINI_API_KEY = "AIzaSyA6erWToNDb8G24MJlg9MrEB2d5SNzrvuQ";
        const YOUTUBE_API_KEY = "AIzaSyA4l8O7-EFJ41iqpV1hdq6VRvxo_WrPwjw";

        if (!GEMINI_API_KEY || GEMINI_API_KEY.includes("GANTI")) {
            return res.status(500).json({ message: "Konfigurasi server tidak lengkap: API Key Gemini belum diatur." });
        }
        if (!YOUTUBE_API_KEY || YOUTUBE_API_KEY.includes("GANTI")) {
            return res.status(500).json({ message: "Konfigurasi server tidak lengkap: API Key YouTube belum diatur." });
        }

        // --- Langkah 1: Minta saran dari Gemini (TANPA link video) ---
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

        // --- Perbaikan Logika Parsing JSON ---
        let suggestions;
        try {
            const startIndex = suggestionsText.indexOf('{');
            const endIndex = suggestionsText.lastIndexOf('}');
            if (startIndex === -1 || endIndex === -1) {
                throw new Error("Respons tidak mengandung blok JSON.");
            }
            const jsonString = suggestionsText.substring(startIndex, endIndex + 1);
            suggestions = JSON.parse(jsonString);
        } catch (parseError) {
            console.error("Gagal mem-parse JSON dari Gemini. Teks Mentah:", suggestionsText);
            throw new Error("Respons dari AI tidak dapat diproses (format JSON tidak valid).");
        }

        const { judul, artis } = suggestions.saranMusik;

        // --- Langkah 2: Cari & Verifikasi Video di YouTube ---
        
        // Step 2.1: Cari 5 video teratas, diurutkan berdasarkan jumlah penayangan
        const searchQuery = encodeURIComponent(`${judul} ${artis}`);
        const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&q=${searchQuery}&key=${YOUTUBE_API_KEY}&maxResults=5&type=video&order=viewCount`;
        console.log("Mencari di YouTube dengan URL:", searchUrl); // LOG UNTUK DEBUG
        
        const searchResponse = await fetch(searchUrl);
        let validVideoLink = null;

        if (searchResponse.ok) {
            const searchResult = await searchResponse.json();
            // LOG UNTUK DEBUG: Tampilkan hasil pencarian mentah
            console.log("--- HASIL PENCARIAN YOUTUBE ---");
            console.log(JSON.stringify(searchResult, null, 2));
            console.log("-------------------------------");

            const videoItems = searchResult.items;

            if (videoItems && videoItems.length > 0) {
                // Step 2.2: Kumpulkan semua ID video untuk diperiksa
                const videoIds = videoItems.map(item => item.id.videoId).join(',');
                const videoDetailsUrl = `https://www.googleapis.com/youtube/v3/videos?part=status&id=${videoIds}&key=${YOUTUBE_API_KEY}`;
                
                const detailsResponse = await fetch(videoDetailsUrl);
                if (detailsResponse.ok) {
                    const detailsResult = await detailsResponse.json();
                    // LOG UNTUK DEBUG: Tampilkan detail status video
                    console.log("--- DETAIL STATUS VIDEO ---");
                    console.log(JSON.stringify(detailsResult, null, 2));
                    console.log("---------------------------");
                    
                    // Step 2.3: Cari video pertama yang dapat diputar
                    const playableVideo = detailsResult.items.find(item => 
                        item.status.privacyStatus === 'public' &&
                        item.status.uploadStatus === 'processed'
                    );

                    if (playableVideo) {
                        validVideoLink = `https://www.youtube.com/watch?v=${playableVideo.id}`;
                    }
                }
            }
        } else {
             // LOG UNTUK DEBUG: Tampilkan pesan error jika pencarian gagal
            console.error("Panggilan API Pencarian YouTube GAGAL. Status:", searchResponse.status);
            const errorBody = await searchResponse.text();
            console.error("Badan Error:", errorBody);
        }
        
        if (!validVideoLink) {
             console.warn(`Tidak ditemukan video yang valid untuk "${judul} - ${artis}".`);
        }
        
        suggestions.saranMusik.linkVideo = validVideoLink;


        // --- Langkah 3: Kirim respons gabungan ke user ---
        res.status(200).json({ message: "Saran berhasil didapatkan", suggestions });

    } catch (error) {
        console.error("Suggestions Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server", error: error.message });
    }
});
// npm install node-fetch
// Jika menggunakan Express, fetch sudah global di Node.js v18+

app.post("/suggestions", async (req, res) => {
    try {
        const { mood, reason } = req.body;

        // Debugging: Log mood dan reason yang diterima
        console.log("Received mood:", mood, "reason:", reason);

        if (!mood || !reason) {
            return res.status(400).json({ message: "Mood dan alasan wajib diisi" });
        }

        const prompt = `hari ini saya '${mood}' karena '${reason}', berikan saya dalam format JSON dengan kunci "saranMusikVideo" dan "saranArtikelKegiatan":  1 link video YouTube dan 5 tips kegiatan yang bisa saya lakukan untuk meningkatkan mood saya.`;

        // Debugging: Log prompt yang akan dikirim
        console.log("Generated prompt:", prompt);

        const chatHistory = [{ role: "user", parts: [{ text: prompt }] }];
        const payload = {
            contents: chatHistory,
            generationConfig: {
                responseMimeType: "application/json",
                responseSchema: {
                    type: "OBJECT",
                    properties: {
                        "saranMusikVideo": { "type": "STRING" },
                        "saranArtikelKegiatan": { "type": "STRING" }
                    },
                    required: ["saranMusikVideo", "saranArtikelKegiatan"]
                },
                temperature: 2 // MENAMBAHKAN TEMPERATURE UNTUK VARIASI
            }
        };
        const apiKey = "AIzaSyBIOJZoPowPDi5K0ElQvD2vxiSDTyVo8GY"; // Kunci API Anda (Sebaiknya disimpan di environment variable)

        const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=${apiKey}`;

        const geminiResponse = await fetch(apiUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!geminiResponse.ok) {
            const errorBody = await geminiResponse.text();
            console.error("Gemini API error status:", geminiResponse.status);
            console.error("Gemini API error body:", errorBody);
            return res.status(geminiResponse.status).json({
                message: "Gagal mendapatkan saran dari Gemini API.",
                error: errorBody
            });
        }

        const result = await geminiResponse.json();

        // Debugging: Log respons penuh dari Gemini
        console.log("Full Gemini response:", JSON.stringify(result, null, 2));


        if (result.candidates && result.candidates.length > 0 &&
            result.candidates[0].content && result.candidates[0].content.parts &&
            result.candidates[0].content.parts.length > 0 &&
            result.candidates[0].content.parts[0].text) {

            const suggestionsText = result.candidates[0].content.parts[0].text;
            // Debugging: Log teks JSON sebelum di-parse
            console.log("Suggestions text from Gemini (before parse):", suggestionsText);

            try {
                const suggestions = JSON.parse(suggestionsText);
                res.status(200).json({
                    message: "Saran berhasil didapatkan",
                    suggestions: suggestions
                });
            } catch (parseError) {
                console.error("Gagal mem-parse JSON dari Gemini:", parseError);
                console.error("Teks yang gagal di-parse:", suggestionsText);
                res.status(500).json({ message: "Internal server error: Gagal mem-parse respons dari Gemini API.", error: parseError.message, rawResponse: suggestionsText });
            }

        } else {
            console.warn("Struktur respons Gemini API tidak seperti yang diharapkan atau konten kosong:", JSON.stringify(result, null, 2));
             if (result.promptFeedback && result.promptFeedback.blockReason) {
                 return res.status(500).json({
                    message: `Gagal mendapatkan saran dari Gemini API: ${result.promptFeedback.blockReason}`,
                    details: result.promptFeedback
                });
            }
            res.status(500).json({
                message: "Gagal memproses respons dari Gemini API atau respons kosong.",
                details: "Struktur respons tidak sesuai atau tidak ada kandidat yang valid.",
                fullResponse: result
            });
        }

    } catch (error) {
        console.error("Error di endpoint /mood-suggestions:", error.message, error.stack); // Tambahkan error.stack untuk detail
        if (error instanceof SyntaxError) { // Ini mungkin tidak akan tertangkap di sini jika SyntaxError terjadi di dalam JSON.parse
            res.status(500).json({ message: "Internal server error: Gagal mem-parse.", error: error.message });
        } else {
            res.status(500).json({ message: "Internal server error", error: error.message });
        }
    }
});

startServer();
