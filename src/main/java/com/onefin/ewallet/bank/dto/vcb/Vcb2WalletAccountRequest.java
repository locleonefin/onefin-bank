package com.onefin.ewallet.bank.dto.vcb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class Vcb2WalletAccountRequest {

    @NotEmpty(message = "Not empty")
    @JsonProperty(value = "PartnerId")
    private String partnerId;

    @NotEmpty(message = "Not empty")
    @JsonProperty(value = "RequestId")
    private String requestId;

    @NotEmpty(message = "Not empty")
    @JsonProperty(value = "MessageType")
    private String messageType;

    @NotEmpty(message = "Not empty")
    @JsonProperty(value = "Data")
    private String data;

    @NotEmpty(message = "Not empty")
    @JsonProperty(value = "Signature")
    private String signature;

}
