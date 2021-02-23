package nl.utwente.ewi.scs.secretnotestaker.secretnotestaker

import android.graphics.Bitmap

class UpdateImageRequest(private val bitmap: Bitmap, private val activity: TakesNotesActivity) : Runnable {
    override fun run() {
        activity.updateImage(bitmap)
    }
}