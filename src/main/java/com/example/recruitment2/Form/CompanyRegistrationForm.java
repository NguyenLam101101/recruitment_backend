package com.example.recruitment2.Form;

import com.example.recruitment2.Entity.Address;
import com.example.recruitment2.Entity.Company;
import com.example.recruitment2.Entity.Employer;
import com.example.recruitment2.Service.AppService;
import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Data
public class CompanyRegistrationForm {
    AppService appService = new AppService();
    @NonNull
    private String name;
//    @NonNull
    private MultipartFile logo;
    @NonNull
    private String field;
    @NonNull
    private String province;
    private String detail;
    private String taxCode;
    private String size;
    private String description;
    private String email;
    private String website;
    
    public Company parseToCompany() throws IOException{
        Company company = new Company();
        company.setName(name);
        //logo
        String logoPath = appService.saveFile(logo, appService.getImageDir());
        company.setLogo(logoPath);
        company.setField(field);
        //address
        company.setAddress(new Address());
        company.getAddress().setDetail(detail);
        company.getAddress().setProvince(province);
        company.setTaxCode(taxCode);
        company.setSize(size);
        company.setDescription(description);
        company.setEmail(email);
        company.setWebsite(website);
        return company;
    }
}
