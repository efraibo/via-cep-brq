package com.brqinterview.viacepbrq.services;

import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.entities.ViaCepResponse;
import com.brqinterview.viacepbrq.exceptions.AddressServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AddressServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Autowired
    private AddressService addressService;

    @Value("${api.url.viacep}")
    private String viaCepUrl;

    // O captor de URL será usado para verificar a URL da API correta
    private ArgumentCaptor<String> urlCaptor;

    @BeforeEach
    void setUp() {
        // Inicialize o captor de URL antes de cada teste
        urlCaptor = ArgumentCaptor.forClass(String.class);

        // Inicialize os mocks usando o MockitoAnnotations
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource({"55500000, Escada", "55590-000, Ipojuca", "02045-970, São Paulo"})
    void testGetAddressByCep_Success(String cep, String cidade) throws AddressServiceException {
        //given
        ViaCepResponse viaCepResponse = new ViaCepResponse();
        viaCepResponse.setCidade(cidade);
        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class))).thenReturn(viaCepResponse);

        //when
        Address address = addressService.getAddressByCep(cep, 0);

        //then
        assertNotNull(address);
        assertEquals(cidade, address.getCidade());
    }

    @ParameterizedTest
    @CsvSource({"5550000", "55590-00", "invalid"})
    void testGetAddressByCep_BadRequest(String invalidCep) {
        //given
        int retryCount = 0;
        String expectedErrorMessage = "Invalid CEP. The CEP must contain exactly 8 digits.";

        //when
        var addressServiceException = Assertions.<IllegalArgumentException>assertThrows(IllegalArgumentException.class,
                () -> addressService.getAddressByCep(invalidCep, retryCount));

        //then
        assertEquals(expectedErrorMessage, addressServiceException.getMessage());
    }

//    @ParameterizedTest
//    @CsvSource({"5550000", "55590-00", "invalid", ""})
//    void testGetAddressByCep_BadRequest(String cep) throws AddressServiceException {
//        // Simula o lançamento da exceção HttpClientErrorException com código de status 400 (BadRequest)
//        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid CEP");
//        when(addressService.getAddressByCep(cep, 0)).thenThrow(exception);
//
//        // Verifica se a exceção AddressServiceException é lançada
//        AddressServiceException addressServiceException = assertThrows(AddressServiceException.class,
//                () -> addressService.getAddressByCep(cep, 0));
//
//        // Verifica se o código de status e a mensagem de erro são os esperados
//        assertEquals("Invalid CEP", addressServiceException.getMessage());
//    }


//    @Test
//    void testGetAddressByCep_ApiError_Retry_Success() throws AddressServiceException, ExecutionException, InterruptedException {
//        // Mocking API responses
//        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class)))
//                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
//                .thenReturn(new ViaCepResponse());
//
//        // Test the method
//        CompletableFuture<Address> futureAddress = addressService.getAddressByCep("12345678", );
//        Address address = futureAddress.get();
//
//        assertNotNull(address);
//        assertEquals("12345678", address.getCep());
//
//        // Verify that the correct API URLs were called and retried
//        verify(restTemplate, times(2)).getForObject(urlCaptor.capture(), eq(ViaCepResponse.class));
//        assertTrue(urlCaptor.getAllValues().get(0).contains("viacep.com.br"));
//        assertTrue(urlCaptor.getAllValues().get(1).contains("viacep.com.br"));
//    }
//
//    @Test
//    void testGetAddressByCep_ApiError_Retry_Failure() {
//        // Mocking API responses to always throw an exception
//        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class)))
//                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));
//
//        // Test the method and verify that it retries for the maximum number of times
//        assertThrows(AddressServiceException.class, () -> addressService.getAddressByCep("12345678", 0));
//
//        // Verify that the correct API URLs were called and retried the maximum number of times
//        verify(restTemplate, times(5)).getForObject(anyString(), eq(ViaCepResponse.class));
//    }

    // Add more tests for the other API endpoints and error scenarios if needed.
}
