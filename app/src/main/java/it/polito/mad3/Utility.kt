package it.polito.mad3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import java.io.*




class Utility {

}

fun isFilePresent(context: Context, fileName: String): Boolean {
    val path = context.filesDir.absolutePath + "/" + fileName
    val file = File(path)
    return file.exists()
}
fun read(context: Context, fileName: String): String? {
    return try {
        val fis: FileInputStream = context.openFileInput(fileName)
        val isr = InputStreamReader(fis)
        val bufferedReader = BufferedReader(isr)
        val sb = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        sb.toString()
    } catch (fileNotFound: FileNotFoundException) {
        null
    } catch (ioException: IOException) {
        null
    }
}

fun create(context: Context, fileName: String, jsonString: String?): Boolean {

    return try {
        val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        if (jsonString != null) {
            fos.write(jsonString.toByteArray())
        }
        fos.close()
        true
    } catch (fileNotFound: FileNotFoundException) {
        false
    } catch (ioException: IOException) {
        false
    }
}
fun fileExist(fileName: String): Boolean {
    return try {
        val file = File(fileName)
        file.exists()
    } catch (fileNotFound: FileNotFoundException) {
        false
    } catch (ioException: IOException) {
        false
    }

}

fun Bitmap.fixBitmapRotation(currentPhotoPath: String): Bitmap {
    val ei = ExifInterface(currentPhotoPath)
    val orientation: Int = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    var rotatedBitmap: Bitmap? = null
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(this, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(this, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(this, 270f)
        ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = this
        else -> rotatedBitmap = this
    }
    return this
}
fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height,
        matrix, true
    )
}


/* load the image on image view and
             * resize it with respect to the
            * layout size  */
fun setImage(imageView: ImageView, currentPhotoPath: String, viewImageWidth:Int=150, viewImageHeight:Int=150) {
    if(fileExist(currentPhotoPath)){
        imagesetbase(currentPhotoPath,viewImageHeight,viewImageWidth)
        imageView.setImageBitmap(imagesetbase(currentPhotoPath,viewImageHeight,viewImageWidth))
    }
}

fun imagesetbase( currentPhotoPath: String, viewImageWidth:Int=150, viewImageHeight:Int=150):Bitmap?{
    // Get the dimensions of the View
    val targetW: Int = viewImageWidth
    val targetH: Int = viewImageHeight

    // Get the dimensions of the bitmap
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    val photoW = bmOptions.outWidth
    val photoH = bmOptions.outHeight

    // Determine how much to scale down the image
    val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = scaleFactor
    bmOptions.inPurgeable = true
    var bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    bitmap = bitmap.fixBitmapRotation(currentPhotoPath)
    if (bitmap.getWidth() >= bitmap.getHeight()){
        bitmap = Bitmap.createBitmap(
            bitmap,
            bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
            0,
            bitmap.getHeight(),
            bitmap.getHeight()
        );
    }else{
        bitmap = Bitmap.createBitmap(
            bitmap,
            0,
            bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
            bitmap.getWidth(),
            bitmap.getWidth()
        );
    }
    return bitmap
}
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}




