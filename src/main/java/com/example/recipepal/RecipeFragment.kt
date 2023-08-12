package com.example.recipepal

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.recipepal.database.DatabaseHandler
import com.example.recipepal.database.Recipe
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RecipeFragment : AppCompatActivity() {

    // Declare all the Component in XML
    private lateinit var addButton: Button
    private lateinit var recipeNameText: EditText
    private lateinit var recipeIngreText: EditText
    private lateinit var recipeInstrucText: EditText
    private lateinit var recipeNotes: EditText
    private lateinit var recipeRecommend: CheckBox
    private lateinit var recordedDateTextView: TextView
    private lateinit var showDatePickerButton: Button
    private lateinit var preparationTimeTextView: TextView
    private lateinit var showTimePickerButton: Button
    private lateinit var recipeImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var shareButton: Button

    // Declare all the value needed
    private var recordedDate: LocalDate? = null
    private var preparationTime: LocalTime? = null
    private var recipe: Recipe? = null
    private var isNewRecipe: Boolean = true
    private var recipeId: Int = -1 // Store the recipe ID

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_fragment_updated)

        initializeComponents()

        isNewRecipe = intent.getBooleanExtra("isNewRecipe", true)
        recipeId = intent.getIntExtra("recipeId", -1)

        if (recipeId == -1 && !isNewRecipe) {
            finish()
            return
        }

        val db = DatabaseHandler(this)
        recipe = db.getRecipeById(recipeId)

        updateUIWithRecipe()
        setClickListeners()
        handleShareButtonVisibility()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeComponents() {
        addButton = findViewById(R.id.btn_addRecipe)
        recipeNameText = findViewById(R.id.recipe_name)
        recipeIngreText = findViewById(R.id.recipe_ingredient)
        recipeInstrucText = findViewById(R.id.recipe_instruction)
        recipeNotes = findViewById(R.id.recipe_note)
        recipeRecommend = findViewById(R.id.recipe_recommend)
        recordedDateTextView = findViewById(R.id.recordedDateTextView)
        showDatePickerButton = findViewById(R.id.showDatePickerButton)
        preparationTimeTextView = findViewById(R.id.preparationTimeTextView)
        showTimePickerButton = findViewById(R.id.showTimePickerButton)
        recipeImageView = findViewById(R.id.recipeImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        shareButton = findViewById(R.id.btn_shareRecipe)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUIWithRecipe() {
        recipe?.let {
            recipeNameText.text = SpannableStringBuilder(it.name)
            recipeIngreText.text = SpannableStringBuilder(it.ingredients.joinToString("\n"))
            recipeInstrucText.text = SpannableStringBuilder(it.instructions.joinToString("\n"))
            recipeIngreText.setLineSpacing(0f, 1.2f)
            recipeInstrucText.setLineSpacing(0f, 1.2f)
            recipeNotes.text = SpannableStringBuilder(it.notes)
            recipeRecommend.isChecked = it.isRecommend

            it.recordedDate?.let { recordedDate ->
                this.recordedDate = LocalDate.parse(recordedDate)
                recordedDateTextView.text = formatDate(recordedDate)
            } ?: run {
                recordedDateTextView.text = "Recorded Date"
            }

            it.preparationTime?.let { preparationTime ->
                this.preparationTime = LocalTime.parse(preparationTime)
                preparationTimeTextView.text = formatTime(preparationTime)
            } ?: run {
                preparationTimeTextView.text = "Preparation Time"
            }

            val imageBitmap = byteArrayToBitmap(it.image ?: byteArrayOf())
            if (imageBitmap != null) {
                recipeImageView.setImageBitmap(imageBitmap)
                selectImageButton.text = "Update Image"
            } else {
                // If image doesn't exist, show the "Select Image" button and hide the "Update Image" button
                selectImageButton.text = "Select Image"
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setClickListeners() {
        showDatePickerButton.setOnClickListener {
            showDatePicker()
        }

        showTimePickerButton.setOnClickListener {
            showTimePicker()
        }

        selectImageButton.setOnClickListener {
            if (recipe?.image != null) {
                showUpdateImageSourceSelectionDialog()
            } else {
                showImageSourceSelectionDialog()
            }
        }

        addButton.setOnClickListener {
            saveOrUpdateRecipe()
        }

        shareButton.setOnClickListener {
            shareRecipe()
        }
    }


    //Function for DateTime Fragment and Format
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val currentDate = LocalDate.now()
        val datePicker = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            recordedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
            recordedDateTextView.text = formatDate(recordedDate.toString())

            // Update the recordedDate in the recipe object
            recipe?.recordedDate = recordedDate.toString()
        }, currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)
        datePicker.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker() {
        val currentTime = LocalTime.now()
        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            preparationTime = LocalTime.of(hourOfDay, minute)
            preparationTimeTextView.text = formatTime(preparationTime.toString())

            // Update the preparationTime in the recipe object
            recipe?.preparationTime = preparationTime.toString()
        }, currentTime.hour, currentTime.minute, true)
        timePicker.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return date.format(formatter)
    }

    // Helper function to format LocalTime as a user-friendly string
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatTime(time: String): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return time.format(formatter)
    }

    //Function for Image Selection
    private fun showImageSourceSelectionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndCaptureImage()
                    1 -> pickImageFromGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun checkCameraPermissionAndCaptureImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            captureImageFromCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun captureImageFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImageFromCamera()
            } else {
                // Camera permission denied. Handle it accordingly.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageBitmap = data?.extras?.get("data") as Bitmap?
                    if (imageBitmap != null) {
                        // Convert the image Bitmap to a byte array
                        val imageByteArray = bitmapToByteArray(imageBitmap)
                        recipeImageView.setImageBitmap(imageBitmap) // Set the captured image in the ImageView

                        // Save the image as a byte array in the Recipe object
                        recipe = recipe?.copy(image = imageByteArray)
                    }
                }
            }
            REQUEST_IMAGE_PICK -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        val imageBitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            imageUri
                        )
                        // Convert the image Bitmap to a byte array
                        val imageByteArray = bitmapToByteArray(imageBitmap)
                        recipeImageView.setImageBitmap(imageBitmap) // Set the picked image in the ImageView

                        // Save the image as a byte array in the Recipe object
                        recipe = recipe?.copy(image = imageByteArray)
                    }
                }
            }
        }
    }

    //Function to Store and Load Image
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray?): Bitmap? {
        return try {
            if (byteArray != null && byteArray.isNotEmpty()) {
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //Function to ShareReceipt
    private fun shareRecipe() {
        val name = recipeNameText.text.toString()
        val ingredientsText = recipeIngreText.text.toString()
        val instructionsText = recipeInstrucText.text.toString()
        val notesText = recipeNotes.text.toString()

        val shareText = "Recipe: $name\n\nIngredients:\n$ingredientsText\n\nInstructions:\n$instructionsText\n\nNotes:\n$notesText"

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Check out this delicious recipe!")
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val currentRecipeId = intent.getIntExtra("recipeId", -1)

        // If the recipeId is valid (not -1), it means we are editing an existing recipe
        if (currentRecipeId != -1) {
            // Set the recipeId in the intent extras to indicate an existing recipe is being edited
            sendIntent.putExtra("recipeId", currentRecipeId)
        }

        try {
            startActivity(Intent.createChooser(sendIntent, "Share Recipe"))
        } catch (e: Exception) {
            // If no app can handle the intent, catch the exception and show a toast
            Toast.makeText(
                this,
                "No app available to handle the share action",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleShareButtonVisibility() {
        if (isNewRecipe) {
            shareButton.visibility = View.GONE // Disable the Share button for new recipes
        } else {
            shareButton.visibility = View.VISIBLE // Enable the Share button for existing recipes
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveOrUpdateRecipe() {
        val name = recipeNameText.text.toString()
        val ingredientsText = recipeIngreText.text.toString()
        val instructionsText = recipeInstrucText.text.toString()
        val notesText = recipeNotes.text.toString()

        val ingredientsList = ingredientsText.split("\n").map { it.trim() }
        val instructionsList = instructionsText.split("\n").map { it.trim() }

        if (name.isNotEmpty() && ingredientsText.isNotEmpty() && instructionsText.isNotEmpty()) {
            val isChecked = recipeRecommend.isChecked

            if (recordedDate == null) {
                Toast.makeText(this, "Please select the Date Recorded.", Toast.LENGTH_SHORT).show()
                return
            }

            val recipeToUpdate: Recipe? = if (isNewRecipe) {
                Recipe(
                    id = 0,
                    name = name,
                    image = null,
                    ingredients = ingredientsList,
                    instructions = instructionsList,
                    notes = notesText,
                    isRecommend = isChecked,
                    recordedDate = recordedDate?.toString(),
                    preparationTime = preparationTime?.toString()
                )
            } else {
                recordedDate = LocalDate.parse(recipe?.recordedDate ?: LocalDate.now().toString())
                preparationTime = LocalTime.parse(recipe?.preparationTime ?: LocalTime.now().toString())

                recipe?.apply {
                    this.name = name
                    this.ingredients = ingredientsList
                    this.instructions = instructionsList
                    this.notes = notesText
                    this.isRecommend = isChecked
                    this.recordedDate = recordedDate?.toString()
                    this.preparationTime = preparationTime?.toString()
                }

                recipe
            }

            if (recipeToUpdate != null) {
                val imageBitmap = (recipeImageView.drawable as? BitmapDrawable)?.bitmap
                if (imageBitmap != null) {
                    val imageByteArray = bitmapToByteArray(imageBitmap)
                    recipeToUpdate.image = imageByteArray
                    selectImageButton.visibility = View.GONE
                }

                val db = DatabaseHandler(this)
                if (isNewRecipe) {
                    db.insertData(recipeToUpdate)
                } else {
                    db.updateRecipe(recipeToUpdate)
                    Toast.makeText(this, "Success Update", Toast.LENGTH_SHORT).show()
                }

                finish()
            }
        } else {
            Toast.makeText(this, "Please enter Name, Ingredients, and Instructions", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun showUpdateImageSourceSelectionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Update Image")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndCaptureImage()
                    1 -> pickImageFromGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}