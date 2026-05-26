package com.example.montesorrilearning.data.remote

import com.example.montesorrilearning.domain.model.Child
import com.example.montesorrilearning.domain.model.Classroom
import com.example.montesorrilearning.domain.model.Message
import com.example.montesorrilearning.domain.model.WorkEntry
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val displayName: String, val role: String)
data class RefreshRequest(val refreshToken: String)
data class AuthResponse(val accessToken: String, val refreshToken: String, val user: UserDto)
data class UserDto(val id: String, val email: String, val displayName: String, val role: String)

data class WorkEntryRequest(
    val childId: String,
    val montessoriArea: String,
    val title: String,
    val teacherComment: String
)

data class UploadResponse(
    val storageKey: String,
    val thumbnailKey: String?,
    val width: Int?,
    val height: Int?,
    val fileSize: Long?
)

data class DailyCount(val childId: String, val date: String, val count: Int, val max: Int)
data class ChildDailyCount(val childId: String, val childName: String, val date: String, val count: Int, val max: Int)
data class DailySummary(val date: String, val classroomId: String?, val entries: List<WorkEntry>, val totalEntries: Int, val totalPhotos: Int)
data class MessageRequest(val subject: String?, val body: String, val classroomId: String?)

// ─── Syllabus DTOs ─────────────────────────────────────────────
data class SyllabusDto(
    val id: String = "",
    val term_id: String = "",
    val classroom_id: String = "",
    val montessori_area: String = "",
    val title: String = "",
    val description: String = "",
    val day_of_week: Int = 1,
    val week_number: Int? = null,
    val sort_order: Int = 0,
    val is_extracurricular: Boolean = false,
    val activity_type: String? = null,
    val duration_minutes: Int? = null,
    val created_at: String = "",
    val updated_at: String = "",
    val classroom_name: String? = null,
    val term_name: String? = null
) {
    fun toDomain() = com.example.montesorrilearning.domain.model.Syllabus(
        id = id, termId = term_id, classroomId = classroom_id,
        montessoriArea = montessori_area, title = title, description = description,
        dayOfWeek = day_of_week, weekNumber = week_number, sortOrder = sort_order,
        isExtracurricular = is_extracurricular, activityType = activity_type,
        durationMinutes = duration_minutes, createdAt = created_at,
        updatedAt = updated_at, classroomName = classroom_name, termName = term_name
    )
}

data class SyllabusRequest(
    val termId: String, val classroomId: String, val montessoriArea: String,
    val title: String, val description: String = "", val dayOfWeek: Int = 1,
    val weekNumber: Int? = null, val sortOrder: Int = 0,
    val isExtracurricular: Boolean = false, val activityType: String? = null,
    val durationMinutes: Int? = null
)

// ─── Term DTOs ─────────────────────────────────────────────────
data class TermDto(
    val id: String = "", val name: String = "",
    val start_date: String = "", val end_date: String = "", val year: Int = 2026
) {
    fun toDomain() = com.example.montesorrilearning.domain.model.Term(
        id = id, name = name, startDate = start_date, endDate = end_date, year = year
    )
}

data class TermRequest(val name: String, val startDate: String, val endDate: String, val year: Int = 2026)

// ─── Teacher Plan DTOs ─────────────────────────────────────────
data class TeacherPlanDto(
    val id: String = "", val syllabus_id: String? = null,
    val teacher_id: String = "", val classroom_id: String = "", val term_id: String = "",
    val title: String = "", val montessori_area: String = "", val description: String = "",
    val planned_date: String = "", val day_of_week: Int = 1, val week_number: Int? = null,
    val is_extracurricular: Boolean = false, val activity_type: String? = null,
    val duration_minutes: Int? = null, val is_completed: Boolean = false,
    val teacher_notes: String? = null,
    val created_at: String = "", val updated_at: String = "",
    val classroom_name: String? = null, val term_name: String? = null
) {
    fun toDomain() = com.example.montesorrilearning.domain.model.TeacherPlan(
        id = id, syllabusId = syllabus_id, teacherId = teacher_id,
        classroomId = classroom_id, termId = term_id, title = title,
        montessoriArea = montessori_area, description = description,
        plannedDate = planned_date, dayOfWeek = day_of_week,
        weekNumber = week_number, isExtracurricular = is_extracurricular,
        activityType = activity_type, durationMinutes = duration_minutes,
        isCompleted = is_completed, teacherNotes = teacher_notes,
        createdAt = created_at, updatedAt = updated_at,
        classroomName = classroom_name, termName = term_name
    )
}

data class TeacherPlanRequest(
    val syllabusId: String? = null,
    val termId: String? = null,
    val classroomId: String? = null,
    val title: String? = null,
    val montessoriArea: String? = null,
    val description: String? = null,
    val plannedDate: String? = null,
    val dayOfWeek: Int? = null,
    val weekNumber: Int? = null,
    val isExtracurricular: Boolean? = null,
    val activityType: String? = null,
    val durationMinutes: Int? = null,
    val isCompleted: Boolean? = null,
    val teacherNotes: String? = null
)

