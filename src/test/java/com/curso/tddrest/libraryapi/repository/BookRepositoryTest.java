package com.curso.tddrest.libraryapi.repository;

import com.curso.tddrest.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest     {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir o livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        String isbn = "123";

        Book book = Book.builder()
                .isbn(isbn)
                .title("Titulo Bacana")
                .author("Autor Famoso")
                .build();

        entityManager.persist(book);

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir o livro na base com o isbn informado")
    public void returnTrueWhenIsbnDoesntExists(){
        String isbn = "123";

        Book book = Book.builder()
                .isbn(isbn)
                .title("Titulo Bacana")
                .author("Autor Famoso")
                .build();

        boolean exists = repository.existsByIsbn(isbn);

        assertThat(exists).isFalse();
    }


//    private Book createValidBook() {
//        return Book.builder()
//                .isbn(isbn)
//                .title("Titulo Bacana")
//                .author("Autor Famoso")
//                .build();
//    }
}
