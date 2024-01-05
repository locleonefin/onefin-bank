package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.dto.bvb.*;
import com.onefin.ewallet.bank.dto.vietin.ConnResponse;
import com.onefin.ewallet.bank.service.bvb.BVBRequestUtil;
import com.onefin.ewallet.bank.service.bvb.BVBVirtualAcct;
import com.onefin.ewallet.bank.service.common.ConfigLoader;
import com.onefin.ewallet.common.base.constants.BankConstants;
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

@RestController
@RequestMapping("/bvb/virtual-acct")
public class BVBVirtualTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BVBVirtualTestController.class);

    @Autowired
    private BVBRequestUtil bvbRequestUtil;

    @Autowired
    private ConfigLoader configLoader;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BVBVirtualAcct bvbVirtualAcct;

    @PostMapping("/create")
    public ResponseEntity<?> createVirtualAcct(
            @Valid @RequestBody(required = true)
            BVBVirtualAcctCreateRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCreateVirtualAcctUrl());

        // Request create api
        ResponseEntity<?> bvbResponse =
                bvbRequestUtil.requestBVB(requestBody,
                        configLoader.getBvbVirtualAcctCreateVirtualAcctUrl(),
                        requestBody.getRequestId(),
                        BankConstants.BVB_BACKUP_CREATE_PREFIX
                );

        // validate signature
//        ConnResponse responseSignature
//                = bvbRequestUtil.checkSignature(bvbResponse);
//        LOGGER.info("responseSignature " + responseSignature);
//        if (responseSignature != null) {
//            return new ResponseEntity<>(responseSignature, HttpStatus.OK);
//        }

        // construct BVB Create virtual code response body
        BVBVirtualAcctCreateResponse bvbVirtualAcctCreateResponse
                = modelMapper.map(
                bvbResponse.getBody(),
                BVBVirtualAcctCreateResponse.class
        );

        // update table
        bvbVirtualAcct.createApiUpdateTable(bvbVirtualAcctCreateResponse, requestBody, "");

        return new ResponseEntity<>(bvbVirtualAcctCreateResponse, HttpStatus.OK);

    }

    @PostMapping("/close")
    public ResponseEntity<?> closeVirtualAcct(
            @Valid @RequestBody(required = true)
            BVBVirtualAcctCloseRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCloseVirtualAcctUrl());

        // Request close api
        ResponseEntity<?> bvbResponse =
                bvbRequestUtil.requestBVB(requestBody,
                        configLoader.getBvbVirtualAcctCloseVirtualAcctUrl(),
                        requestBody.getRequestId(),
                        BankConstants.BVB_BACKUP_CLOSE_PREFIX
                );

        // validate signature
//        ConnResponse responseSignature
//                = bvbRequestUtil.checkSignature(bvbResponse);
//        if (responseSignature != null) {
//            return new ResponseEntity<>(responseSignature, HttpStatus.OK);
//        }

        // construct BVB close virtual code response body
        BVBVirtualAcctCloseResponse bvbVirtualAcctCloseResponse
                = modelMapper.map(
                bvbResponse.getBody(),
                BVBVirtualAcctCloseResponse.class
        );

        bvbVirtualAcct.closeApiUpdateTable(bvbVirtualAcctCloseResponse, requestBody);
        return new ResponseEntity<>(bvbVirtualAcctCloseResponse, HttpStatus.OK);
    }

    @PostMapping("/viewAccountDetail")
    public ResponseEntity<?> viewAccountDetail(
            @Valid @RequestBody(required = true)
            BVBVirtualAcctInfoDetailRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctCheckDetailVirtualAcctUrl());
        return bvbVirtualAcct.bvbVirtualRequest(
                requestBody,
                configLoader.getBvbVirtualAcctCheckDetailVirtualAcctUrl(),
                requestBody.getRequestId(),
                BankConstants.BVB_BACKUP_VIEW_DETAIL_PREFIX,
                BVBVirtualAcctInfoDetailResponse.class
        );

    }

    @PostMapping("/findAccounts")
    public ResponseEntity<?> findAccounts(
            @Valid @RequestBody(required = true)
            BVBAccountListRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbFindVirtualAcctListUrl());

        return bvbVirtualAcct.bvbVirtualRequest(
                requestBody,
                configLoader.getBvbFindVirtualAcctListUrl(),
                requestBody.getRequestId(),
                BankConstants.BVB_BACKUP_FIND_ACCOUNT_PREFIX,
                BVBAccountListResponse.class
        );
    }

    @PostMapping("/updateAccount")
    public ResponseEntity<?> updateAccount(
            @Valid @RequestBody(required = true)
            BVBVirtualAcctUpdateRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctUpdateVirtualAcctUrl());

        // Request update api
        ResponseEntity<?> bvbResponse =
                bvbRequestUtil.requestBVB(requestBody,
                        configLoader.getBvbVirtualAcctUpdateVirtualAcctUrl(),
                        requestBody.getRequestId(),
                        BankConstants.BVB_BACKUP_UPDATE_ACCOUNT_PREFIX
                );

        // validate signature
