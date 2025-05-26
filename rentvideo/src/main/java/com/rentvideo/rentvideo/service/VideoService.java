package com.rentvideo.rentvideo.service;

import com.rentvideo.rentvideo.model.Video;
import com.rentvideo.rentvideo.repository.VideoRepository;
import com.rentvideo.rentvideo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
    }

    public List<Video> searchVideos(String query) {
        // Simple search that checks title, genre, and director
        return videoRepository.findByTitleContainingIgnoreCase(query); // Can extend with more complex queries
    }

    @Transactional
    public Video createVideo(Video video) {
        return videoRepository.save(video);
    }

    @Transactional
    public Video updateVideo(Long id, Video videoDetails) {
        Video video = getVideoById(id);
        video.setTitle(videoDetails.getTitle());
        video.setDirector(videoDetails.getDirector());
        video.setGenre(videoDetails.getGenre());
        video.setReleaseYear(videoDetails.getReleaseYear());
        video.setAvailableCopies(videoDetails.getAvailableCopies());
        video.setRentalPrice(videoDetails.getRentalPrice());
        return videoRepository.save(video);
    }

    @Transactional
    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
    }
}