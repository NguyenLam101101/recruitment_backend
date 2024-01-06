package com.example.recruitment2.Form;

import com.example.recruitment2.Entity.Address;
import com.example.recruitment2.Entity.Cv;
import com.example.recruitment2.Entity.UserFile;
import com.example.recruitment2.Service.AppService;
import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Data
public class CvForm {
    private AppService appService = new AppService();
    @NonNull
    private MultipartFile file;
    private List<String> provinces;
    private List<String> domains;
    private List<String> skills;
    private int experience;
    private String wage;
    @NonNull
    private String privacy;

    public Cv parseToCv() throws IOException{
        Cv cv = new Cv();
        //create file to save cv
        String cvPath=appService.saveFile(file, appService.getUploadCvDir());
        UserFile cvFile = new UserFile();
        cvFile.setOriginalName(file.getOriginalFilename());
        cvFile.setSource(cvPath);
        cv.setFile(cvFile);
        cv.setPrivacy(privacy);
        cv.setExperience(experience);
        cv.setWage(wage);
        cv.setDomains(domains);
        cv.setSkills(skills);
        cv.setProvinces(provinces);
        return cv;
    }

    public void mapToCV(Cv cv) throws Exception{
        //create file to save cv
        String cvPath = appService.saveFile(file, appService.getUploadCvDir());
        UserFile cvFile = new UserFile();
        cvFile.setOriginalName(file.getOriginalFilename());
        cvFile.setSource(cvPath);
        cv.setFile(cvFile);
        cv.setPrivacy(privacy);
        cv.setExperience(experience);
        cv.setWage(wage);
        cv.setDomains(domains);
        cv.setSkills(skills);
        cv.setProvinces(provinces);
    }
}
