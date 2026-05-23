package com.lifeselection.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Bean
    @ConditionalOnProperty(prefix = "ai.langchain4j", name = "enabled", havingValue = "true")
    public ChatLanguageModel chatLanguageModel(
            @Value("${ai.langchain4j.api-key}") String apiKey,
            @Value("${ai.langchain4j.base-url}") String baseUrl,
            @Value("${ai.langchain4j.model-name}") String modelName
    ) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
