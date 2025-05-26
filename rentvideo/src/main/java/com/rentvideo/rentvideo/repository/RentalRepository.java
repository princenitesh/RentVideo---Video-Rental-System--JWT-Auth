package com.rentvideo.rentvideo.repository;
import com.rentvideo.rentvideo.model.Rental;
import com.rentvideo.rentvideo.model.User;
import com.rentvideo.rentvideo.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUser(User user);
    Optional<Rental> findByVideoAndUserAndReturnDateIsNull(Video video, User user);
    Boolean existsByVideoAndUserAndReturnDateIsNull(Video video, User user);
}