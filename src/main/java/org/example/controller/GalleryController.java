package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.requests.ChangeLikeStatusGalleryImageRequest;
import org.example.dto.requests.DeleteGalleryRequest;
import org.example.dto.requests.PostGalleryRequest;
import org.example.dto.responses.GetGalleryResponse;
import org.example.dto.responses.GetSingleImageDataResponse;
import org.example.services.JWTService;
import org.example.services.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final StorageService storageService;
    private final JWTService jwtService;

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/images")
    public ResponseEntity<GetGalleryResponse> getImagesMetaData(@RequestParam int pageNr, @RequestParam int pageSize, @RequestParam String search, @RequestParam String filterChoice, @RequestParam boolean orderValue) {
        return ResponseEntity.ok(storageService.getGalleryImagesMetadata(pageNr, pageSize, search, filterChoice, orderValue));
    }


    @GetMapping("/images/{filepath}")
    public ResponseEntity<Resource> getImage(@PathVariable String filepath, @RequestParam(name = "format") String format) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(storageService.getGalleryImage(filepath,format));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/image/{filepath}")
    public ResponseEntity<GetSingleImageDataResponse> getSingleImageData(@PathVariable String filepath, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUserName(token);
        return ResponseEntity.ok(storageService.getGalleryImageSingleData(filepath, username));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @PostMapping("/images")
    public ResponseEntity<String> uploadImages(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, PostGalleryRequest postGalleryRequest) throws IOException {
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUserName(token);
        storageService.uploadGalleryImages(postGalleryRequest,username);
        return ResponseEntity.ok("Pictures uploaded successfully");
    }

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/myImages")
    public ResponseEntity<GetGalleryResponse> getMyGalleryImages(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUserName(token);
        return ResponseEntity.ok(storageService.getMyGalleryImagesMetadata(username));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @DeleteMapping("/images")
    public ResponseEntity<String> deleteImage(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,@RequestBody DeleteGalleryRequest deleteGalleryRequest) {
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUserName(token);
        storageService.deleteGalleryImage(username, deleteGalleryRequest);
        return ResponseEntity.ok("The image/images was deleted successfully");
    }

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @PutMapping("/changeLikeStatusGalleyImage")
    public ResponseEntity<String> changeLikeStatusForGalleryImage(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,@RequestBody ChangeLikeStatusGalleryImageRequest changeLikeStatusGalleryImageRequest) {
        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUserName(token);
        storageService.changeLikeStatusForGalleryImage(changeLikeStatusGalleryImageRequest.getLikes(), username, changeLikeStatusGalleryImageRequest.getPath());
        return ResponseEntity.ok("Like status changed");
    }
}
