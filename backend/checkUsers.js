const mongoose = require("mongoose");

mongoose.connect("mongodb+srv://akshaykumarshaw44_db_user:Pushpagupta123@cluster0.ebqfkkl.mongodb.net/acadtrack?retryWrites=true&w=majority");

const User = require("./models/User");

async function checkUsers() {
    const users = await User.find({}).select("+password");

    console.log(users);

    process.exit();
}

checkUsers();