const admin = require('firebase-admin');
const serviceAccount = require("./mobile-cb29c-firebase-adminsdk-fbsvc-fab75eea2b.json");

// databaseURL 명시 — 한 프로젝트에 Firestore + RTDB 둘 다 사용하므로 RTDB 위치 알려줘야 함.
// 클라이언트의 FirebaseConfig.BASE_URL과 동일한 값.
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://mobile-cb29c-default-rtdb.asia-southeast1.firebasedatabase.app"
});

const db = admin.firestore();
const rtdb = admin.database();

module.exports = {admin, db, rtdb};