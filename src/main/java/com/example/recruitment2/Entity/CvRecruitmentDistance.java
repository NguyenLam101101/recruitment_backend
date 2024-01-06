package com.example.recruitment2.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection="cv_recruitment_distance")
public class CvRecruitmentDistance{
    @Id
    @JsonIgnore
    private ObjectId _id;
    @DBRef
    private Cv cv;
    @DBRef
    private Recruitment recruitment;
    private int distance;
}
