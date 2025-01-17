package com.rus.nawm.apigateway.api;

import com.rus.nawm.apigateway.api.dto.TouristRequestDTO;
import com.rus.nawm.apigateway.api.dto.TouristResponseDTO;
import com.rus.nawm.apigateway.service.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tourist")
@RequiredArgsConstructor
@Log4j2
public class TouristController {

  private final TouristService touristService;

  @GetMapping("/all")
  public ResponseEntity<List<TouristResponseDTO>> getAll() {
    log.info("Received request to get all tourists");
    List<TouristResponseDTO> tourists = touristService.getAllTourists();
    log.info("Returning {} tourists", tourists.size());
    return ResponseEntity.ok(tourists);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TouristResponseDTO> getById(@PathVariable String id) {
    log.info("Received request to get tourist by ID: {}", id);
    TouristResponseDTO tourist = touristService.getTouristById(id);
    log.info("Returning tourist: {}", tourist);
    return ResponseEntity.ok(tourist);
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<TouristResponseDTO> getByEmail(@PathVariable String email) {
    log.info("Received request to get tourist by email: {}", email);
    TouristResponseDTO tourist = touristService.getTouristByEmail(email);
    log.info("Returning tourist: {}", tourist);
    return ResponseEntity.ok(tourist);
  }

  @GetMapping("/phone/{phoneNumber}")
  public ResponseEntity<TouristResponseDTO> getByPhoneNumber(@PathVariable String phoneNumber) {
    log.info("Received request to get tourist by phone number: {}", phoneNumber);
    TouristResponseDTO tourist = touristService.getTouristByPhoneNumber(phoneNumber);
    log.info("Returning tourist: {}", tourist);
    return ResponseEntity.ok(tourist);
  }

  @GetMapping("/name/{name}/surname/{surname}")
  public ResponseEntity<List<TouristResponseDTO>> getByNameAndSurname(@PathVariable String name, @PathVariable String surname) {
    log.info("Received request to get tourists by name: {} and surname: {}", name, surname);
    List<TouristResponseDTO> tourists = touristService.getTouristsByNameAndSurname(name, surname);
    log.info("Returning {} tourists", tourists.size());
    return ResponseEntity.ok(tourists);
  }

  @PostMapping
  public ResponseEntity<?> createNewTourist(@RequestBody TouristRequestDTO touristRequestDTO) {
    log.info("Received request to create new tourist: {}", touristRequestDTO);
    try {
      touristService.saveNewTourist(touristRequestDTO);
      log.info("Tourist successfully created: {}", touristRequestDTO);
      return ResponseEntity.ok("Created");
    } catch (Exception e) {
      log.error("Error occurred while creating new tourist: {}", touristRequestDTO, e);
      return ResponseEntity.status(500).body(e.getLocalizedMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateTourist(@PathVariable String id, @RequestBody TouristRequestDTO touristRequestDTO) {
    if(touristRequestDTO.getId() == null) {
      touristRequestDTO.setId(id);
    }
    log.info("Received request to update tourist with ID: {} and data: {}", id, touristRequestDTO);
    try {
      touristService.updateTourist(id, touristRequestDTO);
      log.info("Tourist with ID {} successfully updated.", id);
      return ResponseEntity.ok("Updated");
    } catch (Exception e) {
      log.error("Error occurred while updating tourist with ID {}: {}", id, touristRequestDTO, e);
      return ResponseEntity.status(500).body(e.getLocalizedMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteTourist(@PathVariable String id) {

    log.info("Received request to delete tourist with ID: {}", id);
    try {
      touristService.deleteTourist(id);
      log.info("Tourist with ID {} successfully deleted.", id);
      return ResponseEntity.ok("Deleted");
    } catch (Exception e) {
      log.error("Error occurred while deleting tourist with ID {}: {}", id, e.getMessage(), e);
      return ResponseEntity.status(500).body(e.getLocalizedMessage());
    }
  }
}