package com.example.rsocketserver;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class RSocketService {

	private final ItemRepository repository;
	private final Sinks.Many<Item> itemSink;

	public RSocketService(ItemRepository repository) {
		this.repository = repository;
		this.itemSink = Sinks.many().multicast().onBackpressureBuffer();
	}

	@MessageMapping("newItems.request-response")
	public Mono<Item> processNewItemViaRSocketRequestResponse(Item item) {
		return this.repository.save(item)
			.doOnNext(this.itemSink::tryEmitNext);
	}

	@MessageMapping("newItems.request-stream")
	public Flux<Item> findItemsViaRSocketRequestStream() {
		return this.repository.findAll()
			.doOnNext(this.itemSink::tryEmitNext);
	}

	@MessageMapping("newItems.fire-and-forget")
	public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item) {
		return this.repository.save(item)
			.doOnNext(this.itemSink::tryEmitNext)
			.then();
	}
}
