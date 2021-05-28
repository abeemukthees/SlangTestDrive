package com.msa.slangtestdrive

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.msa.slangtestdrive.third.SlangLabsCommunicator
import kotlinx.android.synthetic.main.activity_search_result.*

class SearchResultActivity : AppCompatActivity() {

    companion object {

        const val tag = "SearchResultActivity"

        fun open(context: Context, source: String, destination: String, date: String) {
            val intent = Intent(context, SearchResultActivity::class.java).apply {
                putExtra("source", source)
                putExtra("destination", destination)
                putExtra("date", date)
            }
            with(context) {
                startActivity(intent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search results"

        val source = intent.getStringExtra("source")
        val destination = intent.getStringExtra("destination")
        val date = intent.getStringExtra("date")

        text_title.text = "$source - $destination"
        text_subTitle.text = date

        SlangLabsCommunicator.getInstance(application).hideTrigger(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return false
    }
}