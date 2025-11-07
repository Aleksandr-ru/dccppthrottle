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
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5MappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5SettingsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2MappingFragment
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
        if (savedInstanceState == null) {
            val fragment = when (intent.extras?.getInt(MENU_ID)) {
                R.id.action_lp5_mapping -> {
                    title = getString(R.string.title_fragment_lp5_mapping)
                    Lp5MappingFragment.newInstance()
                }
                R.id.action_lp5_outputs -> {
                    title = getString(R.string.title_fragment_lp5_outputs)
                    Lp5OutputsFragment.newInstance()
                }
                R.id.action_lp5_settings -> {
                    title = getString(R.string.title_fragment_lp5_settings)
                    Lp5SettingsFragment.newInstance()
                }
                R.id.action_sw2_mapping -> {
                    title = getString(R.string.title_fragment_sw2_mapping)
                    Sw2MappingFragment.newInstance()
                }
//                R.id.action_sw2_outputs -> {
//                    title = getString(R.string.title_fragment_sw2_outputs)
//                }
//                R.id.action_sw2_settings -> {
//                    title = getString(R.string.title_fragment_sw2_settings)
//                }
                R.id.action_xp4_mapping_simple -> {
                    title = getString(R.string.title_fragment_xp4_mapping_simple)
                    Xp4SimpleMappingFragment.newInstance()
                }
                R.id.action_xp4_mapping -> {
                    title = getString(R.string.title_fragment_xp4_mapping)
                    Xp4MappingFragment.newInstance()
                }
                R.id.action_xp4_outputs -> {
                    title = getString(R.string.title_fragment_xp4_outputs)
                    Xp4OutputsFragment.newInstance()
                }
                R.id.action_xp4_settings -> {
                    title = getString(R.string.title_fragment_xp4_settings)
                    Xp4SettingsFragment.newInstance()
                }
                R.id.action_xp5_mapping_simple -> {
                    title = getString(R.string.title_fragment_xp5_mapping_simple)
                    Xp5SimpleMappingFragment.newInstance()
                }
                R.id.action_xp5_outputs -> {
                    title = getString(R.string.title_fragment_xp5_outputs)
                    Xp5OutputsFragment.newInstance()
                }
                R.id.action_xp5_settings -> {
                    title = getString(R.string.title_fragment_xp5_settings)
                    Xp5SettingsFragment.newInstance()
                }
                else -> throw Exception("Invalid menu id")
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
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