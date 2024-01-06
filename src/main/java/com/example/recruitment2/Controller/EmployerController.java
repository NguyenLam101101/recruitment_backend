package com.example.recruitment2.Controller;

import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.*;
import com.example.recruitment2.Repository.*;
import com.example.recruitment2.Service.AppService;
import com.example.recruitment2.Service.EmployerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/employer")
public class EmployerController {
    @Autowired
    private EmployerService employerService;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private CvRepository cvRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/register-company")
    public ResponseEntity<String> registerCompany(@ModelAttribute CompanyRegistrationForm form, Authentication authentication) throws IOException {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).body("");
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        Company company = form.parseToCompany();
        employer.setCompany(company);
        employerRepository.save(employer);
        return ResponseEntity.ok("successful");
    }

    @GetMapping("/has-company")
    public boolean hasCompany(Authentication authentication){
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return false;
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        if (employer.getCompany() != null){
            return true;
        }
        return false;
    }

    @GetMapping("/get-recruitments")
    public ResponseEntity<List<Recruitment>> getRecruitments(Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).build();
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        List<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByEmployer__idOrderByTimeDesc(employer.get_id());
        for(Recruitment recruitment:recruitments){
            recruitment.getObjectToSend();
        };
        return ResponseEntity.ok(recruitments);
    }

