package com.example.mindcheckdatacollectionapp

import android.content.Context
import android.util.Log
import android.util.DisplayMetrics
import android.view.WindowManager

import co.thingthing.fleksy.core.bus.events.EventBasedDataCaptureEvent
import co.thingthing.fleksy.core.keyboard.KeyboardConfiguration
import co.thingthing.fleksy.core.keyboard.KeyboardService
import co.thingthing.fleksy.core.keyboard.models.EventDataConfiguration
import co.thingthing.fleksy.core.languages.KeyboardLanguage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.io.File
import java.io.FileInputStream
import java.util.Properties

import kotlin.math.pow
import kotlin.math.sqrt


class SampleKeyboardService : KeyboardService() {

    private val currentLanguage
        get() = KeyboardLanguage("en-US")

    private var totalKeystrokes = 0
    private var xprev = 0.0f
    private var yprev = 0.0f

    private val HTList = mutableListOf<Float>()
    private val FTList = mutableListOf<Float>()
    private val SPList = mutableListOf<Float>()
    private val PFRList = mutableListOf<Float>()

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser


    private fun getScreenDensity(context: Context): Float {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    }

    private fun getPPIX(context: Context): Float {
        val screenDensity = getScreenDensity(context)
        return context.resources.displayMetrics.xdpi / screenDensity
    }

    private fun getPPIY(context: Context): Float {
        val screenDensity = getScreenDensity(context)
        return context.resources.displayMetrics.ydpi / screenDensity
    }

    private fun calculateMedian(numbers: List<Float>): Float {
        val n = numbers.size
        if (n < 4) {
            return Float.NaN
        }
        val sortedNumbers = numbers.sorted()
        val middle = sortedNumbers.size / 2
        return if (sortedNumbers.size % 2 == 0) {
            (sortedNumbers[middle - 1] + sortedNumbers[middle]) / 2.0f
        } else {
            sortedNumbers[middle]
        }
    }

    private fun calculateSD(numbers: List<Float>): Float {
        val n = numbers.size
        if (n < 4) {
            return Float.NaN
        }

        val mean = numbers.sum() / n
        val sumofSquaredDifferences = numbers.map { (it - mean).pow(2) }.sum()

        return sqrt(sumofSquaredDifferences / n)
    }

    private fun calculateSkewness(numbers: List<Float>): Float {
        val n = numbers.size
        if (n < 4) {
            return Float.NaN
        }

        val mean = numbers.sum() / n
        val sd = calculateSD(numbers)
        val sumOfCubedDifferences = numbers.map { (it - mean) / sd }.map { it.pow(3) }.sum()

        return ((n / ((n - 1) * (n - 2).toDouble())) * sumOfCubedDifferences).toFloat()
    }

    private fun calculateKurtosis(numbers: List<Float>): Float {
        val n = numbers.size
        if (n < 4) {
            return Float.NaN
        }

        val mean = numbers.sum() / n
        val sd = calculateSD(numbers)
        val sumOfFourthPowers = numbers.map { (it - mean) / sd }.map { it.pow(4) }.sum()

        return (n * (n + 1).toFloat() / ((n - 1) * (n - 2) * (n - 3).toFloat())) * sumOfFourthPowers -
                3 * ((n - 1).toFloat().pow(2) / ((n - 2) * (n - 3).toFloat()))
    }

    private fun calculateSpeed(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        screenDensityX: Float,
        screenDensityY: Float,
        flightTime: Float
    ): Float {
        val deltaX = ((x2 - x1) / screenDensityX) * 25.4f
        val deltaY = ((y2 - y1) / screenDensityY) * 25.4f
        return (sqrt(deltaX * deltaX + deltaY * deltaY.toDouble()).toFloat()) / flightTime
    }

