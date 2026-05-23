package com.lifeselection.service;

import com.lifeselection.dto.Result;
import com.lifeselection.dto.SupportChatRequest;

public interface ICustomerSupportService {
    Result chat(SupportChatRequest request);
}
