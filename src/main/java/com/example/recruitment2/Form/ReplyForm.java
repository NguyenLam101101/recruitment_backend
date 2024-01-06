package com.example.recruitment2.Form;

import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReplyForm{
    @NonNull
    private String applicationId;
    @NonNull
    private String body;
    private List<MultipartFile> attachments;
}
