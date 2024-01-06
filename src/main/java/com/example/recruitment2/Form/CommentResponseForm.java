package com.example.recruitment2.Form;

import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CommentResponseForm {
    @NonNull
    private String commentId;
    private String content;
    private MultipartFile image;
}
