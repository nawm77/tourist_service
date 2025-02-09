package com.rus.nawm.domain.domainservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tourists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tourist {
  @Id
  private String id;
  @Indexed
  private String name;
  @Indexed
  private String surname;
  @Indexed(unique = true)
  private String email;
  @Indexed(unique = true)
  private String phoneNumber;
  @Indexed
  private String country;
}