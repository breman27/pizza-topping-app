package com.example.springboot

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.boot.test.context.SpringBootTest
import com.example.springboot.ToppingMetricsCollector.Topping

@SpringBootTest
class ApplicationTests {

	val toppingMetrics = ToppingMetricsCollector()
	@Test
	fun contextLoads() {
	}

	@Test
	fun `test calculatePopularCombos with empty array`() {
		val toppings = arrayOf<Topping>()
		val result = toppingMetrics.calculatePopularCombos(toppings)
		assertEquals(emptyMap<List<String>, Int>(), result)
	}

	@Test
	fun `test calculatePopularCombos with one topping`() {
		val toppings = arrayOf(Topping("", listOf("pepperoni", "mushrooms", "onions")))
		val result = toppingMetrics.calculatePopularCombos(toppings)

		assertEquals(1, result[listOf("pepperoni", "mushrooms")])
		assertEquals(1, result[listOf("pepperoni", "onions")])
		assertEquals(1, result[listOf("mushrooms", "onions")])
	}

	@Test
	fun `test calculatePopularCombos with multiple toppings`() {
		val toppings = arrayOf(
			Topping("1", listOf("pepperoni", "mushrooms", "onions")),
			Topping("2", listOf("sausage", "mushrooms")),
			Topping("1", listOf("pepperoni", "green peppers")),
			Topping("3", listOf("sausage", "onions")),
			Topping("3", listOf("ham", "pineapple")),
			Topping("4", listOf("sausage", "green peppers")),
			Topping("1", listOf("mushrooms")),
			Topping("5", listOf("sausage", "pepperoni", "onions")),
			Topping("2", listOf("pepperoni", "green peppers", "onions")),
			Topping("5", listOf("sausage", "pepperoni", "mushrooms"))
		)

		val popularCombos = toppingMetrics.calculatePopularCombos(toppings)

		assertEquals(2, popularCombos[listOf("pepperoni", "mushrooms")])
		assertEquals(2, popularCombos[listOf("sausage", "mushrooms")])
		assertEquals(2, popularCombos[listOf("pepperoni", "green peppers")])
		assertEquals(2, popularCombos[listOf("sausage", "onions")])
		assertEquals(1, popularCombos[listOf("ham", "pineapple")])
		assertEquals(1, popularCombos[listOf("sausage", "green peppers")])
		assertEquals(3, popularCombos[listOf("pepperoni", "onions")])
	}
}
