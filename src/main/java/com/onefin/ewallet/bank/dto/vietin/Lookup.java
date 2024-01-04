package com.onefin.ewallet.bank.dto.vietin;

import lombok.Data;

@Data
public class Lookup {

	private String name;

	private String value;

	public Lookup(String name, String value) {
		this.name = name;
		this.value = value;
	}

}
