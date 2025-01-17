package com.rus.nawm.domain.domainservice.service;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.rus.nawm.domain.domainservice.domain.Tourist;
import com.rus.nawm.domain.domainservice.repository.TouristRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class TouristService {
  private final TouristRepository touristRepository;

  public TouristService(TouristRepository touristRepository) {
    this.touristRepository = touristRepository;
  }

  public void deleteTourist(String id) {
    log.info("Deleting tourist with ID: {}", id);
    try {
      touristRepository.deleteById(id);
      log.info("Tourist with ID: {} successfully deleted", id);
    } catch (Exception e) {
      log.error("Error while deleting tourist with ID: {}", id, e);
    }
  }

  public void save(Tourist tourist) throws MongoWriteException {
    log.info("Saving tourist: {}", tourist);
    try {
      Tourist savedTourist = touristRepository.save(tourist);
      log.info("Tourist successfully saved: {}", savedTourist);
    } catch (MongoWriteException | DuplicateKeyException e) {
      log.error("Error while saving tourist: {}", tourist, e);
      throw e;
    } catch (Exception e) {
      log.error("Error while saving tourist: {}", tourist, e);
    }
  }

  public Optional<Tourist> getTouristByEmail(String email) {
    log.info("Fetching tourist by email: {}", email);
    Optional<Tourist> tourist = touristRepository.findByEmail(email);
    if (tourist.isPresent()) {
      log.info("Tourist found with email: {}", email);
    } else {
      log.info("No tourist found with email: {}", email);
    }
    return tourist;
  }

  public Optional<Tourist> getTouristByPhoneNumber(String phoneNumber) {
    log.info("Fetching tourist by phone number: {}", phoneNumber);
    Optional<Tourist> tourist = touristRepository.findByPhoneNumber(phoneNumber);
    if (tourist.isPresent()) {
      log.info("Tourist found with phone number: {}", phoneNumber);
    } else {
      log.info("No tourist found with phone number: {}", phoneNumber);
    }
    return tourist;
  }

  public Optional<Tourist> getTouristById(String id) {
    log.info("Fetching tourist by ID: {}", id);
    Optional<Tourist> tourist = touristRepository.findById(id);
    if (tourist.isPresent()) {
      log.info("Tourist found with ID: {}", id);
    } else {
      log.info("No tourist found with ID: {}", id);
    }
    return tourist;
  }

  public void updateTourist(Tourist tourist) {
    log.info("Updating tourist: {}", tourist);
    try {
      Tourist updatedTourist = touristRepository.save(tourist);
      log.info("Tourist successfully updated: {}", updatedTourist);
    } catch (Exception e) {
      log.error("Error while updating tourist: {}", tourist, e);
    }
  }

  public List<Tourist> getTouristsByNameAndSurname(String name, String surname) {
    log.info("Fetching tourists by name: {} and surname: {}", name, surname);
    List<Tourist> tourists = touristRepository.findAllByNameAndSurname(name, surname);
    log.info("Found {} tourists with name: {} and surname: {}", tourists.size(), name, surname);
    return tourists;
  }

  public List<Tourist> getAllTourists() {
    log.info("Fetching all tourists");
    List<Tourist> tourists = touristRepository.findAll();
    log.info("Found {} tourists", tourists.size());
    return tourists;
  }
}