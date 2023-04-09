package com.example.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.URL

@Component
class ToppingMetricsCollector {

    data class Topping(
        val email: String,
        val toppings: List<String>
    )

    @Autowired
    lateinit var toppingMetricsRepository: ToppingMetricsRepository

    /**
     * Call the external API every X seconds/minutes/hours based on cron value.
     * This will update the in memory toppings metrics that we have collected so far.
     */
    // Scheduled update every ten seconds
    @Scheduled(cron = "*/10 * * * * *")
    fun collectToppingMetrics() {

        val url = URL("http://localhost:8080/toppings/get-toppings")

        val json = url.readText()
        val objectMapper = ObjectMapper().registerModule(KotlinModule())

        val toppings = objectMapper.readValue(json, Array<Topping>::class.java)

        val totalCountPerTopping = mutableMapOf<String, Int>()
        val uniqueUserCountPerTopping = mutableMapOf<String, MutableSet<String>>()

        for (topping in toppings) {
            for (t in topping.toppings) {
                totalCountPerTopping[t] = totalCountPerTopping.getOrDefault(t, 0) + 1
                uniqueUserCountPerTopping.getOrPut(t) { mutableSetOf() }.add(topping.email)
            }
        }

        toppingMetricsRepository.updatePopularList(calculatePopularCombos(toppings))
        toppingMetricsRepository.updateMetrics(totalCountPerTopping, uniqueUserCountPerTopping)
    }

    fun calculatePopularCombos(toppings: Array<Topping>): Map<List<String>, Int> {
        val popularCombinations = mutableMapOf<List<String>, Int>()

        for (topping in toppings) {
            for (i in 0 until topping.toppings.size) {
                for (j in i + 1 until topping.toppings.size) {
                    val combination = listOf(topping.toppings[i], topping.toppings[j])
                    popularCombinations[combination] = popularCombinations.getOrDefault(combination, 0) + 1
                }
            }
        }
        return popularCombinations
    }
}