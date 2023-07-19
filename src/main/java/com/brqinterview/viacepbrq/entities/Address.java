package com.brqinterview.viacepbrq.entities;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class Address {
    private String logradouro;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;
    @Nullable
    private String codigoIBGECidade;
    @Nullable
    private String codigoIBGEEstado;
}
