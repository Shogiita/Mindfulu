// Mengimpor library yang diperlukan
const express = require("express");
const { Sequelize, Model, DataTypes } = require("sequelize");
const admin = require('firebase-admin');
const fetch = (...args) => import('node-fetch').then(({ default: fetch }) => fetch(...args));

// --- MEMUAT VARIABEL LINGKUNGAN ---
// Memastikan library dotenv diinstal (npm install dotenv)
require('dotenv').config();

// --- INISIALISASI FIREBASE ADMIN ---
// Membaca kredensial dari variabel lingkungan (Base64)
const serviceAccountBase64 = process.env.FIREBASE_SERVICE_ACCOUNT_BASE64;
if (!serviceAccountBase64) {
    console.error("FIREBASE_SERVICE_ACCOUNT_BASE64 tidak ditemukan di file .env");
    process.exit(1);
}

// Mendekode string Base64 kembali ke format JSON
const serviceAccountJson = Buffer.from(serviceAccountBase64, 'base64').toString('ascii');
const serviceAccount = JSON.parse(serviceAccountJson);

// Inisialisasi Firebase Admin SDK dengan service account yang sudah di-parse
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Mendapatkan referensi ke database Firestore
const db = admin.firestore();

const app = express();
// Menggunakan port dari .env atau default 3000
const port = process.env.PORT || 8080;

// Middleware untuk membaca body dari request dalam format JSON
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// --- KONEKSI DATABASE (SEQUELIZE) ---
// Menggunakan konfigurasi database dari file .env
const sequelize = new Sequelize(
    process.env.DB_NAME,
    process.env.DB_USER,
    process.env.DB_PASS,
    {
        host: process.env.DB_HOST,
        port: process.env.DB_PORT,
        dialect: "mysql",
        logging: console.log,
    }
);

// --- DEFINISI MODEL (SEQUELIZE) ---
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

// Endpoint untuk menangani POST request ke /mood menggunakan Firestore
app.post("/mood", async (req, res) => {
    try {
        const { mood, reason, email } = req.body;
        if (!mood || !reason || !email) {
            return res.status(400).json({ message: "Mood, alasan, dan email wajib diisi" });
        }
        const today = new Date();
        const moodData = {
            email: email,
            mood: mood,
            reason: reason,
            date: admin.firestore.Timestamp.fromDate(today) 
        };
        const docRef = await db.collection('moods').add(moodData);
        const responseMood = {
            id: docRef.id,
            email: moodData.email,
            mood: moodData.mood,
            reason: moodData.reason,
            date: moodData.date.toMillis()
        };
        res.status(201).json({ message: "Mood berhasil dicatat di Firestore", mood: responseMood });
    } catch (error) {
        console.error("Firestore Mood Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server saat menyimpan mood ke Firestore" });
    }
});

// Endpoint untuk mendapatkan riwayat mood dari Firestore berdasarkan email
app.get("/mood", async (req, res) => {
    try {
        const userEmail = req.query.email;
        if (!userEmail) {
            return res.status(400).json({ message: "Parameter email wajib diisi" });
        }

        const moodsRef = db.collection('moods');
        const snapshot = await moodsRef.where('email', '==', userEmail).orderBy('date', 'desc').get();

        if (snapshot.empty) {
            return res.status(200).json([]);
        }

        const moodHistory = snapshot.docs.map(doc => {
            const data = doc.data();
            return {
                id: doc.id,
                email: data.email,
                mood: data.mood,
                reason: data.reason,
                date: data.date.toMillis() 
            };
        });

        res.status(200).json(moodHistory);

    } catch (error) {
        console.error("Get Mood History Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server saat mengambil riwayat mood" });
    }
});


// Endpoint /suggestions yang telah diperbarui untuk membaca API Key dari .env
app.post("/suggestions", async (req, res) => {
    try {
        const { mood, reason } = req.body;
        if (!mood || !reason) {
            return res.status(400).json({ message: "Mood dan alasan wajib diisi" });
        }

        // Mengambil API Key dari environment variables
        const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
        const YOUTUBE_API_KEY = process.env.YOUTUBE_API_KEY;

        if (!GEMINI_API_KEY || !YOUTUBE_API_KEY) {
            return res.status(500).json({ message: "API Key belum dikonfigurasi di file .env" });
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

        if (!geminiResponse.ok) throw new Error(`Gagal mendapatkan saran dari Gemini API. Status: ${geminiResponse.status}`);

        const geminiResult = await geminiResponse.json();
        const suggestionsText = geminiResult.candidates?.[0]?.content?.parts?.[0]?.text;

        if (!suggestionsText) throw new Error("Respons dari Gemini API kosong atau tidak valid.");

        let geminiSuggestions;
        try {
            const jsonString = suggestionsText.substring(suggestionsText.indexOf('{'), suggestionsText.lastIndexOf('}') + 1);
            geminiSuggestions = JSON.parse(jsonString);
        } catch (parseError) {
            console.error("Gagal mem-parse JSON dari Gemini. Teks Mentah:", suggestionsText);
            throw new Error("Respons dari AI tidak dapat diproses (format JSON tidak valid).");
        }
        
        if (!geminiSuggestions || !geminiSuggestions.saranMusik || !geminiSuggestions.saranKegiatan) {
            throw new Error("Struktur JSON dari Gemini API tidak sesuai harapan.");
        }

        const { judul, artis, alasan } = geminiSuggestions.saranMusik;
        const kegiatan = geminiSuggestions.saranKegiatan;

        let validVideoLink = null;
        const searchQuery = encodeURIComponent(`${judul} ${artis}`);
        const searchUrl = `https://www.googleapis.com/youtube/v3/search?part=snippet&q=${searchQuery}&key=${YOUTUBE_API_KEY}&maxResults=1&type=video&videoEmbeddable=true&order=relevance`;
        const searchResponse = await fetch(searchUrl);
        if (searchResponse.ok) {
            const searchResult = await searchResponse.json();
            if (searchResult.items && searchResult.items.length > 0) {
                const videoId = searchResult.items[0].id.videoId;
                validVideoLink = `https://www.youtube.com/embed/${videoId}`;
            }
        }

        const finalSuggestions = {
            saranMusik: {
                judul: judul || "Tidak ada judul",
                artis: artis || "Tidak ada artis",
                alasan: alasan || "Tidak ada alasan",
                linkVideo: validVideoLink
            },
            saranKegiatan: kegiatan || []
        };

        res.status(200).json({ message: "Saran berhasil didapatkan", suggestions: finalSuggestions });

    } catch (error) {
        console.error("Suggestions Error:", error);
        res.status(500).json({ message: "Terjadi kesalahan pada server", error: error.message });
    }
});


const startServer = async () => {
    try {
        await sequelize.authenticate(); // Menggunakan authenticate untuk tes koneksi
        console.log("Koneksi ke database MySQL berhasil.");
        
        await sequelize.sync({ force: false });
        console.log("Model telah disinkronkan dengan database.");
        
        app.listen(port, () => console.log(`Server berjalan di http://localhost:${port}, terhubung ke Firestore dan MySQL.`));
    } catch (error) {
        console.error("Tidak dapat terhubung ke database MySQL:", error);
        // Tetap jalankan server jika hanya koneksi MySQL yang gagal
        app.listen(port, () => console.log(`Server berjalan di http://localhost:${port}, koneksi MySQL gagal tetapi Firestore siap.`));
    }
};

startServer();
