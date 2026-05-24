package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wardrobe_items")
data class WardrobeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Tops, Bottoms, Outerwear, Footwear
    val color: String, // Black, White, Off-White/Beige, Navy Blue, Charcoal Grey, Olive Green
    val material: String, // Linen, Cashmere, Leather, Pima Cotton, Wool, etc.
    val notes: String = "",
    val isDefault: Boolean = false
)

@Entity(tableName = "saved_looks")
data class SavedLook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val items: String, // Comma separated names
    val categoryHint: String = "Casual",
    val timestamp: Long = System.currentTimeMillis()
)

object FashionConstants {
    val ClassicColors = listOf(
        "Black",
        "White",
        "Off-White/Beige",
        "Navy Blue",
        "Charcoal Grey",
        "Olive Green"
    )

    val WardrobeCategories = listOf(
        "Tops",
        "Bottoms",
        "Outerwear",
        "Footwear"
    )

    val DefaultItems = listOf(
        WardrobeItem(name = "Classic White Tee", category = "Tops", color = "White", material = "Pima Cotton", notes = "Heavyweight clean crewneck", isDefault = true),
        WardrobeItem(name = "Navy Cashmere Crewneck", category = "Tops", color = "Navy Blue", material = "Cashmere Wool", notes = "Soft tailored fit", isDefault = true),
        WardrobeItem(name = "Off-White Linen Shirt", category = "Tops", color = "Off-White/Beige", material = "Italian Linen", notes = "Breathable casual button-down", isDefault = true),
        WardrobeItem(name = "Charcoal Wool Trousers", category = "Bottoms", color = "Charcoal Grey", material = "Merino Wool", notes = "Tailored leg with clean cuff", isDefault = true),
        WardrobeItem(name = "Olive Green Twill Chinos", category = "Bottoms", color = "Olive Green", material = "Cotton Twill", notes = "Refined tapered casual pant", isDefault = true),
        WardrobeItem(name = "Charcoal Tailored Blazer", category = "Outerwear", color = "Charcoal Grey", material = "Wool Blend", notes = "Structure modern shoulder", isDefault = true),
        WardrobeItem(name = "Navy Double-Breasted Trench", category = "Outerwear", color = "Navy Blue", material = "Gabardine Cotton", notes = "Timeless protective outer layer", isDefault = true),
        WardrobeItem(name = "Black Handcrafted Loafers", category = "Footwear", color = "Black", material = "Calfskin Leather", notes = "Hand-stitched premium sole", isDefault = true),
        WardrobeItem(name = "White Minimalist Sneakers", category = "Footwear", color = "White", material = "Full-grain Leather", notes = "Clean vulcanized low-top", isDefault = true)
    )
}
