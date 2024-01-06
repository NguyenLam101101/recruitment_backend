package com.example.recruitment2.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "application")
public class Application {
    @JsonIgnore
    @Id
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef(lazy=true)
    private Recruitment recruitment;
    private Cv cv;
    private Mail currentMail;
    private List<Mail> applicationHistories;
}

