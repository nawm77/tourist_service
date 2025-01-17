package com.rus.nawm.domain.domainservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.rus.nawm.domain.domainservice.domain.Tourist;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.rus.nawm.domain.domainservice.config.RabbitMQConfig.*;

@Service
@Log4j2
public class TouristExchangeListener {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final TouristService touristService;

  @Autowired
  public TouristExchangeListener(TouristService touristService) {
    this.touristService = touristService;
  }

  @RabbitListener(queues = {touristPostRequestQueueName})
  public void onPostMethod(Tourist tourist) {
    try {
      log.info("Received and deserialized tourist object for POST: {}", tourist);
      touristService.save(tourist);
    } catch (ListenerExecutionFailedException | MongoWriteException | DuplicateKeyException e) {
      log.error("Error saving tourist object: {}", tourist, e);
    }
  }

  @RabbitListener(queues = {touristPutRequestQueueName})
  public void onPutMethod(Tourist tourist) {
    try {
      log.info("Received and deserialized tourist object for PUT: {}", tourist);
      touristService.updateTourist(tourist);
    } catch (ListenerExecutionFailedException | MongoWriteException | DuplicateKeyException e) {
      log.error("Error saving tourist object: {}", tourist, e);
    } catch (Exception e) {
      log.error("Error updating tourist object: {}", tourist, e);
    }
  }

  @RabbitListener(queues = {touristDeleteRequestQueueName})
  public void onDeleteMethod(String touristId) {
    log.info("Received tourist id for DELETE: {}", touristId);
    touristService.deleteTourist(touristId);
  }
}
