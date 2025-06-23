const express = require("express");
const { Sequelize, Model, DataTypes } = require("sequelize");
const fetch = (...args) => import('node-fetch').then(({ default: fetch }) => fetch(...args));

const app = express();
const port = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

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
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."},
                {"kegiatan": "...", "deskripsi": "..."}
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
            suggestions = JSON.parse(suggestionsText);
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

const startServer = async () => {
    app.listen(port, () => console.log(`Server berjalan di http://localhost:${port}`));
};

startServer();
