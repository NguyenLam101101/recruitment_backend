package com.example.recruitment2.Form;

import com.example.recruitment2.Entity.Address;
import com.example.recruitment2.Entity.Employer;
import com.example.recruitment2.Entity.Recruitment;
import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecruitmentForm {
    @NonNull
    private String name;
    @NonNull
    private String position;
    @NonNull
    private String workType;
    @NonNull
    private String province;
    private String detail;
    @NonNull
    private int experience;
    @NonNull
    private int employeeNumber;
    @NonNull
    private List<String> domains;
    private List<String> skills;
    @NonNull
    private LocalDate endDate;
    private String wage;
    @NonNull
    private String description;
    @NonNull
    private String requirement;
    @NonNull
    private String interest;

    public Recruitment parseToRecruitment(){
        Recruitment recruitment = new Recruitment();
        recruitment.setName(name);
        recruitment.setPosition(position);
        recruitment.setWorkType(workType);
        recruitment.setAddress(new Address());
        recruitment.getAddress().setProvince(province);
        recruitment.getAddress().setDetail(detail);
        recruitment.setExperience(experience);
        recruitment.setEmployeeNumber(employeeNumber);
        recruitment.setEndDate(endDate);
        recruitment.setWage(wage);
        recruitment.setDomains(domains);
        recruitment.setSkills(skills);
        recruitment.setDescription(description);
        recruitment.setRequirement(requirement);
        recruitment.setInterest(interest);
        return recruitment;
    }

    public Recruitment parseToRecruitment(Employer employer){
        Recruitment recruitment = parseToRecruitment();
        recruitment.setEmployer(employer);
        return recruitment;
    }

    public void mapToRecruitment(Recruitment recruitment){
        recruitment.setName(name);
        recruitment.setPosition(position);
        recruitment.setWorkType(workType);
        recruitment.setAddress(new Address());
        recruitment.getAddress().setProvince(province);
        recruitment.getAddress().setDetail(detail);
        recruitment.setExperience(experience);
        recruitment.setEmployeeNumber(employeeNumber);
        recruitment.setEndDate(endDate);
        recruitment.setWage(wage);
        recruitment.setDomains(domains);
        recruitment.setSkills(skills);
        recruitment.setDescription(description);
        recruitment.setRequirement(requirement);
        recruitment.setInterest(interest);
        recruitment.setEditedTime(LocalDateTime.now());
    }
}
