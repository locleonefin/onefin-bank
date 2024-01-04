package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

import java.util.Map;

@Data
public class CardInstrumentAuthorizationInstrumentResponse {

	private String id;

	private String ref_id;

	private String user_id;

	private String type;

	private String name;

	private String number;

	private String month;

	private String year;

	private Map<String, Object> issuer;

	private AddressUserRegistrationCard billing_address;

	private String create_time;

	private String update_time;

	private String psp_id;

	private String number_hash;

	private String state;

	private String cif;

	private CardToken token;
}
