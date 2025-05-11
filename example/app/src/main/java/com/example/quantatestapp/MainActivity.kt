package com.example.quantatestapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quantatestapp.databinding.ActivityMainBinding
import java.util.Date
import tools.quanta.sdk.Quanta
import tools.quanta.sdk.util.QuantaLogger

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Simple event logging
        binding.btnLogSimpleEvent.setOnClickListener { logSimpleEvent() }

        // Event logging with arguments
        binding.btnLogEventWithArgs.setOnClickListener { logEventWithArguments() }

        // Event logging with revenue
        binding.btnLogEventWithRevenue.setOnClickListener { logEventWithRevenue() }

        // Show user ID
        binding.btnShowUserId.setOnClickListener { showUserId() }
    }

    private fun logSimpleEvent() {
        try {
            Quanta.log("test_simple_event")
            showSuccess("Simple event logged")
            QuantaLogger.i("Simple event logged")
        } catch (e: Exception) {
            showError("Failed to log simple event: ${e.message}")
            QuantaLogger.e("Failed to log simple event", e)
        }
    }

    private fun logEventWithArguments() {
        try {
            val args =
                    mapOf(
                            "source" to "button_click",
                            "screen" to "main_activity",
                            "timestamp" to Date().time.toString()
                    )
            Quanta.log("test_event_with_args", args)
            showSuccess("Event with arguments logged")
            QuantaLogger.i("Event with arguments logged: $args")
        } catch (e: Exception) {
            showError("Failed to log event with arguments: ${e.message}")
            QuantaLogger.e("Failed to log event with arguments", e)
        }
    }

    private fun logEventWithRevenue() {
        try {
            val revenue = 99.99
            val args =
                    mapOf(
                            "product_id" to "premium_subscription",
                            "currency" to "USD",
                            "is_trial" to "false"
                    )
            Quanta.log("purchase_event", revenue, args)
            showSuccess("Event with revenue logged: $revenue")
            QuantaLogger.i("Event with revenue logged: $revenue, args: $args")
        } catch (e: Exception) {
            showError("Failed to log event with revenue: ${e.message}")
            QuantaLogger.e("Failed to log event with revenue", e)
        }
    }

    private fun showSuccess(message: String) {
        binding.tvStatus.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        binding.tvStatus.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showUserId() {
        try {
            val userId = QuantaTestHelper.getUserId()
            binding.tvStatus.text = "User ID: $userId"
            QuantaLogger.i("Displayed user ID: $userId")
        } catch (e: Exception) {
            showError("Failed to get user ID: ${e.message}")
            QuantaLogger.e("Failed to get user ID", e)
        }
    }
}
