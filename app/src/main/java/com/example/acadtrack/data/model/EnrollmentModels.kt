package com.example.acadtrack.data.model

data class Enrollment(
    val _id: String,
    val student: Student?,
    val mapping: Mapping?,
    val createdAt: String?,
    val updatedAt: String?
) {
    val id: String get() = _id
}

data class CreateEnrollmentRequest(
    val studentId: String,
    val mappingId: String
)

data class EnrollmentResponse(
    val success: Boolean,
    val data: List<Enrollment>
)

data class SingleEnrollmentResponse(
    val success: Boolean,
    val data: Enrollment
)

data class DeleteEnrollmentResponse(
    val success: Boolean,
    val message: String
)
