const admin = require('firebase-admin');
const serviceAccount = require("./mobile-cb29c-firebase-adminsdk-fbsvc-fab75eea2b.json");

// databaseURL은 admin SDK 키와 같은 프로젝트여야 함 (다른 프로젝트면 PERMISSION_DENIED).
// 현재 admin 키는 mobile-cb29c용이라 databaseURL도 mobile-cb29c.
// 향후 attendanceapp-cbf00 admin 키 권한 받으면 키 + 이 URL 둘 다 attendanceapp-cbf00로 교체.
// 그동안 Android는 attendanceapp-cbf00 RTDB를 보고 서버는 mobile-cb29c RTDB를 봄 (데이터 분리).
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://mobile-cb29c-default-rtdb.asia-southeast1.firebasedatabase.app"
});

const db = admin.firestore();
const rtdb = admin.database();

module.exports = {admin, db, rtdb};