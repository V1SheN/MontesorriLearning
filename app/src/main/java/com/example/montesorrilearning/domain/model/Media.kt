package com.example.montesorrilearning.domain.model

data class Media(
    val id: String = "",
    val entryId: String = "",
    val mediaType: String = "image",
    val storageKey: String = "",
    val thumbnailKey: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fileSize: Long? = null,
    val isCover: Boolean = false,
    val caption: String? = null,
    val sortOrder: Int = 0
)
