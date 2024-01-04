package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

import java.util.Map;

@Data
public class AuthorizationCreateInstrumentCard {

	private String id;

	private String ref_id;

	private String parent_type;

	private String parent_id;

	private String create_time;

	private String expire_time;

	private String state;

	private Map<String, Object> links;

}
