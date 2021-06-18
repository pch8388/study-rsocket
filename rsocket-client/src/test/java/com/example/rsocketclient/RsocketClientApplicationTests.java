package com.example.rsocketclient;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureWebTestClient
class RsocketClientApplicationTests {

	@Autowired private WebTestClient webTestClient;
	@Autowired private ItemRepository itemRepository;

	@Test
	void verifyRemoteOperationsThroughRSocketRequestResponse() throws InterruptedException {
		this.itemRepository.deleteAll()
			.as(StepVerifier::create)
			.verifyComplete();

		this.webTestClient.post().uri("/items/request-response")
			.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
			.exchange()
			.expectStatus().isCreated()
			.expectBody(Item.class)
			.value(item -> {
				assertThat(item.getId()).isNotEmpty();
				assertThat(item.getName()).isEqualTo("Alf alarm clock");
				assertThat(item.getDescription()).isEqualTo("nothing important");
				assertThat(item.getPrice()).isEqualTo(19.99);
			});

		Thread.sleep(500);

		this.itemRepository.findAll()
			.as(StepVerifier::create)
			.expectNextMatches(item -> {
				assertThat(item.getId()).isNotEmpty();
				assertThat(item.getName()).isEqualTo("Alf alarm clock");
				assertThat(item.getDescription()).isEqualTo("nothing important");
				assertThat(item.getPrice()).isEqualTo(19.99);
				return true;
			})
			.verifyComplete();
	}

	@Test
	void verifyRemoteOperationsThroughRSocketRequestStream() {
		this.itemRepository.deleteAll().block();

		final List<Item> items = IntStream.rangeClosed(1, 3)
			.mapToObj(i -> new Item("name - " + i, "description - " + i, i))
			.collect(toList());

		this.itemRepository.saveAll(items).blockLast();

		this.webTestClient.get().uri("/items/request-stream")
			.accept(MediaType.APPLICATION_NDJSON)
			.exchange()
			.expectStatus().isOk()
			.returnResult(Item.class)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(itemPredicate("1"))
			.expectNextMatches(itemPredicate("2"))
			.expectNextMatches(itemPredicate("3"))
			.verifyComplete();
	}

	private Predicate<Item> itemPredicate(String num) {
		return item -> {
			assertThat(item.getName()).startsWith("name");
			assertThat(item.getName()).endsWith(num);
			assertThat(item.getDescription()).startsWith("description");
			assertThat(item.getDescription()).endsWith(num);
			assertThat(item.getPrice()).isPositive();
			return true;
		};
	}

	@Test
	void verifyRemoteOperationsThroughRSocketFireAndForget() throws InterruptedException {
		this.itemRepository.deleteAll()
			.as(StepVerifier::create)
			.verifyComplete();

		this.webTestClient.post().uri("/items/fire-and-forget")
			.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
			.exchange()
			.expectStatus().isCreated()
			.expectBody().isEmpty();

		Thread.sleep(500);

		this.itemRepository.findAll()
			.as(StepVerifier::create)
			.expectNextMatches(item -> {
				assertThat(item.getId()).isNotEmpty();
				assertThat(item.getName()).isEqualTo("Alf alarm clock");
				assertThat(item.getDescription()).isEqualTo("nothing important");
				assertThat(item.getPrice()).isEqualTo(19.99);
				return true;
			})
			.verifyComplete();
	}
}
