package com.onefin.ewallet.bank.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onefin.ewallet.common.quartz.controller.JobController;
import com.onefin.ewallet.common.quartz.entity.SchedulerJobInfo;

@RestController
@RequestMapping("/quartz")
public class BankJobController extends JobController {

    @PostMapping(value = "/saveOrUpdate")
    public Object saveOrUpdateJob(@RequestBody SchedulerJobInfo job) throws ClassNotFoundException {
        return saveOrUpdate(job);
    }

}
