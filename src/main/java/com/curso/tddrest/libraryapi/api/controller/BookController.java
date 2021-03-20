package com.curso.tddrest.libraryapi.api.controller;

import com.curso.tddrest.libraryapi.api.exception.ApiErrors;
import com.curso.tddrest.libraryapi.exception.BusinessException;
import com.curso.tddrest.libraryapi.model.dto.response.BookResponse;
import com.curso.tddrest.libraryapi.model.dto.request.BookRequest;
import com.curso.tddrest.libraryapi.model.entity.Book;
import com.curso.tddrest.libraryapi.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;
    private final ModelMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@RequestBody @Valid BookRequest request) {
        Book entity = mapper.map(request, Book.class);
        entity = service.save(entity);
        return mapper.map(entity, BookResponse.class);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new ApiErrors(ex.getBindingResult());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessExceptions(BusinessException ex){
        return new ApiErrors(ex);
    }
}
