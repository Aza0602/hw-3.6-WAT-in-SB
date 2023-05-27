package ru.hogwarts.school;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.hogwarts.school.entity.Faculty;
import ru.hogwarts.school.entity.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private final Faker faker = new Faker();

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }


    @Test
    public void createTest() {
        addStudent(generateStudent(addFaculty(generateFaculty())));
    }

    private Faculty addFaculty(Faculty faculty) {
        ResponseEntity<Faculty> facultyRecordResponseEntity = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/faculty",
                faculty,
                Faculty.class
        );
        assertThat(facultyRecordResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(facultyRecordResponseEntity.getBody()).isNotNull();
        assertThat(facultyRecordResponseEntity.getBody()).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(faculty);
        assertThat(facultyRecordResponseEntity.getBody().getId()).isNotNull();

        return facultyRecordResponseEntity.getBody();
    }

    private Student addStudent(Student student) {
        ResponseEntity<Student> studentRecordResponseEntity = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/student",
                student,
                Student.class
        );
        assertThat(studentRecordResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(studentRecordResponseEntity.getBody()).isNotNull();
        assertThat(studentRecordResponseEntity.getBody()).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(student);
        assertThat(studentRecordResponseEntity.getBody().getId()).isNotNull();

        return studentRecordResponseEntity.getBody();
    }

    @Test
    public void putTest() {
        Faculty faculty1 = addFaculty(generateFaculty());
        Faculty faculty2 = addFaculty(generateFaculty());
        Student student = addStudent(generateStudent(faculty1));

        ResponseEntity<Student> getForEntityResponse = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/student/" + student.getId(),
                Student.class
        );
        assertThat(getForEntityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getForEntityResponse.getBody()).isNotNull();
        assertThat(getForEntityResponse.getBody()).usingRecursiveComparison().isEqualTo(student);
        assertThat(getForEntityResponse.getBody().getFaculty()).usingRecursiveComparison().isEqualTo(faculty1);

        student.setFaculty(faculty2);

        ResponseEntity<Student> recordResponseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/student/" + student.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(student),
                Student.class
        );
        assertThat(getForEntityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(recordResponseEntity.getBody()).isNotNull();
        assertThat(recordResponseEntity.getBody()).usingRecursiveComparison().isEqualTo(student);
        assertThat(recordResponseEntity.getBody().getFaculty()).usingRecursiveComparison().isEqualTo(faculty2);
    }

    @Test
    public void findByAgeBetweenTest() {
        List<Faculty> faculties = Stream.generate(this::generateFaculty)
                .limit(5)
                .map(this::addFaculty)
                .collect(Collectors.toList());
        List<Student> students = Stream.generate(
                        () -> generateStudent(faculties.get(faker.random().nextInt(faculties.size()))))
                .limit(50)
                .map(this::addStudent)
                .collect(Collectors.toList());

        int minAge = 14;
        int maxAge = 17;

        List<Student> expectedStudents = students.stream()
                .filter(
                        studentRecord -> studentRecord.getAge() >= minAge && studentRecord.getAge() <= maxAge
                )
                .collect(Collectors.toList());

        ResponseEntity<List<Student>> getForEntityResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/student?min={minAge}&max={maxAge}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Student>>() {
                },
                minAge,
                maxAge
        );
        assertThat(getForEntityResponse.getStatusCode()).isEqualTo((HttpStatus.OK));
        assertThat(getForEntityResponse.getBody())
                .hasSize(expectedStudents.size())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedStudents);
    }

    private Student generateStudent(Faculty faculty) {
        Student student = new Student();
        student.setName(faker.harryPotter().character());
        student.setAge(faker.random().nextInt(11, 18));
        if (faculty != null) {
            student.setFaculty(faculty);
        }
        return student;
    }

    private Faculty generateFaculty() {
        Faculty faculty = new Faculty();
        faculty.setName(faker.harryPotter().house());
        faculty.setColor(faker.color().name());
        return faculty;
    }

}
