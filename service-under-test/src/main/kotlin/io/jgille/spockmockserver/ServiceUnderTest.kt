package io.jgille.spockmockserver

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

@SpringBootApplication
open class ServiceUnderTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ServiceUnderTest::class.java, *args)
        }
    }

    @Bean
    open fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.setConnectTimeout(2000).setReadTimeout(1000).build()
    }

}

@RestController
class MyController(private val restTemplate: RestTemplate) {

    @PostMapping("/greet")
    fun greet(@RequestBody request: GreetingRequest) {
        restTemplate.postForEntity(
                "http://localhost:8383/events",
                mapOf(
                        "eventType" to "greeting_posted",
                        "greeting" to request.greeting
                ),
                Void::class.java)
    }
}

@ControllerAdvice
class ExceptionHandlers {

    @ExceptionHandler(ResourceAccessException::class)
    fun handle(error: ResourceAccessException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
}

data class GreetingRequest(val greeting: String)