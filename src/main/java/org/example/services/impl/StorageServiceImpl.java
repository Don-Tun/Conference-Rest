package org.example.services.impl;



import lombok.RequiredArgsConstructor;
import org.example.dto.requests.GetGalleryRequest;
import org.example.dto.requests.PostGalleryRequest;
import org.example.dto.responses.GetGalleryResponse;
import org.example.entities.GalleryImage;
import org.example.entities.User;
import org.example.repository.GalleryStorageRepository;
import org.example.repository.UserRepository;
import org.example.services.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final UserRepository userRepository;
    private final GalleryStorageRepository galleryStorageRepository;
    @Value("${file.upload-dir}")
    private String storageDir;

    public void storeProfileImage(MultipartFile file, String username) throws IOException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + username));
        String objectName = "profilePicture/" + user.getId() + ".jpeg";

        user.setAvatarPath(objectName);
        userRepository.save(user);

        Path storageDirectory = Paths.get(storageDir + "/profilePictures");

        // Ensure the storage directory exists
        if (!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
        }

        Path destinationFilePath = storageDirectory.resolve(user.getId() + ".jpg");
        Path destinationFilePathWebP = storageDirectory.resolve(user.getId() + ".webp");

        // Copy the file to the destination, replacing it if it already exists
        try {
            file.transferTo(destinationFilePath);
            byte[] webPImageData = convertToWebP(destinationFilePath.toFile());
            File webPFile = destinationFilePathWebP.toFile();
            try (FileOutputStream fos = new FileOutputStream(webPFile)) {
                fos.write(webPImageData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Resource getProfileImage(String username) throws IOException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + username));

        Path path = Paths.get(storageDir + "/profilePictures", user.getId() + ".jpg");

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found " + user.getId() + ".jpg");
        }

        return resource;
    }

    public GetGalleryResponse getGalleryImagesMetadata(GetGalleryRequest getGalleryRequest) throws MalformedURLException {
        try{
            Pageable pageable = PageRequest.of(getGalleryRequest.getPageNr(), getGalleryRequest.getPageSize(), Sort.by("uploadTime").descending());

            // Fetch the page from the repository
            Page<GalleryImage> page = galleryStorageRepository.findAll(pageable);

            List<String> imgPaths = page.getContent().stream().map(GalleryImage::getPath).toList();

            GetGalleryResponse getGalleryResponse = new GetGalleryResponse();
            getGalleryResponse.setImagePaths(imgPaths);

            return getGalleryResponse;
        } catch (Exception e) {
            System.out.println("Failed to fetch images metadata" + e.getMessage());
            return null;
        }
    }

    public Resource getGalleryImage(String filepath, String format) {
        try {
            Path basePath = Paths.get(storageDir, "Gallery");
            Path fileWebP = basePath.resolve(filepath + "." + format).normalize();

            Resource resourceWebP = new UrlResource(fileWebP.toUri());
            if (resourceWebP.exists() && resourceWebP.isReadable()) {
                return resourceWebP;
            } else {
                Path fileJpg = basePath.resolve(filepath + ".jpeg").normalize();
                Resource resourceJpg = new UrlResource(fileJpg.toUri());
                if (resourceJpg.exists() && resourceJpg.isReadable()) {
                    return resourceJpg;
                } else {
                    return null;
                }
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
        public void uploadGalleryImages(PostGalleryRequest postGalleryRequest, String username) throws IOException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + username));

        Path storageDirectory = Paths.get(storageDir,"Gallery");
        Files.createDirectories(storageDirectory);

        LocalDateTime localDateTime = LocalDateTime.now();
        String time = localDateTime.toString().replace(":", "").replace(".","");

        GalleryImage galleryImage = new GalleryImage();
        galleryImage.setOwner(user);
        galleryImage.setUploadTime(localDateTime);
        galleryImage.setPath(user.getId() + "-" + time);
        galleryStorageRepository.save(galleryImage);


        Path destinationPathJPG = storageDirectory.resolve( user.getId() + "-" + time + ".jpg");
        Path destinationPathWebP = storageDirectory.resolve(user.getId() + "-" + time + ".webp");

        try {
            postGalleryRequest.getFile().transferTo(destinationPathJPG.toFile());
            byte[] webPImageData = convertToWebP(destinationPathJPG.toFile());
            File webPFile = destinationPathWebP.toFile();
            try (FileOutputStream fos = new FileOutputStream(webPFile)) {
                fos.write(webPImageData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteGalleryImage(String username, Integer imageId) {

    }

    public byte[] convertToWebP(File inputImage) throws IOException {
        // Read the input image
        BufferedImage image = ImageIO.read(inputImage);

        // Create a byte array output stream to capture the WebP output
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "webp", baos);
            // Return the byte array
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to WebP", e);
        }
    }
}
