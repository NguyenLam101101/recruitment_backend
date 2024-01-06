package com.example.recruitment2.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Document(collection = "notification")
public class Notification {
    @Id
    @JsonIgnore
    ObjectId _id;
    @Transient
    String id;
    String message;
    LocalDateTime time = LocalDateTime.now();
    String hostRole;
    @DBRef(lazy = true)
    Employer employer;
    @DBRef(lazy = true)
    Employee employee;
    @DBRef(lazy = true)
    Administrator administrator;
    Boolean isRead = false;
    String type;
    String objectId;

    public void getObjectToSend(){
        id = _id.toString();
        if(employee != null)
            employee.setId(employee.get_id().toString());
        if(employer != null)
            employer.setId(employer.get_id().toString());
        if(administrator != null)
            administrator.setId(administrator.get_id().toString());
    }
}
