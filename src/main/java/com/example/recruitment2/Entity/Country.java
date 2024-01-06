//package com.example.recruitment2.Entity;
//
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.Indexed;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.List;
//
//@Data
//@Document(collection = "country")
//public class Country {
//    @Id
//    private int id;
//    private String name;
//    private String areaCode;
//    private String currency;
//    private List<Province> provinces;
//}
//
//@Data
//@Document(collection = "province")
//class Province {
//    @Id
//    private String id;
//    private String name;
//    private List<District> districts;
//}
//@Data
//class District {
//    @Id
//    private int id;
//    private String name;
//}
