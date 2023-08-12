package com.example.recipepal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore

class CaptureImageActivity : Activity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        captureImage()
    }

    private fun captureImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            // Handle the case when the device does not have a camera or camera app
            // For example, you can show a toast message or display an error dialog.
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            // Do something with the captured image (e.g., save to storage or display in ImageView)
            // For example, you can pass the imageBitmap back to the RecipeFragment using an Intent
            val resultIntent = Intent()
            resultIntent.putExtra("capturedImage", imageBitmap)
            setResult(RESULT_OK, resultIntent)
            finish()
        } else {
            // Image capture failed or canceled. Handle it accordingly.
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
