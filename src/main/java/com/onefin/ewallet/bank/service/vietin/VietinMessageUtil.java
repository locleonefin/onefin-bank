package com.onefin.ewallet.bank.service.vietin;

import com.onefin.ewallet.bank.dto.vietin.ConnResponse;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VietinMessageUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(VietinMessageUtil.class);

	@Autowired
	private ConfigLoader configLoader;

	public ConnResponse buildVietinConnectorResponse(String code, Object data, String... args) {
		ConnResponse response = new ConnResponse();
		response.setConnectorCode(code);
		response.setVtbResponse(data);
		response.setVersion(configLoader.getVietinVersion());
		response.setType(args.length > 0 ? args[0] : null);
		return response;
	}

	public ConnResponse buildVietinBankTransferConnectorResponse(String code, Object data, String... args) {
		ConnResponse response = new ConnResponse();
		response.setConnectorCode(code);
		response.setResponse(data);
		return response;
	}

}
