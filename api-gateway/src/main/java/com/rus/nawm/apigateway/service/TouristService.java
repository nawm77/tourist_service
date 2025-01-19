package com.rus.nawm.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rus.nawm.apigateway.TouristServiceGrpc;
import com.rus.nawm.apigateway.TouristServiceOuterClass;
import com.rus.nawm.apigateway.api.dto.TouristRequestDTO;
import com.rus.nawm.apigateway.api.dto.TouristResponseDTO;
import com.rus.nawm.apigateway.config.RabbitMQConfig;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.rus.nawm.apigateway.config.RedisConfig.*;

@Service
@Log4j2
public class TouristService {

  private final ModelMapper modelMapper = new ModelMapper();
  private final CacheManager cacheManager;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @GrpcClient("touristService")
  private TouristServiceGrpc.TouristServiceBlockingStub touristServiceGrpc;

  private final RabbitTemplate rabbitTemplate;

  @Autowired
  public TouristService(CacheManager cacheManager1, RabbitTemplate rabbitTemplate) {
    this.cacheManager = cacheManager1;
    this.rabbitTemplate = rabbitTemplate;
  }

  public List<TouristResponseDTO> getAllTourists() {
    Cache cache = cacheManager.getCache(REDIS_ALL_TOURISTS_CACHE_KEY);
    List<TouristResponseDTO> tourists = cache != null ? cache.get("allTourists", List.class) : null;

    if (tourists == null) {
      log.info("Fetching all tourists from gRPC service");
      var response = touristServiceGrpc.getAllTourists(TouristServiceOuterClass.Empty.newBuilder().build());
      tourists = response.getTouristsList()
              .stream()
              .map(tourist -> modelMapper.map(tourist, TouristResponseDTO.class))
              .collect(Collectors.toList());

      if (cache != null) {
        cache.put("allTourists", tourists);
      }
    }

    return tourists;
  }

  public TouristResponseDTO getTouristById(String id) {
    Cache cache = cacheManager.getCache(REDIS_TOURIST_BY_ID_CACHE_KEY);
    TouristResponseDTO tourist = cache != null ? cache.get(id, TouristResponseDTO.class) : null;

    if (tourist == null) {
      log.info("Fetching tourist by ID from gRPC service: {}", id);
      var request = TouristServiceOuterClass.GetTouristByIdRequest.newBuilder().setId(id).build();
      var response = touristServiceGrpc.getTouristById(request);
      tourist = modelMapper.map(response, TouristResponseDTO.class);

      if (cache != null) {
        cache.put(id, tourist);
      }
    }

    return tourist;
  }

