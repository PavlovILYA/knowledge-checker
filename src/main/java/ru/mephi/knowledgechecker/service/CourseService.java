package ru.mephi.knowledgechecker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.mephi.knowledgechecker.model.course.Course;
import ru.mephi.knowledgechecker.repository.CourseRepository;

import static ru.mephi.knowledgechecker.common.Constants.PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public Course save(Course course) {
        course = courseRepository.save(course);
        log.info("Saved course: {}", course);
        return course;
    }

    public Page<Course> getCoursesByCreatorId(Long creatorId, int from) {
        return courseRepository.getCreatedCourses(creatorId,
                PageRequest.of(from, PAGE_SIZE, Sort.by("title")));
    }

    public Page<Course> getCoursesByCreatorId(Long creatorId) {
        return getCoursesByCreatorId(creatorId, 0);
    }
}
