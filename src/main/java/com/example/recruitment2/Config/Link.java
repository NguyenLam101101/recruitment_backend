package com.example.recruitment2.Config;

import org.springframework.stereotype.Component;

@Component
public class Link {
    public String getWebServer(){
        return "http://localhost:3000";
    }
    public String getEmployeeHomePage(){
        return this.getWebServer()+"/";
    }
    public String getEmployerHomePage(){
        return this.getWebServer()+"/employer";
    }
    public String getCompanyRegisterPage(){
        return this.getWebServer()+"/employer/company-register";
    }
}
