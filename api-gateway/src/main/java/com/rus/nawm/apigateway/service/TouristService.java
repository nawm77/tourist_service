package com.rus.nawm.apigateway.service;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.rus.nawm.apigateway.config.RedisConfig.*;

@Service
@Log4j2
@EnableCaching
@DependsOn("myCacheManager")
public class TouristService {

  private final ModelMapper modelMapper = new ModelMapper();

  @GrpcClient("touristService")
  private TouristServiceGrpc.TouristServiceBlockingStub touristServiceGrpc;

  private final RabbitTemplate rabbitTemplate;
  private final RedisCacheManager redisCacheManager;

  @Autowired
  public TouristService(RabbitTemplate rabbitTemplate, final @Qualifier("myCacheManager") RedisCacheManager cacheManager) {
    this.rabbitTemplate = rabbitTemplate;
    this.redisCacheManager = cacheManager;
//    log.info("Cache manager is missing {}", this.redisCacheManager == null);
  }

  @Cacheable(REDIS_ALL_TOURISTS_CACHE_KEY)
  public List<TouristResponseDTO> getAllTourists() {
    log.info("Fetching all tourists from gRPC service");
    var response = touristServiceGrpc.getAllTourists(TouristServiceOuterClass.Empty.newBuilder().build());
    return response.getTouristsList()
            .stream()
            .map(tourist -> modelMapper.map(tourist, TouristResponseDTO.class))
            .collect(Collectors.toList());
  }

  @Cacheable(value = REDIS_TOURIST_BY_ID_CACHE_KEY, key = "#id")
  public TouristResponseDTO getTouristById(String id) {
    log.info("Fetching tourist by ID from gRPC service: {}", id);
    var request = TouristServiceOuterClass.GetTouristByIdRequest.newBuilder().setId(id).build();
    var tourist = touristServiceGrpc.getTouristById(request);
    return modelMapper.map(tourist, TouristResponseDTO.class);
  }

  @Cacheable(value = REDIS_TOURIST_BY_EMAIL_CACHE_KEY, key = "#email")
  public TouristResponseDTO getTouristByEmail(String email) {
    log.info("Fetching tourist by email from gRPC service: {}", email);
    var request = TouristServiceOuterClass.GetTouristsByEmailRequest.newBuilder().setEmail(email).build();
    var tourist = touristServiceGrpc.getTouristByEmail(request);
    return modelMapper.map(tourist, TouristResponseDTO.class);
  }

  @Cacheable(value = REDIS_TOURIST_BY_PHONE_CACHE_KEY, key = "#phoneNumber")
  public TouristResponseDTO getTouristByPhoneNumber(String phoneNumber) {
    log.info("Fetching tourist by phone number from gRPC service: {}", phoneNumber);
    var request = TouristServiceOuterClass.GetTouristsByPhoneRequest.newBuilder().setPhoneNumber(phoneNumber).build();
    var tourist = touristServiceGrpc.getTouristByPhoneNumber(request);
    return modelMapper.map(tourist, TouristResponseDTO.class);
  }

  @Cacheable(value = REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, key = "#name + '-' + #surname")
  public List<TouristResponseDTO> getTouristsByNameAndSurname(String name, String surname) {
    log.info("Fetching tourists by name and surname from gRPC service: {} {}", name, surname);
    var request = TouristServiceOuterClass.GetTouristsByNameAndSurnameRequest.newBuilder()
            .setName(name)
            .setSurname(surname)
            .build();
    var response = touristServiceGrpc.getTouristsByNameAndSurname(request);
    return response.getTouristsList()
            .stream()
            .map(tourist -> modelMapper.map(tourist, TouristResponseDTO.class))
            .collect(Collectors.toList());
  }

