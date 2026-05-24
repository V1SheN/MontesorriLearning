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

data class DailySummary(val date: String, val classroomId: String?, val entries: List<WorkEntry>, val totalEntries: Int, val totalPhotos: Int)

data class MessageRequest(val subject: String?, val body: String, val classroomId: String?)

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

    @GET("api/daily-count/{childId}")
    suspend fun getDailyCount(@Path("childId") childId: String): DailyCount

    @GET("api/daily-summary")
    suspend fun getDailySummary(
        @Query("date") date: String,
        @Query("classroomId") classroomId: String?
    ): DailySummary

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
}
