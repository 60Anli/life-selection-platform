package com.lifeselection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static com.lifeselection.utils.MessageConstants.SECKILL_ORDER_TOPIC;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic seckillOrderTopic() {
        return new NewTopic(SECKILL_ORDER_TOPIC, 3, (short) 1);
    }
}
