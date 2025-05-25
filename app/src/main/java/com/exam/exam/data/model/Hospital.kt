package com.exam.exam.data.model

import com.google.gson.annotations.SerializedName

data class Hospital(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("region")
    val region: String,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("province")
    val province: String
) {
    // Generate a hospital image URL based on the hospital name
    // Using a medical-themed placeholder service or default hospital image
    val imageUrl: String
        get() = when {
            name.contains("RSUP", ignoreCase = true) -> "https://images.unsplash.com/photo-1587351021759-3e566b6af7cc?w=400&h=300&fit=crop"
            name.contains("UMUM", ignoreCase = true) -> "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=400&h=300&fit=crop"
            name.contains("PARU", ignoreCase = true) -> "https://images.unsplash.com/photo-1576091160399-112ba8d25d1f?w=400&h=300&fit=crop"
            province.contains("Jakarta", ignoreCase = true) -> "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop"
            province.contains("Jawa", ignoreCase = true) -> "https://images.unsplash.com/photo-1582750433449-648ed127bb54?w=400&h=300&fit=crop"
            province.contains("Bali", ignoreCase = true) -> "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=400&h=300&fit=crop"
            else -> "https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=400&h=300&fit=crop"
        }
    
    // Get formatted phone number for display
    val formattedPhone: String
        get() = phone?.takeIf { it.isNotBlank() } ?: "No phone available"
    
    // Check if this is a national referral hospital (RSUP)
    val isNationalHospital: Boolean
        get() = name.contains("RSUP", ignoreCase = true)
}