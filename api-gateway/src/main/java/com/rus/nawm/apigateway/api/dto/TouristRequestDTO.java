package com.rus.nawm.apigateway.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TouristRequestDTO {
  private String id;
  private String name;
  private String surname;
  private String email;
  private String phoneNumber;
  private String country;
}
