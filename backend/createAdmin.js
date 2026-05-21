const mongoose = require("mongoose");
const dotenv = require("dotenv");

dotenv.config();

const User = require("./models/User");

mongoose.connect(process.env.DATABASE_URL);

async function createAdmin() {

    // delete old admin first
    await User.deleteOne({ email: "admin@gmail.com" });

    const admin = new User({
        name: "Admin",
        email: "admin@gmail.com",
        password: "admin123",
        role: "admin"
    });

    await admin.save();

    console.log("Admin recreated successfully!");

    process.exit();
}

createAdmin();