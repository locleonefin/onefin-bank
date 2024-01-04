package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataDecryptBaseResponse {

	private int code;

//	@JsonProperty("Code")
//	public void setCode(int code) {
//		this.code = code;
//	}

	private String desc;
//
//	@JsonProperty("Desc")
//	public void setDesc(String desc) {
//		this.desc = desc;
//	}

	private String message;

//	@JsonProperty("Message")
//	public void setMessage(String message) {
//		this.message = message;
//	}

}
