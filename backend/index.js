const express = require("express")
const app = express()
app.use(express.json())
app.use(express.urlencoded({extended:true}))
const sequelize = require("sequelize")
const {QueryTypes} = require("sequelize")
const sequelize = new Sequelize("","root","",{
    host: "localhost",
    port: 3306,
    dialect: "mysql",
    logging: false,
})

