const express = require("express");
const app = express();
const port = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const { Sequelize, Model, DataTypes, QueryTypes } = require("sequelize");

// Inisialisasi koneksi Sequelize
const sequelize = new Sequelize("mdpjancok", "root", "", {
    host: "localhost",
    port: 3306,
    dialect: "mysql",
    logging: false,
});

// Definisi model Users
class Users extends Model {}
Users.init({
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    username: DataTypes.STRING,
    name: DataTypes.STRING,
    email: DataTypes.STRING,
    password: DataTypes.STRING
}, {
    sequelize,
    modelName: 'users',
    tableName: 'users',
    timestamps: false
});

class Moods extends Model {}
Moods.init({
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    mood: DataTypes.STRING,
    date: DataTypes.DATE,
    reason: DataTypes.STRING
}, {
    sequelize,
    modelName: 'moods',
    tableName: 'moods',
    timestamps: false
});

// Endpoint register
app.post("/register", async (req, res) => {
    try {
        const { username, name, email, password, cpassword } = req.body;

        if (!username || !name || !email || !password || !cpassword) {
            return res.status(400).json({ message: "All fields are required" });
        }

        if (password !== cpassword) {
            return res.status(400).json({ message: "Passwords do not match" });
        }

        const existingUser = await Users.findOne({ where: { username } });
        if (existingUser) {
            return res.status(400).json({ message: "Username already registered" });
        }

        const user = await Users.create({ username, name, email, password });
        res.status(201).json({ message: "User registered successfully", user });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Internal server error" });
    }
});

// Endpoint login
app.post("/login", async (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({ message: "Username and password are required" });
        }

        const user = await Users.findOne({ where: { username, password } });
        if (!user) {
            return res.status(401).json({ message: "Invalid username or password" });
        }

        res.status(200).json({ message: "Login successful", user });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Internal server error" });
    }
});

// Endpoint mood
app.post("/mood", async (req, res) => {
    try {
        const { mood, reason } = req.body;

        if (!mood || !reason) {
            return res.status(400).json({ message: "All fields are required" });
        }

        const today = new Date();
        const date = today.toISOString().split('T')[0]; // Format YYYY-MM-DD

        const newMood = await Moods.create({ mood, date, reason });
        res.status(201).json({ message: "Mood recorded successfully", mood: newMood });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Internal server error" });
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

// Start server
app.listen(port, () => console.log(`App listening on port ${port}`));
