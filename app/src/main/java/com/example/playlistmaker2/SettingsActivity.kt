package com.example.playlistmaker2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val returnFrameLayout = findViewById<FrameLayout>(R.id.return_frame)
        val shareFrameLayout = findViewById<FrameLayout>(R.id.shareFrameLayout)
        val writeToSupportFrameLayout = findViewById<FrameLayout>(R.id.writeToSupportFrameLayout)
        val userAgreementFrameLayout = findViewById<FrameLayout>(R.id.userAgreementFrameLayout)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        val darkThemeEnabled: Boolean = sharedPreferences.getBoolean(DARK_THEME, false)
        themeSwitcher.isChecked = darkThemeEnabled

        returnFrameLayout.setOnClickListener {
            val displayIntent = Intent(this, MainActivity::class.java)
            displayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(displayIntent)
        }
        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)

            sharedPreferences.edit()
                .putBoolean(DARK_THEME, checked)
                .apply()
        }
        shareFrameLayout.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body))
            startActivity(shareIntent)
        }
        writeToSupportFrameLayout.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.write_to_support_subject))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.write_to_support_body))
            startActivity(shareIntent)
        }
        userAgreementFrameLayout.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.data = Uri.parse(getString(R.string.user_agreement_url))
            startActivity(shareIntent)
        }
    }
}