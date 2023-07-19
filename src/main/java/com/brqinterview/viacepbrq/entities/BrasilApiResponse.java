package com.brqinterview.viacepbrq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BrasilApiResponse {
    private String logradouro;
    private String bairro;
    @JsonProperty("city")
    private String cidade;
    @JsonProperty("state")
    private String estado;
    private String pais;
    private String codigoIBGECidade;
    private String codigoIBGEEstado;
}
