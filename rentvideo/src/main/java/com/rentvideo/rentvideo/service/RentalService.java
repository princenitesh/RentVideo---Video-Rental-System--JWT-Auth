package com.rentvideo.rentvideo.service;

import com.rentvideo.rentvideo.model.Rental;
import com.rentvideo.rentvideo.model.User;
import com.rentvideo.rentvideo.model.Video;
import com.rentvideo.rentvideo.repository.RentalRepository;
import com.rentvideo.rentvideo.repository.UserRepository;
import com.rentvideo.rentvideo.repository.VideoRepository;
import com.rentvideo.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.rentvideo.exception.VideoRentalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public RentalService(RentalRepository rentalRepository, UserRepository userRepository, VideoRepository videoRepository) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + id));
    }

    public List<Rental> getRentalsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return rentalRepository.findByUser(user);
    }

    @Transactional
    public Rental rentVideo(Long userId, Long videoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));

        if (video.getAvailableCopies() <= 0) {
            throw new VideoRentalException("No available copies for video: " + video.getTitle());
        }

        if (rentalRepository.existsByVideoAndUserAndReturnDateIsNull(video, user)) {
            throw new VideoRentalException("User already has an unreturned copy of this video: " + video.getTitle());
        }

        video.setAvailableCopies(video.getAvailableCopies() - 1);
        videoRepository.save(video); // Update video availability

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setVideo(video);
        rental.setRentalDate(LocalDate.now());

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental returnVideo(Long rentalId) {
        Rental rental = getRentalById(rentalId);

        if (rental.getReturnDate() != null) {
            throw new VideoRentalException("Video already returned for rental ID: " + rentalId);
        }

        rental.setReturnDate(LocalDate.now());

        // Calculate total cost (simple calculation: days rented * rental price)
        long daysRented = java.time.temporal.ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        if (daysRented == 0) daysRented = 1; // Minimum 1 day rental
        rental.setTotalCost(daysRented * rental.getVideo().getRentalPrice());

        Video video = rental.getVideo();
        video.setAvailableCopies(video.getAvailableCopies() + 1);
        videoRepository.save(video); // Update video availability

        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental returnVideoByUserIdAndVideoId(Long userId, Long videoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));

        Rental rental = rentalRepository.findByVideoAndUserAndReturnDateIsNull(video, user)
                .orElseThrow(() -> new ResourceNotFoundException("No active rental found for user " + user.getUsername() + " and video " + video.getTitle()));

        if (rental.getReturnDate() != null) {
            throw new VideoRentalException("Video already returned for user " + user.getUsername() + " and video " + video.getTitle());
        }

        rental.setReturnDate(LocalDate.now());

        // Calculate total cost (simple calculation: days rented * rental price)
        long daysRented = java.time.temporal.ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        if (daysRented == 0) daysRented = 1; // Minimum 1 day rental
        rental.setTotalCost(daysRented * rental.getVideo().getRentalPrice());

        video.setAvailableCopies(video.getAvailableCopies() + 1);
        videoRepository.save(video); // Update video availability

        return rentalRepository.save(rental);
    }
}