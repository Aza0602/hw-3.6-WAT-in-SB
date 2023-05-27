package ru.hogwarts.school.controller;

import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.entity.Faculty;
import ru.hogwarts.school.entity.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/{id}")
    public Optional<Faculty> getById(@PathVariable Long id) {
        return facultyService.getById(id);
    }

    @GetMapping(params = "color")
    public Collection<Faculty> getAll(@RequestParam(value = "color", required = false) String color) {
        return Optional.ofNullable(color)
                .map(facultyService::getAllByColor)
                .orElseGet(facultyService::getAll);
    }

    @PostMapping
    public Faculty add(@RequestBody Faculty faculty) {
        return facultyService.add(faculty);
    }

    @PutMapping("/{id}")
    public Optional<Faculty> update(@PathVariable Long id, @RequestBody Faculty faculty) {
        return facultyService.update(id, faculty);
    }

    @DeleteMapping("/{id}")
    public Optional<Faculty> deleteById(@PathVariable Long id) {
        return facultyService.deleteById(id);
    }

    @GetMapping(params =  "nameOrColor")
    public Collection<Faculty> getAllByNameOrColor(@RequestParam("nameOrColor") String nameOrColor) {
        return facultyService.getAllByNameOrColor(nameOrColor);
    }

    @GetMapping("/{id}/students")
    public List<Student> getStudentsByFacultyId(@PathVariable Long id) {
        return facultyService.getStudentsByFacultyId(id);
    }

}
