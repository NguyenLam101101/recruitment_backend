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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "cv")
public class Cv {
    @JsonIgnore
    @Id
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef(lazy=true)
    private Employee employee;
    private UserFile file;
    private int experience;
    private String wage;
    private String privacy;
    private LocalDateTime time = LocalDateTime.now();
    private LocalDateTime editedTime = LocalDateTime.now();
    private List<String> domains;
    private List<String> skills;
    private List<String> provinces;
    @JsonIgnore
    private List<Integer> featureVector;

    public void getObjectToSend(Boolean doGetFile) throws IOException {
        AppService appService = new AppService();
        setId(_id.toString());
        getEmployee().setId(getEmployee().get_id().toString());
        if(doGetFile)
            getFile().setSource(appService.pathToBase64(getFile().getSource()));
    }

    public void getObjectToSend() throws IOException {
        AppService appService = new AppService();
        setId(_id.toString());
        getEmployee().setId(getEmployee().get_id().toString());
    }
}
