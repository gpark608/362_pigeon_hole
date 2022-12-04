package ca.sfu.minerva.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri


fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun getBitmap(context: Context, imgUri: Uri, targetW: Int, targetH: Int): Bitmap? {
    // Get the dimensions of the bitmap
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri), null, bmOptions)

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = calculateInSampleSize(bmOptions, targetW, targetH)

    val bitmap = BitmapFactory.decodeStream(
        context.contentResolver.openInputStream(imgUri),
        null,
        bmOptions
    )

    val matrix = getMatrix(context, imgUri)

    return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun getMatrix(context: Context, imgUri: Uri): Matrix {
    val ei = context.contentResolver.openInputStream(imgUri)?.let {
        ExifInterface(it)
    }
    val orientation: Int = ei!!.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
    }
    return matrix
}