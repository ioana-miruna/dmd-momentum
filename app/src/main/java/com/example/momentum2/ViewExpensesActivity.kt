package com.example.momentum2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.momentum2.ui.theme.Momentum2Theme
import org.json.JSONArray

class ViewExpensesActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)

        val totalExpensesText = findViewById<TextView>(R.id.totalExpenses)
        val totalFoodText = findViewById<TextView>(R.id.totalFoodExpenses)
        val totalOtherText = findViewById<TextView>(R.id.totalOtherExpenses)
        val expenseListView = findViewById<ListView>(R.id.expenseListView)

        val sharedPreferences = getSharedPreferences("ExpensesData", MODE_PRIVATE)
        val expenses = sharedPreferences.getString("expenses", "[]")
        val expensesList = JSONArray(expenses)

        var totalExpenses = 0.0
        var totalFood = 0.0
        var totalOthers = 0.0
        val monthlyBudget = 2000

        val expenseArray = ArrayList<String>()
        for (i in 0 until expensesList.length()) {
            val expense = expensesList.getJSONObject(i)
            val amount = expense.getDouble("amount")
            val category = expense.getString("category")

            totalExpenses += amount
            if (category == "Food") totalFood += amount
            else totalOthers += amount

            expenseArray.add("${expense.getString("name")}: $amount ($category)")
        }

        totalExpensesText.text = "Total Expenses: $totalExpenses"
        totalFoodText.text = "Total Food: $totalFood"
        totalOtherText.text = "Total Others: $totalOthers"

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, expenseArray)
        expenseListView.adapter = adapter

        val infoButton = findViewById<ImageButton>(R.id.info_button)
        val shareButton = findViewById<ImageButton>(R.id.share_button)
        val backToMainButton = findViewById<ImageButton>(R.id.backToMain_button)

        infoButton.setOnClickListener {
            Toast.makeText(this, "You have ${monthlyBudget - totalExpenses} left from your Monthly Budget", Toast.LENGTH_LONG).show()
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "This month expenses were is $totalExpenses. $totalFood was spent on food, and $totalOthers on others")
            startActivity(Intent.createChooser(shareIntent, "Share your Expenses"))
        }
        backToMainButton.setOnClickListener {
            finish()
        }
    }

}
