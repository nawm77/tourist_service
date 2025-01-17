package com.rus.nawm.domain.domainservice.grpc;

import com.rus.nawm.apigateway.TouristServiceGrpc;
import com.rus.nawm.apigateway.TouristServiceOuterClass;
import com.rus.nawm.domain.domainservice.domain.Tourist;
import com.rus.nawm.domain.domainservice.service.TouristService;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@GrpcService
public class TouristGrpcService extends TouristServiceGrpc.TouristServiceImplBase {

  private final TouristService touristService;
  private final ModelMapper modelMapper = new ModelMapper();

  public TouristGrpcService(TouristService touristService) {
    this.touristService = touristService;
  }

  @Override
  public void getTouristByEmail(TouristServiceOuterClass.GetTouristsByEmailRequest request, StreamObserver<TouristServiceOuterClass.Tourist> responseObserver) {
    log.info("Received getTouristByEmail request for email: {}", request.getEmail());
    Optional<Tourist> touristOpt = touristService.getTouristByEmail(request.getEmail());
    if (touristOpt.isPresent()) {
      TouristServiceOuterClass.Tourist response = modelMapper.map(touristOpt.get(), TouristServiceOuterClass.Tourist.Builder.class).build();
      responseObserver.onNext(response);
      log.info("Tourist found: {}", response);
    } else {
      log.warn("Tourist not found for email: {}", request.getEmail());
      responseObserver.onError(new Exception("Tourist not found"));
    }
    responseObserver.onCompleted();
  }

  @Override
  public void getTouristByPhoneNumber(TouristServiceOuterClass.GetTouristsByPhoneRequest request, StreamObserver<TouristServiceOuterClass.Tourist> responseObserver) {
    log.info("Received getTouristByPhoneNumber request for phone number: {}", request.getPhoneNumber());
    Optional<Tourist> touristOpt = touristService.getTouristByPhoneNumber(request.getPhoneNumber());
    if (touristOpt.isPresent()) {
      TouristServiceOuterClass.Tourist response = modelMapper.map(touristOpt.get(), TouristServiceOuterClass.Tourist.Builder.class).build();
      responseObserver.onNext(response);
      log.info("Tourist found: {}", response);
    } else {
      log.warn("Tourist not found for phone number: {}", request.getPhoneNumber());
      responseObserver.onError(new Exception("Tourist not found"));
    }
    responseObserver.onCompleted();
  }

  @Override
  public void getTouristById(TouristServiceOuterClass.GetTouristByIdRequest request, StreamObserver<TouristServiceOuterClass.Tourist> responseObserver) {
    log.info("Received getTouristById request for ID: {}", request.getId());
    Optional<Tourist> touristOpt = touristService.getTouristById(request.getId());
    if (touristOpt.isPresent()) {
      TouristServiceOuterClass.Tourist response = modelMapper.map(touristOpt.get(), TouristServiceOuterClass.Tourist.Builder.class).build();
      responseObserver.onNext(response);
      log.info("Tourist found: {}", response);
    } else {
      log.warn("Tourist not found for ID: {}", request.getId());
      responseObserver.onError(new Exception("Tourist not found"));
    }
    responseObserver.onCompleted();
  }

  @Override
  public void getTouristsByNameAndSurname(TouristServiceOuterClass.GetTouristsByNameAndSurnameRequest request, StreamObserver<TouristServiceOuterClass.GetTouristsResponse> responseObserver) {
    log.info("Received getTouristsByNameAndSurname request for name: {} and surname: {}", request.getName(), request.getSurname());
    List<Tourist> tourists = touristService.getTouristsByNameAndSurname(request.getName(), request.getSurname());
    TouristServiceOuterClass.GetTouristsResponse.Builder responseBuilder = TouristServiceOuterClass.GetTouristsResponse.newBuilder();
    tourists.forEach(tourist -> responseBuilder.addTourists(modelMapper.map(tourist, TouristServiceOuterClass.Tourist.Builder.class).build()));
    log.info("Number of tourists found: {}", tourists.size());
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }

  @Override
  public void getAllTourists(TouristServiceOuterClass.Empty request, StreamObserver<TouristServiceOuterClass.GetTouristsResponse> responseObserver) {
    log.info("Received getAllTourists request");
    List<Tourist> tourists = touristService.getAllTourists();
    TouristServiceOuterClass.GetTouristsResponse.Builder responseBuilder = TouristServiceOuterClass.GetTouristsResponse.newBuilder();
    tourists.forEach(tourist -> responseBuilder.addTourists(modelMapper.map(tourist, TouristServiceOuterClass.Tourist.Builder.class).build()));
    log.info("Number of tourists found: {}", tourists.size());
    responseObserver.onNext(responseBuilder.build());
    responseObserver.onCompleted();
  }
}