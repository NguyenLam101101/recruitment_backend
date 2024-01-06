package com.example.recruitment2.Service;

import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.CvSearchForm;
import com.example.recruitment2.Form.RecruitmentSearchForm;
import com.example.recruitment2.Repository.DomainRepository;
import com.example.recruitment2.Repository.ProvinceRepository;
import com.example.recruitment2.Repository.RecruitmentRepository;
import com.example.recruitment2.Repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class AppService {
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    public String getUploadCvDir(){
        return "C:\\Users\\nguye\\Documents\\Recuitment_folder\\cv";
    }
    public String getImageDir(){
        return "C:\\Users\\nguye\\Documents\\Recuitment_folder\\image";
    }
    public String getAttachmentDir(){
        return "C:\\Users\\nguye\\Documents\\Recuitment_folder\\attachment";
    }
    @Autowired
    private ProvinceRepository provinceRepository;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private SkillRepository skillRepository;

    public String saveFile(MultipartFile multipartFile, String dir) throws IOException {
        String path = dir + "/" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + "_" + multipartFile.getOriginalFilename();
        File file = new File(path);
        FileCopyUtils.copy(multipartFile.getBytes(), file);
        return path;
    }

    public byte[] pathToByteArray(String path) throws URISyntaxException, IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return bytes;
    }

    public String pathToBase64(String path) throws IOException {
        File file = new File(path);
        String type = "";
        if(path.toLowerCase().endsWith("png")){
            type = "image/png";
        }
        if(path.toLowerCase().endsWith("jpeg")){
            type = "image/jpeg";
        }
        if(path.toLowerCase().endsWith("jfif")){
            type = "image/jpeg";
        }
        if(path.toLowerCase().endsWith("webp")){
            type = "image/webp";
        }
        if(path.toLowerCase().endsWith("pdf")){
            type = "application/pdf";
        }
        if(path.toLowerCase().endsWith("docx")){
            type =  "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if(path.toLowerCase().endsWith("doc")){
            type = "application/msword";
        }
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String base64File = Base64.getEncoder().encodeToString(bytes);
        return "data:"+type+";base64,"+base64File;
    }

    public List<Integer> createRecruitmentSearchVector(RecruitmentSearchForm form) {
        List<Integer> vector = new ArrayList<>();
        List<Province> provinces = provinceRepository.findAll();
        List<Domain> domains = domainRepository.findAll();
        List<Skill> skills = skillRepository.findAll();

        // kinh nghiệm
        vector.add(form.getExperience());

        // province
        for (Province province : provinces) {
            if (form.getProvinces().contains(province.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // domains
        for (Domain domain : domains) {
            if (form.getDomains().contains(domain.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // skills
        for (Skill skill : skills) {
            if (form.getSkills().contains(skill.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }
        return vector;
    }

    public List<Integer> createCvSearchVector(CvSearchForm form) {
        List<Integer> vector = new ArrayList<>();
        List<Province> provinces = provinceRepository.findAll();
        List<Domain> domains = domainRepository.findAll();
        List<Skill> skills = skillRepository.findAll();

        // kinh nghiệm
        vector.add(form.getExperience());

        // province
        for (Province province : provinces) {
                vector.add(0);
        }

        // domains
        for (Domain domain : domains) {
            if (form.getDomains().contains(domain.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // skills
        for (Skill skill : skills) {
            if (form.getSkills().contains(skill.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }
        return vector;
    }

    public List<Integer> createRecruitmentVector(Recruitment recruitment) {
        List<Integer> vector = new ArrayList<>();
        List<Province> provinces = provinceRepository.findAll();
        List<Domain> domains = domainRepository.findAll();
        List<Skill> skills = skillRepository.findAll();

        // kinh nghiệm
        vector.add(recruitment.getExperience());

        // province
        for (Province province : provinces) {
            if (recruitment.getAddress().getProvince().equals(province.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // domains
        for (Domain domain : domains) {
            if (recruitment.getDomains().contains(domain.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // skills
        for (Skill skill : skills) {
            if (recruitment.getSkills().contains(skill.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        return vector;
    }

    public List<Integer> createCVVector(Cv cv) {
        List<Integer> vector = new ArrayList<>();
        List<Province> provinces = provinceRepository.findAll();
        List<Domain> domains = domainRepository.findAll();
        List<Skill> skills = skillRepository.findAll();

        // kinh nghiệm
        vector.add(cv.getExperience());

        // provinces
        for (Province province : provinces) {
            if (cv.getProvinces().contains(province.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // domains
        for (Domain domain : domains) {
            if (cv.getDomains().contains(domain.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        // skills
        for (Skill skill : skills) {
            if (cv.getSkills().contains(skill.getName())) {
                vector.add(1);
            } else {
                vector.add(0);
            }
        }

        return vector;
    }
}
