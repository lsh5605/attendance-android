package com.example.myapplication

import org.json.JSONArray
import org.json.JSONObject

/**
 * Firebase 임시 데이터.
 * 앱 실행 시 테스트 노드 자동 생성 시도.
 */
object FirebaseSeedData {

    fun seedIfAllowed() {
        FirebaseClient.patch("", rootData())
    }

    private fun rootData(): JSONObject {
        return JSONObject()
            .put("users", users())
            .put("courses", courses())
            .put("studentSchedules", studentSchedules())
            .put("currentClasses", currentClasses())
            .put("attendanceSessions", attendanceSessions())
            .put("professorAttendanceStatus", professorAttendanceStatus())
            .put("attendanceSummaries", attendanceSummaries())
            .put("attendanceCalendars", attendanceCalendars())
            // ── 수업 5분 전 알람용 (RtdbScheduleSyncManager가 읽는 스키마) ──
            .put("Enrollment", enrollment())
            .put("Subjects", subjects())
    }

    private fun users(): JSONObject {
        return JSONObject()
            .put(
                "test",
                JSONObject()
                    .put("userId", 1)
                    .put("loginId", "test")
                    .put("password", "1234")
                    .put("name", "테스트학생")
                    .put("role", "student")
                    .put("department", "소프트웨어학과")
                    .put("studentNumber", "202312345")
                    .put("professorNumber", JSONObject.NULL)
            )
            .put(
                "202312345",
                JSONObject()
                    .put("userId", 1)
                    .put("loginId", "202312345")
                    .put("password", "1234")
                    .put("name", "최은수")
                    .put("role", "student")
                    .put("department", "소프트웨어학과")
                    .put("studentNumber", "202312345")
                    .put("professorNumber", JSONObject.NULL)
            )
            .put(
                "professor",
                JSONObject()
                    .put("userId", 2)
                    .put("loginId", "professor")
                    .put("password", "1234")
                    .put("name", "테스트교수")
                    .put("role", "professor")
                    .put("department", "소프트웨어학과")
                    .put("studentNumber", JSONObject.NULL)
                    .put("professorNumber", "P001")
            )
    }

    private fun courses(): JSONObject {
        return JSONObject()
            .put(
                "MOB001",
                course(
                    classId = 10,
                    code = "MOB001",
                    name = "모바일프로그래밍 (영어강의)",
                    professor = "민홍",
                    room = "AI관-301",
                    dayOfWeek = "TUE",
                    start = "14:00",
                    end = "15:00",
                    times = JSONArray()
                        .put(time("화", 14, 15))
                        .put(time("목", 13, 15))
                )
            )
            .put(
                "DATA001",
                course(
                    classId = 11,
                    code = "DATA001",
                    name = "자료구조 및 실습 (영어강의)",
                    professor = "김교수",
                    room = "AI관-511",
                    dayOfWeek = "TUE",
                    start = "10:00",
                    end = "11:00",
                    times = JSONArray()
                        .put(time("화", 10, 11))
                        .put(time("목", 10, 12))
                )
            )
            .put(
                "SW001",
                course(
                    classId = 12,
                    code = "SW001",
                    name = "소프트웨어공학 (신기술화상강의)",
                    professor = "박교수",
                    room = "화상강의실",
                    dayOfWeek = "FRI",
                    start = "10:00",
                    end = "12:00",
                    times = JSONArray().put(time("금", 10, 12))
                )
            )
    }

    private fun course(
        classId: Int,
        code: String,
        name: String,
        professor: String,
        room: String,
        dayOfWeek: String,
        start: String,
        end: String,
        times: JSONArray
    ): JSONObject {
        return JSONObject()
            .put("classId", classId)
            .put("courseCode", code)
            .put("courseName", name)
            .put("professorName", professor)
            .put("dayOfWeek", dayOfWeek)
            .put("startTime", start)
            .put("endTime", end)
            .put("room", room)
            .put("semester", "2026-1")
            .put("courseTimes", times)
    }