  public TouristResponseDTO getTouristByEmail(String email) {
    Cache cache = cacheManager.getCache(REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    TouristResponseDTO tourist = cache != null ? cache.get(email, TouristResponseDTO.class) : null;

    if (tourist == null) {
      log.info("Fetching tourist by email from gRPC service: {}", email);
      var request = TouristServiceOuterClass.GetTouristsByEmailRequest.newBuilder().setEmail(email).build();
      var response = touristServiceGrpc.getTouristByEmail(request);
      tourist = modelMapper.map(response, TouristResponseDTO.class);

      if (cache != null) {
        cache.put(email, tourist);
      }
    }

    return tourist;
  }

  public TouristResponseDTO getTouristByPhoneNumber(String phoneNumber) {
    Cache cache = cacheManager.getCache(REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    TouristResponseDTO tourist = cache != null ? cache.get(phoneNumber, TouristResponseDTO.class) : null;

    if (tourist == null) {
      log.info("Fetching tourist by phone number from gRPC service: {}", phoneNumber);
      var request = TouristServiceOuterClass.GetTouristsByPhoneRequest.newBuilder().setPhoneNumber(phoneNumber).build();
      var response = touristServiceGrpc.getTouristByPhoneNumber(request);
      tourist = modelMapper.map(response, TouristResponseDTO.class);

      if (cache != null) {
        cache.put(phoneNumber, tourist);
      }
    }

    return tourist;
  }

  public List<TouristResponseDTO> getTouristsByNameAndSurname(String name, String surname) {
    String cacheKey = name + "-" + surname;
    Cache cache = cacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    List<TouristResponseDTO> tourists = cache != null ? cache.get(cacheKey, List.class) : null;

    if (tourists == null) {
      log.info("Fetching tourists by name and surname from gRPC service: {} {}", name, surname);
      var request = TouristServiceOuterClass.GetTouristsByNameAndSurnameRequest.newBuilder()
              .setName(name)
              .setSurname(surname)
              .build();
      var response = touristServiceGrpc.getTouristsByNameAndSurname(request);
      tourists = response.getTouristsList()
              .stream()
              .map(tourist -> modelMapper.map(tourist, TouristResponseDTO.class))
              .collect(Collectors.toList());

      if (cache != null) {
        cache.put(cacheKey, tourists);
      }
    }

    return tourists;
  }

  public void saveNewTourist(TouristRequestDTO touristRequestDTO) throws Exception {
    try {
      log.info("Sending new tourist creation request: {}", touristRequestDTO);
      byte[] message = objectMapper.writeValueAsBytes(touristRequestDTO);
      rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.touristPostRequestQueueRoutingKey, message);
      log.info("Tourist creation request successfully sent to RabbitMQ");
    } catch (AmqpException e) {
      log.error("Error while sending message to RabbitMQ", e);
      throw e;
    } catch (IllegalArgumentException e) {
      log.error("Invalid tourist data: {}", touristRequestDTO, e);
      throw e;
    }
  }

  public void updateTourist(String id, TouristRequestDTO touristRequestDTO) throws Exception {
    try {
      log.info("Sending update request for tourist with ID: {} and data: {}", id, touristRequestDTO);
      byte[] message = objectMapper.writeValueAsBytes(touristRequestDTO);
      rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.touristPutRequestQueueRoutingKey, message);
      log.info("Tourist update request successfully sent to RabbitMQ");
    } catch (AmqpException e) {
      log.error("Error while sending update request for tourist with ID: {}", id, e);
      throw e;
    } catch (IllegalArgumentException e) {
      log.error("Invalid tourist data for ID: {}: {}", id, touristRequestDTO, e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while updating tourist with ID: {}", id, e);
      throw e;
    }
  }

  public void deleteTourist(String id) throws Exception {
    try {
      log.info("Sending delete request for tourist with ID: {}", id);
      rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.touristDeleteRequestQueueRoutingKey, id.getBytes());
    } catch (AmqpException e) {
      log.error("Error while sending delete request for tourist with ID: {}", id, e);
      throw e;
    } catch (IllegalArgumentException e) {
      log.error("Invalid tourist ID for deletion: {}", id, e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while deleting tourist with ID: {}", id, e);
      throw e;
    }
  }

  @RabbitListener(queues = {RabbitMQConfig.touristPostResponseQueueName})
  private void onPostResponseMessage(byte[] message) {
    try {
      TouristResponseDTO touristResponseDTO = objectMapper.readValue(message, TouristResponseDTO.class);
      log.info("Received response message for POST request: {}", touristResponseDTO);
      onSaveMethod(touristResponseDTO);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @RabbitListener(queues = {RabbitMQConfig.touristPutResponseQueueName})
  private void onPutResponseMessage(byte[] message) {
    try {
      TouristResponseDTO touristResponseDTO = objectMapper.readValue(message, TouristResponseDTO.class);
      log.info("Received response message for PUT request: {}", touristResponseDTO);
      onUpdateMethod(touristResponseDTO);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @RabbitListener(queues = {RabbitMQConfig.touristDeleteResponseQueueName})
  private void onDeleteResponseMessage(byte[] message) {
    try {
      TouristResponseDTO touristResponseDTO = objectMapper.readValue(message, TouristResponseDTO.class);
      log.info("Received response message for DELETE request: {}", touristResponseDTO);
      onDeleteMethod(touristResponseDTO);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void onSaveMethod(TouristResponseDTO touristResponseDTO) {
    log.info("Saving tourist: {}", touristResponseDTO);
    Cache cache = cacheManager.getCache(REDIS_ALL_TOURISTS_CACHE_KEY);
    if (cache != null) {
      List<TouristResponseDTO> tourists = cache.get(REDIS_ALL_TOURISTS_CACHE_KEY, List.class);
      if (tourists == null) {
        tourists = new ArrayList<>();
      }
      tourists.add(touristResponseDTO);
      cache.put(REDIS_ALL_TOURISTS_CACHE_KEY, tourists);
      log.debug("Added tourist to cache with key: {}", REDIS_ALL_TOURISTS_CACHE_KEY);
    }

    Cache cacheByNameAndSurname = cacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    if (cacheByNameAndSurname != null) {
      String cacheKey = touristResponseDTO.getName() + "-" + touristResponseDTO.getSurname();
      List<TouristResponseDTO> tourists = cacheByNameAndSurname.get(cacheKey, List.class);
      if (tourists == null) {
        tourists = new ArrayList<>();
      }
      tourists.add(touristResponseDTO);
      cacheByNameAndSurname.put(cacheKey, tourists);
      log.debug("Added tourist to cache with key: {}", cacheKey);
    }
  }

  private void onDeleteMethod(TouristResponseDTO touristResponseDTO) {
    String id = touristResponseDTO.getId();
    log.info("Deleting tourist with ID: {}", id);
    Cache cache = cacheManager.getCache(REDIS_ALL_TOURISTS_CACHE_KEY);
    if (cache != null) {
      List<TouristResponseDTO> tourists = cache.get(REDIS_ALL_TOURISTS_CACHE_KEY, List.class);
      if (tourists != null) {
        tourists.removeIf(tourist -> tourist.getId().equals(id));
        cache.put(REDIS_ALL_TOURISTS_CACHE_KEY, tourists);
        log.debug("Removed tourist from cache with key: {}", REDIS_ALL_TOURISTS_CACHE_KEY);
      }
    }

    Cache cacheById = cacheManager.getCache(REDIS_TOURIST_BY_ID_CACHE_KEY);
    if (cacheById != null) {
      cacheById.evict(id);
      log.debug("Evicted tourist from cache with key: {}", REDIS_TOURIST_BY_ID_CACHE_KEY);
    }

    Cache cacheByEmail = cacheManager.getCache(REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    if (cacheByEmail != null) {
      cacheByEmail.evict(touristResponseDTO.getEmail());
      log.debug("Evicted tourist from cache with key: {}", REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    }

    Cache cacheByPhone = cacheManager.getCache(REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    if (cacheByPhone != null) {
      cacheByPhone.evict(touristResponseDTO.getPhoneNumber());
      log.debug("Evicted tourist from cache with key: {}", REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    }

    Cache cacheByNameAndSurname = cacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    if (cacheByNameAndSurname != null) {
      String cacheKey = touristResponseDTO.getName() + "-" + touristResponseDTO.getSurname();
      List<TouristResponseDTO> tourists = cacheByNameAndSurname.get(cacheKey, List.class);
      if (tourists != null) {
        tourists.removeIf(tourist -> tourist.getId().equals(id));
        cacheByNameAndSurname.put(cacheKey, tourists);
        log.debug("Updated cache for key: {}", cacheKey);
      }
    }
  }

  private void onUpdateMethod(TouristResponseDTO touristResponseDTO) {
    log.info("Updating tourist: {}", touristResponseDTO);
    Cache cacheById = cacheManager.getCache(REDIS_TOURIST_BY_ID_CACHE_KEY);
    if (cacheById != null) {
      cacheById.put(touristResponseDTO.getId(), touristResponseDTO);
      log.debug("Updated cache for ID: {}", touristResponseDTO.getId());
    }

    Cache cacheByEmail = cacheManager.getCache(REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    if (cacheByEmail != null) {
      cacheByEmail.put(touristResponseDTO.getEmail(), touristResponseDTO);
      log.debug("Updated cache for email: {}", touristResponseDTO.getEmail());
    }

    Cache cacheByPhone = cacheManager.getCache(REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    if (cacheByPhone != null) {
      cacheByPhone.put(touristResponseDTO.getPhoneNumber(), touristResponseDTO);
      log.debug("Updated cache for phone: {}", touristResponseDTO.getPhoneNumber());
    }

    Cache cacheByNameAndSurname = cacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    if (cacheByNameAndSurname != null) {
      String cacheKey = touristResponseDTO.getName() + "-" + touristResponseDTO.getSurname();
      List<TouristResponseDTO> tourists = cacheByNameAndSurname.get(cacheKey, List.class);
      if (tourists != null) {
        tourists.removeIf(tourist -> tourist.getId().equals(touristResponseDTO.getId()));
        tourists.add(touristResponseDTO);
        cacheByNameAndSurname.put(cacheKey, tourists);
        log.debug("Updated cache for key: {}", cacheKey);
      }
    }
  }
}