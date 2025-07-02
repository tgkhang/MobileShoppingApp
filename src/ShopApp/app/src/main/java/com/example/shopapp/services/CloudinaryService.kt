package com.example.shopapp.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

class CloudinaryService(private val context: Context) {

    // Process image before uploading
    private suspend fun processImage(uri: Uri, maxWidth: Int = 1024, quality: Int = 80): File = withContext(Dispatchers.IO) {
        try {
            // Get input stream from URI
            val inputStream = context.contentResolver.openInputStream(uri)

            // Decode image size first to determine scaling
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate scaling factor
            var scale = 1
            while (options.outWidth / scale > maxWidth || options.outHeight / scale > maxWidth) {
                scale *= 2
            }

            // Decode with scaling
            val scaledOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val newInputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, scaledOptions)
            newInputStream?.close()

            // Create temporary file for the processed image
            val tempFile = File.createTempFile("optimized_", ".jpg", context.cacheDir)

            // Compress and save to the temporary file
            FileOutputStream(tempFile).use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            bitmap?.recycle()

            Log.d("CloudinaryService", "Image processed: Original size: ${options.outWidth}x${options.outHeight}, " +
                    "New size: ${bitmap?.width}x${bitmap?.height}, " +
                    "File size: ${tempFile.length() / 1024}KB")

            return@withContext tempFile
        } catch (e: Exception) {
            Log.e("CloudinaryService", "Error processing image: ${e.message}")
            throw e
        }
    }

    suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            // First process the image
            val processedFile = processImage(uri)

            // Then upload using suspendCancellableCoroutine
            suspendCancellableCoroutine { continuation ->
                val requestId = MediaManager.get().upload(processedFile.path)
                    .option("folder", "shopapp")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d("CloudinaryService", "Upload started")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100) / totalBytes
                            Log.d("CloudinaryService", "Upload progress: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val url = resultData["secure_url"] as String
                            // Delete the temporary file
                            processedFile.delete()
                            continuation.resume(Result.success(url))
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e("CloudinaryService", "Upload error: ${error.description}")
                            // Delete the temporary file
                            processedFile.delete()
                            continuation.resume(Result.failure(Exception(error.description)))
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.e("CloudinaryService", "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()

                continuation.invokeOnCancellation {
                    MediaManager.get().cancelRequest(requestId)
                    processedFile.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("CloudinaryService", "Error during upload: ${e.message}")
            Result.failure(e)
        }
    }
}