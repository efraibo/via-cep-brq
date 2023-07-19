package com.brqinterview.viacepbrq;

import com.brqinterview.viacepbrq.Erros.ExceptionHandlerController;
import com.brqinterview.viacepbrq.controllers.AddressController;
import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.services.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AddressControllerTest {
    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(addressController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();
    }

    @Test
    public void testGetAddressByCep_Success() throws Exception {

        //given
        String cep = "55500-000";
        Faker faker = new Faker();
        var addressExpected = new Address();
        String city = faker.address().city();
        addressExpected.setCidade(city);
        addressExpected.setEstado(faker.address().state());
        addressExpected.setPais(faker.address().country());
        addressExpected.setLogradouro(faker.address().streetAddress());

        CompletableFuture<Address> addressFuture = CompletableFuture.completedFuture(addressExpected);
        when(addressService.getAddressByCep(cep, 0)).thenReturn(addressFuture);

        //when
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/Addresses/{cep}", cep))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cidade").value(city))
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        String response = mvcResult.getResponse().getContentAsString();
        var addressResponse = objectMapper.readValue(response, Address.class);

        //then

        Assertions.assertEquals(addressExpected, addressResponse);
    }

    @Test
    public void testGetAddressByCep_Exception() throws Exception {
        String cep = "12345-678";
        CompletableFuture<Address> addressFuture = new CompletableFuture<>();
        addressFuture.completeExceptionally(new Exception("Test exception"));

        when(addressService.getAddressByCep(cep, 0)).thenReturn(addressFuture);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/Addresses/{cep}", cep))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Error retrieving address by CEP"));
    }
}
