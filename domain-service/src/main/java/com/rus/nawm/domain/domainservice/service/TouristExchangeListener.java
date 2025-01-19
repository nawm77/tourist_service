package com.rus.nawm.domain.domainservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.rus.nawm.domain.domainservice.domain.Tourist;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.rus.nawm.domain.domainservice.config.RabbitMQConfig.*;

@Service
@Log4j2
public class TouristExchangeListener {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final TouristService touristService;
  private final RabbitTemplate rabbitTemplate;

  @Autowired
  public TouristExchangeListener(TouristService touristService, RabbitTemplate rabbitTemplate) {
    this.touristService = touristService;
    this.rabbitTemplate = rabbitTemplate;
  }

  @RabbitListener(queues = {touristPostRequestQueueName})
  public void onPostMethod(byte[] message) {
    try {
      Tourist tourist = objectMapper.readValue(message, Tourist.class);
      log.info("Received and deserialized tourist object for POST: {}", tourist);
      tourist = touristService.save(tourist);
      byte[] messageMQ = objectMapper.writeValueAsBytes(tourist);
      rabbitTemplate.convertAndSend(directExchangeName, touristPostResponseQueueRoutingKey, messageMQ);
    } catch (Exception e) {
      log.error("Error processing POST request for tourist", e);
    }
  }

  @RabbitListener(queues = {touristPutRequestQueueName})
  public void onPutMethod(byte[] message) {
    try {
      Tourist touristEntity = objectMapper.readValue(message, Tourist.class);
      log.info("Received and deserialized tourist object for PUT: {}", touristEntity);
      touristEntity = touristService.updateTourist(touristEntity);
      byte[] messageMQ = objectMapper.writeValueAsBytes(touristEntity);
      rabbitTemplate.convertAndSend(directExchangeName, touristPutResponseQueueRoutingKey, messageMQ);
    } catch (ListenerExecutionFailedException | MongoWriteException | DuplicateKeyException e) {
      log.error("Error processing PUT request for tourist", e);
    } catch (Exception e) {
      log.error("Error updating tourist object", e);
    }
  }

  @RabbitListener(queues = {touristDeleteRequestQueueName})
  public void onDeleteMethod(byte[] message) {
    String id = new String(message);
    try {
      log.info("Received tourist id for DELETE: {}", id);
      Tourist deletedTourist = touristService.deleteTourist(id);
      byte[] responseMessage = objectMapper.writeValueAsBytes(deletedTourist);
      rabbitTemplate.convertAndSend(directExchangeName, touristDeleteResponseQueueRoutingKey, responseMessage);
    } catch (Exception e) {
      log.error("Error processing DELETE request for tourist", e);
    }
  }
}