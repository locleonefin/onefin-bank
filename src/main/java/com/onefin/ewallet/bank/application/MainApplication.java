package com.onefin.ewallet.bank.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.onefin.ewallet.common.base.application.BaseApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EntityScan({"com.onefin.ewallet.common.domain.bank.vietin",
		"com.onefin.ewallet.common.domain.base.sequenceTrans", "com.onefin.ewallet.common.domain.bank.common", "com.onefin.ewallet.common.domain.holiday",
		"com.onefin.ewallet.common.quartz.entity", "com.onefin.ewallet.common.domain.errorCode", "com.onefin.ewallet.common.domain.bank.transfer"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {"com.onefin.ewallet.bank.repository.jpa", "com.onefin.ewallet.common.quartz.repository"})
@EnableRedisRepositories(basePackages = {"com.onefin.ewallet.bank.repository.redis"})
@EnableTransactionManagement
public class MainApplication extends BaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

}
