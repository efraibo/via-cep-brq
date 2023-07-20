package com.brqinterview.viacepbrq.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends Exception {

    public NotFoundException(HttpStatus notFound, String s) {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable causa) {
        super(message, causa);
    }
}