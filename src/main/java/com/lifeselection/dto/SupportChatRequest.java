package com.lifeselection.dto;

import lombok.Data;

@Data
public class SupportChatRequest {
    private String sessionId;
    private String message;
}
