package com.segula.parkingtracker

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

data class LocationDef(
    val name: String, val type: String, val maneuver: String,
    val options: String, val coordinates: String
)

class MainActivity : AppCompatActivity() {
    private val vehicles = listOf("3980", "3982")

    private val locations = listOf(
        LocationDef("Long Low Steel Beam Crossbar · Outdoor", "Continuous", "Perpendicular",
            "Outdoor · Day / Night\nVariation: parked vehicle left, right or both sides",
            "48.890726, 9.169700"),
        LocationDef("Long Low Steel Beam Crossbar · Indoor", "Continuous", "Perpendicular",
            "Indoor\nVariation: parked vehicle left, right or both sides",
            "48.890726, 9.169700"),
        LocationDef("\"W\" Steel Beam Crossbar", "Continuous", "Perpendicular",
            "Outdoor · Day / Night\nVariation: parked vehicle left, right or both sides · no wall/fence",
            "48.710902, 8.980459"),
        LocationDef("Mid Height Steel Crossbar", "Single", "Perpendicular",
            "Outdoor · Day / Night\nVariation: parked vehicle left, right or both sides · no wall/fence",
            "48.7939754, 9.2233783"),
        LocationDef("Wide Green Crossbar", "Single", "Perpendicular",
            "Outdoor · Day / Night\nVariation: parked vehicle left, right or both sides · no wall/fence",
            "48.792827, 9.226150")
    )

    private lateinit var prefs: android.content.SharedPreferences
    private var entries = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("parking_collection", MODE_PRIVATE)
        entries = try { JSONArray(prefs.getString("entries", "[]")) } catch (_: Exception) { JSONArray() }

        val vehicle = findViewById<Spinner>(R.id.vehicle)
        val location = findViewById<Spinner>(R.id.location)
        val variation = findViewById<Spinner>(R.id.variation)
        val info = findViewById<TextView>(R.id.locationInfo)

        vehicle.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vehicles)
        vehicle.setSelection(prefs.getInt("vehicle_index", 0))

        location.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations.map { it.name })
        variation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Car / parked vehicle", "Wall", "Empty"))

        location.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val l = locations[position]
                info.text = "${l.maneuver} parking · ${l.type} crossbar\n${l.options}\nCoordinates: ${l.coordinates}"
                updateStats()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.complete).setOnClickListener {
            prefs.edit().putInt("vehicle_index", vehicle.selectedItemPosition).apply()
            val loc = locations[location.selectedItemPosition]
            val dir = if (findViewById<RadioButton>(R.id.forward).isChecked) "Forward" else "Reverse"
            val side = if (findViewById<RadioButton>(R.id.left).isChecked) "Left" else "Right"
            val vari = variation.selectedItem.toString()

            // Minimal collection record by design: vehicle + predefined location + scenario only.
            val record = JSONObject()
                .put("vehicle", vehicles[vehicle.selectedItemPosition])
                .put("location", loc.name)
                .put("scenario", "$dir · $side · $vari")

            entries.put(record)
            save()
            findViewById<TextView>(R.id.lastEntry).text =
                "Last: Vehicle ${record.getString("vehicle")} · ${record.getString("scenario")}"
            updateStats()
            Toast.makeText(this, "Maneuver counted", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.undo).setOnClickListener {
            if (entries.length() > 0) {
                entries.remove(entries.length() - 1)
                save()
                findViewById<TextView>(R.id.lastEntry).text = "Last maneuver removed."
                updateStats()
                Toast.makeText(this, "Last maneuver removed", Toast.LENGTH_SHORT).show()
            }
        }

        updateStats()
    }

    private fun save() {
        prefs.edit().putString("entries", entries.toString()).apply()
    }

    private fun updateStats() {
        var forward = 0; var reverse = 0; var left = 0; var right = 0
        var car = 0; var wall = 0; var empty = 0
        val perLocation = IntArray(locations.size)

        for (i in 0 until entries.length()) {
            val e = entries.getJSONObject(i)
            val s = e.optString("scenario")
            if (s.contains("Forward")) forward++ else if (s.contains("Reverse")) reverse++
            if (s.contains("Left")) left++ else if (s.contains("Right")) right++
            if (s.contains("Car / parked vehicle")) car++
            if (s.contains("Wall")) wall++
            if (s.contains("Empty")) empty++
            val idx = locations.indexOfFirst { it.name == e.optString("location") }
            if (idx >= 0) perLocation[idx]++
        }

        val selected = findViewById<Spinner>(R.id.location).selectedItemPosition.coerceAtLeast(0)
        val locCount = perLocation.getOrElse(selected) { 0 }
        findViewById<TextView>(R.id.stats).text =
            "TOTAL: ${entries.length()} maneuvers\n" +
            "Selected location: $locCount\n\n" +
            "Forward $forward  ·  Reverse $reverse\n" +
            "Left $left  ·  Right $right\n" +
            "Car $car  ·  Wall $wall  ·  Empty $empty\n\n" +
            "Target: not defined"
    }
}
