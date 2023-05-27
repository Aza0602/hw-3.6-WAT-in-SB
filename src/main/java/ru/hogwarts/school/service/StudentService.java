package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.entity.Faculty;
import ru.hogwarts.school.entity.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public Student add(Student student) {
        student.setId(null);
        student.setFaculty(
                Optional.ofNullable(student.getFaculty())
                        .filter(f -> f.getId() != null)
                        .flatMap(f -> facultyRepository.findById(f.getId()))
                        .orElse(null)
        );
        return studentRepository.save(student);
    }

    public Optional<Student> update(Long id, Student newStudent) {
        return studentRepository.findById(id)
                .map(oldStudent -> {
                    oldStudent.setName(newStudent.getName());
                    oldStudent.setAge(newStudent.getAge());
                    oldStudent.setFaculty(
                            Optional.ofNullable(newStudent.getFaculty())
                                    .filter(f -> f.getId() != null)
                                    .flatMap(f -> facultyRepository.findById(f.getId()))
                                    .orElse(null)
                    );
                    return studentRepository.save(oldStudent);
                });
    }

    public Optional<Student> getById(Long id) {
        return studentRepository.findById(id);
    }

    public Collection<Student> getAll() {
        return Collections.unmodifiableCollection(studentRepository.findAll());
    }

    public Optional<Student> deleteById(Long id) {
        return studentRepository.findById(id)
                .map(student -> {
                    studentRepository.deleteById(id);
                    return student;
                });
    }

    public Collection<Student> getAllByAge(int age) {
        return studentRepository.findAllByAge(age);
    }

    public Collection<Student> getAllByAgeBetween(int minAge, int maxAge) {
        return studentRepository.findAllByAgeBetween(minAge, maxAge);
    }

    public Optional<Faculty> getFacultyByStudentId(Long id) {
        return studentRepository.findById(id)
                .map(Student::getFaculty);
    }
}
