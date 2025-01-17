package com.rus.nawm.domain.domainservice.repository;

import com.rus.nawm.domain.domainservice.domain.Tourist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TouristRepository extends MongoRepository<Tourist, String> {
  @Query("{ 'email' : ?0 }")
  Optional<Tourist> findByEmail(String email);

  @Query("{ 'country' : ?0 }")
  List<Tourist> findAllByCountry(String country);

  @Query("{ 'name' : ?0, 'surname' : ?1 }")
  List<Tourist> findAllByNameAndSurname(String name, String surname);

  @Query("{ 'phoneNumber' : ?0 }")
  Optional<Tourist> findByPhoneNumber(String phoneNumber);
}
