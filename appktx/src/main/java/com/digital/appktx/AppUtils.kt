package com.digital.appktx

import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.util.*
import java.io.*
import java.net.URLConnection
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

fun Any?.isNullValue() = this == null

//region Locale
fun getCurrentLanguageCode(context: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return context.resources.configuration.locales.get(0).language
    } else {
        return context.resources.configuration.locale.language
    }
}

/**
 * change app language,
 * & send locale broadcast ,so every activity should recreate it self with new configuration
 * */
val APP_LANGUAGE_CHANGE_BROADCAST = "com.digital.gg.hu.lang_changes"
val LANGUAGE = "lang_code"
fun changeAppLocale(
    langCode: String,
    context: Context?,
    sendBroadCast: () -> Unit
): ContextWrapper {
    context?.let {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val res = context.resources
        val config: Configuration = Configuration(res.configuration)
//        val config = res.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, it.resources.displayMetrics)

        } else {
            config.locale = locale
            context.resources.updateConfiguration(config, it.resources.displayMetrics)
        }

        sendBroadCast()
    }

    return ContextWrapper(context)

}

//endregion

//region Extending Functions


fun String?.isEmptyText(): Boolean {
    return isNullOrEmpty() || this.equals("null", true)
}

fun String?.isNotEmptyText(): Boolean {
    return !isEmptyText()
}

fun String?.toSafetyString(defValue: String = ""): String {
    return if (this?.isEmptyText() != false) defValue else this!!
}


fun TextView.setCustomTypeFace(fontTypeNameRes: Int) {
    getTypeFace(context, fontTypeNameRes)?.let { fontFace ->
        typeface = fontFace
    }
}

fun TextView.setCustomTypeFace(fontTypeName: String) {
    getTypeFace(context, fontTypeName)?.let { fontFace ->
        typeface = fontFace
    }
}


//endregion


//region sharedPreference
var sharedPreferencesApp = "SharedPreferencesApp"

fun sharedEditor(context: Context): SharedPreferences.Editor {
    return shared(context).edit()

}

/**
 * save key value in sharedPreference
 * */
fun saveInShared(context: Context, key: String, value: Any): Boolean {
    shared(context).edit().run {
        when (value) {
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            else -> {
                return false
            }
        }
        return commit()
    }
}

/**
 * save vararg keyValue in sharedPreference
 * */
fun saveInShared(context: Context, vararg keyValue: Pair<String, Any>): Boolean {
    shared(context).edit().run {
        keyValue.forEach {
            when (val value = it.second) {
                is String -> putString(it.first, value)
                is Boolean -> putBoolean(it.first, value)
                is Int -> putInt(it.first, value)
                is Float -> putFloat(it.first, value)
                is Long -> putLong(it.first, value)
                else -> {
                    return false
                }
            }
        }
        return commit()
    }
}

fun shared(context: Context): SharedPreferences {
    return context.getSharedPreferences(sharedPreferencesApp, Context.MODE_PRIVATE)

}


/**
 * save user language code.
 */
fun saveUserLanguage(context: Context, langCode: String) {
    sharedEditor(context).putString(LANGUAGE, langCode).apply()
}

/**
 * get saved user language code,default `en`
 * */
fun getUserLanguage(context: Context): String {
    return shared(context).getString(LANGUAGE, "en")
        .toSafetyString("en")
}


/**
 * clear user save. e.g: sharedPreferences
 * unRegister/stop background works depending on user, e.g: PushNotification,DownloadFiles..
 * */
