package com.example.recruitment2.Form;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class RecruitmentSearchForm{
    @NonNull
    List<String> provinces;
    @NonNull
    List<String> domains;
    @NonNull
    List<String> skills;
    @NonNull
    int experience;
}
