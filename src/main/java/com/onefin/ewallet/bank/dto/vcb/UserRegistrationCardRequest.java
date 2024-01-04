package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

@Data
public class UserRegistrationCardRequest {

	private String group_id;

	private String ref_id;

	private String first_name;

	private String last_name;

	private String mpin;

	private String mobile;

	private String email;

	private AddressUserRegistrationCard address;

}
