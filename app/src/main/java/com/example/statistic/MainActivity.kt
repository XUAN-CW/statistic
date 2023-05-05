package com.example.statistic


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.common.io.Files
import com.google.gson.Gson
import java.io.File
import java.nio.charset.Charset
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

    @SuppressLint("ShowToast")
    private fun addItem() {
        var now = LocalDateTime.now()
        items.add(now.toString())
        adapter.notifyDataSetChanged()
        val toast = Toast.makeText(applicationContext, now.toString(), Toast.LENGTH_SHORT)
    }

    private fun deleteItem(position: Int) {
        items.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    private val fileToSave = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "items.json")

    private fun saveItemsToDisk() {
        val gson = Gson()
        Files.asCharSink(fileToSave, Charset.defaultCharset()).write(gson.toJson(items))
    }


    private fun loadItemsFromDisk(): MutableList<String> {
        Log.i("fileToSave",fileToSave.absolutePath)
        if(!fileToSave.exists()){
            return emptySet<String>().toMutableList()
        }
        val gson = Gson()
        // Read the JSON string from file
        val jsonFromFile = Files.asCharSource(fileToSave, Charset.defaultCharset()).read()
        // Convert the JSON string back to a list of students
        val itemsFromFile = gson.fromJson(jsonFromFile, Array<String>::class.java).toSet()
        itemsFromFile.stream().sorted()
        return itemsFromFile.toMutableList()
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