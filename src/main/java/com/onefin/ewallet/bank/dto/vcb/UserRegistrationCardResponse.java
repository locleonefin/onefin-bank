package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserRegistrationCardResponse extends ErrorResponse {

	private String id;

	private String ref_id;

	private String group_id;

	private String first_name;

	private String last_name;

	private String mobile;

	private String email;

	private AddressUserRegistrationCard address;

	private String state;

}
