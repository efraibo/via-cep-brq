package com.brqinterview.viacepbrq.controllers;

import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.erros.CustomExceptionHandler;
import com.brqinterview.viacepbrq.services.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AddressControllerTest {
    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(addressController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @CsvSource({"55500000", "55500-000"})
    void testGetAddressByCep_Success(String cep) throws Exception {

        //given
        Faker faker = new Faker();
        Address addressExpected = new Address();
        String city = faker.address().city();
        addressExpected.setCidade(city);
        addressExpected.setEstado(faker.address().state());
        addressExpected.setPais(faker.address().country());
        addressExpected.setLogradouro(faker.address().streetAddress());
        Mockito.when(addressService.getAddressByCep(cep, 0)).thenReturn(addressExpected);

        //when
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/Addresses/{cep}", cep))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cidade").value(city))
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        String response = mvcResult.getResponse().getContentAsString();
        var addressResponse = objectMapper.readValue(response, Address.class);

        // Then
        assertEquals(addressExpected.getCidade(), addressResponse.getCidade());
        assertEquals(addressExpected.getEstado(), addressResponse.getEstado());
        assertEquals(addressExpected.getPais(), addressResponse.getPais());
        assertEquals(addressExpected.getLogradouro(), addressResponse.getLogradouro());

        // Verifique se o método do serviço foi chamado corretamente
        Mockito.verify(addressService).getAddressByCep(cep, 0);
    }

    @Test
    void testGetAddressByCep_InternalError() throws Exception {
        // Given
        String cep = "55500-000";
        final String error_message ="Simulated internal error";
        Mockito.when(addressService.getAddressByCep(cep, 0))
                .thenThrow(new RuntimeException(error_message));

        // When
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/Addresses/{cep}", cep))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        String response = mvcResult.getResponse().getContentAsString();
        assertTrue(response.contains(error_message));
    }
}
