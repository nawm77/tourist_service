package com.rus.nawm.domain.domainservice.service;

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
    touristRepository.deleteById(id);
  }

  public Tourist save(Tourist tourist) throws MongoWriteException {
    return touristRepository.save(tourist);
  }

  public Optional<Tourist> getTouristByEmail(String email) {
    return touristRepository.findByEmail(email);
  }

  public Optional<Tourist> getTouristByPhoneNumber(String phoneNumber) {
    return touristRepository.findByPhoneNumber(phoneNumber);
  }

  public Optional<Tourist> getTouristById(String id) {
    return touristRepository.findById(id);
  }

  public Tourist updateTourist(Tourist tourist) {
    return touristRepository.save(tourist);
  }

  public List<Tourist> getTouristsByNameAndSurname(String name, String surname) {
    return touristRepository.findAllByNameAndSurname(name, surname);
  }

  public List<Tourist> getAllTourists() {
    return touristRepository.findAll();
  }
}
