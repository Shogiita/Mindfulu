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



// Start server
app.listen(port, () => console.log(`App listening on port ${port}`));
