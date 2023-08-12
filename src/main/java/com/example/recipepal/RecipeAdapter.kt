package com.example.recipepal

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.recipepal.database.Recipe
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RecipeAdapter(
    private var recipeList: List<Recipe>,
    private val itemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(recipeId: Int)
    }

    interface OnRecipeDeleteListener {
        fun onRecipeDeleted(recipeId: Int)
    }

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.recipe_name)
        val imageView: ImageView = itemView.findViewById(R.id.recipe_image)
        val preparationTimeTextView: TextView = itemView.findViewById(R.id.preparation_time)
        val isRecommendedTextView: TextView = itemView.findViewById(R.id.is_recommend)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_fragment_recipe, parent, false)
        return RecipeViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentItem = recipeList[position]
        holder.titleTextView.text = currentItem.name
        if (currentItem.image != null) {
            holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(currentItem.image, 0, currentItem.image!!.size))
        } else {
            // Set the default image
            holder.imageView.setImageResource(R.drawable.baseline_no_food_24)
        }
        holder.preparationTimeTextView.text = if (currentItem.preparationTime.isNullOrEmpty()) {
            "Preparation Time: Not specified"
        } else {
            "Preparation Time: ${formatTime(currentItem.preparationTime.toString())}"
        }
        holder.isRecommendedTextView.text = if (currentItem.isRecommend) "Recommended" else "Not Recommended"
        val recommendIcon: ImageView = holder.itemView.findViewById(R.id.recommend_icon)
        val thumbsUpImageResId = if (currentItem.isRecommend) {
            R.drawable.baseline_thumb_up_24 // Use the filled thumbs-up icon for recommended recipes
        } else {
            R.drawable.baseline_thumb_down_24 // Use the transparent thumbs-up icon for not recommended recipes
        }
        val thumbsUpDrawable = ContextCompat.getDrawable(holder.itemView.context, thumbsUpImageResId)
        recommendIcon.setImageDrawable(thumbsUpDrawable)

        holder.itemView.setOnClickListener {
            // Pass the id of the clicked recipe instead of the entire Recipe object
            itemClickListener.onItemClick(currentItem.id)
        }

        holder.deleteButton.setOnClickListener {
            val recipeIdToDelete = recipeList[position].id
            val context = it.context
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("OK") { _, _ ->
                    // User clicked OK, proceed with the deletion
                    val recipeViewModel = RecipeViewModel(context.applicationContext as Application)
                    recipeViewModel.deleteRecipe(recipeIdToDelete)
                    // Remove the item from the RecyclerView immediately
                    removeItem(position)
                }
                .setNegativeButton("Cancel", null)
                .create()
            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    fun updateRecipes(newRecipeList: List<Recipe>) {
        recipeList = newRecipeList
        notifyDataSetChanged()
    }

    // Helper function to format LocalTime as a user-friendly string
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatTime(time: String?): String {
        if (time.isNullOrEmpty()) {
            return "Preparation Time"
        }
        val localTime = LocalTime.parse(time)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return localTime.format(formatter)
    }

    fun removeItem(position: Int) {
        recipeList = recipeList.filterIndexed { index, _ -> index != position }
        notifyItemRemoved(position)
    }
}