//    @GetMapping("/get-unread-mail-num")
//    public int getUnReadMailNum(Authentication authentication){
//        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
//            throw new RuntimeException("authentication is null");
//        }
//        Employer employer = employerRepository.findEmployerByAccount_Email(authentication.getName());
//        return mailRepository.countByEmployer_IdAndIsRead(employer.getId(),0);
//    }
    @GetMapping("/get-applications")
    public ResponseEntity<List<Application>> getApplications(Authentication authentication) throws IOException
    {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).build();
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        List<Application> applications = employerService.findApplicationByEmployer(employer);
        for(Application application: applications){
            //id
            application.setId(application.get_id().toString());
//            application.setApplicationHistories(null);
        }
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/post-recruitment")
    public ResponseEntity<Integer> postRecruitment(@ModelAttribute RecruitmentForm form,Authentication authentication) {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            System.out.println("not authenticated");
            return ResponseEntity.ok(-1);
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        Recruitment recruitment = form.parseToRecruitment(employer);
        System.out.println(recruitment);
        List<Integer> vector = appService.createRecruitmentVector(recruitment);
        recruitment.setFeatureVector(vector);
        recruitmentRepository.save(recruitment);
        return ResponseEntity.ok(1);
    }

    @PostMapping("/update-recruitment")
    public ResponseEntity<String> updateRecruitment(@ModelAttribute RecruitmentForm form, @RequestParam String recruitmentId, Authentication authentication) {
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).build();
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        Recruitment recruitment = recruitmentRepository.findById(new ObjectId(recruitmentId)).get();
        if(! recruitment.getEmployer().get_id().equals(employer.get_id()))
            return ResponseEntity.status(403).build();
        form.mapToRecruitment(recruitment);
        recruitment.setEditedTime(LocalDateTime.now());
        recruitmentRepository.save(recruitment);
        return ResponseEntity.ok("Successful");
    }

    @PostMapping("/approve-application")
    public ResponseEntity<String> approveApplication(ApprovalForm form, Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).build();
        }
        Application application = applicationRepository.findById(new ObjectId(form.getApplicationId())).get();
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        if(!employer.get_id().equals(application.getRecruitment().getEmployer().get_id())){
            return ResponseEntity.status(403).build();
        }
        employerService.approveApplications(form);
        return ResponseEntity.ok("Successful");
    }

    @PostMapping("/finish-application")
    public ResponseEntity<String> finishApplication(String applicationId, Authentication authentication) throws IOException{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            System.out.println("not authenticated");
            return ResponseEntity.status(401).build();
        }
        Application application = applicationRepository.findById(new ObjectId(applicationId)).get();
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        if(!employer.get_id().equals(application.getRecruitment().getEmployer().get_id())){
            return ResponseEntity.status(403).build();
        }
        application.getCurrentMail().setStatus("done");
        applicationRepository.save(application);
        return ResponseEntity.ok("Successful");
    }

    @GetMapping("/get-power-bi-embed-info")
    public ResponseEntity<Map<String, String>> getPowerBIEmbedInfo(Authentication authentication) throws Exception{
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).build();
        }
        Map<String, String> response = new HashMap<>();
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        response.put("employer_id", employer.get_id().toString());

        String report_id = "21df951f-2906-4e16-b002-c665471e546f";
        String group_id = "2ed59409-2f06-40d7-a2c4-4dab8428b821";
        String grant_type = "client_credentials";
        String client_id = "fdd6b115-1117-45e2-9aaf-7b91f98bffc7";
        String client_secret = "D1r8Q~FcseUVFEGwYpnLyuv~m4L-I3hZGFGIZcgS";
        String resource = "https://analysis.windows.net/powerbi/api";
        String formData = String.format("grant_type=%s&client_id=%s&client_secret=%s&resource=%s",
                                         grant_type, client_id, client_secret, resource);
        java.net.http.HttpRequest httpAccessTokenRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(new URI("https://login.microsoftonline.com/04735073-7d4c-4821-b25b-d1eb37e67057/oauth2/token"))
                .build();
        HttpResponse<String> httpAccessTokenResponse = HttpClient.newHttpClient().send(httpAccessTokenRequest,HttpResponse.BodyHandlers.ofString());
        String accessTokenResponseBody = httpAccessTokenResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode accessTokenResponseJson = objectMapper.readTree(accessTokenResponseBody);
        String accessToken = accessTokenResponseJson.get("access_token").asText();
        response.put("accessToken", accessToken);

        //get report info
        String body = "{\"accessLevel\": \"Edit\",\"allowSaveAs\": false}";
        java.net.http.HttpRequest httpEmbedTokenRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .uri(new URI(String.format("https://api.powerbi.com/v1.0/myorg/groups/%s/reports/%s/GenerateToken", group_id, report_id)))
                .build();
        HttpResponse<String> httpEmbedTokenResponse = HttpClient.newHttpClient().send(httpEmbedTokenRequest, HttpResponse.BodyHandlers.ofString());
        String embedTokenResponseBody = httpEmbedTokenResponse.body();
        JsonNode embedTokenResponseJson = objectMapper.readTree(embedTokenResponseBody);
        String embedToken = embedTokenResponseJson.get("token").asText();
        response.put("embedToken", embedToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search-cvs")
    public List<Cv> searchCvs(@ModelAttribute CvSearchForm form, Authentication authentication) throws IOException{
        List<Integer> searchVector = appService.createCvSearchVector(form);
        Files.writeString(Path.of("C:\\Users\\nguye\\Documents\\Java_project\\Recruitment2\\src\\main\\python\\cvSearchVector.txt"), searchVector.toString());
        ProcessBuilder processBuilder = new ProcessBuilder("python", "C:\\Users\\nguye\\Documents\\Java_project\\Recruitment2\\src\\main\\python\\searchCvs.py");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String output = "[]";
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("cv_ids"))
                output = line.substring(7, line.length()-1);
        }
        String[] cvIds = output.split(",");
        cvIds = Arrays.copyOfRange(cvIds, 0, Math.min(100,cvIds.length));
        List<Cv> cvs = new ArrayList<>();
        for (String id: cvIds){
            Cv cv = cvRepository.findById(new ObjectId(id)).get();
            cv.getObjectToSend();
            cvs.add(cv);
        }
        return cvs;
    }

    @GetMapping("/get-notifications")
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication){
        if(authentication==null || !authentication.getAuthorities().toArray()[0].toString().equals("ROLE_EMPLOYER")) {
            return ResponseEntity.status(401).build();
        }
        Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
        List<Notification> notifications = notificationRepository.findNotificationsByEmployer__id(employer.get_id());
        return ResponseEntity.ok(notifications);
    }
}
