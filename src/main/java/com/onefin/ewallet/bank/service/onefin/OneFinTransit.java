package com.onefin.ewallet.bank.service.onefin;

import com.onefin.ewallet.bank.common.OtherConstants;
import com.onefin.ewallet.bank.dto.oneFin.ErrorResponse;
import com.onefin.ewallet.bank.dto.oneFin.OneFinBankTransferRequest;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.service.RestTemplateHelper;
import com.onefin.ewallet.common.domain.bank.vietin.VietinNotifyTrans;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OneFinTransit {

  private static final Logger LOGGER = LoggerFactory.getLogger(OneFinTransit.class);

  @Autowired
  private ModelMapper modelMapper;
  @Autowired
  private ConfigLoader configLoader;

  @Autowired
  protected RestTemplateHelper restTemplateHelper;

  @Async
  public void updateTransitBankTransfer(VietinNotifyTrans data) throws ParseException {
    OneFinBankTransferRequest oneFinBankTransferRequest = new OneFinBankTransferRequest();
    oneFinBankTransferRequest.setTransId(data.getBankTransId());
    oneFinBankTransferRequest.setAmount(Long.parseLong(data.getAmount()));
    SimpleDateFormat df = new SimpleDateFormat(OtherConstants.DATE_FORMAT_yyyyMMDDHHmmss);
    Date date = df.parse(data.getTransTime());
    oneFinBankTransferRequest.setTransDateTime(date.getTime());
    oneFinBankTransferRequest.setComment(data.getRemark());
    oneFinBankTransferRequest.setToAccount(data.getRecvVirtualAcctId());
    oneFinBankTransfer(oneFinBankTransferRequest);
  }

  public ResponseEntity<ErrorResponse> oneFinBankTransfer(OneFinBankTransferRequest data) {
      String url = "http://onefin-transit-hub.onefin.in/bank/transfer";
    // String url = configLoader.getTransitUrlBankTransfer();
    LOGGER.info("== Send oneFinBankTransfer request to {} {} - url: {}", OtherConstants.ONEFIN_TRANSIT_HUB_SV_NAME, data, url);
    ResponseEntity<ErrorResponse> responseEntity = restTemplateHelper.post(url, MediaType.APPLICATION_JSON_VALUE, genHeaderMap(), new ArrayList<String>(), new HashMap<>(), null, data, new ParameterizedTypeReference<ErrorResponse>() {
    });
    LOGGER.info("== Success receive response from {} {}", OtherConstants.ONEFIN_TRANSIT_HUB_SV_NAME, responseEntity.getBody());
    return responseEntity;
  }

  private HashMap<String, String> genHeaderMap() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.ALL));
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, String> headersMap = new HashMap<>();
    headers.keySet().forEach(header -> {
      headersMap.put(header, headers.getFirst(header));
    });
    return (HashMap<String, String>) headersMap;
  }
}
