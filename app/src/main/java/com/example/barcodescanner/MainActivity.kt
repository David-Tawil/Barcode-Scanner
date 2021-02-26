package com.example.barcodescanner


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.barcodescanner.settings.PreferenceUtil
import com.example.barcodescanner.settings.SettingsActivity
import com.example.barcodescanner.utilities.UtilTools
import com.example.barcodescanner.viewModel.ItemsModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_app_bar))

        setTheme()
        //theme mode
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .registerOnSharedPreferenceChangeListener(this)


        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_scanner, R.id.navigation_search
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        ItemsModel().pullDataToDB(this)
    }


    private fun setTheme() {
        when (PreferenceUtil.themeMode(applicationContext)) {
            resources.getString(R.string.dark_theme_value) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            resources.getString(R.string.light_theme_value) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)



        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_update_data -> {

                update_progress_bar.show()
                val snackbar = Snackbar.make(
                    container,
                    getString(R.string.updating_pls_wait_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                GlobalScope.launch {
                    try {
                        withTimeout(60000) {
                            if (ItemsModel().newDataAvail(this@MainActivity)) {
                                snackbar.show()
                                ItemsModel().updateData(this@MainActivity)
                                snackbar.dismiss()
                                runOnUiThread {
                                    update_progress_bar.hide()
                                    // snackbar.dismiss()
                                    // progress_bar_main.hide()
                                    val lastTime = this@MainActivity.getPreferences(MODE_PRIVATE)
                                        .getLong(getString(R.string.saved_last_updated_time_key), 0)

                                    MaterialAlertDialogBuilder(
                                        this@MainActivity,
                                        R.style.MaterialAlertDialog__Center
                                    )
                                        .setTitle(getString(R.string.update_success))
                                        .setMessage(
                                            getString(
                                                R.string.last_update,
                                                UtilTools.getReadableRelativeDate(
                                                    applicationContext,
                                                    lastTime
                                                )
                                            )
                                        )
                                        .setNeutralButton(getString(R.string.thumb_up), null)
                                        .show()
                                }

                            } else {
                                runOnUiThread {
                                    update_progress_bar.hide()
                                    // snackbar.dismiss()
                                    // progress_bar_main.hide()
                                    val lastTime = this@MainActivity.getPreferences(MODE_PRIVATE)
                                        .getLong(getString(R.string.saved_last_updated_time_key), 0)

                                    MaterialAlertDialogBuilder(
                                        this@MainActivity,
                                        R.style.MaterialAlertDialog__Center
                                    )
                                        .setTitle(getString(R.string.no_update_avail))
                                        .setMessage(
                                            getString(
                                                R.string.last_update,
                                                UtilTools.getReadableRelativeDate(
                                                    applicationContext,
                                                    lastTime
                                                )
                                            )
                                        )
                                        .setNeutralButton(getString(R.string.ok), null)
                                        .show()
                                }
                            }
                        }
                    } catch (t: TimeoutCancellationException) {
                        t.printStackTrace()
                        runOnUiThread {
                            update_progress_bar.hide()
                            snackbar.dismiss()
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                R.style.MaterialAlertDialog__Center
                            )
                                .setTitle("")
                                .setMessage(getString(R.string.update_data_error_msg))
                                .setNeutralButton(getString(R.string.ok), null)
                                .show()
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                return true
            }

            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == resources.getString(R.string.pref_key_dark_theme)) {
            /*val intent = intent    // from getIntent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            finish()
            startActivity(intent)*/
            recreate()
        }
    }
}