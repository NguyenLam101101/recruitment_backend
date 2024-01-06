package com.example.recruitment2.Controller;

import com.example.recruitment2.Config.JWTProvider;
import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.EmployeeSignupForm;
import com.example.recruitment2.Form.EmployerSignupForm;
import com.example.recruitment2.Form.RecruitmentSearchForm;
import com.example.recruitment2.Repository.*;
import com.example.recruitment2.Service.AppService;
import com.example.recruitment2.Service.EmployeeService;
import com.example.recruitment2.Service.EmployerService;
import com.example.recruitment2.Service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@RestController
public class ViewerController {
    final static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private ProvinceRepository provinceRepository;
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private EmployerService employerService;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CommentResponseRepository commentResponseRepository;
    @Autowired
    private JWTProvider jwtProvider;

    @PostMapping("/signup-employee")
    public ResponseEntity<Integer> signupEmployee(@ModelAttribute EmployeeSignupForm form){
        return ResponseEntity.ok(employeeService.saveEmployee(form));
    }

    @PostMapping("/signup-employer")
    public ResponseEntity<Integer> signupEmployer(@ModelAttribute EmployerSignupForm form){
        return ResponseEntity.ok(employerService.saveEmployer(form));
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@RequestParam String email,@RequestParam String password) throws Exception{
        UserDetails userDetails = userService.loadUserByUsername(email);
        if (passwordEncoder.matches(password,userDetails.getPassword())){
            String token = jwtProvider.generateToken(email);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            Map<String, Object> userMap = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            String role = userDetails.getAuthorities().toArray()[0].toString();
            userMap.put("role", role);
            userMap.put("email", email);
            switch (role){
                case "ROLE_EMPLOYEE":
                    Employee employee = employeeService.findEmployeeByAccount_Email(email);
                    employee.getObjectToSend();
                    userMap.put("profile", employee);
                    break;
                case "ROLE_EMPLOYER":
                    Employer employer = employerService.findEmployerByAccount_Email(email);
                    employer.getObjectToSend();
                    userMap.put("profile", employer);
                    break;
                default:
                    userMap.put("username", "");
            }
            map.put("user", userMap);
            String response = mapper.writeValueAsString(map);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    @GetMapping("/login-google")
    public void getGoogleCode(String code) throws IOException, InterruptedException {
        String formData = "";
        formData += "code=" + code;
        formData += "&client_id=" + "902701951384-klsv3s3904jknfn47p44h7hk9ko0rvir.apps.googleusercontent.com";
        formData += "&client_secret=" + "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";
//        formData.put("redirect_uri", "");
        formData += "&grant_type=authorization_code";

        java.net.http.HttpRequest httpAccessTokenRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(new URI("https://oauth2.googleapis.com/token"))
                .build();
        HttpResponse<String> httpAccessTokenResponse = HttpClient.newHttpClient().send(httpAccessTokenRequest, HttpResponse.BodyHandlers.ofString());
        String accessTokenResponseBody = httpAccessTokenResponse.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode accessTokenResponseJson = objectMapper.readTree(accessTokenResponseBody);
        String accessToken = accessTokenResponseJson.get("access_token").asText();
        System.out.println(accessTokenResponseJson.toString());
    }

    @GetMapping("/get-google-token")
    public void getGoogleToken(){

    }

    @GetMapping("/get-recruitment-by-id")
    public Recruitment getRecruitmentById(@RequestParam String id) throws IOException{
        Recruitment recruitment = recruitmentRepository.findById(new ObjectId(id)).get();
        recruitment.getObjectToSend();
        return recruitment;
    }

    @GetMapping("/get-comments")
    public List<Comment> getRecruitmentComment(@RequestParam String id, @RequestParam String postType) throws IOException{
        List<Comment> comments = new ArrayList<>();
        if(postType.equals("recruitment"))
            comments = commentRepository.getCommentsByRecruitment__id(new ObjectId(id));
        if(postType.equals("news"))
            comments = commentRepository.getCommentsByNews__id(new ObjectId(id));
        if(comments == null)
            comments = new ArrayList<>();
        String path;
        for (Comment comment: comments) {
            comment.getObjectToSend();
            List<CommentResponse> responses = commentResponseRepository.getCommentResponsesByComment__id(comment.get_id());
            for (CommentResponse response: responses) {
                response.getObjectToSend();
            }
            comment.setResponses(responses);
        }
        return comments;
    }

    @GetMapping("/get-new-recruitments")
    public List<Recruitment> getNewRecruitments(@RequestParam int page, Optional<Integer> pageSize) throws IOException{
        Sort sort = Sort.by(Sort.Direction.DESC, "time", "_id");
        Pageable pageable = PageRequest.of(Math.max(0, page-1), pageSize.orElse(10), sort);
        List<Recruitment> recruitments = recruitmentRepository.findAll(pageable).toList();
        for(Recruitment recruitment: recruitments){
            recruitment.getObjectToSend();
        }
        return recruitments;
    }

    @PostMapping("/search-recruitments")
    public List<Recruitment> searchRecruitment(@ModelAttribute RecruitmentSearchForm form) throws IOException{
        List<Integer> searchVector = appService.createRecruitmentSearchVector(form);
        Files.writeString(Path.of("C:\\Users\\nguye\\Documents\\Java_project\\Recruitment2\\src\\main\\python\\recruitmentSearchVector.txt"), form.getProvinces().toString() + "###" + searchVector.toString());
        ProcessBuilder processBuilder = new ProcessBuilder("python", "C:\\Users\\nguye\\Documents\\Java_project\\Recruitment2\\src\\main\\python\\searchRecruitments.py");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String output = "";
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("recruitment_ids"))
                output = line.substring(16, line.length()-1);
        }
        String[] recruitmentIds = output.split(",");
        recruitmentIds = Arrays.copyOfRange(recruitmentIds, 0, Math.min(100,recruitmentIds.length));
        List<Recruitment> recruitments = new ArrayList<>();
        for (String id: recruitmentIds){
            try{
                Recruitment recruitment = recruitmentRepository.findById(new ObjectId(id)).get();
                recruitment.getObjectToSend();
                recruitments.add(recruitment);
            }
            catch(Exception e){
                continue;
            }
        }
        return recruitments;
    }

    @GetMapping("/get-provinces")
    public List<Province> getProvinces(){
        return provinceRepository.findAll();
    }

    @GetMapping("/get-domains")
    public List<Domain> getDomains(){
        return domainRepository.findAll();
    }

    @GetMapping("/get-skills")
    public List<Skill> getSkills(){
        return skillRepository.findAll();
    }

    @GetMapping("/get-image")
    public ResponseEntity<Resource> getImage(){
        Resource resource = new PathResource("C:/Users/nguye/OneDrive/Hình ảnh/logo_bk.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}