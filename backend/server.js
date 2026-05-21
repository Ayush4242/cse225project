const mongoose = require('mongoose');
const dotenv = require('dotenv');
const app = require('./app');

// Load env vars
dotenv.config();

const DB = process.env.DATABASE_URL || 'mongodb+srv://akshaykumarshaw44_db_user:Pushpagupta123@cluster0.ebqfkkl.mongodb.net/acadtrack?retryWrites=true&w=majority';

mongoose.connect(DB)
    .then(() => console.log('DB connection successful!'))
    .catch(err => console.error('DB connection error:', err));

const port = process.env.PORT || 5000;
const server = app.listen(port, "0.0.0.0", () => {
    console.log(`App running on port ${port}...`);
});
// Handle unhandled rejections
process.on('unhandledRejection', err => {
    console.log('UNHANDLED REJECTION! 💥 Shutting down...');
    console.log(err.name, err.message);
    server.close(() => {
        process.exit(1);
    });
});
