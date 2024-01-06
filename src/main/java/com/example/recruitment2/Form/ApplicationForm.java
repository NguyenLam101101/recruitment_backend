package com.example.recruitment2.Form;

import com.example.recruitment2.Entity.Application;
import com.example.recruitment2.Entity.Recruitment;
import com.example.recruitment2.Repository.RecruitmentRepository;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplicationForm {
    private String recruitmentId;
    private String head;
    private String body;
    private String cvId;
    private List<MultipartFile> attachedFiles = new ArrayList<>();
}
