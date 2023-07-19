package com.brqinterview.viacepbrq.controllers;

import com.brqinterview.viacepbrq.Erros.ErrorDetails;
import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.services.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping(value = "/api/Addresses")
@Api(tags = "Address API")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/{cep}")
    @ApiOperation(value = "Get address by CEP", response = Address.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Address.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDetails.class)
    })
    public ResponseEntity<?> getAddressByCep(@PathVariable String cep) {
        log.debug("Agora vai");
        try {
            CompletableFuture<Address> addressFuture = addressService.getAddressByCep(cep, 0);
            Address result = addressFuture.join();
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.err.println("Erro ao obter o endereço por CEP: " + e.getMessage());
            ErrorDetails errorDetails = new ErrorDetails("Error retrieving address by CEP");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }
}
