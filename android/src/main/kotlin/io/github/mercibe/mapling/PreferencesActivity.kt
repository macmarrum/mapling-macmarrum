// SPDX-FileCopyrightText: 2026 Benoit Mercier
// SPDX-License-Identifier: GPL-3.0-or-later

package io.github.mercibe.mapling

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.os.LocaleListCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Preferences, on a page of their own. Today the language only: the phone's, or one of
 * Mapling's. AndroidX keeps the choice (autoStoreLocales, in the manifest) and, from
 * Android 13, shares it with the system's setting (Settings → Apps → Mapling → Language).
 */
class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.preferences)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationContentDescription(R.string.back)
        toolbar.setNavigationOnClickListener { finish() }
        keepClearOfSystemBars()
        findViewById<TextView>(R.id.language_value).setText(LANGUAGES[chosen()].second)
        findViewById<View>(R.id.language).setOnClickListener { chooseLanguage() }
    }

    /** Index in LANGUAGES of the language chosen for Mapling; 0, the phone's, when none is. */
    private fun chosen(): Int {
        val tag = AppCompatDelegate.getApplicationLocales()[0]?.language ?: return 0
        return LANGUAGES.indexOfFirst { it.first == tag }.coerceAtLeast(0)
    }

    /**
     * Applying another language recreates the screens, this one and the map's, as Android
     * does for any change of language: the open map comes back as after any recreation.
     */
    private fun chooseLanguage() {
        val names = LANGUAGES.map { getString(it.second) }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.preference_language)
            .setSingleChoiceItems(names, chosen()) { dialog, which ->
                dialog.dismiss()
                if (which == chosen()) return@setSingleChoiceItems
                val tag = LANGUAGES[which].first
                AppCompatDelegate.setApplicationLocales(
                    if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** From Android 15, the page draws under the system bars, as the map's screen does. */
    private fun keepClearOfSystemBars() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val toolbar = findViewById<View>(R.id.toolbar)
        findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { root, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            toolbar.setPadding(0, bars.top, 0, 0)
            root.setPadding(bars.left, 0, bars.right, bars.bottom)
            WindowInsets.CONSUMED
        }
    }

    private companion object {
        /** The phone's language (""), then Mapling's, as in res/xml/locales_config.xml. */
        val LANGUAGES = listOf(
            "" to R.string.language_system,
            "en" to R.string.language_en,
            "fr" to R.string.language_fr,
        )
    }
}
