package com.rus.nawm.domain.domainservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.rus.nawm.apigateway.TouristServiceOuterClass;
import com.rus.nawm.domain.domainservice.domain.Tourist;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
      TouristServiceOuterClass.Tourist tourist = TouristServiceOuterClass.Tourist.parseFrom(message);
      log.info("Received and deserialized tourist object for POST: {}", tourist);
      Tourist touristEntity = mapToTourist(tourist);
      touristEntity = touristService.save(touristEntity);
      TouristServiceOuterClass.Tourist touristProto = TouristServiceOuterClass.Tourist.newBuilder()
              .setId(touristEntity.getId())
              .setEmail(touristEntity.getEmail())
              .setCountry(touristEntity.getCountry())
              .setName(touristEntity.getName())
              .setSurname(touristEntity.getSurname())
              .setPhoneNumber(touristEntity.getPhoneNumber())
              .build();
      Message messageMQ = toProtobufMessage(touristProto);
      rabbitTemplate.convertAndSend(directExchangeName, touristPostResponseQueueRoutingKey, messageMQ);
    } catch (Exception e) {
      log.error("Error processing POST request for tourist", e);
    }
  }

  @RabbitListener(queues = {touristPutRequestQueueName})
  public void onPutMethod(byte[] message) {
    try {
      TouristServiceOuterClass.Tourist tourist = TouristServiceOuterClass.Tourist.parseFrom(message);
      log.info("Received and deserialized tourist object for PUT: {}", tourist);
      Tourist touristEntity = mapToTourist(tourist);
      touristEntity = touristService.updateTourist(touristEntity);
      TouristServiceOuterClass.Tourist touristProto = TouristServiceOuterClass.Tourist.newBuilder()
              .setId(touristEntity.getId())
              .setEmail(touristEntity.getEmail())
              .setCountry(touristEntity.getCountry())
              .setName(touristEntity.getName())
              .setSurname(touristEntity.getSurname())
              .setPhoneNumber(touristEntity.getPhoneNumber())
              .build();
      Message messageMQ = toProtobufMessage(touristProto);
      rabbitTemplate.convertAndSend(directExchangeName, touristPutResponseQueueRoutingKey, messageMQ);
    } catch (ListenerExecutionFailedException | MongoWriteException | DuplicateKeyException e) {
      log.error("Error processing PUT request for tourist", e);
    } catch (Exception e) {
      log.error("Error updating tourist object", e);
    }
  }

  @RabbitListener(queues = {touristDeleteRequestQueueName})
  public void onDeleteMethod(String id) {
    try {
      log.info("Received tourist id for DELETE: {}", id);
      Tourist deletedTourist = touristService.deleteTourist(id);
      TouristServiceOuterClass.Tourist touristProto = TouristServiceOuterClass.Tourist.newBuilder()
              .setId(deletedTourist.getId())
              .setEmail(deletedTourist.getEmail())
              .setCountry(deletedTourist.getCountry())
              .setName(deletedTourist.getName())
              .setSurname(deletedTourist.getSurname())
              .setPhoneNumber(deletedTourist.getPhoneNumber())
              .build();
      Message message = toProtobufMessage(touristProto);
      rabbitTemplate.convertAndSend(directExchangeName, touristDeleteResponseQueueRoutingKey, message);
    } catch (Exception e) {
      log.error("Error processing DELETE request for tourist", e);
    }
  }

  private Tourist mapToTourist(TouristServiceOuterClass.Tourist tourist) {
    String id = tourist.getId().isEmpty() ? UUID.randomUUID().toString() : tourist.getId();
    return Tourist.builder()
            .id(id)
            .email(tourist.getEmail())
            .name(tourist.getName())
            .surname(tourist.getSurname())
            .phoneNumber(tourist.getPhoneNumber())
            .country(tourist.getCountry())
            .build();
  }

  private Message toProtobufMessage(com.google.protobuf.Message protoMessage) {
    byte[] payload = protoMessage.toByteArray();
    MessageProperties properties = new MessageProperties();
    properties.setContentType("application/x-protobuf");
    return new Message(payload, properties);
  }
}