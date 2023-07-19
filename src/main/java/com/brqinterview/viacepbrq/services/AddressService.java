package com.brqinterview.viacepbrq.services;

import com.brqinterview.viacepbrq.entities.Address;
import com.brqinterview.viacepbrq.entities.ApiCepResponse;
import com.brqinterview.viacepbrq.entities.BrasilApiResponse;
import com.brqinterview.viacepbrq.entities.ViaCepResponse;
import com.brqinterview.viacepbrq.utilities.CepFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class AddressService {

    private RestTemplate restTemplate;

    @Value("${api.url.viacep}")
    private String viaCepUrl;

    @Value("${api.url.apicep}")
    private String apiCepUrl;

    @Value("${api.url.brasilapi}")
    private String brasilApiUrl;

    @Value("${api.limit.retry}")
    private int limitRetry;

    public AddressService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Async
    public CompletableFuture<Address> getAddressByCep(String cep, int retryCount) {

        CompletableFuture<Address> viaCepFuture = CompletableFuture.supplyAsync(() -> getViaCepAddress(cep))
                .exceptionally(ex -> {
                    // Tratamento de exceção para a chamada do serviço ViaCep
                    if (ex.getCause() instanceof HttpClientErrorException) {
                        HttpClientErrorException httpClientErrorException = (HttpClientErrorException) ex.getCause();
                        if (httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            // Código de status 429 (Too Many Requests)
                            // Implemente sua lógica de espera e retry aqui ou chame outro serviço
                            // Se quiser continuar para a próxima chamada, retorne null
                            return null;
                        }
                    }
                    // Trate outros erros conforme necessário
                    System.err.println("Exceção na chamada do serviço ViaCep: " + ex);
                    return null;
                });
        CompletableFuture<Address> apiCepFuture = CompletableFuture.supplyAsync(() -> getApiCepAddress(cep))
                .exceptionally(ex -> {
                    // Tratamento de exceção para a chamada do serviço ApiCep
                    if (ex.getCause() instanceof HttpClientErrorException) {
                        HttpClientErrorException httpClientErrorException = (HttpClientErrorException) ex.getCause();
                        if (httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            // Código de status 429 (Too Many Requests)
                            // Implemente sua lógica de espera e retry aqui ou chame outro serviço
                            // Se quiser continuar para a próxima chamada, retorne null
                            return null;
                        }
                    }
                    // Trate outros erros conforme necessário
                    System.err.println("Exceção na chamada do serviço ApiCep: " + ex);
                    return null;
                });
        CompletableFuture<Address> brasilApiFuture = CompletableFuture.supplyAsync(() -> getBrasilApiAddress(cep))
                .exceptionally(ex -> {
                    // Tratamento de exceção para a chamada do serviço BrasilApi
                    if (ex.getCause() instanceof HttpClientErrorException) {
                        HttpClientErrorException httpClientErrorException = (HttpClientErrorException) ex.getCause();
                        if (httpClientErrorException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            // Código de status 429 (Too Many Requests)
                            // Implemente sua lógica de espera e retry aqui ou chame outro serviço
                            // Se quiser continuar para a próxima chamada, retorne null
                            return null;
                        }
                    }
                    // Trate outros erros conforme necessário
                    System.err.println("Exceção na chamada do serviço BrasilApi: " + ex);
                    return null;
                });

        System.out.println("-------------------------------------------------------------------");
        return CompletableFuture.anyOf(viaCepFuture, apiCepFuture, brasilApiFuture)
                .handle((result, ex) -> {
                    if (ex != null) {
                        // Tratamento de exceção geral
                        System.err.println("Ocorreu uma exceção: " + ex);
                        // Realize o retry ou chame outro serviço aqui se necessário
                        if (viaCepFuture.isCompletedExceptionally() && apiCepFuture.isCompletedExceptionally() && brasilApiFuture.isCompletedExceptionally()) {
                            // Todas as chamadas falharam, chame a função de retry
                            return retryGetAddressByCep(cep, retryCount + 1);
                        }
                    }
                    // Resultado obtido com sucesso ou alguma das chamadas teve sucesso
                    if (result != null) {
                        return CompletableFuture.completedFuture((Address) result);
                    } else {
                        return retryGetAddressByCep(cep, retryCount);
                    }
                })
                .thenCompose(result -> (CompletableFuture<Address>) result);

    }

    private Address getViaCepAddress(String cep) {
        String url = viaCepUrl + cep + "/json/";
        //https://viacep.com.br/ws/55500000/json/
        ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);

        if (response != null) {
            Address address = new Address();
            address.setLogradouro(response.getLogradouro());
            address.setBairro(response.getBairro());
            address.setCidade(response.getCidade());
            address.setEstado(response.getEstado());
            address.setPais("Brasil");
            address.setCodigoIBGECidade(response.getCodigoIBGECidade());
            address.setCodigoIBGEEstado(response.getCodigoIBGEEstado());
            System.out.println("viacep");

            return address;
        }

        return null;
    }

    private Address getApiCepAddress(String cep) {
        String url = apiCepUrl + cep + ".json";
        //https://cdn.apicep.com/file/apicep/55500-000.json
        ApiCepResponse response = restTemplate.getForObject(url, ApiCepResponse.class);

        if (response != null) {
            Address address = new Address();
            address.setLogradouro(response.getLogradouro());
            address.setBairro(response.getBairro());
            address.setCidade(response.getCidade());
            address.setEstado(response.getEstado());
            address.setPais("Brasil");
            address.setCodigoIBGECidade(response.getCodigoIBGECidade());
            address.setCodigoIBGEEstado(response.getCodigoIBGEEstado());
            System.out.println("apiCepUrl");

            return address;
        }

        return null;
    }

    private Address getBrasilApiAddress(String cep) {
        String url = brasilApiUrl + cep;
        //https://brasilapi.com.br/api/cep/v1/55500000
        BrasilApiResponse response = restTemplate.getForObject(url, BrasilApiResponse.class);

        if (response != null) {
            Address address = new Address();
            address.setLogradouro(response.getLogradouro());
            address.setBairro(response.getBairro());
            address.setCidade(response.getCidade());
            address.setEstado(response.getEstado());
            address.setPais("Brasil");
            address.setCodigoIBGECidade(response.getCodigoIBGECidade());
            address.setCodigoIBGEEstado(response.getCodigoIBGEEstado());
            System.out.println("brasilApiUrl");

            return address;
        }

        return null;
    }

    private CompletableFuture<Address> retryGetAddressByCep(String cep, int retryCount) {
        // Verificar o número máximo de tentativas (5 neste caso)
        if (retryCount >= limitRetry) {
            // Excedeu o limite máximo de tentativas, retornar uma exceção
            System.err.println("Falha ao obter o endereço por CEP após 5 tentativas.");
            throw new RuntimeException("Falha ao obter o endereço por CEP após 5 tentativas.");
        }

        // Lógica de espera antes da próxima tentativa (1 segundo neste exemplo)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Chamar getAddressByCep novamente com o número de tentativas atualizado
        return getAddressByCep(cep, retryCount + 1);
    }



}
