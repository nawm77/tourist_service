package com.rus.nawm.apigateway.service;

import com.rus.nawm.apigateway.TouristServiceGrpc;
import com.rus.nawm.apigateway.TouristServiceOuterClass;
import com.rus.nawm.apigateway.api.dto.TouristResponseDTO;
import com.rus.nawm.apigateway.config.RedisConfig;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.rus.nawm.apigateway.config.RedisConfig.*;

@Service
@Log4j2
@EnableCaching
public class TouristService {

  private final ModelMapper modelMapper = new ModelMapper();

  @GrpcClient("touristService")
  private TouristServiceGrpc.TouristServiceBlockingStub touristServiceGrpc;

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
}