package com.brqinterview.viacepbrq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ViaCepResponse {

    private String logradouro;
    private String bairro;
    @JsonProperty("localidade")
    private String cidade;
    @JsonProperty("uf")
    private String estado;
    private String pais;
    @JsonProperty("ibge")
    private String codigoIBGECidade;
    @JsonProperty("gia")
    private String codigoIBGEEstado;
}
