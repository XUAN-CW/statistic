package com.example.statistic


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var items: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var addButton: Button
    private lateinit var copyButton: Button
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        items = loadItemsFromDisk()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView = findViewById(R.id.list_view)
        addButton = findViewById(R.id.add_button)
        copyButton = findViewById(R.id.copy_button)

        listView.adapter = adapter

        addButton.setOnClickListener {
            addItem()
            saveItemsToDisk()
        }

        copyButton.setOnClickListener {
            copyItemsToClipboard()
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            deleteItem(position)
            saveItemsToDisk()
        }

        listView.setOnItemLongClickListener { parent, view, position, id ->
            deleteItem(position)
            saveItemsToDisk()
            true
        }
    }

    private fun addItem() {
        items.add(LocalDateTime.now().toString())
        adapter.notifyDataSetChanged()
    }

    private fun deleteItem(position: Int) {
        items.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    private fun saveItemsToDisk() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putStringSet("items", items.toSet())
            apply()
        }
    }

    private fun loadItemsFromDisk(): MutableList<String> {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val itemSet = prefs.getStringSet("items", emptySet()) ?: emptySet()
        return itemSet.toMutableList()
    }

    private fun copyItemsToClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val itemsText = items.joinToString("\n")
        val clip = ClipData.newPlainText("List Items", itemsText)
        clipboardManager.setPrimaryClip(clip)
    }

    inner class ItemAdapter(context: Context, resource: Int, items: MutableList<String>) :
        ArrayAdapter<String>(context, resource, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)

            val itemText = itemView.findViewById<TextView>(R.id.item_text)
            val deleteButton = itemView.findViewById<Button>(R.id.delete_button)

            itemText.text = items[position]

            deleteButton.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteItem(position)
                        saveItemsToDisk()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            return itemView
        }
    }

    override fun onResume() {
        super.onResume()
        adapter = ItemAdapter(this, R.layout.item_layout, items)
        listView.adapter = adapter
    }

}