package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.common.base.anotation.MeasureExcutionTime;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/woori/virtual-acct")
public class WooriBankVirtualAcctNotifyController {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(WooriBankVirtualAcctNotifyController.class);

	@PostMapping("/notify-trans")
	@MeasureExcutionTime
	public ResponseEntity<?> notifyTrans(
			@Valid @RequestBody(required = true)
			String requestBody,
			@RequestHeader Map<String, String> headers) {

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
