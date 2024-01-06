package com.example.recruitment2.Entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MailResponse{
    private LocalDateTime time = LocalDateTime.now();
    private String body;
    private List<UserFile> attachments;
    private int direction;
    private int isRead = 0;
}
