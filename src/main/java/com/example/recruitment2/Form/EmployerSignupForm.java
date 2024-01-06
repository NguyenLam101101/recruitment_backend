package com.example.recruitment2.Form;

import lombok.Data;
import lombok.NonNull;

@Data
public class EmployerSignupForm {
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String areaCode;
    @NonNull
    private String phoneNumber;
    @NonNull
    private String email;
    @NonNull
    private String password;
}
