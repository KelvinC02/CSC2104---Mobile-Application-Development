package com.example.recipepal

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.recipepal.database.Recipe
import com.example.recipepal.database.RecipeDao

class RecipeRepository(application: Application) {
    private val recipeDao: RecipeDao = RecipeDao(application)

    fun getAllRecipesLiveData(): LiveData<List<Recipe>> {
        return recipeDao.getAllRecipesLiveData()
    }

    fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    fun getRecipeById(id: Int): Recipe? {
        return recipeDao.getRecipeById(id)
    }

    fun updateRecipe(recipe: Recipe): Int {
        return recipeDao.updateRecipe(recipe)
    }

    fun deleteRecipe(recipeId: Int): Boolean {
        return recipeDao.deleteRecipe(recipeId)
    }
}