// ─── Child Progress DTOs ───────────────────────────────────────
data class ChildProgressDto(
    val id: String = "", val child_id: String = "",
    val syllabus_id: String? = null, val teacher_plan_id: String? = null,
    val status: String = "pending", val observation_notes: String? = null,
    val completed_at: String? = null,
    val created_at: String = "", val updated_at: String = "",
    val child_name: String? = null, val syllabus_title: String? = null,
    val syllabus_area: String? = null, val plan_title: String? = null
) {
    fun toDomain() = com.example.montesorrilearning.domain.model.ChildProgress(
        id = id, childId = child_id, syllabusId = syllabus_id,
        teacherPlanId = teacher_plan_id, status = status,
        observationNotes = observation_notes, completedAt = completed_at,
        createdAt = created_at, updatedAt = updated_at,
        childName = child_name, syllabusTitle = syllabus_title,
        syllabusArea = syllabus_area, planTitle = plan_title
    )
}

data class ChildProgressRequest(
    val childId: String, val syllabusId: String? = null,
    val teacherPlanId: String? = null, val status: String = "pending",
    val observationNotes: String? = null
)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

    @GET("api/children")
    suspend fun getChildren(): List<Child>

    @POST("api/children")
    suspend fun createChild(@Body body: Child): Child

    @GET("api/work-entries")
    suspend fun getWorkEntries(
        @Query("childId") childId: String? = null,
        @Query("date") date: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<WorkEntry>

    @POST("api/work-entries")
    suspend fun createWorkEntry(@Body body: WorkEntryRequest): WorkEntry

    @DELETE("api/work-entries/{id}")
    suspend fun deleteWorkEntry(@Path("id") id: String): Response<Void>

    @Multipart
    @POST("api/upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("childId") childId: RequestBody,
        @Part("isCover") isCover: RequestBody?,
        @Header("X-Override-Limit") overrideLimit: String?
    ): UploadResponse

    @GET("api/daily-counts")
    suspend fun getDailyCounts(@Query("classroomId") classroomId: String? = null): List<ChildDailyCount>

    @GET("api/daily-count/{childId}")
    suspend fun getDailyCount(@Path("childId") childId: String): DailyCount

    @GET("api/daily-summary")
    suspend fun getDailySummary(@Query("date") date: String, @Query("classroomId") classroomId: String?): DailySummary

    @GET("api/messages")
    suspend fun getMessages(): List<Message>

    @POST("api/messages")
    suspend fun sendMessage(@Body body: MessageRequest): Message

    @PUT("api/messages/{id}/read")
    suspend fun markMessageRead(@Path("id") id: String): Response<Void>

    @GET("api/classrooms")
    suspend fun getClassrooms(): List<Classroom>

    @GET("api/upload/{key}")
    suspend fun getUpload(@Path("key") key: String): ResponseBody

    // ─── Terms ──────────────────────────────────────────────
    @GET("api/admin/terms")
    suspend fun getTerms(@Query("year") year: Int? = null): List<TermDto>

    @POST("api/admin/terms")
    suspend fun createTerm(@Body body: TermRequest): TermDto

    @PUT("api/admin/terms/{id}")
    suspend fun updateTerm(@Path("id") id: String, @Body body: TermRequest): TermDto

    @DELETE("api/admin/terms/{id}")
    suspend fun deleteTerm(@Path("id") id: String): Response<Void>

    // ─── Syllabus ───────────────────────────────────────────
    @GET("api/admin/syllabus")
    suspend fun getSyllabus(
        @Query("classroomId") classroomId: String? = null,
        @Query("termId") termId: String? = null,
        @Query("montessoriArea") montessoriArea: String? = null,
        @Query("weekNumber") weekNumber: Int? = null,
        @Query("dayOfWeek") dayOfWeek: Int? = null,
        @Query("isExtracurricular") isExtracurricular: Boolean? = null
    ): List<SyllabusDto>

    @GET("api/admin/syllabus/{id}")
    suspend fun getSyllabusItem(@Path("id") id: String): SyllabusDto

    @POST("api/admin/syllabus")
    suspend fun createSyllabus(@Body body: SyllabusRequest): SyllabusDto

    @PUT("api/admin/syllabus/{id}")
    suspend fun updateSyllabus(@Path("id") id: String, @Body body: SyllabusRequest): SyllabusDto

    @DELETE("api/admin/syllabus/{id}")
    suspend fun deleteSyllabus(@Path("id") id: String): Response<Void>

    // ─── Teacher Plans ──────────────────────────────────────
    @GET("api/teacher-plans")
    suspend fun getTeacherPlans(
        @Query("classroomId") classroomId: String? = null,
        @Query("termId") termId: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): List<TeacherPlanDto>

    @POST("api/teacher-plans")
    suspend fun createTeacherPlan(@Body body: TeacherPlanRequest): TeacherPlanDto

    @PUT("api/teacher-plans/{id}")
    suspend fun updateTeacherPlan(@Path("id") id: String, @Body body: TeacherPlanRequest): TeacherPlanDto

    @DELETE("api/teacher-plans/{id}")
    suspend fun deleteTeacherPlan(@Path("id") id: String): Response<Void>

    // ─── Child Progress ─────────────────────────────────────
    @GET("api/child-progress")
    suspend fun getChildProgress(
        @Query("childId") childId: String? = null,
        @Query("status") status: String? = null,
        @Query("syllabusId") syllabusId: String? = null
    ): List<ChildProgressDto>

    @POST("api/child-progress")
    suspend fun upsertChildProgress(@Body body: ChildProgressRequest): ChildProgressDto

    @DELETE("api/child-progress/{id}")
    suspend fun deleteChildProgress(@Path("id") id: String): Response<Void>
}
