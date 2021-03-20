package com.curso.tddrest.libraryapi.service.impl;

import com.curso.tddrest.libraryapi.exception.BusinessException;
import com.curso.tddrest.libraryapi.model.entity.Book;
import com.curso.tddrest.libraryapi.repository.BookRepository;
import com.curso.tddrest.libraryapi.service.BookService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository repository;

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("ISBN já cadastrado");
        }
        return repository.save(book);
    }
}
