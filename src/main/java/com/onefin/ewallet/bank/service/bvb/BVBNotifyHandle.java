package com.onefin.ewallet.bank.service.bvb;


import com.onefin.ewallet.bank.repository.jpa.VietinNotifyTransTableRepo;
import com.onefin.ewallet.bank.repository.jpa.VirtualAcctTransHistoryRepo;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.anotation.MeasureExcutionTime;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.utility.json.JSONHelper;
import com.onefin.ewallet.common.utility.string.StringHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BVBNotifyHandle {

	private static final Logger LOGGER = LogManager.getLogger(BVBNotifyHandle.class);

	@Autowired
	private BVBRequestUtil bvbRequestUtil;

	@Autowired
	private JSONHelper jsonHelper;

	@Autowired
	private VirtualAcctTransHistoryRepo virtualAcctTransHistoryRepo;

	@Autowired
	private VietinNotifyTransTableRepo vietinNotifyTransTableRepo;

	@Autowired
	private ConfigLoader configLoader;

	@Autowired
	private StringHelper stringHelper;

	@Autowired
	private BVBVirtualAcct bvbVirtualAcct;

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	@Async("asyncExecutor")
	@MeasureExcutionTime
	public ResponseEntity<?> callbackWithPoolVirtualAcct(String backendUrl, String virtualAcctTransHistoryId, String statusCode, String bankTransId, String from) {
		LOGGER.info("== Send callbackWithPoolVirtualAcct to url: {}", backendUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.ALL));
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, String> headersMap = new HashMap<>();
		headers.keySet().forEach(header -> headersMap.put(header, headers.getFirst(header)));
		Map<String, String> data = new HashMap<>();
		data.put("statusCode", statusCode);
		data.put("orderId", virtualAcctTransHistoryId);
		data.put("bankTransId", bankTransId);
		data.put("bankAccountName", from);
		LOGGER.info("CallbackWithPoolVirtualAcct request {}", data);
		ResponseEntity<Map> responseEntity = restTemplateHelper.post(backendUrl, MediaType.APPLICATION_JSON_VALUE,
				headersMap, new ArrayList<>(), new HashMap<>(), null, data,
				new ParameterizedTypeReference<Map>() {
				});
		LOGGER.info("CallbackWithPoolVirtualAcct response {}", responseEntity.getBody());
		return responseEntity;
	}
}
