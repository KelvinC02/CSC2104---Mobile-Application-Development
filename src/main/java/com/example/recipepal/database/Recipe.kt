package com.example.recipepal.database

import java.io.Serializable

data class Recipe(
    var id: Int = 0,
    var name: String = "",
    var image: ByteArray? = null,
    var ingredients: List<String> = emptyList(),
    var instructions: List<String> = emptyList(),
    var notes: String? = null,
    var isRecommend: Boolean = false,
    var recordedDate: String? = null,
    var preparationTime: String? = null
) : Serializable
