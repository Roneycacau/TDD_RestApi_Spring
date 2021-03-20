package com.curso.tddrest.libraryapi.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotEmpty
    private String author;

    @NotEmpty
    private String title;

    @NotEmpty
    private String isbn;
}
