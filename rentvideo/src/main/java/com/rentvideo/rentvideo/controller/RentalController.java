package com.rentvideo.rentvideo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // Ensure this is imported
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentvideo.rentvideo.dto.RentalDTO;
import com.rentvideo.rentvideo.exception.VideoRentalException;
import com.rentvideo.rentvideo.model.Rental;
import com.rentvideo.rentvideo.service.RentalService;
import com.rentvideo.rentvideo.service.UserService;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService; // This remains UserService

    public RentalController(RentalService rentalService, UserService userService) {
        this.rentalService = rentalService;
        this.userService = userService;
    }

    // Admin only - view all rentals
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalDTO>> getAllRentals() {
        List<Rental> rentals = rentalService.getAllRentals();
        List<RentalDTO> rentalDTOS = rentals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rentalDTOS);
    }

    // Admin or self - view rentals for a specific user
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#userId).username == authentication.principal.username")
    public ResponseEntity<List<RentalDTO>> getRentalsByUser(@PathVariable Long userId) {
        List<Rental> rentals = rentalService.getRentalsByUser(userId);
        List<RentalDTO> rentalDTOS = rentals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rentalDTOS);
    }

    // Accessible by specific user - rent a video
    @PostMapping("/rent/{videoId}")
    @PreAuthorize("hasRole('USER')") // Only regular users can rent
    public ResponseEntity<?> rentVideo(@PathVariable Long videoId, Authentication authentication) {
        try {
            // Get the current authenticated user's ID using the new method in UserService
            Long currentUserId = userService.getUserByUsername(authentication.getName()).getId(); // THIS LINE IS FIXED
            Rental newRental = rentalService.rentVideo(currentUserId, videoId);
            return new ResponseEntity<>(convertToDto(newRental), HttpStatus.CREATED);
        } catch (VideoRentalException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Accessible by specific user - return a video by rental ID
    @PutMapping("/return/{rentalId}")
    @PreAuthorize("hasRole('USER') and @rentalService.getRentalById(#rentalId).user.username == authentication.principal.username")
    public ResponseEntity<?> returnVideo(@PathVariable Long rentalId) {
        try {
            Rental returnedRental = rentalService.returnVideo(rentalId);
            return ResponseEntity.ok(convertToDto(returnedRental));
        } catch (VideoRentalException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Accessible by specific user - return a video by user ID and video ID
    @PutMapping("/return/user/{userId}/video/{videoId}")
    @PreAuthorize("hasRole('USER') and @userService.getUserById(#userId).username == authentication.principal.username")
    public ResponseEntity<?> returnVideoByUserIdAndVideoId(
            @PathVariable Long userId,
            @PathVariable Long videoId) {
        try {
            Rental returnedRental = rentalService.returnVideoByUserIdAndVideoId(userId, videoId);
            return ResponseEntity.ok(convertToDto(returnedRental));
        } catch (VideoRentalException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    private RentalDTO convertToDto(Rental rental) {
        RentalDTO rentalDTO = new RentalDTO();
        rentalDTO.setId(rental.getId());
        rentalDTO.setUserId(rental.getUser().getId());
        rentalDTO.setUsername(rental.getUser().getUsername());
        rentalDTO.setVideoId(rental.getVideo().getId());
        rentalDTO.setVideoTitle(rental.getVideo().getTitle());
        rentalDTO.setRentalDate(rental.getRentalDate());
        rentalDTO.setReturnDate(rental.getReturnDate());
        rentalDTO.setTotalCost(rental.getTotalCost());
        return rentalDTO;
    }
}