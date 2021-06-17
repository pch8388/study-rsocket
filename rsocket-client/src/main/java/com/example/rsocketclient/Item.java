package com.example.rsocketclient;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {

	@Id
	private String id;

	private String name;

	private String description;

	private double price;

	public Item(String name, String description, double price) {
		this.name = name;
		this.description = description;
		this.price = price;
	}

	public Item(String id, String name, String description, double price) {
		this(name, description, price);
		this.id = id;
	}
}
