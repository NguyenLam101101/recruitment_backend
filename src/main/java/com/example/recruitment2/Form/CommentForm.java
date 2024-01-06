package com.example.recruitment2.Form;

import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CommentForm {
    @NonNull
    private String postType;
    private String id;
    private String content;
    private MultipartFile image;
}
