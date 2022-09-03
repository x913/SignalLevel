package com.github.x913.signallevel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.view.children
import com.github.x913.signallevel.databinding.ActivityMainBinding

abstract class SeekBarChange : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val seekBarChange = object: SeekBarChange() {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when(seekBar?.id) {
                R.id.bars_count -> binding.signalLevel.signalBarsCount = progress
                R.id.bars_gap -> binding.signalLevel.signalBarsGap = progress
                R.id.bars_signal_level -> binding.signalLevel.signalLevel = progress / 100f
                R.id.bars_corners -> binding.signalLevel.signalBarCorners = progress.toFloat()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        with(binding) {
            barsCount.setOnSeekBarChangeListener(seekBarChange)
            barsGap.setOnSeekBarChangeListener(seekBarChange)
            barsSignalLevel.setOnSeekBarChangeListener(seekBarChange)
            barsCorners.setOnSeekBarChangeListener(seekBarChange)
        }

    }
}
