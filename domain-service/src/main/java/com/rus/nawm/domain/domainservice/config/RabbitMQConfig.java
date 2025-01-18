package com.rus.nawm.domain.domainservice.config;

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
  public static final String touristPostResponseQueueName = "touristPostResponseQueue";
  public static final String touristPutResponseQueueName = "touristPutResponseQueue";
  public static final String touristDeleteResponseQueueName = "touristDeleteResponseQueue";

  public static final String touristPostRequestQueueRoutingKey = "tourist.post";
  public static final String touristPutRequestQueueRoutingKey = "tourist.put";
  public static final String touristDeleteRequestQueueRoutingKey = "tourist.delete";
  public static final String touristPostResponseQueueRoutingKey = "tourist.post.response";
  public static final String touristPutResponseQueueRoutingKey = "tourist.put.response";
  public static final String touristDeleteResponseQueueRoutingKey = "tourist.delete.response";

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

  @Bean("touristPostResponseQueue")
  public Queue touristPostResponseQueue() {
    return new Queue(touristPostResponseQueueName, true);
  }

  @Bean("touristPutResponseQueue")
  public Queue touristPutResponseQueue() {
    return new Queue(touristPutResponseQueueName, true);
  }

  @Bean("touristDeleteResponseQueue")
  public Queue touristDeleteResponseQueue() {
    return new Queue(touristDeleteResponseQueueName, true);
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

  @Bean
  public Binding postResponseBinding(@Qualifier("touristPostResponseQueue") Queue touristPostResponseQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(touristPostResponseQueue).to(directExchange).with(touristPostResponseQueueRoutingKey);
  }

  @Bean
  public Binding putResponseBinding(@Qualifier("touristPutResponseQueue") Queue touristPutResponseQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(touristPutResponseQueue).to(directExchange).with(touristPutResponseQueueRoutingKey);
  }

  @Bean
  public Binding deleteResponseBinding(@Qualifier("touristDeleteResponseQueue") Queue touristDeleteResponseQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(touristDeleteResponseQueue).to(directExchange).with(touristDeleteResponseQueueRoutingKey);
  }
}
