package com.example.viruses

import android.annotation.SuppressLint
import android.app.UiModeManager.MODE_NIGHT_NO
import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import com.zeugmasolutions.localehelper.Locales
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonLogout.setOnClickListener {
            requireActivity().onBackPressed()
        }

        buttonRus.setOnClickListener {
            (requireActivity() as LocaleAwareCompatActivity).updateLocale(Locales.Russian)
        }

        buttonEng.setOnClickListener {
            (requireActivity() as LocaleAwareCompatActivity).updateLocale(Locales.English)
        }

        seekBarMode.progress = when (AppCompatDelegate.getDefaultNightMode()) {
            MODE_NIGHT_YES -> 0
            MODE_NIGHT_NO -> 1
            else -> 1
        }

        seekBarMode.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("WrongConstant")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 1)  {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }  else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }

        })

        seekBarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("WrongConstant")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val textNew = progress.toFloat() + 16
                val buttonNew = progress.toFloat() + 14
                textViewLightmode.textSize = textNew
                textViewFont.textSize = textNew
                buttonEng.textSize = buttonNew
                buttonRus.textSize = buttonNew
                buttonLogout.textSize = buttonNew
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }

        })
    }
}