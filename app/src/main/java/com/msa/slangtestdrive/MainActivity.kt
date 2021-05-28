package com.msa.slangtestdrive

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.msa.slangtestdrive.base.Action
import com.msa.slangtestdrive.third.SlangLabsCommunicator
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val slangLabsCommunicator by lazy {
        SlangLabsCommunicator.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        button_clear.setOnClickListener {
            edit_source.text?.clear()
            edit_destination.text?.clear()
            edit_date_of_journey.text?.clear()
        }
        button_show_trigger.setOnClickListener {
            slangLabsCommunicator.showTrigger(this)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        slangLabsCommunicator.showTrigger(this)
        slangLabsCommunicator.actions.observe(this, {
            processActions(it)
        })
    }

    private fun processActions(action: Action) {
        Log.d(TAG, "processActions = ${action.javaClass.simpleName}")
        when (action) {

            is SlangLabsCommunicator.SlangLabsAction.OpenSearchScreenAction -> {
                Log.d(TAG, "$action")
                updateViews(action.sourceIdName, action.destinationIdName, action.dateOfJourney)
                //slangLabsCommunicator.hideTrigger(this)
                SearchResultActivity.open(
                    this,
                    action.sourceIdName.second,
                    action.destinationIdName.second,
                    action.dateOfJourney.toString()
                )
            }

            is SlangLabsCommunicator.SlangLabsAction.PrintLogAction -> {
                text_log.text = action.textList.joinToString(separator = "\n")
            }
        }
    }

    private fun updateViews(
        sourceIdName: Pair<Int, String>,
        destinationIdName: Pair<Int, String>,
        dateOfJourney: Date
    ) {

        edit_source.setText(sourceIdName.second)
        edit_destination.setText(destinationIdName.second)
        edit_date_of_journey.setText(dateOfJourney.toString())

    }
}