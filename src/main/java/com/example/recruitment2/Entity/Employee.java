package com.example.recruitment2.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection="employee")
public class Employee {
    @JsonIgnore
    @Id
    private ObjectId _id;
    @Transient
    private String id;
    @JsonIgnore
    @DBRef(lazy=true)
    private Account account;
    private String firstName;
    private String lastName;
    private String yearOfBirth;
    private int gender;
    private String phoneNumber;
    @JsonIgnore
    @DBRef(lazy = true)
    private List<Recruitment> savedRecruitments = new ArrayList<>();

    @JsonIgnore
    public void getObjectToSend(){
        setId(_id.toString());
    }
}


