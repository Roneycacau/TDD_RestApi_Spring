package com.curso.tddrest.libraryapi.service;

import com.curso.tddrest.libraryapi.exception.BusinessException;
import com.curso.tddrest.libraryapi.model.entity.Book;
import com.curso.tddrest.libraryapi.repository.BookRepository;
import com.curso.tddrest.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {
    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve Salvar um livro")
    public void SaveBook(){
        Book book = createValidBook();

        when(repository.existsByIsbn(anyString())).thenReturn(false);
        when(repository.save(book))
                .thenReturn(Book.builder()
                        .id(1L)
                        .isbn("123")
                        .author("Autor Famoso")
                        .title("Titulo Bacana")
                        .build());

        Book saved = service.save(book);


        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsbn()).isEqualTo("123");
        assertThat(saved.getAuthor()).isEqualTo("Autor Famoso");
        assertThat(saved.getTitle()).isEqualTo("Titulo Bacana");
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar livro com ISBN duplicado")
    public void saveBookWithDuplicatedIsbn(){

        Book book = createValidBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");

        verify(repository, never()).save(book);
    }

    private Book createValidBook() {
        return Book.builder()
                .isbn("123")
                .title("Titulo Bacana")
                .author("Autor Famoso")
                .build();
    }
}
