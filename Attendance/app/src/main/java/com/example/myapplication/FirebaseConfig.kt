package com.example.myapplication

/**
 * Firebase Realtime Database 설정 — 모든 RTDB 접근의 단일 진실원천.
 *
 * 이 BASE_URL을 바꾸면 다음이 모두 새 프로젝트로 전환됨:
 *   - FirebaseClient (그쪽 화면 11곳: 로그인/시간표/출석 조회 등)
 *   - RtdbScheduleSyncManager (내 시간표 12h 동기화)
 *   - FirebaseSeedData (첫 실행 시드 주입)
 *
 * 현재: mobile-cb29c (개발/테스트용)
 * 향후: attendanceapp-cbf00 (그쪽 팀 공유 RTDB)로 전환 예정 — 이 한 줄만 수정.
 *
 * 주의: trailing slash 없음. FirebaseClient가 "${BASE_URL}/${path}.json" 형식으로 붙임.
 */
object FirebaseConfig {
    const val BASE_URL = "https://mobile-cb29c-default-rtdb.asia-southeast1.firebasedatabase.app"
}