fun appLogOut(context: Context, startActivity: Class<Activity>, block: () -> Unit) {
    block()
    context.startActivity(Intent(context, startActivity).also {
        //        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

//endregion


//region time&Date

fun convertToDate(dateText: String?, format: String, locale: Locale): Date? {
    if (dateText.isEmptyText())
        return null
    val df = SimpleDateFormat(format, locale)
    df.timeZone = TimeZone.getTimeZone("UTC")
    var date: Date? = null
    try {
        date = df.parse(dateText)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return date
}

fun convertDateFormat(dateText: String?, format: String, toFormat: String, locale: Locale): String {
    if (dateText.isEmptyText())
        return ""
    //date&time
    val df = SimpleDateFormat(format, locale)
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    var date: Date? = null
    try {
        date = df.parse(dateText)
    } catch (e: ParseException) {
        e.printStackTrace()
        return ""
    }
    df.setTimeZone(TimeZone.getDefault());
//        String formattedDate = df.format(date);
    df.applyPattern(toFormat)
    val dt = df.format(date);
    return dt
}

fun convertDateFormat(date: Date?, toFormat: String, locale: Locale): String {
    if (date == null)
        return ""
    //date&time
    val df = SimpleDateFormat(toFormat, locale)
    df.timeZone = TimeZone.getDefault()
    val dt = df.format(date)
    return dt
}

fun convertTimeFormat(dateText: String?, format: String, toFormat: String, locale: Locale): String {
    if (dateText.isEmptyText())
        return ""
    //date&time
    val df = SimpleDateFormat(format, locale)

    var date: Date? = null
    try {
        date = df.parse(dateText)
    } catch (e: ParseException) {
        e.printStackTrace()
        return ""
    }

    df.applyPattern(toFormat)
    val dt = df.format(date);
    return dt
}


/**
 * Functionality to get millisecond Time ago
 * */
fun getMillSecondTimeAgo(date: String, format: String): Long? {

    val df = SimpleDateFormat(format, Locale.ENGLISH)
    df.timeZone = TimeZone.getTimeZone("UTC")
    var date2: Date? = null
    try {
        date2 = df.parse(date)
    } catch (err: Exception) {
        err.printStackTrace()
    }
    df.timeZone = TimeZone.getDefault()
    //get diff millSeconds
    if (date2 != null) {
//        var l = (date2.getTime() + TimeZone.getDefault().getOffset(date2.getTime()));
        val l = System.currentTimeMillis() - date2.time
        return l
    } else
        return null
}

data class TimeAgo(val days: Long, val hours: Long, val minutes: Long)

/**
 * Functionality to get days,hours,minutes ago
 * */
fun getTimeAgo(date: String, format: String): TimeAgo? {
    val milliseconds = getMillSecondTimeAgo(date, format)
    milliseconds ?: return null
//    val seconds = TimeUnit.SECONDS.convert(milliseconds, TimeUnit.MILLISECONDS) 1568192245291
    val minutes = TimeUnit.MINUTES.convert(milliseconds, TimeUnit.MILLISECONDS)
    val hours = TimeUnit.HOURS.convert(milliseconds, TimeUnit.MILLISECONDS)
    val days = TimeUnit.DAYS.convert(milliseconds, TimeUnit.MILLISECONDS)
    return TimeAgo(days, hours, minutes)
}

/**
 * get time ago from now to specific date
 * */
//fun getTimeAgo(date: String, format: String = HttpConstants.Values.API_DATE_FORMAT): Long? {
//
//    val df = SimpleDateFormat(format, Locale.ENGLISH)
//    df.timeZone = TimeZone.getTimeZone("UTC")
//    var date2: Date? = null;
//    try {
//        date2 = df.parse(date);
//    } catch (err: Exception) {
//        err.printStackTrace();
//    }
//
//    df.timeZone = TimeZone.getDefault()
//
//
//    //end Test
//    //get diff millSeconds
//    if (date2 != null) {
////        var l = (date2.getTime() + TimeZone.getDefault().getOffset(date2.getTime()));
//        var l = System.currentTimeMillis() - date2.getTime();
//        return l
//
//    } else
//        return null
//}

//2019-09-10T13:13:47.861Z

/*Functionality to format Time ago */
//fun ago(
//    context: Context,
//    milliseconds: Long?,
//    date: String?,
//    locale: Locale,
//    format: String,
//    toFormat: String,
//    maxDays: Int
//): String {
//    //        long diff = System.currentTimeMillis() - milliseconds;
//    //        long calMilli = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.MILLISECONDS);
//    if (milliseconds == null) {
//        return convertDateFormat(date, format, toFormat, locale)
//    }
////    val seconds = TimeUnit.SECONDS.convert(milliseconds, TimeUnit.MILLISECONDS) 1568192245291
//    val minutes = TimeUnit.MINUTES.convert(milliseconds, TimeUnit.MILLISECONDS)
//    val hours = TimeUnit.HOURS.convert(milliseconds, TimeUnit.MILLISECONDS)
//    val days = TimeUnit.DAYS.convert(milliseconds, TimeUnit.MILLISECONDS)
//    val ago: String
//    if (minutes < 2) {
//        ago = context.resources.getString(R.string.justnow_time)
//    } else if (minutes < 60) {
//        ago =
//            context.resources.getQuantityString(R.plurals.min_ago, minutes.toInt(), minutes.toInt())
//    } else if (hours < 24) {
//        ago = context.resources.getQuantityString(R.plurals.hour_ago, hours.toInt(), hours.toInt())
//    } else {
//        if (days < 7L) {
//            ago = context.resources.getQuantityString(
//                R.plurals.daysPlurals,
//                days.toInt(),
//                days.toInt()
//            )
//        } else {
//            ago = convertDateFormat(date, format, DATE_FORMAT, locale)
//        }
//    }
//    //context.getString(R.string.daysPlurals)
//
//    return ago
//}
//endregion


//region View
/**
 * get typeface for font inside assets folder.
 * */
fun getTypeFace(context: Context?, fontTypeNameRes: Int): Typeface? {
    context?.let {
        try {
            return Typeface.createFromAsset(it.assets, it.getString(fontTypeNameRes))
        } catch (ex: Exception) {
            return null
        }
    }

    return null
}

/**
 * get typeface for font inside assets folder.
 * */
fun getTypeFace(context: Context?, fontTypeName: String): Typeface? {
    context?.let {
        try {
            return Typeface.createFromAsset(it.assets, fontTypeName)
        } catch (ex: Exception) {
            return null
        }
    }

    return null
}

fun applyTypefaceToText(typeface: Typeface?, text: String): CharSequence {
    return SpannableStringBuilder(text).also {
        it.setSpan(CustomTypeFaceSpan(typeface), 0, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
    }
}

fun pxToDp(px: Float): Float {
    val metrics = Resources.getSystem().displayMetrics
    val dp = px / (metrics.densityDpi / 160f)
    return Math.round(dp).toFloat()
}

fun dpToPx(dp: Float): Float {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return Math.round(px).toFloat()
}

fun EditText.enableNestedScrolling() {
    setOnTouchListener { view, event ->

        if (hasFocus()) {
            view.parent.requestDisallowInterceptTouchEvent(true)
            return@setOnTouchListener when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_SCROLL -> {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                    true
                }
                else -> false
            }
        }
        false
    }


}

fun <T : RecyclerView.Adapter<*>> RecyclerView.getAdapter(): T? = adapter as T?

//fun EditText.validation(){
//
//}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.inVisible() {
    visibility = View.INVISIBLE
}


fun TextView.isEmptyText(): Boolean {
    return text.toString().isEmptyText()
}

/**
 *  get index based in all text,not just the specific line.
 */
fun TextView.getLastCharIndexOfLine(line: Int): Int {
    return this.layout.getLineVisibleEnd(line)
}

/**
 * set clickable part of text with swipe it whiling clicking.
 * e.g Read more <->Read Less
 * */
fun TextView.setToggleClickableSequenceText(
    text: String,
    text2: String,
    textColor: Int,
    maxLines: Int,
    clickCallBack: (() -> Unit)?
) {
    if (maxLines <= 0) return
    if (layout.lineCount > maxLines) {
        val lastChar = getLastCharIndexOfLine(maxLines - 1)
//        tag = this.text.toString()
        if (lastChar > text.length) {
            val finalText = this.text.substring(0, lastChar - text.length) + 1 + " " + text
            val finalSecondText = "${this.text}  $text2"
            if (clickCallBack != null) {
                movementMethod = LinkMovementMethod.getInstance()
                val sText = SpannableStringBuilder(finalText)
                val sText2 = SpannableStringBuilder(finalSecondText)
                val sClick = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        //swipe tag <=> text
                        val tag1 = this@setToggleClickableSequenceText.tag as CharSequence
                        this@setToggleClickableSequenceText.tag =
                            this@setToggleClickableSequenceText.text
                        this@setToggleClickableSequenceText.text = tag1

                        clickCallBack.invoke()

                    }
                }
                //first clickable text
                //click span
                sText.setSpan(
                    sClick,
                    lastChar - text.length + 1,
                    finalText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                //color span
                sText.setSpan(
                    ForegroundColorSpan(textColor),
                    lastChar - text.length + 1,
                    finalText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                //second clickable text
                //click span
                sText2.setSpan(
                    sClick,
                    finalSecondText.length - text2.length,
                    finalSecondText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                //color span
                sText2.setSpan(
                    ForegroundColorSpan(textColor),
                    finalSecondText.length - text2.length,
                    finalSecondText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                this.tag = sText2


                this.text = sText
            } else

                this.text = finalText
        } else {
//            text.subSequence(0,lastChar - text.length)
        }
    }
}


//endregion


//region files

/**
 * try get file mime type
 * */
fun getMimeType(context: Context?, url: String): String? {
    if (url.isEmptyText()) return null
    val file = File(url)
    val ins = BufferedInputStream(FileInputStream(file))
    val mimeType = URLConnection.guessContentTypeFromStream(ins)

    if (mimeType.isNotEmptyText())
        return mimeType

    var type: String? = null
    val uri = Uri.parse(url)
    if (context != null
        && uri.scheme?.equals(ContentResolver.SCHEME_CONTENT) == true
    ) {
        val cr = context.applicationContext.contentResolver
        type = cr.getType(uri)
    } else {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
        } else {
//            type = "*/*"
        }
    }
    return type
}


//endregion

fun File.toBase64(): String? {
    val fileInputStreamReader = FileInputStream(this)
    val bytes = ByteArray(length().toInt())
    fileInputStreamReader.read(bytes)
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

inline fun <reified T> getAs(obj: Any?, block: (o: T) -> Unit) {
    if (obj is T) block(obj)
}

inline fun <reified T> getAs(obj: Any?): T? = if (obj is T) obj else null

fun <T> MutableList<T>.removeItemIf(p: (x: T) -> Boolean) {
    var item: T
    val itemsIterator = iterator()
    while (itemsIterator.hasNext()) {
        item = itemsIterator.next()
        if (p(item))
            itemsIterator.remove()
    }
}

fun <T> MutableList<T>.removeFirstItemIf(p: (x: T) -> Boolean): T? {
    var item: T? = null
    val itemsIterator = iterator()
    while (itemsIterator.hasNext()) {
        item = itemsIterator.next()
        if (p(item)) {
            itemsIterator.remove()
            return item
        }
    }
    return null
}

inline fun catchMe(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

inline fun catchMe(block: () -> Unit, catch: ((e: Exception) -> Unit)) {
    try {
        block()
    } catch (e: Exception) {
        catch(e)
    }
}

inline fun catchMe(block: () -> Unit, catch: ((e: Exception) -> Unit), finally: (() -> Unit)) {
    try {
        block()
    } catch (e: Exception) {
        catch(e)
    } finally {
        finally()
    }
}

fun arabicNumberToDecimal(number: String): String {
    val chars = CharArray(number.length)
    for (x in 0 until number.length) {
        var ch = number[x]
        if (ch >= 0x0660.toChar() && ch <= 0x0669.toChar())
            ch -= 0x0660.toChar() - '0'
        else if (ch >= 0x06f0.toChar() && ch <= 0x06F9.toChar())
            ch -= 0x06f0.toChar() - '0'
        chars[x] = ch
    }

    return String(chars)
}