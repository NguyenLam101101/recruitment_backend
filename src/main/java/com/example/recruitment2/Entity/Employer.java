package com.example.recruitment2.Entity;

import com.example.recruitment2.Service.AppService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.util.List;

@Data
@Document(collection = "employer")
public class Employer {
    @JsonIgnore
    @Id
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef(lazy=true)
    @JsonIgnore
    private Account account;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Company company;
    @JsonIgnore
    private List<Cv> savedCvs;

    public void getObjectToSend() throws IOException {
        AppService appService = new AppService();
        setId(get_id().toString());
        try{
            String base64Logo = appService.pathToBase64(getCompany().getLogo());
            getCompany().setLogo(base64Logo);
        }
        catch (Exception e){}
    }
}

