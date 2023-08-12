package com.example.recipepal.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DatabaseHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "MyDB"
        const val TABLE_NAME = "Recipe"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_IMG = "image"
        const val COL_INGRE = "ingredients"
        const val COL_INSTRUC = "instructions"
        const val COL_NOTES = "notes"
        const val COL_REC = "isRecommend"
        const val COL_RECORDED_DATE = "recordedDate"
        const val COL_PREPARATION_TIME = "preparationTime"
        const val DATABASE_VERSION = 2
    }

    private val recipesLiveData: MutableLiveData<List<Recipe>> = MutableLiveData()

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("DatabaseHandler", "Database created")
        val createTable =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COL_NAME VARCHAR(255)," +
                    "$COL_IMG BLOB," +
                    "$COL_INGRE VARCHAR(255)," +
                    "$COL_INSTRUC VARCHAR(255)," +
                    "$COL_NOTES VARCHAR(255)," +
                    "$COL_REC INTEGER," +
                    "$COL_RECORDED_DATE TEXT," +
                    "$COL_PREPARATION_TIME TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < DATABASE_VERSION) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    fun insertData(recipe: Recipe) {
        val db = this.writableDatabase
        try {
            val cv = getRecipeContentValues(recipe)
            val result = db.insert(TABLE_NAME, null, cv)
            if (result == -1L) {
                Log.e("DatabaseHandler", "Failed to insert data")
                showToast("Failed to insert data")
            } else {
                Log.d("DatabaseHandler", "Data inserted successfully")
                showToast("Success")
                loadRecipes()
            }
        } catch (e: Exception) {
            Log.e("DatabaseHandler", "Error inserting data: ${e.message}")
            showToast("Error inserting data")
        } finally {
            db.close()
        }
    }

    fun getAllRecipesLiveData(): LiveData<List<Recipe>> {
        loadRecipes()
        return recipesLiveData
    }

    private fun loadRecipes() {
        val db = this.readableDatabase
        val recipeList = mutableListOf<Recipe>()
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COL_ID))
                val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndex(COL_IMG)) // Retrieve the image as ByteArray
                val ingredients = cursor.getString(cursor.getColumnIndex(COL_INGRE)).split(",")
                val instructions = cursor.getString(cursor.getColumnIndex(COL_INSTRUC)).split(",")
                val notes = cursor.getString(cursor.getColumnIndex(COL_NOTES))
                val isRecommend = cursor.getInt(cursor.getColumnIndex(COL_REC)) == 1
                val recordedDate = cursor.getString(cursor.getColumnIndex(COL_RECORDED_DATE))
                val preparationTime = cursor.getString(cursor.getColumnIndex(COL_PREPARATION_TIME))

                val recipe = Recipe(id, name, imageByteArray, ingredients, instructions, notes, isRecommend)
                recipe.recordedDate = recordedDate // Assign the recorded date to the Recipe object
                recipe.preparationTime = preparationTime // Assign the preparation time to the Recipe object

                recipeList.add(recipe)
            } while (cursor.moveToNext())
        }
        cursor.close()
        recipesLiveData.postValue(recipeList)
    }

    fun getRecipeById(id: Int): Recipe? {
        val db = this.readableDatabase
        val cursor: Cursor? = db.query(
            TABLE_NAME,
            arrayOf(
                COL_ID,
                COL_NAME,
                COL_IMG,
                COL_INGRE,
                COL_INSTRUC,
                COL_NOTES,
                COL_REC,
                COL_RECORDED_DATE,
                COL_PREPARATION_TIME
            ),
            "$COL_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        val recipe: Recipe? = if (cursor != null && cursor.count > 0) {
            val recipe = extractRecipeFromCursor(cursor)
            recipe?.copy(id = id) // Update the recipe with the correct ID
        } else {
            null
        }
        cursor?.close()
        return recipe
    }

    fun updateRecipe(recipe: Recipe): Int {
        val db = this.writableDatabase
        val cv = getRecipeContentValues(recipe)
        return db.update(
            TABLE_NAME,
            cv,
            "$COL_ID = ?",
            arrayOf(recipe.id.toString())
        )
    }

    fun deleteRecipe(recipeId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(recipeId.toString()))
        db.close()
        loadRecipes()
        return result != -1
    }

    fun searchRecipes(query: String): List<Recipe> {
        val recipeList = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COL_NAME LIKE ?",
            arrayOf("%$query%")
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COL_ID))
                val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndex(COL_IMG)) // Retrieve the image as ByteArray
                val ingredients = cursor.getString(cursor.getColumnIndex(COL_INGRE)).split(",")
                val instructions = cursor.getString(cursor.getColumnIndex(COL_INSTRUC)).split(",")
                val notes = cursor.getString(cursor.getColumnIndex(COL_NOTES))
                val isRecommend = cursor.getInt(cursor.getColumnIndex(COL_REC)) == 1
                val recordedDate = cursor.getString(cursor.getColumnIndex(COL_RECORDED_DATE))
                val preparationTime = cursor.getString(cursor.getColumnIndex(COL_PREPARATION_TIME))

                val recipe = Recipe(id, name, imageByteArray, ingredients, instructions, notes, isRecommend)
                recipe.recordedDate = recordedDate // Assign the recorded date to the Recipe object
                recipe.preparationTime = preparationTime // Assign the preparation time to the Recipe object

                recipeList.add(recipe)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return recipeList
    }

    fun searchRecipesLiveData(query: String): LiveData<List<Recipe>> {
        val liveData = MutableLiveData<List<Recipe>>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COL_NAME LIKE ?",
            arrayOf("%$query%")
        )
        if (cursor.moveToFirst()) {
            val recipeList = mutableListOf<Recipe>()
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COL_ID))
                val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndex(COL_IMG)) // Retrieve the image as ByteArray
                val ingredients = cursor.getString(cursor.getColumnIndex(COL_INGRE)).split(",")
                val instructions = cursor.getString(cursor.getColumnIndex(COL_INSTRUC)).split(",")
                val notes = cursor.getString(cursor.getColumnIndex(COL_NOTES))
                val isRecommend = cursor.getInt(cursor.getColumnIndex(COL_REC)) == 1
                val recordedDate = cursor.getString(cursor.getColumnIndex(COL_RECORDED_DATE))
                val preparationTime = cursor.getString(cursor.getColumnIndex(COL_PREPARATION_TIME))

                val recipe = Recipe(id, name, imageByteArray, ingredients, instructions, notes, isRecommend)
                recipe.recordedDate = recordedDate // Assign the recorded date to the Recipe object
                recipe.preparationTime = preparationTime // Assign the preparation time to the Recipe object

                recipeList.add(recipe)
            } while (cursor.moveToNext())

            liveData.postValue(recipeList)
        } else {
            liveData.postValue(emptyList())
        }
        cursor.close()
        return liveData
    }

    private fun getRecipeContentValues(recipe: Recipe): ContentValues {
        val cv = ContentValues()
        cv.put(COL_NAME, recipe.name)
        cv.put(COL_IMG, recipe.image)
        cv.put(COL_INGRE, recipe.ingredients.joinToString(","))
        cv.put(COL_INSTRUC, recipe.instructions.joinToString(","))
        cv.put(COL_NOTES, recipe.notes)
        cv.put(COL_REC, if (recipe.isRecommend) 1 else 0)
        cv.put(COL_RECORDED_DATE, recipe.recordedDate)
        cv.put(COL_PREPARATION_TIME, recipe.preparationTime)
        return cv
    }

    private fun extractRecipeFromCursor(cursor: Cursor): Recipe? {
        return try {
            val id = cursor.getInt(cursor.getColumnIndex(COL_ID))
            val name = cursor.getString(cursor.getColumnIndex(COL_NAME))
            val image = cursor.getBlob(cursor.getColumnIndex(COL_IMG))
            val ingredients = cursor.getString(cursor.getColumnIndex(COL_INGRE)).split(",").map { it.trim() }
            val instructions = cursor.getString(cursor.getColumnIndex(COL_INSTRUC)).split(",").map { it.trim() }
            val notes = cursor.getString(cursor.getColumnIndex(COL_NOTES))
            val isRecommend = cursor.getInt(cursor.getColumnIndex(COL_REC)) == 1
            val recordedDate = cursor.getString(cursor.getColumnIndex(COL_RECORDED_DATE))
            val preparationTime = cursor.getString(cursor.getColumnIndex(COL_PREPARATION_TIME))

            Recipe(
                id,
                name,
                image,
                ingredients,
                instructions,
                notes,
                isRecommend,
                recordedDate,
                preparationTime
            )
        } catch (e: Exception) {
            Log.e("DatabaseHandler", "Error extracting recipe: ${e.message}")
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
