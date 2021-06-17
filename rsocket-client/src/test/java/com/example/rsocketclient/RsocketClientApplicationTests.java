package com.example.rsocketclient;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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

}
