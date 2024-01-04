package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CreateInstrumentCardResponse extends ErrorResponse {

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String ref_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String user_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String type;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String name;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String number;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String month;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String year;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Map<String, Object> issuer;

	@JsonProperty("issuer")
	public void setIssuer(Map<String, Object> issuer) {
		this.issuer = issuer;
	}

	private AddressUserRegistrationCard billing_address;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String create_time;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String update_time;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String psp_id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String number_hash;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String state;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private AuthorizationCreateInstrumentCard authorization;

	@JsonProperty("authorization")
	public void setAuthorization(AuthorizationCreateInstrumentCard authorization) {
		this.authorization = authorization;
	}

}
