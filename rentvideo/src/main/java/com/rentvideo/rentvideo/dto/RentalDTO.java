package com.rentvideo.rentvideo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalDTO {
    private Long id;
    private Long userId;
    private String username; // Include username for clarity
    private Long videoId;
    private String videoTitle; // Include video title for clarity
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private Double totalCost;
}