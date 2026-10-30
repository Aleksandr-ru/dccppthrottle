/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5MappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5SettingsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2MappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2SettingsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4MappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4SettingsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4SimpleMappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp5OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp5SettingsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp5SimpleMappingFragment

class DecoderActivity : AwakeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prog)
        val screen: Pair<Int, () -> Fragment> = when (intent.extras?.getInt(MENU_ID)) {
            R.id.action_lp5_mapping -> R.string.title_fragment_lp5_mapping to { Lp5MappingFragment.newInstance() }
            R.id.action_lp5_outputs -> R.string.title_fragment_lp5_outputs to { Lp5OutputsFragment.newInstance() }
            R.id.action_lp5_settings -> R.string.title_fragment_lp5_settings to { Lp5SettingsFragment.newInstance() }
            R.id.action_sw2_mapping -> R.string.title_fragment_sw2_mapping to { Sw2MappingFragment.newInstance() }
            R.id.action_sw2_outputs -> R.string.title_fragment_sw2_outputs to { Sw2OutputsFragment.newInstance() }
            R.id.action_sw2_settings -> R.string.title_fragment_sw2_settings to { Sw2SettingsFragment.newInstance() }
            R.id.action_xp4_mapping_simple -> R.string.title_fragment_xp4_mapping_simple to { Xp4SimpleMappingFragment.newInstance() }
            R.id.action_xp4_mapping -> R.string.title_fragment_xp4_mapping to { Xp4MappingFragment.newInstance() }
            R.id.action_xp4_outputs -> R.string.title_fragment_xp4_outputs to { Xp4OutputsFragment.newInstance() }
            R.id.action_xp4_settings -> R.string.title_fragment_xp4_settings to { Xp4SettingsFragment.newInstance() }
            R.id.action_xp5_mapping_simple -> R.string.title_fragment_xp5_mapping_simple to { Xp5SimpleMappingFragment.newInstance() }
            R.id.action_xp5_outputs -> R.string.title_fragment_xp5_outputs to { Xp5OutputsFragment.newInstance() }
            R.id.action_xp5_settings -> R.string.title_fragment_xp5_settings to { Xp5SettingsFragment.newInstance() }
            else -> throw IllegalArgumentException("Invalid menu id")
        }
        title = getString(screen.first)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, screen.second())
                .commitNow()
        }
    }

    companion object {
        private val MENU_ID = "menuid"

        @JvmStatic
        fun start(context: Context, menuId: Int) {
            val intent = Intent(context, DecoderActivity::class.java)
            intent.putExtra(MENU_ID, menuId)
            context.startActivity(intent)
        }
    }
}