package com.example.recruitment2.Entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class Reaction {
    @DBRef
    private Employee employee;
    @DBRef
    private Employer employer;
    @DBRef
    private Administrator administrator;
    private String hostRole;
    private String reaction;
    private LocalDateTime time = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    public void getObjectToSend(){
        if(hostRole.equals("ROLE_EMPLOYEE")){
            employee.setId(employee.get_id().toString());
        } else if(hostRole.equals("ROLE_EMPLOYER")){
            employer.setId(employer.get_id().toString());
        } else if(hostRole.equals("ROLE_ADMIN")){
            administrator.setId(administrator.get_id().toString());
        }
    }
}
