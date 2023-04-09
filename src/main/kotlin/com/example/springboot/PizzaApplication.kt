package com.example.springboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableScheduling
class PizzaApplication

/**
 * Interface to implement for the metrics API
 */
interface ToppingMetricsRepository {
	fun updateMetrics(totalCountPerTopping: Map<String, Int>, uniqueUserCountPerTopping: Map<String, MutableSet<String>>)
	fun getTotalCountPerTopping(): Map<String, Int>
	fun getUniqueUserCountPerTopping(): Map<String, Int>
	fun getMostPopularToppings(count: Int): List<String>
	fun getMostPopularToppingCombos(count: Int): List<List<String>>
	fun getLeastPopularToppings(count: Int): List<String>
	fun getToppings(): List<ToppingMetricsCollector.Topping>
	fun updateToppings(toppings: List<ToppingMetricsCollector.Topping>)
    fun updatePopularList(popularList: Map<List<String>, Int>)
}

/**
 * For the purpose of this exercise, in-memory seems appropriate. In
 * a larger scaled environment we'd want to switch to a database that stores this info.
 */
@Component
class InMemoryToppingMetricsRepository : ToppingMetricsRepository {

	private val totalCountPerTopping = mutableMapOf<String, Int>()
	private val uniqueUserCountPerTopping = mutableMapOf<String, MutableSet<String>>()
    private val popularCombos = mutableMapOf<List<String>, Int>()
	private val toppings = mutableListOf<ToppingMetricsCollector.Topping>()

	/**
	 * Update the Total Count metric as well as the Unique User Count per Topping metric.
	 * This is called every time the ToppingMetricsCollector.collectToppingMetrics function
	 * is called, which is set on a schedule.
	 */
	override fun updateMetrics(
		totalCountPerTopping: Map<String, Int>,
		uniqueUserCountPerTopping: Map<String, MutableSet<String>>
	) {
		synchronized(this) {
			this.totalCountPerTopping.putAll(totalCountPerTopping)
			for ((topping, users) in uniqueUserCountPerTopping) {
				this.uniqueUserCountPerTopping.getOrPut(topping) { mutableSetOf() }.addAll(users)
			}
		}
	}

    override fun updatePopularList(popularCombos: Map<List<String>, Int>) {
        synchronized(this) {
			this.popularCombos.putAll(popularCombos)
		}
    }

	/**
	 * Get the total count of toppings
	 */
	override fun getTotalCountPerTopping(): Map<String, Int> {
		synchronized(this) {
			return totalCountPerTopping.toMap()
		}
	}

	/**
	 * Get the unique users per topping
	 */
	override fun getUniqueUserCountPerTopping(): Map<String, Int> {
		synchronized(this) {
			return uniqueUserCountPerTopping.mapValues { it.value.size }.toMap()
		}
	}

	/**
	 * Get the most popular topping
	 */
	override fun getMostPopularToppings(count: Int): List<String> {
		synchronized(this) {
			return totalCountPerTopping.entries.sortedByDescending { it.value }.take(count).map { it.key }
		}
	}

	override fun getMostPopularToppingCombos(count: Int): List<List<String>> {
		synchronized(this) {
			return popularCombos.entries.sortedByDescending { it.value }.take(count).map { it.key }
		}
	}

	/**
	 * Get the least popular topping
	 */
	override fun getLeastPopularToppings(count: Int): List<String> {
		synchronized(this) {
			return totalCountPerTopping.entries.sortedBy { it.value }.take(count).map { it.key }
		}
	}

	/**
	 * Added for the purpose of local testing. This would not be necessary if access
	 * to an external API exists. This returns the list of Topping objects, which is
	 * created using a tuple of (email: String, toppings: List<String>) in json format.
	 * eg. {"email": "example@example.com", "toppings": ["pepperoni", "mushrooms"]}
	 */
	override fun getToppings(): List<ToppingMetricsCollector.Topping> {
		synchronized(this) {
			return this.toppings
		}
	}

	/**
	 * Another method used for testing. This adds to the Topping list. Duplicates are allowed.
	 */
	override fun updateToppings(toppingList: List<ToppingMetricsCollector.Topping>) {
		synchronized(this) {
			this.toppings.addAll(toppingList)
		}
	}
}

@RestController
@RequestMapping("/toppings")
class ToppingMetricsController(
	private val toppingMetricsRepository: ToppingMetricsRepository
) {

	@PostMapping("/update-metrics")
	fun updateMetrics(@RequestBody toppings: List<ToppingMetricsCollector.Topping>) {
		val totalCountPerTopping = mutableMapOf<String, Int>()
		val uniqueUserCountPerTopping = mutableMapOf<String, MutableSet<String>>()

		for ((email, toppingList) in toppings) {
			for (topping in toppingList) {
				totalCountPerTopping[topping] = totalCountPerTopping.getOrDefault(topping, 0) + 1
				uniqueUserCountPerTopping.getOrPut(topping) { mutableSetOf() }.add(email)
			}
		}

		toppingMetricsRepository.updateMetrics(totalCountPerTopping, uniqueUserCountPerTopping)
	}

	@GetMapping("/get-toppings")
	fun getToppings(): List<ToppingMetricsCollector.Topping> {
		return toppingMetricsRepository.getToppings()
	}

	@GetMapping("/total-count")
	fun getTotalCountPerTopping(): Map<String, Int> {
		return toppingMetricsRepository.getTotalCountPerTopping()
	}

	@GetMapping("/unique-user-count")
	fun getUniqueUserCountPerTopping(): Map<String, Int> {
		return toppingMetricsRepository.getUniqueUserCountPerTopping()
	}

	@GetMapping("/most-popular-combo")
	fun getMostPopularCombos(@RequestParam(defaultValue = "1") count: Int): List<List<String>> {
		return toppingMetricsRepository.getMostPopularToppingCombos(count)
	}

	/**
	 * Use ?count=n to get n most popular toppings
	 */
	@GetMapping("/most-popular")
	fun getMostPopularToppings(@RequestParam(defaultValue = "1") count: Int): List<String> {
		return toppingMetricsRepository.getMostPopularToppings(count)
	}

	/**
	 * Use ?count=n to get n least popular toppings
	 */
	@GetMapping("/least-popular")
	fun getLeastPopularToppings(@RequestParam(defaultValue = "1") count: Int): List<String> {
		return toppingMetricsRepository.getLeastPopularToppings(count)
	}

	@PostMapping("/post")
	fun submitToppings(@RequestBody toppings: List<ToppingMetricsCollector.Topping>) {
		return toppingMetricsRepository.updateToppings(toppings)
	}
}


fun main(args: Array<String>) {
	runApplication<PizzaApplication>(*args)
}