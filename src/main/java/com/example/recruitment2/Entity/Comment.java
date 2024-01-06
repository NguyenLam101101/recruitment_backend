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
@Document(collection = "comment")
public class Comment {
    @Id
    @JsonIgnore
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef(lazy = true)
    @JsonIgnore
    private Recruitment recruitment;
    @DBRef(lazy = true)
    @JsonIgnore
    private News news;
    private String content;
    private String image;
    private String hostRole;
    @DBRef(lazy = true)
    private Employee employee;
    @DBRef(lazy = true)
    private Employer employer;
    @DBRef(lazy = true)
    private Administrator administrator;
    private String postType;
    private LocalDateTime time = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    @Transient
    private List<CommentResponse> responses = new ArrayList<>();

    public void getObjectToSend() throws IOException {
        AppService appService = new AppService();
        setId(_id.toString());
        switch (hostRole){
            case "ROLE_EMPLOYEE":
                employee.setId(employee.get_id().toString());
                break;
            case "ROLE_EMPLOYER":
                employer.setId(employer.get_id().toString());
                break;
            case "ROLE_ADMIN":
                administrator.setId(administrator.get_id().toString());
                break;
        }
        if(image != null)
            setImage(appService.pathToBase64(image));
    }
}
