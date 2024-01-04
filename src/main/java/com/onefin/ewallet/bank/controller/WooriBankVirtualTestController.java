package com.onefin.ewallet.bank.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onefin.ewallet.bank.dto.woori.*;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualBQ001;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV002;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV004;
import com.onefin.ewallet.bank.dto.woori.individual.DirectMessageIndividualRV006;
import com.onefin.ewallet.bank.service.woori.WooriRequestUtil;
import com.onefin.ewallet.bank.service.woori.WooriVirtualAcct;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/woori-bank/virtual-acct")
public class WooriBankVirtualTestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(WooriBankVirtualTestController.class);

	@Autowired
	private WooriRequestUtil wooriRequestUtil;

	@Autowired
	private WooriVirtualAcct wooriVirtualAcct;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private JSONHelper jsonHelper;

	@PostMapping("/RV006") // create virtual acct
	public ResponseEntity<?> rv006(@RequestBody(required = true)
								   DirectMessageRV006 requestBody) throws Exception {

		String returnString = wooriRequestUtil.objectToXml(requestBody);
		DirectMessageRV006 dir = wooriRequestUtil.xmlToObject(returnString, DirectMessageRV006.class);

		// Convert to Direct Message Common
		DirectMessageDto<DirectMessageIndividualRV006> dto
				= jsonHelper.mappingObject2Instance(dir,
				new TypeReference<DirectMessageDto<DirectMessageIndividualRV006>>() {
				});

		LOGGER.info("dir: {}", dir);
		LOGGER.info("dir: {}", dir.getIndividual());
		wooriVirtualAcct.createApiUpdateTable(dir, "");
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}


	@PostMapping("/RV004")
	public ResponseEntity<?> rv004(@Valid @RequestBody(required = true)
								   DirectMessageDto<DirectMessageIndividualRV004> requestBody) throws Exception {
		String returnString = wooriRequestUtil.objectToXml(requestBody);
		return new ResponseEntity<>(returnString, HttpStatus.OK);
	}

	@PostMapping("/RV005")
	public ResponseEntity<?> rv005(@RequestBody(required = true)
								   DirectMessageRV005 requestBody) throws Exception {

		String returnString = wooriRequestUtil.objectToXml(requestBody);
		DirectMessageRV005 dir = wooriRequestUtil.xmlToObject(returnString, DirectMessageRV005.class);
		LOGGER.info("dir: {}", dir);
		LOGGER.info("dir: {}", dir.getIndividual());
		return new ResponseEntity<>(returnString, HttpStatus.OK);
	}


	@PostMapping("/RV007")  // update virtual acct
	public ResponseEntity<?> rv007(@RequestBody(required = true)
								   DirectMessageRV007 requestBody) throws Exception {

		String returnString = wooriRequestUtil.objectToXml(requestBody);
		DirectMessageRV007 dir = wooriRequestUtil.xmlToObject(returnString, DirectMessageRV007.class);
		LOGGER.info("dir: {}", dir);
		LOGGER.info("dir: {}", dir.getIndividual());
		wooriVirtualAcct.updateApiUpdateTable(dir);
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}

	@PostMapping("/RV002")  // update virtual acct
	public ResponseEntity<?> rv002(@RequestBody(required = true)
								   DirectMessageRV002 requestBody) throws Exception {

		String returnString = wooriRequestUtil.objectToXml(requestBody);
		DirectMessageRV002 dir = wooriRequestUtil.xmlToObject(returnString, DirectMessageRV002.class);
		LOGGER.info("dir: {}", dir);
		LOGGER.info("dir: {}", dir.getIndividual());
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}

	@PostMapping("/MN001")  // update virtual acct
	public ResponseEntity<?> mn001(@RequestBody(required = true)
								   DirectMessageMN001 requestBody) throws Exception {

		String returnString = wooriRequestUtil.objectToXml(requestBody);
		DirectMessageMN001 dir = wooriRequestUtil.xmlToObject(returnString, DirectMessageMN001.class);
		LOGGER.info("dir: {}", dir);
		LOGGER.info("dir: {}", dir.getIndividual());
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}

	@PostMapping("/BQ001")  // update virtual acct
	public ResponseEntity<?> bq001(@RequestBody(required = true)
								   DirectMessageBQ001 requestBody) throws Exception {

		String returnString = wooriRequestUtil.objectToXml(requestBody);

		ObjectMapper mapper = new ObjectMapper();
		TypeReference<DirectMessageDto<DirectMessageIndividualBQ001>> typeRef
				= new TypeReference<DirectMessageDto<DirectMessageIndividualBQ001>>() {
		};


		DirectMessageBQ001 dir = wooriRequestUtil.xmlToObject(returnString, DirectMessageBQ001.class);
		DirectMessageDto<DirectMessageIndividualBQ001> dto =
				mapper.readValue(mapper.writeValueAsString(dir), typeRef);
		LOGGER.info("dir: {}", dir);
		LOGGER.info("dir: {}", dir.getIndividual());
		LOGGER.info("dir: {}", mapper.writeValueAsString(dir));
		LOGGER.info("dir: {}", mapper.writeValueAsString(dto));
		return new ResponseEntity<>(dir, HttpStatus.OK);
	}


}
