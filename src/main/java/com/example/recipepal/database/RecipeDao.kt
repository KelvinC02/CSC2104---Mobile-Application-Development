package com.example.recipepal.database

import android.content.Context
import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class RecipeDao(context: Context) {
    private val dbHelper: DatabaseHandler = DatabaseHandler(context)

    fun getAllRecipesLiveData() = dbHelper.getAllRecipesLiveData()

    fun insertRecipe(recipe: Recipe) {
        dbHelper.insertData(recipe)
    }

    fun getRecipeById(id: Int): Recipe? {
        return dbHelper.getRecipeById(id)
    }

    fun updateRecipe(recipe: Recipe): Int {
        return dbHelper.updateRecipe(recipe)
    }

    fun deleteRecipe(recipeId: Int): Boolean {
        return dbHelper.deleteRecipe(recipeId)
    }

    fun searchRecipesLiveData(query: String): LiveData<List<Recipe>> {
        return dbHelper.searchRecipesLiveData(query)
    }
}
