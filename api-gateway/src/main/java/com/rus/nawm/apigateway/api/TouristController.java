package com.rus.nawm.apigateway.api;

import com.rus.nawm.apigateway.TouristServiceGrpc;
import com.rus.nawm.apigateway.TouristServiceOuterClass;
import com.rus.nawm.apigateway.api.dto.TouristResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tourist")
@RequiredArgsConstructor
@Log4j2
public class TouristController {

  private final ModelMapper modelMapper = new ModelMapper();

  @GrpcClient("touristService")
  private TouristServiceGrpc.TouristServiceBlockingStub touristServiceGrpc;

  @GetMapping("/all")
  public ResponseEntity<List<TouristResponseDTO>> getAll() {
    log.info("Received request to get all tourists");
    var response = touristServiceGrpc.getAllTourists(TouristServiceOuterClass.Empty.newBuilder().build());
    var tourists = response.getTouristsList()
            .stream()
            .map(tourist -> modelMapper.map(tourist, TouristResponseDTO.class))
            .collect(Collectors.toList());
    log.info("Returning {} tourists", tourists.size());
    return ResponseEntity.ok(tourists);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TouristResponseDTO> getById(@PathVariable String id) {
    log.info("Received request to get tourist by ID: {}", id);
    var request = TouristServiceOuterClass.GetTouristByIdRequest.newBuilder().setId(id).build();
    var tourist = touristServiceGrpc.getTouristById(request);
    var response = modelMapper.map(tourist, TouristResponseDTO.class);
    log.info("Returning tourist: {}", response);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<TouristResponseDTO> getByEmail(@PathVariable String email) {
    log.info("Received request to get tourist by email: {}", email);
    var request = TouristServiceOuterClass.GetTouristsByEmailRequest.newBuilder().setEmail(email).build();
    var tourist = touristServiceGrpc.getTouristByEmail(request);
    var response = modelMapper.map(tourist, TouristResponseDTO.class);
    log.info("Returning tourist: {}", response);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/phone/{phoneNumber}")
  public ResponseEntity<TouristResponseDTO> getByPhoneNumber(@PathVariable String phoneNumber) {
    log.info("Received request to get tourist by phone number: {}", phoneNumber);
    var request = TouristServiceOuterClass.GetTouristsByPhoneRequest.newBuilder().setPhoneNumber(phoneNumber).build();
    var tourist = touristServiceGrpc.getTouristByPhoneNumber(request);
    var response = modelMapper.map(tourist, TouristResponseDTO.class);
    log.info("Returning tourist: {}", response);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/name/{name}/surname/{surname}")
  public ResponseEntity<List<TouristResponseDTO>> getByNameAndSurname(@PathVariable String name, @PathVariable String surname) {
    log.info("Received request to get tourists by name: {} and surname: {}", name, surname);
    var request = TouristServiceOuterClass.GetTouristsByNameAndSurnameRequest.newBuilder()
            .setName(name)
            .setSurname(surname)
            .build();
    var response = touristServiceGrpc.getTouristsByNameAndSurname(request);
    var tourists = response.getTouristsList()
            .stream()
            .map(tourist -> modelMapper.map(tourist, TouristResponseDTO.class))
            .collect(Collectors.toList());
    log.info("Returning {} tourists", tourists.size());
    return ResponseEntity.ok(tourists);
  }
}