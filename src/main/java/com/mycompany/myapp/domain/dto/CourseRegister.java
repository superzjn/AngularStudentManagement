package com.mycompany.myapp.domain.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseRegister {

    private String courseName;

    private long userId;
}
