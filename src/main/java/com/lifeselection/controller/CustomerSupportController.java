package com.lifeselection.controller;

import com.lifeselection.dto.Result;
import com.lifeselection.dto.SupportChatRequest;
import com.lifeselection.service.ICustomerSupportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/customer-service")
public class CustomerSupportController {

    @Resource
    private ICustomerSupportService customerSupportService;

    @PostMapping("/chat")
    public Result chat(@RequestBody SupportChatRequest request) {
        return customerSupportService.chat(request);
    }
}