    override fun onCreate() {
        super.onCreate()

        var lastReleaseTime: Long? = null

        eventBus.eventBasedDataCapture.subscribe {
            //Data Capture received here
            when (it) {
                is EventBasedDataCaptureEvent.KeyStrokeCaptureEvent -> {
                    // get pressTime and releaseTime from API
                    val pressTime = it.keyStroke.pressTime
                    val releaseTime = it.keyStroke.releaseTime
                    val keyPressBegin = it.keyStroke.keyPressBegin
                    val keyPressEnd = it.keyStroke.keyPressEnd

                    //Calculate HT, FT, SP, PFR from values above
                    val holdTime: Float = (releaseTime - pressTime).toFloat()
                    val flightTime: Float =
                        lastReleaseTime?.let { last -> (pressTime - last).toFloat() } ?: 0.0f
                    lastReleaseTime = releaseTime
                    var pfr = 0.0f
                    var speed = calculateSpeed(
                        xprev, yprev, keyPressEnd?.x ?: -1.0f,
                        keyPressEnd?.y ?: -1.0f, getPPIX(this), getPPIY(this), flightTime
                    )

                    if (flightTime != 0.0f) {
                        pfr = (holdTime / flightTime)
                        totalKeystrokes++

                        if (holdTime > 300) {
                            Log.d("HT", "Excluded: Unexceptionally long hold time")
                        } else {
                            HTList.add(holdTime)
                        }

                        if (flightTime > 3000 || flightTime < 0) {
                            Log.d("FT", "Excluded: Unexceptionally long flight time")
                        } else {
                            FTList.add(flightTime)
                        }
                        PFRList.add(pfr)
                        SPList.add(speed)
                        if (keyPressEnd != null) {
                            xprev = keyPressEnd.x
                            yprev = keyPressEnd.y
                        }
                        Log.d("KBTAGKS", totalKeystrokes.toString())
                    } else {
                        Log.d("KBTAGPFR", "Cannot divide by 0")
                    }
                }

                is EventBasedDataCaptureEvent.DeleteCaptureEvent -> {}
                is EventBasedDataCaptureEvent.KeyPlaneCaptureEvent -> {}
                is EventBasedDataCaptureEvent.WordCaptureEvent -> {}
                is EventBasedDataCaptureEvent.SwipeCaptureEvent -> {}
                is EventBasedDataCaptureEvent.SessionUpdateCaptureEvent -> {
                    Log.d("DEBUG", "Session update event received")
                    Log.d("CLOSEEEE", it.sessionUpdate.endTimestamp.toString())

                    val condition1 = it.sessionUpdate.endTimestamp.toString() == "0"
                    val condition2 = user != null
                    val condition3 = totalKeystrokes > 8
                    val allConditionsMet = listOf(condition1, condition2, condition3).all { it }

                    if (!condition2) {
                        Log.d("DEBUG", "User is null")
                    }
                    if (!condition1) {
                        Log.d("DEBUG", "End timestamp not 0")
                    }
                    if (!condition3) {
                        Log.d("DEBUG", "Total keystrokes less than 8")
                    }
                    if (allConditionsMet) {
                        Log.d("DEBUG", "Session ended, all conditions met")
                        val medianHT = calculateMedian(HTList)
                        val medianFT = calculateMedian(FTList)
                        val medianSP = calculateMedian(SPList)
                        val medianPFR = calculateMedian(PFRList)

                        val sdHT = calculateSD(HTList)
                        val sdFT = calculateSD(FTList)
                        val sdSP = calculateSD(SPList)
                        val sdPFR = calculateSD(PFRList)

                        val skewnessHT = calculateSkewness(HTList)
                        val skewnessFT = calculateSkewness(FTList)
                        val skewnessSP = calculateSkewness(SPList)
                        val skewnessPFR = calculateSkewness(PFRList)

                        val kurtosisHT = calculateKurtosis(HTList)
                        val kurtosisFT = calculateKurtosis(FTList)
                        val kurtosisSP = calculateKurtosis(SPList)
                        val kurtosisPFR = calculateKurtosis(PFRList)

                        Log.d("DEBUG", "All calculations done, checking conditions")

                        // If all the val above > 0, then add to database
                        //condition error! skewness and kurtosis can be below 0
                        if (!(listOf(
                                medianHT,
                                medianFT,
                                medianSP,
                                medianPFR,
                                sdHT,
                                sdFT,
                                sdSP,
                                sdPFR,
                                skewnessHT,
                                skewnessFT,
                                skewnessSP,
                                skewnessPFR,
                                kurtosisHT,
                                kurtosisFT,
                                kurtosisSP,
                                kurtosisPFR
                            ).any { it -> it.isNaN() })
                        ) {
                            Log.d("DEBUG", "Conditions met, writing to database")
                            val db = FirebaseFirestore.getInstance()
                            val userId = user?.uid
                            val data = hashMapOf(
                                "userId" to userId,
                                "medianHT" to medianHT,
                                "medianFT" to medianFT,
                                "medianSP" to medianSP,
                                "medianPFR" to medianPFR,
                                "sdHT" to sdHT,
                                "sdFT" to sdFT,
                                "sdSP" to sdSP,
                                "sdPFR" to sdPFR,
                                "skewnessHT" to skewnessHT,
                                "skewnessFT" to skewnessFT,
                                "skewnessSP" to skewnessSP,
                                "skewnessPFR" to skewnessPFR,
                                "kurtosisHT" to kurtosisHT,
                                "kurtosisFT" to kurtosisFT,
                                "kurtosisSP" to kurtosisSP,
                                "kurtosisPFR" to kurtosisPFR,
                                "totalCharTyped" to totalKeystrokes,
                                "timestamp" to FieldValue.serverTimestamp(),
                                "trained" to "False"
                            )

                            db.collection("typingSession").add(data)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "DBFAdd",
                                        "Document added with ID: ${documentReference.id}"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.d("DBFail", "Error adding document: $e")
                                }
                        } else {
                            Log.d("DEBUG", "Some conditions not met")
                        }
                    } else {
                        Log.d("DEBUG", "End Timestamp not 0")
                    }
                    HTList.clear()
                    FTList.clear()
                    SPList.clear()
                    PFRList.clear()
                    totalKeystrokes = 0
                }

                is EventBasedDataCaptureEvent.StressUpdateCaptureEvent -> {}
            }
        }
    }

    override fun createConfiguration(): KeyboardConfiguration {
        val props = Properties()
        try {
            applicationContext.assets.open("secrets.properties").use { stream ->
                props.load(stream)
            }
        } catch (e: Exception) {
            Log.e("DEBUG", "Failed to load secrets.properties", e)
        }

        val licenseKey = props.getProperty("LICENSE_KEY")
        val licenseSecret = props.getProperty("LICENSE_SECRET")

        return KeyboardConfiguration(
            language = KeyboardConfiguration.LanguageConfiguration(
                current = currentLanguage,
                automaticDownload = true,
                orderMode = KeyboardConfiguration.LanguageOrderMode.STATIC
            ),
            license = KeyboardConfiguration.LicenseConfiguration(
                licenseKey = licenseKey,
                licenseSecret = licenseSecret
            ),
            dataCapture = KeyboardConfiguration.DataCaptureMode.EventBased(
                EventDataConfiguration(
                    keyStroke = true,
                    word = true,
                    sessionUpdate = true,
                    stressUpdate = true
                )
            )
        )
    }
}