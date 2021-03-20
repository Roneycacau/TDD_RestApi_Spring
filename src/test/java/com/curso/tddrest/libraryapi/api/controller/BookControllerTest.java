package com.curso.tddrest.libraryapi.api.controller;

import com.curso.tddrest.libraryapi.exception.BusinessException;
import com.curso.tddrest.libraryapi.model.dto.request.BookRequest;
import com.curso.tddrest.libraryapi.model.dto.response.BookResponse;
import com.curso.tddrest.libraryapi.model.entity.Book;
import com.curso.tddrest.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static final String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar novo livro com base no json recebido")
    public void createBookTest() throws Exception {

        BookRequest bookRequest = newBook();

        Book bookSaved = Book.builder()
                .id(1L)
                .author("Escritor Famoso")
                .title("Titulo Maroto")
                .isbn("123456")
                .build();

        BookResponse response = BookResponse.builder()
                .id(bookSaved.getId())
                .isbn(bookSaved.getIsbn())
                .author(bookSaved.getAuthor())
                .title(bookSaved.getTitle())
                .build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(bookSaved);

        String json = new ObjectMapper().writeValueAsString(bookRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(response.getTitle()))
                .andExpect(jsonPath("author").value(response.getAuthor()))
                .andExpect(jsonPath("isbn").value(response.getIsbn()))
        ;
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criar o livro")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookRequest());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));

    }

    @Test
    @DisplayName("Deve lançar erro caso ISBN já seja cadastrado")
    public void createBookWithDuplicatedIsbn() throws Exception {
        BookRequest bookRequest = newBook();

        String json = new ObjectMapper().writeValueAsString(bookRequest);

        String message = "ISBN já cadastrado";
        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(message));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(message));
    }

    private BookRequest newBook() {
        return BookRequest.builder()
                .author("Escritor Famoso")
                .title("Titulo Maroto")
                .isbn("123456")
                .build();
    }
}