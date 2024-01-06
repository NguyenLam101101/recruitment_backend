package com.example.recruitment2.Entity;
import lombok.Data;

@Data
public class Company{
    private String name;
    private String logo;
    private byte[] logoBytes;
    private String field;
    private Address address;
    private String taxCode;
    private String size;
    private String description;
    private String email;
    private String website;
}