  public void saveNewTourist(TouristRequestDTO touristRequestDTO) throws Exception {
    try {
      log.info("Sending new tourist creation request: {}", touristRequestDTO);
      rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.touristPostRequestQueueRoutingKey, touristRequestDTO);
      log.info("Tourist creation request successfully sent to RabbitMQ");
    } catch (AmqpException e) {
      log.error("Error while sending message to RabbitMQ", e);
      throw e;
    } catch (IllegalArgumentException e) {
      log.error("Invalid tourist data: {}", touristRequestDTO, e);
      throw e;
    }
  }

  private void onSaveMethod(TouristResponseDTO touristResponseDTO) {
    if(this.redisCacheManager == null) {
      log.info("Cache manager in method is null");
    }
    if(this.redisCacheManager != null) {
      Cache cacheByEmail = redisCacheManager.getCache(REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
      Cache cacheByPhone = redisCacheManager.getCache(REDIS_TOURIST_BY_PHONE_CACHE_KEY);
      Cache allTouristsCache = redisCacheManager.getCache(REDIS_ALL_TOURISTS_CACHE_KEY);
      Cache cacheById = redisCacheManager.getCache(REDIS_TOURIST_BY_ID_CACHE_KEY);
      Cache cacheByNameAndSurname = redisCacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
      if (cacheByEmail != null) {
        log.info("Saving tourist in cache by email: {}", touristResponseDTO.getEmail());
        cacheByEmail.put(touristResponseDTO.getEmail(), touristResponseDTO);
      } else {
        log.warn("Cache not found for email: {}", REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
      }
      if (cacheByPhone != null) {
        log.info("Saving tourist in cache by phone: {}", touristResponseDTO.getPhoneNumber());
        cacheByPhone.put(touristResponseDTO.getPhoneNumber(), touristResponseDTO);
      } else {
        log.warn("Cache not found for phone: {}", REDIS_TOURIST_BY_PHONE_CACHE_KEY);
      }
      if (allTouristsCache != null) {
        log.info("Saving tourist in all tourists cache");
        List tourists = allTouristsCache.get(REDIS_ALL_TOURISTS_CACHE_KEY, List.class);
        tourists.add(touristResponseDTO);
        allTouristsCache.put(REDIS_ALL_TOURISTS_CACHE_KEY, tourists);
      } else {
        log.warn("Cache not found for all tourists: {}", REDIS_ALL_TOURISTS_CACHE_KEY);
      }
      if (cacheById != null) {
        log.info("Saving tourist in cache by ID: {}", touristResponseDTO.getId());
        cacheById.put(touristResponseDTO.getId(), touristResponseDTO);
      } else {
        log.warn("Cache not found for ID: {}", REDIS_TOURIST_BY_ID_CACHE_KEY);
      }
      if (cacheByNameAndSurname != null) {
        log.info("Saving tourist in cache by name and surname: {} {}", touristResponseDTO.getName(), touristResponseDTO.getSurname());
        List tourists = cacheByNameAndSurname.get(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, List.class);
        tourists.add(touristResponseDTO);
        cacheByNameAndSurname.put(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, tourists);
      } else {
        log.warn("Cache not found for name and surname: {}", REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
      }
    } else {
      log.error("CacheManager is null");
    }
  }

  private void onDeleteMethod(String id) {
    Cache cacheById = redisCacheManager.getCache(REDIS_TOURIST_BY_ID_CACHE_KEY);
    Cache cacheByEmail = redisCacheManager.getCache(REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    Cache cacheByPhone = redisCacheManager.getCache(REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    Cache allTouristsCache = redisCacheManager.getCache(REDIS_ALL_TOURISTS_CACHE_KEY);
    Cache cacheByNameAndSurname = redisCacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    if (cacheById != null) {
      log.info("Deleting tourist from cache by ID: {}", id);
      TouristResponseDTO touristResponseDTO = cacheById.get(id, TouristResponseDTO.class);
      if (touristResponseDTO != null) {
        cacheById.evict(id);
        if (cacheByEmail != null) {
          log.info("Deleting tourist from cache by email: {}", id);
          cacheByEmail.evict(touristResponseDTO.getEmail());
        } else {
          log.warn("Cache not found for email: {}", REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
        }
        if (cacheByPhone != null) {
          log.info("Deleting tourist from cache by phone: {}", id);
          cacheByPhone.evict(touristResponseDTO.getPhoneNumber());
        } else {
          log.warn("Cache not found for phone: {}", REDIS_TOURIST_BY_PHONE_CACHE_KEY);
        }
        if (allTouristsCache != null) {
          log.info("Deleting tourist from all tourists cache");
          List tourists = allTouristsCache.get(REDIS_ALL_TOURISTS_CACHE_KEY, List.class);
          tourists.removeIf(tourist -> ((TouristResponseDTO) tourist).getId().equals(id));
          allTouristsCache.put(REDIS_ALL_TOURISTS_CACHE_KEY, tourists);
        } else {
          log.warn("Cache not found for all tourists: {}", REDIS_ALL_TOURISTS_CACHE_KEY);
        }
        if (cacheByNameAndSurname != null) {
          log.info("Deleting tourist from cache by name and surname: {} {}", touristResponseDTO.getName(), touristResponseDTO.getSurname());
          List tourists = cacheByNameAndSurname.get(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, List.class);
          tourists.removeIf(tourist -> ((TouristResponseDTO) tourist).getId().equals(id));
          cacheByNameAndSurname.put(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, tourists);
        } else {
          log.warn("Cache not found for name and surname: {}", REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
        }
      } else {
        log.warn("Tourist not found in cache by ID: {}", id);
      }
    } else {
      log.warn("Cache not found for ID: {}", REDIS_TOURIST_BY_ID_CACHE_KEY);
    }
  }

  private void onUpdateMethod(TouristResponseDTO touristResponseDTO) {
    Cache cacheById = redisCacheManager.getCache(REDIS_TOURIST_BY_ID_CACHE_KEY);
    Cache cacheByEmail = redisCacheManager.getCache(REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    Cache cacheByPhone = redisCacheManager.getCache(REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    Cache allTouristsCache = redisCacheManager.getCache(REDIS_ALL_TOURISTS_CACHE_KEY);
    Cache cacheByNameAndSurname = redisCacheManager.getCache(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    if (cacheById != null) {
      log.info("Updating tourist in cache by ID: {}", touristResponseDTO.getId());
      cacheById.put(touristResponseDTO.getId(), touristResponseDTO);
    } else {
      log.warn("Cache not found for ID: {}", REDIS_TOURIST_BY_ID_CACHE_KEY);
    }
    if (cacheByEmail != null) {
      log.info("Updating tourist in cache by email: {}", touristResponseDTO.getEmail());
      cacheByEmail.put(touristResponseDTO.getEmail(), touristResponseDTO);
    } else {
      log.warn("Cache not found for email: {}", REDIS_TOURIST_BY_EMAIL_CACHE_KEY);
    }
    if (cacheByPhone != null) {
      log.info("Updating tourist in cache by phone: {}", touristResponseDTO.getPhoneNumber());
      cacheByPhone.put(touristResponseDTO.getPhoneNumber(), touristResponseDTO);
    } else {
      log.warn("Cache not found for phone: {}", REDIS_TOURIST_BY_PHONE_CACHE_KEY);
    }
    if (allTouristsCache != null) {
      log.info("Updating all tourists cache");
      List tourists = allTouristsCache.get(REDIS_ALL_TOURISTS_CACHE_KEY, List.class);
      tourists.removeIf(tourist -> ((TouristResponseDTO) tourist).getId().equals(touristResponseDTO.getId()));
      tourists.add(touristResponseDTO);
      allTouristsCache.put(REDIS_ALL_TOURISTS_CACHE_KEY, tourists);
    } else {
      log.warn("Cache not found for all tourists: {}", REDIS_ALL_TOURISTS_CACHE_KEY);
    }
    if (cacheByNameAndSurname != null) {
      log.info("Updating tourist in cache by name and surname: {} {}", touristResponseDTO.getName(), touristResponseDTO.getSurname());
      List tourists = cacheByNameAndSurname.get(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, List.class);
      tourists.removeIf(tourist -> ((TouristResponseDTO) tourist).getId().equals(touristResponseDTO.getId()));
      tourists.add(touristResponseDTO);
      cacheByNameAndSurname.put(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, tourists);
    } else {
      log.warn("Cache not found for name and surname: {}", REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY);
    }
  }

  public void updateTourist(String id, TouristRequestDTO touristRequestDTO) throws Exception {
    try {
      log.info("Sending update request for tourist with ID: {} and data: {}", id, touristRequestDTO);
      rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.touristPutRequestQueueRoutingKey, touristRequestDTO);
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
      rabbitTemplate.convertAndSend(RabbitMQConfig.directExchangeName, RabbitMQConfig.touristDeleteRequestQueueRoutingKey, id);
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
  private void onPostResponseMessage(TouristResponseDTO touristResponseDTO) {
    log.info("Received response message for POST request: {}", touristResponseDTO);
    onSaveMethod(touristResponseDTO);
  }

  @RabbitListener(queues = {RabbitMQConfig.touristPutResponseQueueName})
  private void onPutResponseMessage(TouristResponseDTO touristResponseDTO) {
    log.info("Received response message for PUT request: {}", touristResponseDTO);
    onUpdateMethod(touristResponseDTO);
  }

  @RabbitListener(queues = {RabbitMQConfig.touristDeleteResponseQueueName})
  private void onDeleteResponseMessage(String id) {
    log.info("Received response message for DELETE request: {}", id);
    onDeleteMethod(id);
  }
}