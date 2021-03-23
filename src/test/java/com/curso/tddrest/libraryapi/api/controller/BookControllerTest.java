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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static final String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;
    private final Long id = 1L;

    @Test
    @DisplayName("Deve criar novo livro com base no json recebido")
    public void createBookTest() throws Exception {

        BookRequest bookRequest = newBookRequest();

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

        BDDMockito.given(service.save(any(Book.class))).willReturn(bookSaved);

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
        BookRequest bookRequest = newBookRequest();

        String json = new ObjectMapper().writeValueAsString(bookRequest);

        String message = "ISBN já cadastrado";
        BDDMockito.given(service.save(any(Book.class))).willThrow(new BusinessException(message));

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

    @Test
    @DisplayName("Deve exibir informações do livro solicitado")
    public void getBookDetailsById() throws Exception {

        Long bookId = id;
        Book book = newBook();
        BDDMockito.given(service.getById(bookId)).willReturn(Optional.ofNullable(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve exibir erro de livro não encontrado")
    public void bookNotFoundTest() throws Exception {

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(newBook()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve exibir erro de livro não encontrado ao tentar deletar")
    public void bookToDeleteNotFound() throws Exception {
        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {

        Long bookId = id;
        Book bookToBeUpdated = Book.builder()
                .id(bookId)
                .isbn("321")
                .author("Autor desconhecido")
                .title("Titulo meia boca")
                .build();
        Book bookUpdated = Book.builder()
                .id(id)
                .author("Escritor Famoso")
                .title("Titulo Maroto")
                .isbn("321")
                .build();

        BDDMockito.given(service.getById(bookId)).willReturn(Optional.of(bookToBeUpdated));
        BDDMockito.given(service.update(bookUpdated)).willReturn(bookUpdated);


        String json = new ObjectMapper().writeValueAsString(bookUpdated);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(bookUpdated.getId()))
                .andExpect(jsonPath("title").value(bookUpdated.getTitle()))
                .andExpect(jsonPath("author").value(bookUpdated.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookUpdated.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar livro não existe ao tentar atualizar")
    public void updateBookNotFoundTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(newBookRequest());

        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void bookFilterTest() throws Exception {
        Book book = newBook();
        String queryString = String.format("?title=%s&author=%s&isbn=%s&page=0&size=100",
                book.getTitle(), book.getAuthor(), book.getIsbn());

        BookResponse bookResponse = BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .title(book.getTitle())
                .build();

        BDDMockito.given(service.find(any(Book.class), any(Pageable.class))).willReturn(
                new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0))
        ;
    }

    private BookRequest newBookRequest() {
        return BookRequest.builder()
                .author("Escritor Famoso")
                .title("Titulo Maroto")
                .isbn("123456")
                .build();
    }

    private Book newBook() {
        return Book.builder()
                .id(id)
                .author("Escritor Famoso")
                .title("Titulo Maroto")
                .isbn("123456")
                .build();
    }
}