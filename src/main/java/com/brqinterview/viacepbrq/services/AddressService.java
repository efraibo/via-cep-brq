package com.brqinterview.viacepbrq.services;

import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.entities.ApiCepResponse;
import com.brqinterview.viacepbrq.entities.BrasilApiResponse;
import com.brqinterview.viacepbrq.entities.ViaCepResponse;
import com.brqinterview.viacepbrq.exceptions.AddressServiceException;
import com.brqinterview.viacepbrq.utilities.CepUtils;
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
import java.util.function.Supplier;

@Slf4j
@Service
public class AddressService {

    private final RestTemplate restTemplate;
    private final ModelMapper modelMapper;

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
    public Address getAddressByCep(String cep, int retryCount) throws AddressServiceException {
        log.info("Starting the address lookup by CEP");
        String formatCep = CepUtils.formatCep(cep);
        var viaCepFuture = getViaCepAddressFuture(formatCep);
        var apiCepFuture = getApiCepAddressFuture(formatCep);
        var brazilApiFuture = getBrasilApiAddressFuture(formatCep);

        return getAddressCompletableFuture(formatCep, retryCount, viaCepFuture, apiCepFuture, brazilApiFuture)
                .join();
    }

    private CompletableFuture<Address> getBrasilApiAddressFuture(String cep) {
        return callApiAsync(() -> getBrasilApiAddress(cep));
    }

    private CompletableFuture<Address> getApiCepAddressFuture(String cep) {
        return callApiAsync(() -> getApiCepAddress(cep));
    }

    private CompletableFuture<Address> getViaCepAddressFuture(String cep) {
        return callApiAsync(() -> getViaCepAddress(cep));
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

    private CompletableFuture<Address> getAddressCompletableFuture(String cep, int retryCount, CompletableFuture<Address> viaCepFuture, CompletableFuture<Address> apiCepFuture, CompletableFuture<Address> brasilApiFuture) {
        return CompletableFuture.anyOf(viaCepFuture, apiCepFuture, brasilApiFuture)
                .handle((result, ex) -> {

                    if (ex != null && viaCepFuture.isCompletedExceptionally() && apiCepFuture.isCompletedExceptionally() && brasilApiFuture.isCompletedExceptionally()) {
                        return retryGetAddressByCep(cep, retryCount + 1);
                    }

                    if (result != null) {
                        log.info("Success execution.");
                        return CompletableFuture.completedFuture((Address) result);
                    } else {
                        log.info("Executing retry for the {} time.", retryCount + 1);
                        return retryGetAddressByCep(cep, retryCount);
                    }
                })
                .thenCompose(result -> result);
    }

    private <T> CompletableFuture<T> callApiAsync(Supplier<T> serviceSupplier) {
        return CompletableFuture.supplyAsync(serviceSupplier)
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof HttpClientErrorException httpClientErrorException &&
                            httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        return null;
                    }
                    log.error("Exception during API call: " + ex);
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

    private CompletableFuture<Address> retryGetAddressByCep(String cep, int retryCount) throws AddressServiceException {
        if (retryCount >= limitRetry) {
            log.error("Failed to get the address by CEP after {} attempts.", limitRetry);
            throw new AddressServiceException("Failed to get the address by CEP after 5 attempts.");
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return CompletableFuture.completedFuture(getAddressByCep(cep, retryCount + 1));
    }
}
