package com.example.recruitment2.Controller;

import com.example.recruitment2.Config.Link;
import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.CommentForm;
import com.example.recruitment2.Form.CommentResponseForm;
import com.example.recruitment2.Form.ReplyForm;
import com.example.recruitment2.Repository.*;
import com.example.recruitment2.Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    final static Link link= new Link();
    final static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserService userService;
    @Autowired
    private EmployerService employerService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private CvRepository cvRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentResponseRepository commentResponseRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/has-logged-in")
    public ResponseEntity<String> hasLoggedIn(Authentication authentication) throws IOException {
        if(authentication==null) {
            return ResponseEntity.ok("{\"role\": \"\", \"username\": \"\"}");
        }
        Map<String, Object> map = new HashMap<>();
        String role = authentication.getAuthorities().toArray()[0].toString();
        ObjectMapper mapper = new ObjectMapper();
        map.put("role", role);
        map.put("email", authentication.getName());
        switch (role){
            case "ROLE_EMPLOYEE":
                Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
                employee.getObjectToSend();
                map.put("profile", employee);
                break;
            case "ROLE_EMPLOYER":
                Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
                employer.getObjectToSend();
                map.put("profile", employer);
                break;
            default:
                map.put("username", "");
        }
        return ResponseEntity.ok(mapper.writeValueAsString(map));
    }

    @GetMapping("/get-cv-by-id")
    public ResponseEntity<String> getCvById(@RequestParam String id, Authentication authentication){
        Cv cv = cvRepository.findById(new ObjectId(id)).get();
        if(cv.getPrivacy() == "private"){
            if(authentication == null)
                return ResponseEntity.status(401).build();
            if(! cv.getEmployee().getAccount().getEmail().equals(authentication.getName())){
                return ResponseEntity.status(404).build();
            }
        }
        if(!cv.getFile().getOriginalName().strip().toLowerCase().endsWith("html")){
            try{
                return ResponseEntity.ok(appService.pathToBase64(cv.getFile().getSource()));
            }catch(Exception e){
                System.out.println(e);
                return ResponseEntity.status(500).build();
            }
        }
        else {
            try{
                String htmlContent = Files.readString(Path.of(cv.getFile().getSource()));
                Map<String, String> map = new HashMap<>();
                map.put("htmlContent", htmlContent);
                map.put("name", cv.getFile().getOriginalName());
                String response = new ObjectMapper().writeValueAsString(map);
                return ResponseEntity.ok(response);
            }catch(Exception e){
                System.out.println(e);
                return ResponseEntity.status(500).build();
            }
        }
    }

    @GetMapping("/get-application-by-id")
    public ResponseEntity<Application> getApplicationById(@RequestParam String id, Authentication authentication) throws IOException{
        if(authentication == null){
            return ResponseEntity.status(401).build();
        }
        Application application = applicationRepository.findById(new ObjectId(id)).get();
        if(!application.getCv().getEmployee().getAccount().getEmail().equals(authentication.getName()) &&
                !application.getRecruitment().getEmployer().getAccount().getEmail().equals(authentication.getName())){
            return ResponseEntity.status(403).build();
        }
        //id
        application.setId(application.get_id().toString());
        //cv
        if(application.getCv() != null)
            application.getCv().getFile().setSource(appService.pathToBase64(application.getCv().getFile().getSource()));
        //recruitment
        application.getRecruitment().setId(application.getRecruitment().get_id().toString());
        String base64Logo = appService.pathToBase64(application.getRecruitment().getEmployer().getCompany().getLogo());
        application.getRecruitment().getEmployer().getCompany().setLogo(base64Logo);
        //attachment
        if(application.getApplicationHistories() != null)
        {
            for(Mail history:application.getApplicationHistories()){
                for(UserFile attachment: history.getAttachments()){
                    String base64Attachment = appService.pathToBase64(attachment.getSource());
                    attachment.setSource(base64Attachment);
                }
            }
        }
        return ResponseEntity.ok(application);
    }

    @PostMapping("/reply-mail")
    public ResponseEntity<String> replyMail(ReplyForm form, Authentication authentication) throws IOException{
        if(authentication == null){
            return ResponseEntity.status(401).build();
        }
        Application application = applicationRepository.findById(new ObjectId(form.getApplicationId())).get();
        if(!application.getCv().getEmployee().getAccount().getEmail().equals(authentication.getName()) &&
                !application.getRecruitment().getEmployer().getAccount().getEmail().equals(authentication.getName())){
            return ResponseEntity.status(403).build();
        }
        int direction = 1;
        if(application.getCv().getEmployee().getAccount().getEmail().equals(authentication.getName()))
            direction = 0;
        userService.saveMailResponse(form, direction);
        return ResponseEntity.ok("Successful");
    }

    @PostMapping("/comment")
    public ResponseEntity<Comment> comment(@ModelAttribute CommentForm form, Authentication authentication) throws Exception {
        if(authentication == null){
            return ResponseEntity.status(401).build();
        }
        String role = authentication.getAuthorities().toArray()[0].toString();
        Object host;
        if(role.equals("ROLE_EMPLOYEE"))
            host = (Employee) employeeService.findEmployeeByAccount_Email(authentication.getName());
        else if (role.equals("ROLE_EMPLOYER"))
            host = (Employer) employerService.findEmployerByAccount_Email(authentication.getName());
        else if (role.equals("ROLE_ADMIN")) {
            host = (Administrator) adminService.findAdminByAccount_Email(authentication.getName());
        }
        else
            return ResponseEntity.status(400).build();
        Comment comment = userService.saveComment(form, host, role);
        comment.getObjectToSend();
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/reply-comment")
    public ResponseEntity<CommentResponse> replyComment(@ModelAttribute CommentResponseForm form, Authentication authentication) throws IOException{
        if(authentication == null){
            return ResponseEntity.status(401).build();
        }
        String role = authentication.getAuthorities().toArray()[0].toString();
        Object host;
        if(role.equals("ROLE_EMPLOYEE"))
            host = (Employee) employeeService.findEmployeeByAccount_Email(authentication.getName());
        else if (role.equals("ROLE_EMPLOYER"))
            host = (Employer) employerService.findEmployerByAccount_Email(authentication.getName());
        else if (role.equals("ROLE_ADMIN")) {
            host = (Administrator) adminService.findAdminByAccount_Email(authentication.getName());
        }
        else
            return ResponseEntity.status(400).build();
        CommentResponse response = userService.saveCommentResponse(form, host, role);
        response.getObjectToSend();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-comment")
    public ResponseEntity<String> deleteComment(@RequestParam String id, Authentication authentication){
        if(authentication == null){
            return ResponseEntity.status(401).build();
        }
        Comment comment = commentRepository.findById(new ObjectId(id)).get();
        if(comment.getHostRole().equals("ROLE_EMPLOYEE"))
            if(!comment.getEmployee().getAccount().getEmail().equals(authentication.getName()))
                return ResponseEntity.status(403).build();
        if(comment.getHostRole().equals("ROLE_EMPLOYER"))
            if(!comment.getEmployer().getAccount().getEmail().equals(authentication.getName()))
                return ResponseEntity.status(403).build();
        if(comment.getHostRole().equals("ROLE_ADMIN"))
            if(!comment.getAdministrator().getAccount().getEmail().equals(authentication.getName()))
                return ResponseEntity.status(403).build();
        List<CommentResponse> responses = commentResponseRepository.getCommentResponsesByComment__id(new ObjectId(id));
        commentResponseRepository.deleteAll(responses);
        commentRepository.delete(comment);
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/delete-comment-response")
    public ResponseEntity<String> deleteCommentResponse(@RequestParam String id, Authentication authentication){
        if(authentication == null){
            return ResponseEntity.status(401).build();
        }
        CommentResponse response = commentResponseRepository.findById(new ObjectId(id)).get();
        if(response.getHostRole().equals("ROLE_EMPLOYEE"))
            if(!response.getEmployee().getAccount().getEmail().equals(authentication.getName()))
                return ResponseEntity.status(403).build();
        if(response.getHostRole().equals("ROLE_EMPLOYER"))
            if(!response.getEmployer().getAccount().getEmail().equals(authentication.getName()))
                return ResponseEntity.status(403).build();
        if(response.getHostRole().equals("ROLE_ADMIN"))
            if(!response.getAdministrator().getAccount().getEmail().equals(authentication.getName()))
                return ResponseEntity.status(403).build();
        commentResponseRepository.delete(response);
        return ResponseEntity.ok("");
    }

    @PostMapping("/react")
    public Object react(@RequestParam String id,
                      @RequestParam String postType,
                      @RequestParam String reaction,
                      Authentication authentication) throws Exception {
        if(authentication==null) {
            return null;
        }
        Account account = accountRepository.findAccountByEmail(authentication.getName());
        Object host = "";
        if(account.getRole().equals("ROLE_EMPLOYEE"))
            host = (Employee) employeeService.findEmployeeByAccount_Email(authentication.getName());
        else if (account.getRole().equals("ROLE_EMPLOYER"))
            host = (Employer) employerService.findEmployerByAccount_Email(authentication.getName());
        else if (account.getRole().equals("ROLE_ADMIN")) {
            host = (Administrator) adminService.findAdminByAccount_Email(authentication.getName());
        }
        Object object = userService.saveReaction(id, postType, reaction, host, account.getRole());
        if(postType.equals("recruitment"))
            ((Recruitment) object).getObjectToSend();
        return object;
    }

    @GetMapping("/get-reaction")
    public ResponseEntity<String> getReaction(@RequestParam String id,
                                              @RequestParam String postType,
                                              Authentication authentication) throws Exception{
        if(authentication==null) {
            return ResponseEntity.status(401).build();
        }
        Account account = accountRepository.findAccountByEmail(authentication.getName());
        Object host = "";
        if(account.getRole().equals("ROLE_EMPLOYEE"))
            host = (Employee) employeeService.findEmployeeByAccount_Email(authentication.getName());
        else if (account.getRole().equals("ROLE_EMPLOYER"))
            host = (Employer) employerService.findEmployerByAccount_Email(authentication.getName());
        else if (account.getRole().equals("ROLE_ADMIN")) {
            host = (Administrator) adminService.findAdminByAccount_Email(authentication.getName());
        }
        Reaction reaction = userService.getReaction(id, postType, host, account.getRole());
        return ResponseEntity.ok(reaction.getReaction());
    }

    @PutMapping("/read-notification")
    public ResponseEntity<String> readNotification(@RequestParam String id, Authentication authentication){
        if(authentication==null) {
            return ResponseEntity.status(401).build();
        }
        Notification notification = notificationRepository.findById(new ObjectId(id)).get();
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("");
    }

    @GetMapping("/get-notifications")
    public ResponseEntity<Object> getNotifications(Authentication authentication, Optional<Integer> page, Optional<Integer> rowLimit) throws Exception{
        if(authentication==null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> map = new HashMap<>();
        String role = authentication.getAuthorities().toArray()[0].toString();
        if(role.equals("ROLE_EMPLOYEE")){
            Employee employee = employeeService.findEmployeeByAccount_Email(authentication.getName());
            map.put("notReadCount", notificationRepository.countNotificationsByEmployee__idAndIsRead(employee.get_id(), false));
            List<Notification> notifications = userService.getNotificationByUserId(employee.get_id(), role, page, rowLimit);
            for(Notification notification:notifications){
                notification.getObjectToSend();
            }
            map.put("notifications", notifications);
        }
        else if(role.equals("ROLE_EMPLOYER")){
            Employer employer = employerService.findEmployerByAccount_Email(authentication.getName());
            map.put("notReadCount", notificationRepository.countNotificationsByEmployer__idAndIsRead(employer.get_id(), false));
            List<Notification> notifications = userService.getNotificationByUserId(employer.get_id(), role, page, rowLimit);
            for(Notification notification:notifications){
                notification.getObjectToSend();
            }
            map.put("notifications", notifications);
        }
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return ResponseEntity.ok(mapper.writeValueAsString(map));
    }
}