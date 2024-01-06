package com.example.recruitment2.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection="administrator")
public class Administrator {
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
}
