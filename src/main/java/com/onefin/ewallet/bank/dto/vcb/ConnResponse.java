package com.onefin.ewallet.bank.dto.vcb;

import com.onefin.ewallet.common.base.model.BaseConnResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConnResponse extends BaseConnResponse {

	private Object response;

	private String version;

	private String type;
}
