package com.brqinterview.viacepbrq.services;

import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.entities.ApiCepResponse;
import com.brqinterview.viacepbrq.entities.BrasilApiResponse;
import com.brqinterview.viacepbrq.entities.ViaCepResponse;
import com.brqinterview.viacepbrq.exceptions.AddressServiceException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AddressService {

    private RestTemplate restTemplate;
    private ModelMapper modelMapper;

    @Value("${api.url.viacep}")
    private String viaCepUrl;

    @Value("${api.url.apicep}")
    private String apiCepUrl;

    @Value("${api.url.brasilapi}")
    private String brasilApiUrl;

    @Value("${api.limit.retry}")
    private int limitRetry;

    public AddressService(RestTemplateBuilder restTemplateBuilder, ModelMapper modelMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.modelMapper = modelMapper;
    }

    @Async
    public CompletableFuture<Address> getAddressByCep(String cep, int retryCount) throws AddressServiceException {

        var viaCepFuture = getViaCepAddressFuture(cep);
        var apiCepFuture = getApiCepAddressFuture(cep);
        var brasilApiFuture = getBrasilApiAddressFuture(cep);

        return CompletableFuture.anyOf(viaCepFuture, apiCepFuture, brasilApiFuture)
                .handle((result, ex) -> {

                    if (ex != null && viaCepFuture.isCompletedExceptionally() && apiCepFuture.isCompletedExceptionally() && brasilApiFuture.isCompletedExceptionally()) {
                        return retryGetAddressByCep(cep, retryCount + 1);
                    }

                    if (result != null) {
                        return CompletableFuture.completedFuture((Address) result);
                    } else {
                        return retryGetAddressByCep(cep, retryCount);
                    }
                })
                .thenCompose(result -> (CompletableFuture<Address>) result);

    }

    private CompletableFuture<Address> getBrasilApiAddressFuture(String cep) {
        return CompletableFuture.supplyAsync(() -> getBrasilApiAddress(cep))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof HttpClientErrorException httpClientErrorException && httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                       return null;
                    }

                    log.error("Exceção na chamada do serviço BrasilApi: " + ex);
                    return null;
                });
    }

    private CompletableFuture<Address> getApiCepAddressFuture(String cep) {
        return CompletableFuture.supplyAsync(() -> getApiCepAddress(cep))
                .exceptionally(ex -> {
                    if ((ex.getCause() instanceof HttpClientErrorException httpClientErrorException) && httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            return null;
                    }
                    log.error("Exceção na chamada do serviço ApiCep: " + ex);
                    return null;
                });
    }

    private CompletableFuture<Address> getViaCepAddressFuture(String cep) {
        return CompletableFuture.supplyAsync(() -> getViaCepAddress(cep))
                .exceptionally(ex -> {
                    // Tratamento de exceção para a chamada do serviço ViaCep
                    if (ex.getCause() instanceof HttpClientErrorException httpClientErrorException && httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        return null;
                    }
                    // Trate outros erros conforme necessário
                    log.error("Exceção na chamada do serviço ViaCep: " + ex);
                    return null;
                });
    }

    private <T> Address getAddressFromApi(String url, Class<T> responseType) {
        T response = restTemplate.getForObject(url, responseType);

        if (response != null) {
            Address address = modelMapper.map(response, Address.class);
            address.setPais("Brasil");
            return address;
        }

        return null;
    }

    private Address getViaCepAddress(String cep) {
        String url = viaCepUrl + cep + "/json/";
        // https://viacep.com.br/ws/55500000/json/
        return getAddressFromApi(url, ViaCepResponse.class);
    }

    private Address getApiCepAddress(String cep) {
        String url = apiCepUrl + cep + ".json";
        // https://cdn.apicep.com/file/apicep/55500-000.json
        return getAddressFromApi(url, ApiCepResponse.class);
    }

    private Address getBrasilApiAddress(String cep) {
        String url = brasilApiUrl + cep;
        // https://brasilapi.com.br/api/cep/v1/55500000
        return getAddressFromApi(url, BrasilApiResponse.class);
    }


    private CompletableFuture<Address> retryGetAddressByCep(String cep, int retryCount) throws AddressServiceException {
        if (retryCount >= limitRetry) {
            log.error("Falha ao obter o endereço por CEP após 5 tentativas.");
            throw new AddressServiceException("Falha ao obter o endereço por CEP após 5 tentativas.");
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Chamar getAddressByCep novamente com o número de tentativas atualizado
        return getAddressByCep(cep, retryCount + 1);
    }
}
