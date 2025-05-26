package com.rentvideo.rentvideo.controller;

import com.rentvideo.rentvideo.dto.VideoDTO;
import com.rentvideo.rentvideo.model.Video;
import com.rentvideo.rentvideo.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // Accessible by all authenticated users
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VideoDTO>> getAllVideos() {
        List<Video> videos = videoService.getAllVideos();
        List<VideoDTO> videoDTOS = videos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(videoDTOS);
    }

    // Accessible by all authenticated users
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoDTO> getVideoById(@PathVariable Long id) {
        Video video = videoService.getVideoById(id);
        return ResponseEntity.ok(convertToDto(video));
    }

    // Accessible by all authenticated users
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VideoDTO>> searchVideos(@RequestParam String query) {
        List<Video> videos = videoService.searchVideos(query);
        List<VideoDTO> videoDTOS = videos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(videoDTOS);
    }

    // Admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoDTO> createVideo(@Valid @RequestBody VideoDTO videoDTO) {
        Video video = convertToEntity(videoDTO);
        Video createdVideo = videoService.createVideo(video);
        return new ResponseEntity<>(convertToDto(createdVideo), HttpStatus.CREATED);
    }

    // Admin only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoDTO> updateVideo(@PathVariable Long id, @Valid @RequestBody VideoDTO videoDTO) {
        Video videoDetails = convertToEntity(videoDTO);
        Video updatedVideo = videoService.updateVideo(id, videoDetails);
        return ResponseEntity.ok(convertToDto(updatedVideo));
    }

    // Admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    private VideoDTO convertToDto(Video video) {
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(video.getId());
        videoDTO.setTitle(video.getTitle());
        videoDTO.setDirector(video.getDirector());
        videoDTO.setGenre(video.getGenre());
        videoDTO.setReleaseYear(video.getReleaseYear());
        videoDTO.setAvailableCopies(video.getAvailableCopies());
        videoDTO.setRentalPrice(video.getRentalPrice());
        return videoDTO;
    }

    private Video convertToEntity(VideoDTO videoDTO) {
        Video video = new Video();
        video.setId(videoDTO.getId()); // ID can be null for new creations
        video.setTitle(videoDTO.getTitle());
        video.setDirector(videoDTO.getDirector());
        video.setGenre(videoDTO.getGenre());
        video.setReleaseYear(videoDTO.getReleaseYear());
        video.setAvailableCopies(videoDTO.getAvailableCopies());
        video.setRentalPrice(videoDTO.getRentalPrice());
        return video;
    }
}