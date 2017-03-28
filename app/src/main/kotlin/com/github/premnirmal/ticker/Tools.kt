package com.github.premnirmal.ticker

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Environment
import com.github.premnirmal.ticker.model.StocksProvider
import com.github.premnirmal.ticker.network.Stock
import com.github.premnirmal.tickerwidget.R
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.text.DecimalFormat
import java.text.Format
import java.util.ArrayList
import java.util.Arrays
import java.util.Random

/**
 * Created by premnirmal on 2/26/16.
 */
class Tools private constructor(private val sharedPreferences: SharedPreferences) {

  enum class ChangeType {
    value, percent
  }

  companion object {

    val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:MM")

    @JvmField
    val PREFS_NAME = "com.github.premnirmal.ticker"
    @JvmField
    val FONT_SIZE = "com.github.premnirmal.ticker.textsize"
    @JvmField
    val START_TIME = "START_TIME"
    @JvmField
    val END_TIME = "END_TIME"
    @JvmField
    val SETTING_AUTOSORT = "SETTING_AUTOSORT"
    @JvmField
    val ENABLE_GOOGLE_FINANCE = "ENABLE_GOOGLE_FINANCE"
    @JvmField
    val WIDGET_BG = "WIDGET_BG"
    @JvmField
    val TEXT_COLOR = "TEXT_COLOR"
    @JvmField
    val UPDATE_INTERVAL = "UPDATE_INTERVAL"
    @JvmField
    val TRANSPARENT = 0
    @JvmField
    val TRANSLUCENT = 1
    @JvmField
    val DARK = 2
    @JvmField
    val LIGHT = 3
    @JvmField
    val LAYOUT_TYPE = "LAYOUT_TYPE"
    @JvmField
    val BOLD_CHANGE = "BOLD_CHANGE"
    @JvmField
    val FIRST_TIME_VIEWING_SWIPELAYOUT = "FIRST_TIME_VIEWING_SWIPELAYOUT"
    @JvmField
    val WHATS_NEW = "WHATS_NEW"
    @JvmField
    val PERCENT = "PERCENT"
    @JvmField
    val DID_RATE = "DID_RATE"
    @JvmField
    val DECIMAL_FORMAT: Format = DecimalFormat("0.00")

    private val random = Random(System.currentTimeMillis())

    val instance: Tools by lazy {
      val sharedPreferences = StocksApp.instance.getSharedPreferences(Tools.PREFS_NAME,
          Context.MODE_PRIVATE)
      Tools(sharedPreferences)
    }

    private fun trackInitial(context: Context) {
      Analytics.trackIntialSettings(LAYOUT_TYPE,
          if (stockViewLayout() == 0) "Animated" else "Tabbed")
      Analytics.trackIntialSettings(TEXT_COLOR,
          if (instance.sharedPreferences.getInt(TEXT_COLOR, 0) == 0) "White" else "Dark")
      Analytics.trackIntialSettings(START_TIME,
          instance.sharedPreferences.getString(START_TIME, "09:30"))
      Analytics.trackIntialSettings(END_TIME,
          instance.sharedPreferences.getString(END_TIME, "09:30"))
      Analytics.trackIntialSettings(SETTING_AUTOSORT, java.lang.Boolean.toString(autoSortEnabled()))
      Analytics.trackIntialSettings(WIDGET_BG,
          instance.sharedPreferences.getInt(WIDGET_BG, TRANSPARENT).toString())
      Analytics.trackIntialSettings(FONT_SIZE, getFontSize(context).toString())
      run {
        val updatePref = instance.sharedPreferences.getInt(Tools.UPDATE_INTERVAL, 1)
        val time = AlarmManager.INTERVAL_FIFTEEN_MINUTES * (updatePref + 1)
        Analytics.trackIntialSettings(UPDATE_INTERVAL, time.toString())
      }
      Analytics.trackIntialSettings(BOLD_CHANGE, java.lang.Boolean.toString(boldEnabled()))
      val tickerListVars = instance.sharedPreferences.getString(StocksProvider.SORTED_STOCK_LIST,
          "EMPTY!")
    }

    val changeType: ChangeType
      get() {
        val state = instance.sharedPreferences.getBoolean(PERCENT, false)
        return if (state) ChangeType.percent else ChangeType.value
      }

    fun flipChange() {
      val state = instance.sharedPreferences.getBoolean(PERCENT, false)
      instance.sharedPreferences.edit().putBoolean(PERCENT, !state).apply()
    }

    fun stockViewLayout(): Int {
      val pref = instance.sharedPreferences.getInt(LAYOUT_TYPE, 0)
      if (pref == 0) {
        return R.layout.stockview
      } else if (pref == 1) {
        return R.layout.stockview2
      } else {
        return R.layout.stockview3
      }
    }

    fun getTextColor(context: Context): Int {
      val pref = instance.sharedPreferences.getInt(TEXT_COLOR, 0)
      return if (pref == 0) Color.WHITE else context.resources.getColor(R.color.dark_text)
    }

    val updateInterval: Long
      get() {
        val pref = instance.sharedPreferences.getInt(UPDATE_INTERVAL, 1)
        return AlarmManager.INTERVAL_FIFTEEN_MINUTES * (pref + 1)
      }

    fun timeAsIntArray(time: String): IntArray {
      val split = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      val times = intArrayOf(Integer.valueOf(split[0])!!, Integer.valueOf(split[1])!!)
      return times
    }

    fun startTime(): IntArray {
      val startTimeString = instance.sharedPreferences.getString(START_TIME, "09:30")
      return timeAsIntArray(startTimeString)
    }

    fun endTime(): IntArray {
      val endTimeString = instance.sharedPreferences.getString(END_TIME, "16:30")
      return timeAsIntArray(endTimeString)
    }

    fun autoSortEnabled(): Boolean {
      return instance.sharedPreferences.getBoolean(SETTING_AUTOSORT, true)
    }

    fun firstTimeViewingSwipeLayout(): Boolean {
      val firstTime = instance.sharedPreferences.getBoolean(FIRST_TIME_VIEWING_SWIPELAYOUT, true)
      instance.sharedPreferences.edit().putBoolean(FIRST_TIME_VIEWING_SWIPELAYOUT, false).apply()
      return firstTime || (random.nextInt() % 2 == 0)
    }

    fun getBackgroundResource(context: Context): Int {
      val bgPref = instance.sharedPreferences.getInt(WIDGET_BG, TRANSPARENT)
      when (bgPref) {
        TRANSLUCENT -> return R.drawable.translucent_widget_bg
        DARK -> return R.drawable.dark_widget_bg
        LIGHT -> return R.drawable.light_widget_bg
        else -> return R.drawable.transparent_widget_bg
      }
    }

    fun getFontSize(context: Context): Float {
      val size = instance.sharedPreferences.getInt(FONT_SIZE, 1)
      when (size) {
        0 -> return context.resources.getInteger(R.integer.text_size_small).toFloat()
        2 -> return context.resources.getInteger(R.integer.text_size_large).toFloat()
        else -> return context.resources.getInteger(R.integer.text_size_medium).toFloat()
      }
    }

    val tickersFile: File
      get() {
        val dir = Environment.getExternalStoragePublicDirectory("StockTickers")
        if (!dir.exists()) {
          dir.mkdir()
        }
        val fileName = "Tickers.txt"
        val file = File(dir, fileName)
        return file
      }

    fun isNetworkOnline(context: Context): Boolean {
      try {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val i = connectivityManager.activeNetworkInfo ?: return false
        if (!i.isConnected) return false
        if (!i.isAvailable) return false
        return true
      } catch (e: Exception) {
        e.printStackTrace()
        return false
      }

    }

    fun toCommaSeparatedString(list: List<String>): String {
      val builder = StringBuilder()
      for (string in list) {
        builder.append(string)
        builder.append(",")
      }
      val length = builder.length
      if (length > 1) {
        builder.deleteCharAt(length - 1)
      }
      return builder.toString()
    }

    fun positionsToString(stockList: List<Stock>): String {
      val builder = StringBuilder()
      for (stock in stockList) {
        if (stock.IsPosition == true) {
          builder.append(stock.symbol)
          builder.append(",")
          builder.append(stock.IsPosition)
          builder.append(",")
          builder.append(stock.PositionPrice)
          builder.append(",")
          builder.append(stock.PositionShares)
          builder.append("\n")
        }
      }
      return builder.toString()
    }

    fun stringToPositions(positions: String): MutableList<Stock> {
      val tickerListCSV = ArrayList(Arrays.asList(
          *positions.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
      val stockList = ArrayList<Stock>()
      var tickerFields: ArrayList<String>
      var tmpStock: Stock
      for (tickerCSV in tickerListCSV) {
        tickerFields = ArrayList(Arrays.asList(
            *tickerCSV.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        if (tickerFields.size >= 4 && java.lang.Boolean.parseBoolean(tickerFields[1]) == true) {
          tmpStock = Stock()
          tmpStock.IsPosition = true
          tmpStock.symbol = tickerFields[0]
          tmpStock.PositionPrice = java.lang.Float.parseFloat(tickerFields[2])
          tmpStock.PositionShares = java.lang.Float.parseFloat(tickerFields[3]).toInt()
          stockList.add(tmpStock)
        }
      }
      return stockList
    }

    fun boldEnabled(): Boolean {
      return instance.sharedPreferences.getBoolean(BOLD_CHANGE, false)
    }

    fun userDidRate() {
      instance.sharedPreferences.edit().putBoolean(DID_RATE, true).apply()
    }

    fun hasUserAlreadyRated(): Boolean {
      return instance.sharedPreferences.getBoolean(DID_RATE, false)
    }

    fun shouldPromptRate(): Boolean {
      // if the user hasn't rated, try again on occasions
      return (random.nextInt() % 2 == 0) && !hasUserAlreadyRated()
    }

    fun googleFinanceEnabled(): Boolean {
      return instance.sharedPreferences.getBoolean(Tools.ENABLE_GOOGLE_FINANCE, true)
    }

    fun getStatusBarHeight(): Int {
      val result: Int
      val resourceId: Int = StocksApp.instance.resources.getIdentifier("status_bar_height", "dimen",
          "android")
      if (resourceId > 0) {
        result = StocksApp.instance.resources.getDimensionPixelSize(resourceId)
      } else {
        result = 0
      }
      return result
    }
  }
}