package com.example.recruitment2.Form;

import com.example.recruitment2.Entity.UserFile;
import com.example.recruitment2.Service.AppService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovalForm{
    @NonNull
    private String applicationId;
    @NonNull
    private String head;
    @NonNull
    private String body;
    @NonNull
    private String action;
    private List<MultipartFile> attachments = new ArrayList<>();

    public List<UserFile> getAttachments(){
        AppService appService = new AppService();
        List<UserFile> attachmentUserFiles = new ArrayList<>();
        for(MultipartFile file : attachments){
            UserFile userFile = new UserFile();
            try{
                String path = appService.saveFile(file, appService.getAttachmentDir());
                userFile.setSource(path);
                userFile.setOriginalName(file.getOriginalFilename());
                attachmentUserFiles.add(userFile);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        return attachmentUserFiles;
    }


}


