package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Course;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.UserCourse;
import com.mycompany.myapp.domain.dto.CourseDto;
import com.mycompany.myapp.domain.dto.CourseRegister;
import com.mycompany.myapp.domain.dto.CourseWithTNDto;
import com.mycompany.myapp.repository.CourseRepository;
import com.mycompany.myapp.repository.UserCourseRepository;
import com.mycompany.myapp.repository.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    List<CourseDto> courseDtos = new ArrayList<>();

    public List<CourseDto> findAllCourses() {

        //Cache
        if (courseDtos.isEmpty()) {
            List<Course> courses = courseRepository.findAll();

            for (Course c : courses) {
                courseDtos.add(new CourseDto(c.getCourseName(), c.getCourseLocation(), c.getCourseContent(), c.getTeacherId()));
            }

            return courseDtos;
        }

        return courseDtos;
    }

    public List<CourseDto> findAllCoursesDtoFromDB() {
        return courseRepository.findAllCoursesDto();
    }

    public List<CourseWithTNDto> findAllCoursesDtoWithTeacherNameFromDB() {
        return courseRepository.findAllCoursesDtoWithTeacherName();
    }

    public List<CourseDto> getAllCourseForUser(String userId) {

        Optional<User> user = userRepository.findById(Long.parseLong(userId));
        List<UserCourse> userCourse = userCourseRepository.findByUser(user.get());
        List<CourseDto> result = new ArrayList<>();

        Iterator<UserCourse> iterator = userCourse.iterator();

        while(iterator.hasNext()) {
            Course course = iterator.next().getCourse();
            CourseDto crDto = CourseDto.builder().courseName(course.getCourseName())
                .courseContent(course.getCourseContent())
                .courseLocation(course.getCourseLocation())
                .teacherId(course.getTeacherId())
                .build();
            result.add(crDto);
        }
        return result;
    }

    public void registerCourse(String courseName) throws Exception {
        Optional<User> curUser = userService.getUserWithAuthorities();
        Optional<Course> curCourse = courseRepository.findCourseByCourseName(courseName);

        if (curUser.isPresent() && curCourse.isPresent()) {
            userCourseRepository.save(UserCourse.builder()
                .user(curUser.get())
                .course(curCourse.get())
                .build());
        } else {
            throw new Exception("UnExpected Exception");
        }

        //TODO move AddCourseToStudent to here
    }

    public void addCourse(CourseDto course) throws Exception {
        Optional<Course> courseDto = courseRepository.findCourseByCourseName(course.getCourseName());

        if (courseDto.isPresent()) {
            throw new Exception("Course is existing.");
        }

        Course courseBeingSaved = Course.builder()
            .courseName(course.getCourseName())
            .courseContent(course.getCourseContent())
            .courseLocation(course.getCourseContent())
            .teacherId(course.getTeacherId())
            .build();

        try {
            courseRepository.saveAndFlush(courseBeingSaved);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public void deleteCourse(String courseName) throws Exception {
        Optional<Course> OptionalExistingCourse = courseRepository.findCourseByCourseName(courseName);

        if (!OptionalExistingCourse.isPresent()) {
            throw new Exception("Course is not exist.");
        }

        try {
            courseRepository.delete(OptionalExistingCourse.get());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }


    public void updateCourse(CourseDto course) throws Exception {
        Optional<Course> OptionalExistingCourse = courseRepository.findCourseByCourseName(course.getCourseName());

        if (!OptionalExistingCourse.isPresent()) {
            throw new Exception("Course is not exist.");
        }

        Course existingCourse = OptionalExistingCourse.get();
        existingCourse.setCourseContent(course.getCourseContent());
        existingCourse.setCourseLocation(course.getCourseLocation());
        existingCourse.setCourseName(course.getCourseName());
        existingCourse.setTeacherId(course.getTeacherId());

    }

    public void addCourseToStudent(CourseRegister register) throws Exception {

        String name = register.getCourseName();
        long id = register.getUserId();
        System.out.println(name + "=========================" + id);
        long courseId = courseRepository.findCourseIdByName(name);
        System.out.println("CourseId =========================" + courseId);

        Optional<User> curUser = userService.getUserWithAuthorities();
        Optional<Course> course = courseRepository.findCourseByCourseName(name);

        UserCourse registerCourse = UserCourse.builder().course(course.get()).user(curUser.get()).build();
        try {
            userCourseRepository.saveAndFlush(registerCourse);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
