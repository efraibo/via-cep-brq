package com.brqinterview.viacepbrq.controllers;

import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.erros.ErrorDetails;
import com.brqinterview.viacepbrq.services.AddressService;
import io.swagger.annotations.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @ApiOperation(value = "Get address by CEP", notes = "Retrieves address information based on the provided CEP.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Address.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDetails.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDetails.class)
    })
    public ResponseEntity<Address> getAddressByCep(
            @ApiParam(value = "CEP (Código de Endereçamento Postal) to lookup.", example = "12345678", required = true)
            @PathVariable String cep) {
        var address = addressService.getAddressByCep(cep, 0);
        return ResponseEntity.ok(address);
    }
}
