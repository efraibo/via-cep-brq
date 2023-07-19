package com.brqinterview.viacepbrq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiCepResponse {
    @JsonProperty("address")
    private String logradouro;
    @JsonProperty("district")
    private String bairro;
    @JsonProperty("city")
    private String cidade;
    @JsonProperty("state")
    private String estado;
    private String pais;
    private String codigoIBGECidade;
    private String codigoIBGEEstado;
}
