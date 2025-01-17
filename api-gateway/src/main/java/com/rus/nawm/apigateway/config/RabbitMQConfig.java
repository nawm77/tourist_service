package com.rus.nawm.apigateway.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  public static final String touristPostRequestQueueName = "touristPostRequestQueue";
  public static final String touristPutRequestQueueName = "touristPutRequestQueue";
  public static final String touristDeleteRequestQueueName = "touristDeleteRequestQueue";

  public static final String touristPostRequestQueueRoutingKey = "tourist.post";
  public static final String touristPutRequestQueueRoutingKey = "tourist.put";
  public static final String touristDeleteRequestQueueRoutingKey = "tourist.delete";

  public static final String directExchangeName = "touristExchange";
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public DirectExchange exchange() {
    return new DirectExchange(directExchangeName);
  }

  @Bean("touristPostRequestQueue")
  public Queue touristPostRequestQueue() {
    return new Queue(touristPostRequestQueueName, true);
  }

  @Bean("touristPutRequestQueue")
  public Queue touristPutRequestQueue() {
    return new Queue(touristPutRequestQueueName, true);
  }

  @Bean("touristDeleteRequestQueue")
  public Queue touristDeleteRequestQueue() {
    return new Queue(touristDeleteRequestQueueName, true);
  }

  @Bean
  public Binding postRequestBinding(@Qualifier("touristPostRequestQueue") Queue touristPostRequestQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(touristPostRequestQueue).to(directExchange).with(touristPostRequestQueueRoutingKey);
  }

  @Bean
  public Binding putRequestBinding(@Qualifier("touristPutRequestQueue") Queue touristPutRequestQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(touristPutRequestQueue).to(directExchange).with(touristPutRequestQueueRoutingKey);
  }

  @Bean
  public Binding deleteRequestBinding(@Qualifier("touristDeleteRequestQueue") Queue touristDeleteRequestQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(touristDeleteRequestQueue).to(directExchange).with(touristDeleteRequestQueueRoutingKey);
  }
}
