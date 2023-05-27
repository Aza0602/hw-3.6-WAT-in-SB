package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.entity.Avatar;
import ru.hogwarts.school.entity.Student;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.exception.AvatarProcessingException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;
    private final String avatarDir;

    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository,
                         @Value("${application.avatar.store}") String avatarDir) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
        this.avatarDir = avatarDir;
    }

    public void upload(long studentId, MultipartFile multipartFile) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(StudentNotFoundException::new);
            String fileName = String.format("%d,%s",
                    student.getId(),
                    StringUtils.getFilenameExtension(multipartFile.getOriginalFilename()
                    ));
            byte[] data = multipartFile.getBytes();
            Path path = Paths.get(avatarDir, fileName);
            Files.write(path, data);

            Avatar avatar = new Avatar();
            avatar.setData(data);
            avatar.setFilePath(path.toString());
            avatar.setFileSize(data.length);
            avatar.setStudent(student);
            avatar.setMediaType(multipartFile.getContentType());
            avatarRepository.save(avatar);

        } catch (IOException e) {
            throw new AvatarProcessingException();
        }
    }

    public Pair<byte[], String> downloadFromDb(long studentId) {
        Avatar avatar = avatarRepository.findByStudent_Id(studentId).orElseThrow(
                AvatarNotFoundException::new
        );
        return Pair.of(avatar.getData(), avatar.getMediaType());
    }

    public Pair<byte[], String> downloadFromFs(long studentId) {
        try {
            Avatar avatar = avatarRepository.findByStudent_Id(studentId).orElseThrow(
                    AvatarNotFoundException::new
            );
            Path path = Paths.get(avatar.getFilePath());
            return Pair.of(Files.readAllBytes(path), avatar.getMediaType());
        } catch (IOException e) {
            throw new AvatarProcessingException();
        }
    }
}
