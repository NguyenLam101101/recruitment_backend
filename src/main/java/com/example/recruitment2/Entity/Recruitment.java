package com.example.recruitment2.Entity;

import com.example.recruitment2.Service.AppService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "recruitment")
public class Recruitment {
    @JsonIgnore
    @Id
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef(lazy=true)
    private Employer employer;
    @Indexed
    private LocalDateTime time = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    private LocalDateTime editedTime = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    private String name;
    private String workType;
    private LocalDate endDate;
    private String position;
    private Address address;
    private int experience;
    private int employeeNumber;
    private String wage;
    private String currency;
    private List<String> domains;
    private List<String> skills;
    private String description;
    private String requirement;
    private String interest;
    private String status = "active";
    @JsonIgnore
    private List<Integer> featureVector;
    private List<Reaction> reactions = new ArrayList<>();
    @Transient
    private List<Comment> comments = new ArrayList<>();

    public void getObjectToSend() throws IOException {
        AppService appService = new AppService();
        setId(get_id().toString());
        getEmployer().getObjectToSend();
        for (Reaction reaction : reactions) {
            reaction.getObjectToSend();
        }
    }
}
