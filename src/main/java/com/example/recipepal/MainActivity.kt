package com.example.recipepal

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipepal.RecipeAdapter
import com.example.recipepal.RecipeFragment
import com.example.recipepal.RecipeViewModel
import com.example.recipepal.database.Recipe
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), RecipeAdapter.OnItemClickListener, SearchView.OnQueryTextListener {
    private lateinit var addButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var searchView: SearchView
    private lateinit var recipeViewModel: RecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addButton = findViewById(R.id.floating_btn_addRecipe)
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        recipeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(RecipeViewModel::class.java)

        addButton.setOnClickListener {
            val intent = Intent(this@MainActivity, RecipeFragment::class.java)
            intent.putExtra("isNewRecipe", true) // Pass the flag to indicate creating a new recipe
            startActivity(intent)
        }

        recipeAdapter = RecipeAdapter(emptyList(), this)

        recyclerView.adapter = recipeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView.setOnQueryTextListener(this)

        loadRecipes()
    }

    private fun loadRecipes() {
        recipeViewModel.getAllRecipesLiveData().observe(this, { recipes ->
            recipeAdapter.updateRecipes(recipes)
        })
    }

    // Filter the recipes based on the search query
    private fun filterRecipes(query: String) {
        recipeViewModel.searchRecipes(query).observe(this, { recipes ->
            recipeAdapter.updateRecipes(recipes)
        })
    }

    override fun onItemClick(recipeId: Int) {
        val intent = Intent(this, RecipeFragment::class.java)
        intent.putExtra("recipeId", recipeId)
        intent.putExtra("isNewRecipe", false) // Pass the flag to indicate an existing recipe is being retrieved
        startActivity(intent)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        filterRecipes(newText)
        return true
    }

    override fun onResume() {
        super.onResume()
        // Reload all recipes in case any changes occurred
        loadRecipes()
    }
}
