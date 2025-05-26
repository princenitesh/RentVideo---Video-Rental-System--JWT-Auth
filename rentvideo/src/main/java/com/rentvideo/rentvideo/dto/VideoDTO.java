package com.rentvideo.rentvideo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Director is required")
    private String director;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotNull(message = "Release year is required")
    @Min(value = 1888, message = "Release year must be after 1888 (first film)")
    private Integer releaseYear;

    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    @NotNull(message = "Rental price is required")
    @Min(value = 0, message = "Rental price cannot be negative")
    private Double rentalPrice;
}