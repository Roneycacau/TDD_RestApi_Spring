package com.curso.tddrest.libraryapi.service;

import com.curso.tddrest.libraryapi.model.dto.response.BookResponse;
import com.curso.tddrest.libraryapi.model.entity.Book;

public interface BookService {
    Book save(Book book);
}