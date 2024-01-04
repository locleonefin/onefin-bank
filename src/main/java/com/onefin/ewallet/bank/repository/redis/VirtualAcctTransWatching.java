package com.onefin.ewallet.bank.repository.redis;

import com.onefin.ewallet.bank.common.OtherConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@RedisHash(timeToLive = OtherConstants.VIRTUAL_ACCOUNT_TRANS_WATCHING_DEFAULT_TTL)
@AllArgsConstructor
public class VirtualAcctTransWatching {
    @Id
    private String transUniqueKey;
    private String merchantCode;
    private String bankCode;
    private boolean isNotified;

    @TimeToLive
    private Long expiration;
}
