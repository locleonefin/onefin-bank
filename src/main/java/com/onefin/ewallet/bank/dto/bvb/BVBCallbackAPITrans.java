package com.onefin.ewallet.bank.dto.bvb;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BVBCallbackAPITrans {


	private String to;

	private String traceId;

	private String eventName;

	private Long bankTime;

	private BVBCallbackAPITransData data;

	public BVBCallbackAPITrans() {

	}


}
