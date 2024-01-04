package com.onefin.ewallet.bank.dto.vcb;

import lombok.Data;

@Data
public class Vcb2EWalletUserDetailResponse {

    private String fullname;

    private String idNumber;

    private String idNumberType;

    private String address;

    private String walletId;

}
