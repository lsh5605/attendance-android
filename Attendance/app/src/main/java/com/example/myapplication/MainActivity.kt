package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myapplication.launcher.AttendanceServiceLauncher
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var contentFrame: FrameLayout

    private var currentPageResId: Int = R.layout.main1
    private var userId: Int = 1
    private var loginId: String = "test"
    private var userRole: String = "student"
    private var studentNumber: String = ""
    private var professorNumber: String = ""
    private var currentClassId: Int = 10
    private var currentSessionId: Int = 100

    /**
     * 출석 Service 시작/종료 + 권한 흐름 + Service→Activity broadcast 수신 캡슐화.
     * 두 출석 버튼 onClick이 이걸로 위임됨.
     */
    private lateinit var attendanceLauncher: AttendanceServiceLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readLoginInfo()

        attendanceLauncher = AttendanceServiceLauncher(this)
        attendanceLauncher.setListener(object : AttendanceServiceLauncher.SessionEventsListener {
            override fun onSessionStarted(sessionCode: String?, lectureSessionId: String?) {
                val page = contentFrame.getChildAt(0) ?: return
                if (!sessionCode.isNullOrEmpty()) showPin(page, sessionCode)
                Toast.makeText(this@MainActivity, "출석 세션 시작: $sessionCode", Toast.LENGTH_SHORT).show()
            }

            override fun onSessionFailed(reason: String?) {
                Toast.makeText(this@MainActivity, reason ?: "세션 실패", Toast.LENGTH_SHORT).show()
            }

            override fun onSessionExpired() {
                Toast.makeText(this@MainActivity, "출석 마감", Toast.LENGTH_SHORT).show()
            }

            override fun onAttendanceConfirmed(sessionCode: String?) {
                val page = contentFrame.getChildAt(0)
                if (page != null) setText(page, "tvAttendanceStatus", "출석 완료")
                Toast.makeText(this@MainActivity, "출석 완료", Toast.LENGTH_SHORT).show()
            }

            override fun onAttendanceFailed(reason: String?) {
                Toast.makeText(this@MainActivity, reason ?: "출석 실패", Toast.LENGTH_SHORT).show()
            }

            override fun onAttendanceAbsent(attendanceId: String?) {
                Toast.makeText(this@MainActivity, "결석 처리되었습니다", Toast.LENGTH_LONG).show()
            }
        })

        setContentView(R.layout.activity_drawer_host)

        drawerLayout = findViewById(R.id.drawerLayout)
        contentFrame = findViewById(R.id.contentFrame)

        if (userRole == "professor") {
            loadPage(R.layout.main_p_1)
        } else {
            loadPage(R.layout.main1)
        }

        setupDrawerMenuClick()
    }

    override fun onResume() {
        super.onResume()
        attendanceLauncher.registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        attendanceLauncher.unregisterReceiver()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        attendanceLauncher.handlePermissionResult(requestCode, grantResults)
    }

    private fun readLoginInfo() {
        val pref = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE)
        userId = pref.getInt("userId", 1)
        loginId = pref.getString("loginId", "test") ?: "test"
        userRole = pref.getString("userRole", "student") ?: "student"
        studentNumber = pref.getString("studentNumber", "") ?: ""
        professorNumber = pref.getString("professorNumber", "") ?: ""
    }

    private fun loadPage(layoutResId: Int) {
        currentPageResId = layoutResId
        contentFrame.removeAllViews()

        val pageView = LayoutInflater.from(this).inflate(layoutResId, contentFrame, false)
        contentFrame.addView(pageView)

        connectTopMenuButton(pageView)
        connectBottomMenu(pageView)
        loadFirebaseDataForPage(layoutResId, pageView)
    }

    private fun loadFirebaseDataForPage(layoutResId: Int, pageView: View) {
        when (layoutResId) {
            R.layout.main1 -> {
                loadCurrentClass(pageView)
                pageView.findViewById<View?>(R.id.btnAttendance)?.setOnClickListener {
                    // 가짜 Firebase PUT 대신 실제 BLE 스캔 + 서버 check-in 시작.
                    // 학번이 비어있으면 loginId 사용 (테스트 계정 fallback).
                    val sid = studentNumber.ifEmpty { loginId }
                    attendanceLauncher.startStudent(sid)
                }
            }

            R.layout.main_p_1 -> {
                loadProfessorStatus(pageView)
                pageView.findViewById<View?>(R.id.btnProfessorAttendanceCheck)?.setOnClickListener {
                    // 가짜 Firebase GET 대신 실제 /start + BLE 광고 시작.
                    // 받은 PIN은 onSessionStarted broadcast로 showPin(tvPinDigit1~4)에 채워짐.
                    val pid = professorNumber.ifEmpty { loginId }
                    attendanceLauncher.startProfessor(currentClassId.toString(), pid)
                }
            }

            R.layout.schedule_1 -> loadSchedule(pageView, "classBlockLayer")

            R.layout.mypage -> {
                loadMyPage(pageView)
                loadSchedule(pageView, "myScheduleBlockLayer")
            }

            R.layout.all_attendance -> loadAttendanceSummary(pageView)

            R.layout.week_1, R.layout.week_2 -> loadAttendanceCalendar(pageView)
        }
    }

    private fun connectTopMenuButton(pageView: View) {
        pageView.findViewById<View?>(R.id.btnMenu)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun connectBottomMenu(pageView: View) {
        val btnHome = pageView.findViewById<View?>(R.id.btnBottomHome)
        val btnRefresh = pageView.findViewById<View?>(R.id.btnBottomRefresh)
        val btnNotice = pageView.findViewById<View?>(R.id.btnBottomNotice)
        val btnSchedule = pageView.findViewById<View?>(R.id.btnBottomSchedule)
        val btnLogout = pageView.findViewById<View?>(R.id.btnBottomLogout)

        btnHome?.setOnClickListener {
            if (userRole == "professor") loadPage(R.layout.main_p_1) else loadPage(R.layout.main1)
        }

        btnRefresh?.setOnClickListener {
            loadPage(currentPageResId)
            Toast.makeText(this, "새로고침되었습니다", Toast.LENGTH_SHORT).show()
        }

        btnNotice?.setOnClickListener {
            if (userRole == "professor") loadPage(R.layout.notice_2) else loadPage(R.layout.notice_1)
        }

        btnSchedule?.setOnClickListener {
            loadPage(R.layout.schedule_1)
        }

        btnLogout?.setOnClickListener {
            logout()
        }
    }

    private fun setupDrawerMenuClick() {
        findViewById<View?>(R.id.menuMyPage)?.setOnClickListener { moveTo(R.layout.mypage) }
        findViewById<View?>(R.id.menuSchedule)?.setOnClickListener { moveTo(R.layout.schedule_1) }
        findViewById<View?>(R.id.menuWeekAttendance)?.setOnClickListener { moveTo(R.layout.week_1) }
        findViewById<View?>(R.id.menuAllAttendance)?.setOnClickListener { moveTo(R.layout.all_attendance) }
        findViewById<View?>(R.id.menuConfirmPeriod)?.setOnClickListener { moveTo(R.layout.confirm_1) }
        findViewById<View?>(R.id.menuConfirmOfficial)?.setOnClickListener { moveTo(R.layout.confirm_2) }

        findViewById<View?>(R.id.menuNotice)?.setOnClickListener {
            if (userRole == "professor") moveTo(R.layout.notice_2) else moveTo(R.layout.notice_1)
        }

        findViewById<View?>(R.id.menuCancel)?.setOnClickListener {
            if (userRole == "professor") moveTo(R.layout.cancel_2) else moveTo(R.layout.cancel_1)
        }
    }

    private fun moveTo(layoutResId: Int) {
        drawerLayout.closeDrawer(GravityCompat.END)
        loadPage(layoutResId)
    }

    private fun loadCurrentClass(pageView: View) {
        FirebaseClient.get("currentClasses/$userId") { json ->
            val data = FirebaseParsers.currentClass(json) ?: FirebaseParsers.currentClass(null)
            if (data == null || !data.hasClass) {
                setText(pageView, "tvDate", todayText())
                setText(pageView, "tvPeriod", "현재 수업 없음")
                setText(pageView, "tvAttendanceStatus", "출석 전")
                return@get
            }

            currentClassId = data.classId
            currentSessionId = data.sessionId
            setText(pageView, "tvDate", todayText())
            setText(pageView, "tvPeriod", "${data.startTime} - ${data.endTime}")
            setText(pageView, "tvAttendanceStatus", data.attendanceMessage.ifBlank { statusToKorean(data.attendanceStatus) })
        }
    }

    // requestBluetoothAttendance / startProfessorAttendance 제거:
    //   기존엔 Firebase에 가짜 attendance 레코드를 직접 PUT/GET 했지만,
    //   이제 두 동작 모두 AttendanceServiceLauncher로 위임됨.
    //   실제 BLE 스캔/광고 + 서버 /check-in / /start + UWB ranging은
    //   Foreground Service(StudentAttendanceService / ProfessorAttendanceService)가 담당.

    private fun loadProfessorStatus(pageView: View) {
        FirebaseClient.get("professorAttendanceStatus/$currentClassId") { json ->
            val data = json ?: FirebaseSeedDataFallback.professorStatus()

            setText(pageView, "tvClassName", data.optString("courseName", "모바일프로그래밍"))
            setText(pageView, "tvClassTime", data.optString("classTime", "화 14:00 - 15:00"))
            setText(pageView, "tvAttendanceRate", "${data.optInt("attendanceRate", 80)}%")
            setText(pageView, "tvLateRate", "${data.optInt("lateRate", 10)}%")
            setText(pageView, "tvAbsentRate", "${data.optInt("absentRate", 10)}%")
            setText(pageView, "tvUwbCheckCount", "${data.optInt("uwbCheckCount", 0)}회")

            val rows = findChildByIdName<LinearLayout>(pageView, "layoutStudentAttendanceRows")
            rows?.removeAllViews()

            FirebaseParsers.studentAttendanceList(data).forEach {
                addStudentRow(pageView, it.studentId, it.name, it.status)
            }
        }
    }

    private fun loadSchedule(pageView: View, parentIdName: String) {
        FirebaseClient.get("studentSchedules/$userId") { json ->
            val courses = FirebaseParsers.courseList(json).ifEmpty { FirebaseSeedDataFallback.courseList() }
            val parent = findChildByIdName<FrameLayout>(pageView, parentIdName)
                ?: findChildByIdName<FrameLayout>(pageView, "classBlockLayer")

            parent?.removeAllViews()
            courses.forEachIndexed { index, course ->
                addCourseBlock(parent, course, index)
            }
        }
    }

    private fun loadMyPage(pageView: View) {
        FirebaseClient.get("users/$loginId") { json ->
            val user = FirebaseParsers.user(json, loginId)
            val pref = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE)

            if (userRole == "professor") {
                setText(pageView, "tvProfessorName", user?.name ?: pref.getString("userName", "테스트교수") ?: "테스트교수")
                setText(pageView, "tvProfessorMajor", user?.department ?: pref.getString("department", "소프트웨어학과") ?: "소프트웨어학과")
            } else {
                setText(pageView, "tvStudentName", user?.name ?: pref.getString("userName", "테스트학생") ?: "테스트학생")
                setText(pageView, "tvStudentMajor", user?.department ?: pref.getString("department", "소프트웨어학과") ?: "소프트웨어학과")
                setText(pageView, "tvStudentInfo", user?.studentNumber ?: pref.getString("studentNumber", "202312345") ?: "202312345")
            }
        }
    }

    private fun loadAttendanceSummary(pageView: View) {
        FirebaseClient.get("attendanceSummaries/$userId") { json ->
            val array = json?.optJSONArray("courses") ?: return@get
            val text = StringBuilder()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                text.append(item.optString("courseName"))
                    .append(" 출석 ").append(item.optInt("presentRate")).append("%")
                    .append(" / 지각 ").append(item.optInt("lateRate")).append("%")
                    .append(" / 결석 ").append(item.optInt("absentRate")).append("%\n")
            }
            setText(pageView, "tvAttendanceSummary", text.toString())
            addSimpleText(pageView, "layoutAttendanceSummary", text.toString())
        }
    }

    private fun loadAttendanceCalendar(pageView: View) {
        val month = SimpleDateFormat("yyyy-MM", Locale.KOREA).format(Date())
        FirebaseClient.get("attendanceCalendars/$userId/$month") { json ->
            val array = json?.optJSONArray("days") ?: return@get
            val text = StringBuilder()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                text.append(item.optString("date"))
                    .append(" / ").append(item.optString("courseName"))
                    .append(" / ").append(statusToKorean(item.optString("status")))
                    .append("\n")
            }
            setText(pageView, "tvAttendanceCalendar", text.toString())
            addSimpleText(pageView, "layoutAttendanceCalendar", text.toString())
        }
    }

    private fun addCourseBlock(parent: FrameLayout?, course: Course, index: Int) {
        if (parent == null) return

        val colors = listOf("#8FA2C7", "#B9AAA5", "#79B2B8", "#A7B58D", "#C39DA4")
        val color = colors[index % colors.size]

        course.schedules.forEach { time ->
            val block = TextView(this).apply {
                text = course.name + "\n" + course.classroom
                setTextColor(Color.WHITE)
                textSize = 10f
                gravity = Gravity.CENTER
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                setBackgroundColor(Color.parseColor(color))
            }

            val params = FrameLayout.LayoutParams(
                getColumnWidth(parent),
                getBlockHeight(time.startHour, time.endHour)
            )

            params.leftMargin = getLeftMarginByDay(parent, time.day)
            params.topMargin = getTopMarginByHour(time.startHour)

            parent.addView(block, params)
        }
    }

    private fun showPin(pageView: View, pinCode: String) {
        val pin = pinCode.padEnd(4, '0')
        setText(pageView, "tvPinDigit1", pin[0].toString())
        setText(pageView, "tvPinDigit2", pin[1].toString())
        setText(pageView, "tvPinDigit3", pin[2].toString())
        setText(pageView, "tvPinDigit4", pin[3].toString())
    }

    private fun addStudentRow(pageView: View, studentId: String, name: String, status: String) {
        val parent = findChildByIdName<LinearLayout>(pageView, "layoutStudentAttendanceRows") ?: return
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 10, 12, 10)
        }
        row.addView(makeRowText(studentId, 1f))
        row.addView(makeRowText(name, 1f))
        row.addView(makeRowText(if (status == "PRESENT") "○" else "", 1f))
        row.addView(makeRowText(if (status == "ABSENT") "○" else "", 1f))
        row.addView(makeRowText(if (status == "LATE") "○" else "", 1f))
        parent.addView(row)
    }

    private fun makeRowText(value: String, weight: Float): TextView {
        return TextView(this).apply {
            text = value
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        }
    }

    private fun addSimpleText(pageView: View, parentIdName: String, value: String) {
        val parent = findChildByIdName<LinearLayout>(pageView, parentIdName) ?: return
        parent.removeAllViews()
        parent.addView(TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(Color.parseColor("#222222"))
            setPadding(16, 12, 16, 12)
        })
    }

    private fun setText(pageView: View, idName: String, value: String) {
        findChildByIdName<TextView>(pageView, idName)?.text = value
    }

    private inline fun <reified T> findChildByIdName(pageView: View, idName: String): T? {
        val id = resources.getIdentifier(idName, "id", packageName)
        return if (id != 0) pageView.findViewById(id) else null
    }

    private fun getColumnWidth(parent: FrameLayout): Int {
        val width = parent.width
        return if (width > 0) width / 5 else (resources.displayMetrics.widthPixels - dpToPx(120)) / 5
    }

    private fun getLeftMarginByDay(parent: FrameLayout, day: String): Int {
        val columnWidth = getColumnWidth(parent)
        return when (day) {
            "월" -> columnWidth * 0
            "화" -> columnWidth * 1
            "수" -> columnWidth * 2
            "목" -> columnWidth * 3
            "금" -> columnWidth * 4
            else -> 0
        }
    }

    private fun getTopMarginByHour(hour: Int): Int {
        val oneHourHeight = dpToPx(52)
        return when (hour) {
            9 -> oneHourHeight * 0
            10 -> oneHourHeight * 1
            11 -> oneHourHeight * 2
            12 -> oneHourHeight * 3
            13 -> oneHourHeight * 4
            14 -> oneHourHeight * 5
            else -> 0
        }
    }

    private fun getBlockHeight(startHour: Int, endHour: Int): Int {
        return (endHour - startHour) * dpToPx(52)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun statusToKorean(status: String): String {
        return when (status) {
            "PRESENT" -> "출석"
            "LATE" -> "지각"
            "ABSENT" -> "결석"
            "NOT_STARTED" -> "출석 전"
            else -> status
        }
    }

    private fun todayText(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
    }

    private fun nowText(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).format(Date())
    }

    private fun logout() {
        getSharedPreferences("LOGIN_INFO", MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("login_pref", MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

/**
 * Firebase 연결 실패 시 화면 표시용 기본값.
 */
object FirebaseSeedDataFallback {
    fun courseList(): List<Course> {
        return listOf(
            Course(10, "MOB001", "모바일프로그래밍 (영어강의)", "민홍", "AI관-301", listOf(CourseTime("화", 14, 15), CourseTime("목", 13, 15))),
            Course(11, "DATA001", "자료구조 및 실습 (영어강의)", "김교수", "AI관-511", listOf(CourseTime("화", 10, 11), CourseTime("목", 10, 12))),
            Course(12, "SW001", "소프트웨어공학 (신기술화상강의)", "박교수", "화상강의실", listOf(CourseTime("금", 10, 12)))
        )
    }

    fun professorStatus(): JSONObject {
        return JSONObject()
            .put("courseName", "모바일프로그래밍 (영어강의)")
            .put("classTime", "화 14:00 - 15:00 / 목 13:00 - 15:00")
            .put("attendanceRate", 80)
            .put("lateRate", 10)
            .put("absentRate", 10)
            .put("uwbCheckCount", 0)
            .put(
                "students",
                org.json.JSONArray()
                    .put(JSONObject().put("studentId", "202312345").put("name", "최은수").put("status", "PRESENT"))
                    .put(JSONObject().put("studentId", "202312346").put("name", "홍길동").put("status", "LATE"))
                    .put(JSONObject().put("studentId", "202312347").put("name", "김가천").put("status", "ABSENT"))
            )
    }
}