package com.raafat.revise.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.raafat.revise.R
import com.raafat.revise.databinding.ActivityMainBinding
import com.raafat.revise.model.Aya
import com.raafat.revise.numberOfAyahsForSuraArray
import com.raafat.revise.pageForSuraArray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var ayaList: List<Aya>

    private lateinit var spinner: Spinner
    private lateinit var slider: Slider
    private lateinit var quranContent: WebView
    private lateinit var hide: MaterialSwitch
    private lateinit var previousAya: ImageButton
    private lateinit var pageCount: TextView
    private lateinit var ayaCount: TextView
    private lateinit var clicker: View

    private var isDialogShown = false
    private var isSpinnerItemClickedBefore = false

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        spinner = activityMainBinding.suraSpinner
        slider = activityMainBinding.slider
        quranContent = activityMainBinding.quranContent
        hide = activityMainBinding.hide
        previousAya = activityMainBinding.previousAya
        pageCount = activityMainBinding.pageCount
        ayaCount = activityMainBinding.ayaCount
        clicker = activityMainBinding.clicker

        val suraNames = resources.getStringArray(R.array.sura_names)

        val getSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        val savedSura = getSharedPrefs.getInt("spinner", 0)
        val savedAya = getSharedPrefs.getFloat("slider", 1f)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initializeSuraSpinner(suraNames = suraNames, savedSura = savedSura)
        handleSpinnerClicks(savedAya = savedAya)

        observeAyaList {
            handleSliderClicks()
            updateQuranContent(ayaList, savedAya.toInt())
        }

        previousAya.setOnClickListener {
            if (!hide.isChecked) {
                quranContent.evaluateJavascript(
                    """
                (function() {
                    const ayaTextDiv = document.getElementById('ayaText');
                    const hasOverflow = ayaTextDiv.scrollHeight > ayaTextDiv.clientHeight;
                    const isNotScrolled = ayaTextDiv.scrollTop === 0;
                
                    return hasOverflow && !isNotScrolled;
                })();
                """.trimIndent()
                ) { hasOverflow ->
                    if (hasOverflow.toBoolean() && !hide.isChecked) {
                        quranContent.evaluateJavascript(
                            """
                        (function() {
                            const ayaTextDiv = document.getElementById('ayaText');
                            const scrollAmount = parseFloat(getComputedStyle(document.documentElement).fontSize) * 4; 
                            ayaTextDiv.scroll({
                                top: 0,
                                behavior: 'smooth'
                            });
                        })();
                        """.trimIndent(), null
                        )
                    } else if (slider.value > 1) {
                        updateQuranContent(ayaList, (--slider.value).toInt())
                    } else {
                        if (spinner.selectedItemPosition > 0) {
                            val sura =
                                (spinner.adapter.getItem(spinner.selectedItemPosition - 1) as String).substringAfter(
                                    " "
                                )

                            showCustomDialog(context = this@MainActivity,
                                message = getString(R.string.go_to).plus(sura),
                                onPositiveClick = {
                                    spinner.setSelection(spinner.selectedItemPosition - 1)
                                })

                        }
                    }
                }
            } else {
                quranContent.evaluateJavascript(
                    "javascript:showPrev(${slider.value.toInt()})"
                ) { hasPrev ->
                    if (hasPrev.toBoolean()) {
                        if (slider.value > 1) {
                            updateQuranContentPrev(ayaList, (--slider.value).toInt())
                        }
                        else {
                            if (slider.value == 1f && spinner.selectedItemPosition > 0) {
                                val sura =
                                    (spinner.adapter.getItem(spinner.selectedItemPosition - 1) as String).substringAfter(
                                        " "
                                    )

                                showCustomDialog(context = this@MainActivity,
                                    message = getString(R.string.go_to).plus(sura),
                                    onPositiveClick = {
                                        spinner.setSelection(spinner.selectedItemPosition - 1)
                                    })

                            }
                        }
                    }
                }
            }
        }
        clicker.setOnClickListener {
            if (!hide.isChecked) {
                quranContent.evaluateJavascript(
                    """
                (function() {
                    const ayaTextDiv = document.getElementById('ayaText');
                    const hasOverflow = ayaTextDiv.scrollHeight > ayaTextDiv.clientHeight;
                    const isScrolledToBottom = ayaTextDiv.scrollTop + ayaTextDiv.clientHeight >= ayaTextDiv.scrollHeight - 2;
               
                    return hasOverflow && !isScrolledToBottom;
                })();
                """.trimIndent()
                ) { hasOverflow ->
                    if (hasOverflow.toBoolean()) {
                        quranContent.evaluateJavascript(
                            """
                        (function() {
                            let y;
                            if (window.matchMedia("(min-width: 600px)").matches) {
                                y = 6
                            } else {
                                y = 4
                            }
                            const ayaTextDiv = document.getElementById('ayaText');
                            const scrollAmount = parseFloat(getComputedStyle(document.documentElement).fontSize) * y; 
                            const numLines = Math.floor(window.innerHeight / scrollAmount);

                            ayaTextDiv.scroll({
                                top: ayaTextDiv.scrollTop + scrollAmount * ( numLines - 1),
                                behavior: 'smooth'
                            });
                        })();
                        """.trimIndent(), null
                        )
                    } else if (slider.value < slider.valueTo) updateQuranContent(
                        ayaList, (++slider.value).toInt()
                    )
                    else {
                        if (spinner.selectedItemPosition < spinner.adapter.count - 1) {
                            val sura =
                                (spinner.adapter.getItem(spinner.selectedItemPosition + 1) as String).substringAfter(
                                    " "
                                )

                            showCustomDialog(context = this@MainActivity,
                                message = getString(R.string.go_to).plus(sura),
                                onPositiveClick = {
                                    spinner.setSelection(spinner.selectedItemPosition + 1)
                                })
                        }
                    }
                }
            } else {
                quranContent.evaluateJavascript(
                    "javascript:showNext()"
                ) { hasNext ->
                    if (hasNext.toBoolean()) {
                        if (slider.value < slider.valueTo) {
                            updateQuranContent(ayaList, (++slider.value).toInt())
                        } else {
                            if (spinner.selectedItemPosition < spinner.adapter.count - 1) {
                                val sura =
                                    (spinner.adapter.getItem(spinner.selectedItemPosition + 1) as String).substringAfter(
                                        " "
                                    )

                                showCustomDialog(context = this@MainActivity,
                                    message = getString(R.string.go_to).plus(sura),
                                    onPositiveClick = {
                                        spinner.setSelection(spinner.selectedItemPosition + 1)
                                    })
                            }
                        }
                    }
                }
            }
        }

        hide.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                quranContent.evaluateJavascript("toggleHideShow();", null)
            } else {
                quranContent.evaluateJavascript("toggleHideShow();", null)
            }
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun updateQuranContentPrev(ayaList: List<Aya>, ayaNo: Int) {
        val currentAya =
            ayaList.first { it.sora == (spinner.selectedItemPosition + 1) && it.ayaNo == ayaNo }.ayaText

        if (!quranContent.url.isNullOrEmpty()) {
            quranContent.evaluateJavascript("javascript:receiveAyaPrev(\'$currentAya\')", null)
            return
        }

        quranContent.apply {
            loadUrl("file:///android_asset/index.html")
            settings.javaScriptEnabled = true
            settings.textZoom = 100
            setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    evaluateJavascript("javascript:receiveAyaPrev(\'$currentAya\')", null)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun updateQuranContent(ayaList: List<Aya>, ayaNo: Int) {
        val currentAya =
            ayaList.first { it.sora == (spinner.selectedItemPosition + 1) && it.ayaNo == ayaNo }.ayaText

        updateAyaChosen(ayaNo.toFloat())

        if (!quranContent.url.isNullOrEmpty()) {
            quranContent.evaluateJavascript("javascript:receiveAya(\'$currentAya\')", null)
            return
        }

        quranContent.apply {
            loadUrl("file:///android_asset/index.html")
            settings.javaScriptEnabled = true
            settings.textZoom = 100
            setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    evaluateJavascript("javascript:receiveAya(\'$currentAya\')", null)
                }
            }
        }
    }


    private fun observeAyaList(onInitialized: () -> Unit) {
        lifecycleScope.launch {
            viewModel.ayaListFlow.collect { list ->
                if (list.isNotEmpty()) {
                    ayaList = list
                    onInitialized()
                }
            }
        }
    }

    private fun initializeSuraSpinner(savedSura: Int, suraNames: Array<String>) {
        val adapter = CustomSpinnerAdapter(this, suraNames)
        spinner.adapter = adapter
        spinner.setSelection(savedSura)
    }

    private fun updateAyaChosen(value: Float) {
        val chosenAyah =
            ayaList.first { it.sora == (spinner.selectedItemPosition + 1) && it.ayaNo == value.toInt() }
        pageCount.text = getString(R.string.page_word_ar).plus(chosenAyah.page.toString())
        ayaCount.text = getString(R.string.aya_word_ar).plus(chosenAyah.ayaNo.toString())
        slider.value = chosenAyah.ayaNo.toFloat()
    }

    private fun handleSpinnerClicks(savedAya: Float) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                if (isSpinnerItemClickedBefore) {
                    hide.isChecked = false

                    slider.value = 1f
                    slider.valueFrom = 1f
                    slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

                    pageCount.text =
                        getString(R.string.page_word_ar).plus(pageForSuraArray[position].toString())
                    ayaCount.text = getString(R.string.aya_word_ar).plus(1)

                    if (this@MainActivity::ayaList.isInitialized) {
                        updateQuranContent(ayaList, 1)
                    }

                } else {
                    isSpinnerItemClickedBefore = true
                    slider.valueFrom = 1f
                    slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()
                    slider.value = savedAya
                    if (this@MainActivity::ayaList.isInitialized) {
                        updateQuranContent(ayaList, savedAya.toInt())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun handleSliderClicks() {
        slider.addOnChangeListener { _, value, _ ->
            viewModel.onSliderValueChanged(value)
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.onStartTouch()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.onStopTouch()
                hide.isChecked = false
            }
        })

        viewModel.sliderValue.observe(this) { value ->
            updateAyaChosen(value)
        }

        viewModel.isTouching.observe(this) { isTouching ->
            if (!isTouching) {
                updateQuranContent(ayaList, slider.value.toInt())
            }
        }
    }

    private fun showCustomDialog(
        context: Context,
        message: String,
        onPositiveClick: () -> Unit = {},
    ) {
        if (isDialogShown) return // Prevent showing the dialog if it's already shown

        val customLayout = LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null)
        val messageTextView = customLayout.findViewById<TextView>(R.id.dialog_message)
        val positiveButton = customLayout.findViewById<Button>(R.id.positive_button)
        val negativeButton = customLayout.findViewById<Button>(R.id.negative_button)

        messageTextView.text = message

        val dialog = MaterialAlertDialogBuilder(context).setView(customLayout)
            .setCancelable(false) // Prevent dismissal on back button press
            .create()

        dialog.setCanceledOnTouchOutside(false) // Prevent dismissal on outside touch

        positiveButton.setOnClickListener {
            onPositiveClick()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            isDialogShown = false // Reset the flag when the dialog is dismissed
        }

        isDialogShown = true
        dialog.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL

        dialog.show()
    }


    override fun onPause() {
        super.onPause()
        val putSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        putSharedPrefs.edit().putInt("spinner", spinner.selectedItemPosition).apply()
        putSharedPrefs.edit().putFloat("slider", slider.value).apply()
    }


    override fun attachBaseContext(newBase: Context?) {

        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)

        super.attachBaseContext(newBase)
    }
}

