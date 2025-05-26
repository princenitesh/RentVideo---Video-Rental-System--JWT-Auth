package com.rentvideo.rentvideo.repository;

import com.rentvideo.rentvideo.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByTitleContainingIgnoreCase(String title);
    List<Video> findByGenreContainingIgnoreCase(String genre);
    List<Video> findByDirectorContainingIgnoreCase(String director);
    List<Video> findByReleaseYear(Integer releaseYear);
}