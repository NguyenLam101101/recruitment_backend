package com.example.recruitment2.Entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Data
public class Mail{
    private LocalDateTime time = LocalDateTime.now();
    private String head;
    private String body;
    private String phase;
    private String status = "processing";
    private List<UserFile> attachments;
    private List<MailResponse> responses = new ArrayList<>();
    private int isRead = 0;
}