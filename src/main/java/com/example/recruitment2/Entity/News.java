package com.example.recruitment2.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "news")
public class News {
    @JsonIgnore
    @Id
    private ObjectId _id;
    @Transient
    private String id;
    @DBRef(lazy=true)
    private Administrator administrator;
    private String header;
    private String body;
    private LocalDateTime time;
    private List<Reaction> reactions = new ArrayList<>();
}
