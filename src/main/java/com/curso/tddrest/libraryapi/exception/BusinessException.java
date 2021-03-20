package com.curso.tddrest.libraryapi.exception;

public class BusinessException extends RuntimeException{

    public BusinessException(String s) {
        super(s);
    }
}
