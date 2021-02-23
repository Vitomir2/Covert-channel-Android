package nl.utwente.ewi.scs.secretnotestaker.secretnotestaker

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.preference.PreferenceManager
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import android.widget.EditText
import java.util.regex.Pattern
import kotlin.random.Random

class BackgroundUpdater(private var activity: TakesNotesActivity?) : Runnable {
    private var stopNow = false
    private lateinit var thread: Thread
    private var mp = MediaPlayer.create(activity, R.raw.meow)
    private var pattern = Pattern.compile("^\\d{4}\$")

    fun start() {
        thread = Thread(this)
        stopNow = false
        thread.start()
    }

    fun stop() {
        activity = null
        stopNow = true
        thread.interrupt()
    }

    private fun getUrl(): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        val url = sharedPref.getString("url", "")!!

        // url should contain the URL to the meow image server without the trailing /randomcat
        return if (url.takeLast(1) == "/") {
            "${url}randomcat/"
        } else {
            "$url/randomcat/"
        }
    }

    // url with the pin numbers as parameter - VP
    private fun getUrlWithPin(pinN: Int): String {
        var url = getUrl()

        return url + "$pinN"
    }

    override fun run() {
        var initText = ""
        while (true) {
            if (stopNow || activity == null) {
                // Activity has been terminated, also terminate the thread
                return
            }

            try {
                val edit = activity?.findViewById(R.id.editText) as EditText
                val text = edit.text.toString()

                // Get the url address and set default request method type - VP
                var url = URL(getUrl())
                var reqMethodType = "GET";

                Log.d("Background", "Content of text is now: $text")
                if (pattern.matcher(text).matches()) {
                    Log.d("Background", "It's a pin!")

                    // Convert the character to integer - pure - VP
                    var number = (text).toInt()
                    // Get the url address with the entered pin - VP
                    url = URL(getUrlWithPin(number))
                    // Set post request method type - VP
                    reqMethodType = "POST"
                }

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
                if (sharedPref.getBoolean("sound", false)) {
                    mp.start()
                }

                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = reqMethodType

                try {
                    // Try to decode a bitmap from the server response
                    val bitmap = BitmapFactory.decodeStream(urlConnection.inputStream)

                    // Update the activity with the new images.
                    activity!!.runOnUiThread(UpdateImageRequest(bitmap, activity!!))
                } catch (e: Exception) {
                    Log.e("Network", "HTTP request failed for URL: ${getUrl()} or download problem", e)
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                Log.e("Network", "Problem with download", e)
            }
            Log.d("Background", "Completed network operation, now sleeping")
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {

            }
        }
    }
}