    private fun time(day: String, start: Int, end: Int): JSONObject {
        return JSONObject()
            .put("day", day)
            .put("startHour", start)
            .put("endHour", end)
    }

    private fun studentSchedules(): JSONObject {
        val classes = JSONArray()
            .put(courses().getJSONObject("MOB001"))
            .put(courses().getJSONObject("DATA001"))
            .put(courses().getJSONObject("SW001"))

        return JSONObject()
            .put(
                "1",
                JSONObject()
                    .put("success", true)
                    .put("studentId", 1)
                    .put("classes", classes)
                    .put("message", "시간표 조회 성공")
            )
    }

    private fun currentClasses(): JSONObject {
        return JSONObject()
            .put(
                "1",
                JSONObject()
                    .put("success", true)
                    .put("hasClass", true)
                    .put("classId", 10)
                    .put("courseCode", "MOB001")
                    .put("courseName", "모바일프로그래밍 (영어강의)")
                    .put("professorName", "민홍")
                    .put("room", "AI관-301")
                    .put("startTime", "14:00")
                    .put("endTime", "15:00")
                    .put("attendanceStatus", "NOT_STARTED")
                    .put("attendanceMessage", "출석 전")
                    .put("sessionId", 100)
                    .put("message", "현재 수업 조회 성공")
            )
    }

    private fun attendanceSessions(): JSONObject {
        return JSONObject()
            .put(
                "10",
                JSONObject()
                    .put("success", true)
                    .put("sessionId", 100)
                    .put("classId", 10)
                    .put("pinCode", "4821")
                    .put("bluetoothEnabled", true)
                    .put("startedAt", "2026-05-19T09:00:00")
                    .put("pinExpiresAt", "2026-05-19T09:15:00")
                    .put("message", "출석 시작")
            )
    }

    private fun professorAttendanceStatus(): JSONObject {
        return JSONObject()
            .put(
                "10",
                JSONObject()
                    .put("success", true)
                    .put("classId", 10)
                    .put("courseName", "모바일프로그래밍 (영어강의)")
                    .put("classTime", "화 14:00 - 15:00 / 목 13:00 - 15:00")
                    .put("attendanceRate", 80)
                    .put("lateRate", 10)
                    .put("absentRate", 10)
                    .put("uwbCheckCount", 0)
                    .put(
                        "students",
                        JSONArray()
                            .put(student("202312345", "최은수", "PRESENT"))
                            .put(student("202312346", "홍길동", "LATE"))
                            .put(student("202312347", "김가천", "ABSENT"))
                    )
                    .put("message", "출석 현황 조회 성공")
            )
    }

    private fun student(id: String, name: String, status: String): JSONObject {
        return JSONObject()
            .put("studentId", id)
            .put("name", name)
            .put("status", status)
    }

    private fun attendanceSummaries(): JSONObject {
        return JSONObject()
            .put(
                "1",
                JSONObject()
                    .put("success", true)
                    .put("studentId", 1)
                    .put(
                        "courses",
                        JSONArray()
                            .put(summary(10, "모바일프로그래밍", 8, 1, 1, 80, 10, 10))
                            .put(summary(11, "자료구조", 9, 0, 1, 90, 0, 10))
                    )
                    .put("message", "출결 통계 조회 성공")
            )
    }

    private fun summary(
        classId: Int,
        courseName: String,
        presentCount: Int,
        lateCount: Int,
        absentCount: Int,
        presentRate: Int,
        lateRate: Int,
        absentRate: Int
    ): JSONObject {
        return JSONObject()
            .put("classId", classId)
            .put("courseName", courseName)
            .put("presentCount", presentCount)
            .put("lateCount", lateCount)
            .put("absentCount", absentCount)
            .put("presentRate", presentRate)
            .put("lateRate", lateRate)
            .put("absentRate", absentRate)
    }

    private fun attendanceCalendars(): JSONObject {
        return JSONObject()
            .put(
                "1",
                JSONObject()
                    .put(
                        "2026-05",
                        JSONObject()
                            .put("success", true)
                            .put("month", "2026-05")
                            .put(
                                "days",
                                JSONArray()
                                    .put(day("2026-05-03", 10, "모바일프로그래밍", "ABSENT"))
                                    .put(day("2026-05-10", 11, "자료구조", "LATE"))
                            )
                            .put("message", "출결 캘린더 조회 성공")
                    )
            )
    }

