package com.rus.nawm.apigateway.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristResponseDTO {
  private String id;
  private String name;
  private String surname;
  private String email;
  private String phoneNumber;
  private String country;
}
