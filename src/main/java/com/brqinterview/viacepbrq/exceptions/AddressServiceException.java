package com.brqinterview.viacepbrq.exceptions;

public class AddressServiceException extends RuntimeException {

    public AddressServiceException() {
        super();
    }

    public AddressServiceException(String message) {
        super(message);
    }

    public AddressServiceException(String message, Throwable causa) {
        super(message, causa);
    }
}