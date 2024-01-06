package com.example.recruitment2.Controller;

import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.ApplicationForm;
import com.example.recruitment2.Form.CvForm;
import com.example.recruitment2.Repository.*;
import com.example.recruitment2.Service.AppService;
import com.example.recruitment2.Service.EmployeeService;
import com.example.recruitment2.Service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CvRepository cvRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/get-cvs")
    public ResponseEntity<List<Cv>> getCvs(Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).build();
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        List<Cv> cvs = cvRepository.findCvsByEmployee(employee);
        for(Cv cv: cvs){
            cv.getObjectToSend();
        }
        return ResponseEntity.ok(cvs);
    }

    @PostMapping("/upload-cv")
    public ResponseEntity<String> uploadCv(@ModelAttribute CvForm form, Authentication authentication) throws Exception {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).body("UnAuthenticated");
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        Cv cv = form.parseToCv();
        cv.setEmployee(employee);
        List<Integer> vector = appService.createCVVector(cv);
        cv.setFeatureVector(vector);
        cvRepository.save(cv);
        return ResponseEntity.ok("Successful");
    }

    @DeleteMapping("/delete-cv")
    public ResponseEntity<String> deleteCv(@RequestParam String id, Authentication authentication) throws Exception {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).body("UnAuthenticated");
        }
        Cv cv = cvRepository.findById(new ObjectId(id)).get();
        if(!cv.getEmployee().getAccount().getEmail().equals(authentication.getName()))
            return ResponseEntity.status(403).build();
        cvRepository.delete(cv);
        return ResponseEntity.ok("Successful");
    }

    @PostMapping("/update-cv")
    public ResponseEntity<String> updateCv(@ModelAttribute CvForm form, @RequestParam String cvId, Authentication authentication) throws Exception {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).body("UnAuthenticated");
        }
        Cv cv = cvRepository.findById(new ObjectId(cvId)).get();
        if(!cv.getEmployee().getAccount().getEmail().equals(authentication.getName()))
            return ResponseEntity.status(403).build();
        form.mapToCV(cv);
        cv.setEditedTime(LocalDateTime.now());
        List<Integer> vector = appService.createCVVector(cv);
        cv.setFeatureVector(vector);
        cvRepository.save(cv);
        return ResponseEntity.ok("Successful");
    }

    @PostMapping("/apply")
    public ResponseEntity<String> apply(@ModelAttribute ApplicationForm form,Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).build();
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        employeeService.apply(form, employee);
        return ResponseEntity.ok("successful");
    }

    @GetMapping("/get-applications")
    public ResponseEntity<List<Application>> getApplications(Authentication authentication){
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).build();
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        List<Cv> cvs = cvRepository.findCvsByEmployee__id(employee.get_id());
        List<Application> applications = new ArrayList<>();
        for(Cv cv:cvs){
            applications.addAll(applicationRepository.findApplicationByCv__id(cv.get_id()));
        }
        for(Application application: applications){
            //id
            application.setId(application.get_id().toString());
        }
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/get-most-suitable-recruitments")
    public ResponseEntity<List<Recruitment>> getMostSuitableRecruitmentsByEmployee(@RequestParam Optional<Integer> limit, Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).build();
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        List<Recruitment> recruitments = employeeService.findMostSuitableRecruitmentsByEmployee(employee, limit);
        for(Recruitment recruitment: recruitments){
            //id
            recruitment.setId(recruitment.get_id().toString());
            //logo
            String base64Logo = appService.pathToBase64(recruitment.getEmployer().getCompany().getLogo());
            if(base64Logo != null){
                recruitment.getEmployer().getCompany().setLogo(base64Logo);
            }
        }
        return ResponseEntity.ok(recruitments);
    }

    @PostMapping("/save-recruitment")
    public ResponseEntity<String> saveRecruitment(@RequestParam String recruitmentId, Authentication authentication){
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).build();
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        Recruitment recruitment = recruitmentRepository.findById(new ObjectId(recruitmentId)).get();
        if(employee.getSavedRecruitments() == null)
            employee.setSavedRecruitments(new ArrayList<>());
        employee.getSavedRecruitments().add(recruitment);
        employeeRepository.save(employee);
        return ResponseEntity.ok("Successful");
    }

    @GetMapping("/get-saved-recruitments")
    public ResponseEntity<List<Recruitment>> getSavedRecruitment(Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.status(401).build();
        }
        Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
        if (employee.getSavedRecruitments() == null)
            employee.setSavedRecruitments(new ArrayList<>());
        for (Recruitment recruitment: employee.getSavedRecruitments()){
            recruitment.getObjectToSend();
        }
        return ResponseEntity.ok(employee.getSavedRecruitments());
    }
}
