package ru.hogwarts.school.controller;


import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.service.AvatarService;

@RestController
@RequestMapping("/avatars")
public class AvatarController {

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam MultipartFile avatar, @RequestParam long studentId) {
        avatarService.upload(studentId, avatar);
    }

    @GetMapping("/download-from-db")
    public ResponseEntity<byte[]> downloadFromDb(@RequestParam long studentId) {
        Pair<byte[], String> result = avatarService.downloadFromDb(studentId);
        return prepareResponce(result);
    }

    @GetMapping("/download-from-fs")
    public ResponseEntity<byte[]> downloadFromFs(@RequestParam long studentId) {
        Pair<byte[], String> result = avatarService.downloadFromFs(studentId);
        return prepareResponce(result);
    }

    private ResponseEntity<byte[]> prepareResponce(Pair<byte[], String> result) {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(result.getSecond()))
                .contentLength(result.getFirst().length)
                .body(result.getFirst());
    }

}
