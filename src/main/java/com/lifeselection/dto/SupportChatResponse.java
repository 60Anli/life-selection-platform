package com.lifeselection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportChatResponse {
    private String sessionId;
    private String reply;
    private List<String> suggestions;
}
