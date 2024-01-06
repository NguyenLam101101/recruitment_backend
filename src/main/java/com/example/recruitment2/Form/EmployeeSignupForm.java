package com.example.recruitment2.Form;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
public class EmployeeSignupForm {
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private int gender;
    @NonNull
    private String yearOfBirth;
    @NonNull
    private String email;
    @NonNull
    private String password;
}
