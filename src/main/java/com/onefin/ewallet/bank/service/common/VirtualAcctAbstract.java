package com.onefin.ewallet.bank.service.common;

import com.onefin.ewallet.bank.dto.vietin.ConnResponse;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctCreateRequest;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctSeedRequest;
import com.onefin.ewallet.bank.dto.vietin.VirtualAcctUpdateStatusRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class VirtualAcctAbstract {

	public String getTransTime() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}

	public abstract ConnResponse buildCreateResponseEntity(VirtualAcctCreateRequest requestBody) throws Exception;

	public abstract ConnResponse
	buildActUpdateResponseEntity(VirtualAcctUpdateStatusRequest requestBody) throws Exception;

	public abstract void
	buildSeedVirtualAcctPoolResponseEntity(VirtualAcctSeedRequest requestBody) throws Exception;

}
