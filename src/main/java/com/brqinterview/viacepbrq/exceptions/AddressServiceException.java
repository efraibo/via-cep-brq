package com.brqinterview.viacepbrq.exceptions;

public class AddressServiceException extends RuntimeException {

    public AddressServiceException() {
        super();
    }

    public AddressServiceException(String mensagem) {
        super(mensagem);
    }

    public AddressServiceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}