//        ConnResponse responseSignature
//                = bvbRequestUtil.checkSignature(bvbResponse);
//        if (responseSignature != null) {
//            return new ResponseEntity<>(responseSignature, HttpStatus.OK);
//        }

        // construct BVB Update virtual code response body
        BVBVirtualAcctUpdateResponse responseBody
                = modelMapper.map(
                bvbResponse.getBody(),
                BVBVirtualAcctUpdateResponse.class
        );

        bvbVirtualAcct.updateApiUpdateTable(responseBody, requestBody);

        return new ResponseEntity<>(responseBody, HttpStatus.OK);

    }

    @PostMapping("/reOpenAccount")
    public ResponseEntity<?> reOpenAccount(
            @Valid @RequestBody(required = true)
            BVBVirtualAcctReopenRequest requestBody
    ) throws Exception {
        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctReopenVirtualAcctUrl());

        // Request reopen api

        ResponseEntity<?> bvbResponse =
                bvbRequestUtil.requestBVB(requestBody,
                        configLoader.getBvbVirtualAcctReopenVirtualAcctUrl(),
                        requestBody.getRequestId(),
                        BankConstants.BVB_BACKUP_REOPEN_PREFIX
                );

        // validate signature
//        ConnResponse responseSignature = bvbRequestUtil.checkSignature(bvbResponse);
//        if (responseSignature != null) {
//            return new ResponseEntity<>(responseSignature, HttpStatus.OK);
//        }

        // construct BVB reopen virtual code response body
        BVBVirtualAcctReopenResponse responseBody
                = modelMapper.map(
                bvbResponse.getBody(),
                BVBVirtualAcctReopenResponse.class
        );

        bvbVirtualAcct.reOpenApiUpdateTable(responseBody, requestBody);

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/getCallbackData")
    public ResponseEntity<?> getCallbackData(
            @Valid @RequestBody(required = true)
            BVBValidateCallbackRequest requestBody
    ) throws Exception {
        LOGGER.info("URL: {}", configLoader.getBvbValidateCallBackUrl());

        return bvbVirtualAcct.bvbVirtualRequest(
                requestBody,
                configLoader.getBvbValidateCallBackUrl(),
                requestBody.getRequestId(),
                BankConstants.BVB_BACKUP_GET_CALLBACK_PREFIX,
                BVBValidateCallbackResponse.class
        );
    }

    @PostMapping("/searchTransactionByAccount")
    public ResponseEntity<?> searchTransactionByAccount(
            @Valid @RequestBody(required = true)
            BVBVirtualAcctTransSearchRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbSearchTransByAccountUrl());
        return bvbVirtualAcct.bvbVirtualRequest(
                requestBody,
                configLoader.getBvbSearchTransByAccountUrl(),
                requestBody.getRequestId(),
                BankConstants.BVB_BACKUP_SEARCH_TRANS_PREFIX,
                BVBVirtualAcctTransSearchResponse.class
        );
    }

    @PostMapping("/virtual-transaction")
    public ResponseEntity<?> virtualTrans(
            @Valid @RequestBody(required = true)
            BVBTestUnitRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctTestUnit());


        // Request create api
        ResponseEntity<?> bvbResponse =
                bvbRequestUtil.requestBVB(
                        requestBody,
                        configLoader.getBvbVirtualAcctTestUnit(),
                        requestBody.getRequestId(),
                        BankConstants.BVB_BACKUP_VIRTUAL_TRANS_PREFIX
                );
        LOGGER.info("body: {}", bvbResponse.getBody());
        // Construct response
        BVBTestUnitResponse returnResponse
                = modelMapper.map(
                bvbResponse.getBody(),
                BVBTestUnitResponse.class
        );

        return new ResponseEntity<>(returnResponse, HttpStatus.OK);

    }

    @PostMapping("/virtual-transaction-test")
    public ResponseEntity<?> virtualTransTest(
            @Valid @RequestBody(required = true)
            BVBTestUnitRequest requestBody
    ) throws Exception {

        LOGGER.info("URL: {}", configLoader.getBvbVirtualAcctTestUnit());


        // Request create api
        ResponseEntity<?> bvbResponse =
                bvbRequestUtil.requestBVB(
                        requestBody,
                        configLoader.getBvbVirtualAcctTestUnit(),
                        requestBody.getRequestId(),
                        BankConstants.BVB_BACKUP_VIRTUAL_TRANS_PREFIX
                );
        LOGGER.info("body: {}", bvbResponse.getBody());
        // Construct response
        BVBTestUnitResponse returnResponse
                = modelMapper.map(
                bvbResponse.getBody(),
                BVBTestUnitResponse.class
        );
        ConnResponse response = new ConnResponse();
        if (returnResponse.getRCode().equals(BankConstants.BVB_REQUEST_STATUS_SUCCESS)) {
            response.setConnectorCode(BankConstants.BVB_REQUEST_STATUS_SUCCESS);
            response.setMessage("TRANSACTION_SUCCESS");
            bvbRequestUtil.transformErrorCode(response, BankConstants.VIRTUAL_ACCT,
                    BankConstants.BVB_REQUEST_STATUS_SUCCESS, requestBody.getLang());
        } else {
            try {
                if (returnResponse.getData().getError().equals("13")) {
                    bvbRequestUtil.transformErrorCode(response,
                            BankConstants.BVBVirtualAcctErrorCode.INVALID_AMOUNT.getPartnerCode(),
                            BankConstants.BVBVirtualAcctErrorCode.INVALID_AMOUNT.getDomainCode(),
                            BankConstants.BVBVirtualAcctErrorCode.INVALID_AMOUNT.getCode(),
                            requestBody.getLang());
                } else if (returnResponse.getData().getError().equals("01")) {
                    bvbRequestUtil.transformErrorCode(response,
                            BankConstants.BVBVirtualAcctErrorCode.ACCT_NOT_FOUND.getPartnerCode(),
                            BankConstants.BVBVirtualAcctErrorCode.ACCT_NOT_FOUND.getDomainCode(),
                            BankConstants.BVBVirtualAcctErrorCode.ACCT_NOT_FOUND.getCode(),
                            requestBody.getLang());
                } else {
                    bvbRequestUtil.transformErrorCode(response, BankConstants.BVB_UNKNOWN,
                            BankConstants.BVB_UNKNOWN_ERROR_CODE, requestBody.getLang());
                }

            } catch (Exception e) {
                bvbRequestUtil.transformErrorCode(response,
                        BankConstants.BVB_UNKNOWN,
                        BankConstants.BVB_UNKNOWN_ERROR_CODE,
                        requestBody.getLang());
            }
        }


        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
