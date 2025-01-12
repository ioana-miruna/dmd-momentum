package com.example.momentum2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
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
import org.json.JSONObject

class AddExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val expenseName = findViewById<EditText>(R.id.expenseName)
        val expenseAmount = findViewById<EditText>(R.id.expenseAmount)
        val categoryGroup = findViewById<RadioGroup>(R.id.categoryGroup)
        val btnAdd = findViewById<Button>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val name = expenseName.text.toString()
            val amount = expenseAmount.text.toString().toDoubleOrNull()
            val category = when (categoryGroup.checkedRadioButtonId) {
                R.id.radioFood -> "Food"
                R.id.radioOthers -> "Others"
                else -> ""
            }

            if (amount == null || category.isEmpty()) {
                Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save data in SharedPreferences
            val sharedPreferences = getSharedPreferences("ExpensesData", MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val expenses = sharedPreferences.getString("expenses", "[]")
            val expensesList = JSONArray(expenses)
            val expenseObject = JSONObject()
            expenseObject.put("name", name)
            expenseObject.put("amount", amount)
            expenseObject.put("category", category)
            expensesList.put(expenseObject)

            editor.putString("expenses", expensesList.toString())
            editor.apply()

            Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
