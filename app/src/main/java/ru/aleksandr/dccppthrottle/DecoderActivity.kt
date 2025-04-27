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
import ru.aleksandr.dccppthrottle.ui.decoder.Lp5MappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.Lp5OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.Xp4MappingFragment
import ru.aleksandr.dccppthrottle.ui.decoder.Xp4OutputsFragment
import ru.aleksandr.dccppthrottle.ui.decoder.Xp5OutputsFragment

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
                R.id.action_xp4_mapping -> {
                    title = getString(R.string.title_fragment_xp4_mapping)
                    Xp4MappingFragment.newInstance()
                }
                R.id.action_xp4_outputs -> {
                    title = getString(R.string.title_fragment_xp4_outputs)
                    Xp4OutputsFragment.newInstance()
                }
                R.id.action_xp5_outputs -> {
                    title = getString(R.string.title_fragment_xp5_outputs)
                    Xp5OutputsFragment.newInstance()
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