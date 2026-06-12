package dam

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlin.test.Test
import java.util.Properties
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Section 3.3 Temperature Tests
 * Demonstrates how changes in the temperature value lead to noticeably different outputs.
 */
class TemperatureTest {

    @Test
    fun testLowTemperature() = runBlocking {
        println("\n--- Running testLowTemperature ---")
        val baseProps = getProperties()
        if (baseProps.getProperty("GEMINI_API_KEY").isNullOrBlank() && 
            baseProps.getProperty("OPENAI_API_KEY").isNullOrBlank()) {
            println("Skipping test: No API keys configured.")
            return@runBlocking
        }

        val props = baseProps.clone() as Properties
        props.setProperty("temperature", "0.1")
        props.setProperty("max_tokens", "150")
        
        val assistant = AIAssistantFactory.createAssistant(props)
        println("Using assistant system: ${assistant.getSystem()} with model: ${assistant.model}")

        // Simple prompt with a highly probable answer to ensure consistency
        val prompt = "Name a single common color. Respond with exactly one word only, e.g. 'Blue'."
        val responses = mutableListOf<String>()

        try {
            repeat(3) { i ->
                if (i > 0) delay(12000L) // Wait between calls to prevent 429
                val res = assistant.processInput(prompt)
                println("Response ${i + 1} (temp=0.1): $res")
                assertNotNull(res)
                responses.add(res.trim().lowercase())
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("429") || msg.contains("401") || msg.contains("quota") || msg.contains("key") || msg.contains("limit")) {
                println("Skipping test verification: API rate limit/quota or key issue: ${e.message}")
                return@runBlocking
            }
            throw e
        }

        val uniqueResponses = responses.toSet()
        println("Unique responses count (temp=0.1): ${uniqueResponses.size} / 3")
        // Low temperature should result in highly consistent/repetitive outputs.
        // We assert that at least some responses are identical (unique count <= 2)
        assertTrue(uniqueResponses.size <= 2, "Low temperature should result in highly consistent outputs.")
    }

    @Test
    fun testHighTemperature() = runBlocking {
        println("\n--- Running testHighTemperature ---")
        val baseProps = getProperties()
        if (baseProps.getProperty("GEMINI_API_KEY").isNullOrBlank() && 
            baseProps.getProperty("OPENAI_API_KEY").isNullOrBlank()) {
            println("Skipping test: No API keys configured.")
            return@runBlocking
        }

        val props = baseProps.clone() as Properties
        props.setProperty("temperature", "1.9") // Max randomness
        props.setProperty("max_tokens", "150")

        val assistant = AIAssistantFactory.createAssistant(props)
        println("Using assistant system: ${assistant.getSystem()} with model: ${assistant.model}")

        // Creative request where high temperature will lead to different responses
        val prompt = "Name a single color, but be as creative and unusual as possible. Respond with exactly one word only."
        val responses = mutableListOf<String>()

        try {
            repeat(3) { i ->
                if (i > 0) delay(12000L) // Wait between calls to prevent 429
                val res = assistant.processInput(prompt)
                println("Response ${i + 1} (temp=1.9): $res")
                assertNotNull(res)
                responses.add(res.trim().lowercase())
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("429") || msg.contains("401") || msg.contains("quota") || msg.contains("key") || msg.contains("limit")) {
                println("Skipping test verification: API rate limit/quota or key issue: ${e.message}")
                return@runBlocking
            }
            throw e
        }

        val uniqueResponses = responses.toSet()
        println("Unique responses count (temp=1.9): ${uniqueResponses.size} / 3")
    }
}
