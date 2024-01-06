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

@Document(collection = "comment_response")
@Data
public class CommentResponse {
    @Id
    @JsonIgnore
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef
    @JsonIgnore
    private Comment comment;
    private String content;
    private String image;
    private String hostRole;
    @DBRef(lazy = true)
    private Employee employee;
    @DBRef(lazy = true)
    private Employer employer;
    @DBRef(lazy = true)
    private Administrator administrator;
    private LocalDateTime time = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

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
