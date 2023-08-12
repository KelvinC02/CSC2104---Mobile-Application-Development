package com.example.recipepal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.recipepal.database.Recipe
import com.example.recipepal.database.RecipeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private var recipeDao: RecipeDao = RecipeDao(application)

    constructor(application: Application, recipeDao: RecipeDao) : this(application) {
        this.recipeDao = recipeDao
    }

    fun getAllRecipesLiveData(): LiveData<List<Recipe>> {
        return recipeDao.getAllRecipesLiveData()
    }

    fun insertRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.insertRecipe(recipe)
        }
    }

    fun getRecipeById(id: Int): Recipe? {
        return recipeDao.getRecipeById(id)
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.deleteRecipe(recipeId)
        }
    }

    // Function to search recipes based on the query
    fun searchRecipes(query: String): LiveData<List<Recipe>> {
        return recipeDao.searchRecipesLiveData(query)
    }
}
