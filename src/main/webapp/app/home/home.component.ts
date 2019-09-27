import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { LoginModalService, Principal, Account } from 'app/core';
import { CourseService } from 'app/shared/service/CourseService';
import { CourseDto } from 'app/shared/model/course-dto.model';
import { CourseWithTNDto } from 'app/shared/model/courseWithTN-dto.model';
import { Course } from 'app/shared/model/course.model';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: ['home.css']
})
export class HomeComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;
    classeNameNeedToReg: string;
    courses: CourseDto[] = [];
    coursesWithTN: CourseWithTNDto[] = [];
    userCourses: CourseDto[] = [];

    constructor(
        private principal: Principal,
        private loginModalService: LoginModalService,
        private eventManager: JhiEventManager,
        private courseService: CourseService
    ) {}

    ngOnInit() {
        this.principal.identity().then(account => {
            this.account = account;
        });
        this.registerAuthenticationSuccess();
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', message => {
            this.principal.identity().then(account => {
                this.account = account;
            });
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    getAllCourses() {
        this.courseService.getCourseInfo().subscribe(curDto => {
            if (!curDto) {
                this.courses = [];
            } else {
                this.courses = curDto;
            }
        });
    }

    getAllCoursesWithTN() {
        this.courseService.getCourseInfoWithTN().subscribe(curDto => {
            if (!curDto) {
                this.coursesWithTN = [];
            } else {
                this.coursesWithTN = curDto;
            }
        });
    }

    getAllCoursesOfUser(userId: String) {
        this.courseService.getCourseForUser(userId).subscribe(curDto => {
            if (!curDto) {
                this.userCourses = [];
            } else {
                this.userCourses = curDto;
            }
        });
    }

    deleteCourse(courseName: String) {
        this.courseService.deleteCourse(courseName).subscribe(response => {
            // TODO delete fail when "2019-09-27 11:35:47.751 ERROR 70227 --- [ XNIO-2 task-22] o.h.engine.jdbc.spi.SqlExceptionHelper
            // : Cannot delete or update a parent row: a foreign key constraint fails (`jiuzhangquanzhanke`.`user_course`, CONSTRAINT `FK_course_id_course_id`
            // FOREIGN KEY (`course_id`) REFERENCES `course` (`id`))
            this.getAllCourses();
        });
    }

    addCourseToStudent(courseName: String, userId: String) {
        this.courseService.addCourseToStudent(courseName, userId).subscribe(response => {
            this.getAllCoursesOfUser(userId);
            //     TODO successful & failure message
        });
    }

    createCourse(courseName: string, courseLocation: string, courseContent: string, courseTeacher: number) {
        this.courseService.createCourse(new Course(courseName, courseLocation, courseContent, courseTeacher)).subscribe(response => {
            this.getAllCourses();
        });
    }

    clearAllCourses() {
        this.courses = [];
    }

    clearAllRegisteredCourses() {
        this.userCourses = [];
    }
}
