package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Ewallet2VcbDataDecryptResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ewallet2VcbDataDecryptResponse.class);

	private Ewallet2VcbDetailResponse data;

	@JsonProperty("data")
	public Ewallet2VcbDetailResponse getEwallet2VcbDetailResponse() {
		return data;
	}

	@JsonProperty(value = "Data", access = JsonProperty.Access.WRITE_ONLY)
	public void setData(String data) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			this.data = mapper.readValue(data, Ewallet2VcbDetailResponse.class);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private String connRequestId;

	private int code;

	@JsonProperty("code")
	public int getCode() {
		return code;
	}

	@JsonProperty("Code")
	public void setCode(int code) {
		this.code = code;
	}

	private String desc;

//	@JsonProperty("desc")
//	public String getDesc() {
//		return desc;
//	}

	@JsonProperty(value = "Desc", access = JsonProperty.Access.WRITE_ONLY)
	public void setDesc(String desc) {
		this.desc = desc;
	}

	private String message;

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	@JsonProperty("Message")
	public void setMessage(String message) {
		this.message = message;
	}

}
