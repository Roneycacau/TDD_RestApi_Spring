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
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        Book book = bookRequest();

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

        Book book = bookRequest();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");

        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Deve obter livro por ID")
    public void getByIdTest(){
        Long id = 1L;
        Book book = getBook(id);

        when(repository.findById(id)).thenReturn(Optional.of(book));
        Optional<Book> bookFound = service.getById(id);

        assertThat(bookFound.isPresent()).isTrue();
        assertThat(bookFound.get()).isEqualTo(book);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar livro por ID inexistente")
    public void getByIdNotFoundTest(){
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());
        Optional<Book> bookFound = service.getById(id);

        assertThat(bookFound.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        Long id = 1L;
        Book book = getBook(id);

        assertDoesNotThrow(() -> service.delete(book));

        verify(repository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve apresentar erro ao tentar deletar livro inválido")
    public void deleteBookInvalidTest(){
        Book book = new Book();

        assertThrows(IllegalArgumentException.class, () -> service.delete(book));
        assertThat(assertThrows(IllegalArgumentException.class, () -> service.delete(book)).getMessage()).isEqualTo("Id do livro não pode ser nulo");

        verify(repository, never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar dados de um livro")
    public void updateBookTest(){
        Long id = 1L;
        Book toBeUpdated = Book.builder().id(id).build();
        Book bookUpdated= getBook(id);
        when(repository.save(toBeUpdated)).thenReturn(bookUpdated);

        Book book = service.update(toBeUpdated);

        assertThat(book.getId()).isEqualTo(bookUpdated.getId());
        assertThat(book.getIsbn()).isEqualTo(bookUpdated.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(bookUpdated.getAuthor());
        assertThat(book.getTitle()).isEqualTo(bookUpdated.getTitle());
    }

    @Test
    @DisplayName("Deve apresentar erro ao tentar deletar livro inválido")
    public void updateBookInvalidTest(){
        Book book = new Book();

        assertThrows(IllegalArgumentException.class, () -> service.update(book));
        assertThat(assertThrows(IllegalArgumentException.class, () -> service.update(book)).getMessage()).isEqualTo("Id do livro não pode ser nulo");
        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar um livro pelas propriedades")
    public void bookFilterTest(){
        Book book = getBook(1L);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> books = Arrays.asList(book);
        Page<Book> page = new PageImpl<>(books, pageRequest, 1);
        when(repository.findAll(any(Example.class), any(PageRequest.class))).thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(books);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    private Book getBook(Long id) {
        return Book.builder()
                .id(id)
                .isbn("123")
                .title("Titulo Bacana")
                .author("Autor Famoso")
                .build();
    }

    private Book bookRequest() {
        return Book.builder()
                .isbn("123")
                .title("Titulo Bacana")
                .author("Autor Famoso")
                .build();
    }
}