    private fun day(date: String, classId: Int, courseName: String, status: String): JSONObject {
        return JSONObject()
            .put("date", date)
            .put("classId", classId)
            .put("courseName", courseName)
            .put("status", status)
    }

    // ────────────────────────────────────────────────────────────
    // 알람 시스템용 시드 (Enrollment + Subjects)
    //
    // RtdbScheduleSyncManager가 읽는 스키마:
    //   Enrollment/{studentId}/{subjectId} = true
    //   Subjects/{subjectId}/{
    //     subjectName, subjectCode, professorName,
    //     schedule/day1/{ dayOfWeek("Monday"~), location, periods[null, {st,et}, ...] },
    //     schedule/day2/{ ... }   // 주 N회면 여러 day 키
    //   }
    //
    // periods[0]은 항상 null (대학 1교시부터 시작이라 그렇게 저장).
    // ────────────────────────────────────────────────────────────

    /**
     * 학번 → 수강 과목 classId 목록 매핑.
     *
     * 키는 classId(String) 사용 — attendance_sessions/professorAttendanceStatus와 일관성 유지.
     * 교수가 출석 시작 시 보내는 courseId("10")가 그대로 매칭됨.
     */
    private fun enrollment(): JSONObject {
        return JSONObject()
            .put(
                "202312345",
                JSONObject()
                    .put("10", true)   // 모바일프로그래밍 (MOB001)
                    .put("11", true)   // 자료구조 (DATA001)
                    .put("12", true)   // 소프트웨어공학 (SW001)
            )
    }

    /**
     * 과목별 시간표 (RtdbScheduleSyncManager.parseSubject가 파싱하는 형식).
     *
     * 키는 classId(String) — Enrollment와 동일. subjectCode 필드는 정보 보존용으로 유지.
     */
    private fun subjects(): JSONObject {
        return JSONObject()
            .put(
                "10",
                subject(
                    code = "MOB001",
                    name = "모바일프로그래밍 (영어강의)",
                    professor = "민홍",
                    location = "AI관-301",
                    days = listOf(
                        SubjectDay("Tuesday", "14:00", "15:00"),
                        SubjectDay("Thursday", "13:00", "15:00")
                    )
                )
            )
            .put(
                "11",
                subject(
                    code = "DATA001",
                    name = "자료구조 및 실습 (영어강의)",
                    professor = "김교수",
                    location = "AI관-511",
                    days = listOf(
                        SubjectDay("Tuesday", "10:00", "11:00"),
                        SubjectDay("Thursday", "10:00", "12:00")
                    )
                )
            )
            .put(
                "12",
                subject(
                    code = "SW001",
                    name = "소프트웨어공학 (신기술화상강의)",
                    professor = "박교수",
                    location = "화상강의실",
                    days = listOf(
                        SubjectDay("Friday", "10:00", "12:00")
                    )
                )
            )
    }

    private data class SubjectDay(val dayOfWeek: String, val start: String, val end: String)

    private fun subject(
        code: String,
        name: String,
        professor: String,
        location: String,
        days: List<SubjectDay>
    ): JSONObject {
        val scheduleNode = JSONObject()
        days.forEachIndexed { i, d ->
            val periods = JSONArray()
                .put(JSONObject.NULL)   // 0번 인덱스는 항상 null (1교시부터 시작)
                .put(
                    JSONObject()
                        .put("startTime", d.start)
                        .put("endTime", d.end)
                )
            scheduleNode.put(
                "day${i + 1}",
                JSONObject()
                    .put("dayOfWeek", d.dayOfWeek)
                    .put("location", location)
                    .put("periods", periods)
            )
        }
        return JSONObject()
            .put("subjectCode", code)
            .put("subjectName", name)
            .put("professorName", professor)
            .put("schedule", scheduleNode)
    }
}