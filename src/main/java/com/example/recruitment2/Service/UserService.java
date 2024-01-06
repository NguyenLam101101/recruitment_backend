package com.example.recruitment2.Service;

import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.CommentForm;
import com.example.recruitment2.Form.CommentResponseForm;
import com.example.recruitment2.Form.ReplyForm;
import com.example.recruitment2.Repository.*;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@EnableScheduling
public class UserService implements UserDetailsService{
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private AppService appService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentResponseRepository commentResponseRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByEmail(email);
        if(account == null){
            throw new UsernameNotFoundException("not found email");
        }
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(account.getRole()));
        return new User(account.getEmail(), account.getPassword(), grantedAuthorities);
    }
    @Transactional(rollbackFor = {Exception.class, Throwable.class})
    public Account saveAccount(Account account){
        if(accountRepository.findAccountByEmail(account.getEmail()) != null) {
            throw new RuntimeException("Existed email");
        }
        account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    public void saveMailResponse(ReplyForm form, int direction) throws IOException{
        Application application = applicationRepository.findById(new ObjectId(form.getApplicationId())).get();
        MailResponse mailResponse= new MailResponse();
        mailResponse.setBody(form.getBody());
        if(form.getAttachments() != null){
            List<UserFile> attachments = new ArrayList<>();
            for(MultipartFile multipartFile : form.getAttachments()){
                String path = appService.saveFile(multipartFile, appService.getAttachmentDir());
                UserFile userFile = new UserFile();
                userFile.setSource(path);
                userFile.setOriginalName(multipartFile.getOriginalFilename());
                attachments.add(userFile);
            }
            mailResponse.setAttachments(attachments);
        }
        mailResponse.setDirection(direction);
        if(application.getCurrentMail().getResponses() == null){
            application.getCurrentMail().setResponses(new ArrayList<>());
        }
        application.getCurrentMail().getResponses().add(mailResponse);
        applicationRepository.save(application);

        //info employer
        Notification notification = new Notification();
        notification.setTime(LocalDateTime.now());
        notification.setType("replyMail");
        if(direction == 1){
            notification.setMessage(application.getRecruitment().getEmployer().getFirstName() + " " +
                    application.getRecruitment().getEmployer().getLastName() +
                    " vừa trả lời thư");
            notification.setEmployee(application.getCv().getEmployee());
            notification.setHostRole("ROLE_EMPLOYEE");
        }
        else {
            notification.setMessage(application.getCv().getEmployee().getFirstName() + " " +
                    application.getCv().getEmployee().getLastName() +
                    " vừa trả lời thư");
            notification.setEmployer(application.getRecruitment().getEmployer());
            notification.setHostRole("ROLE_EMPLOYER");
            notification.setObjectId(application.get_id().toString());
        }
        notificationRepository.save(notification);
    }

    public Comment saveComment(CommentForm form, Object host, String hostRole) throws Exception {
        Comment comment = new Comment();
        comment.setPostType(form.getPostType());
        if(form.getPostType().equals("recruitment")){
            Recruitment recruitment = recruitmentRepository.findById(new ObjectId(form.getId())).get();
            comment.setRecruitment(recruitment);
        } else if (form.getPostType().equals("news")){
            News news = newsRepository.findById(new ObjectId(form.getId())).get();
            comment.setNews(news);
        } else
            throw new Exception("invalid postType");
        comment.setContent(form.getContent());
        if(form.getImage() != null){
            String path = appService.saveFile(form.getImage(), appService.getImageDir());
            comment.setImage(path);
        }
        comment.setHostRole(hostRole);
        comment.setTime(LocalDateTime.now());
        if(hostRole.equals("ROLE_EMPLOYEE")){
            comment.setEmployee((Employee) host);
        } else if (hostRole.equals("ROLE_EMPLOYER")) {
            comment.setEmployer((Employer) host);
        } else if (hostRole.equals("ROLE_ADMIN")) {
            comment.setAdministrator((Administrator) host);
        }
        return commentRepository.save(comment);
    }

    public CommentResponse saveCommentResponse(CommentResponseForm form, Object host, String hostRole) throws IOException{
        Comment comment = commentRepository.findById(new ObjectId(form.getCommentId())).get();
        CommentResponse response = new CommentResponse();
        response.setComment(comment);
        response.setContent(form.getContent());
        if(form.getImage() != null){
            String path = appService.saveFile(form.getImage(), appService.getImageDir());
            response.setImage(path);
        }
        response.setHostRole(hostRole);
        response.setTime(LocalDateTime.now());
        if(hostRole.equals("ROLE_EMPLOYEE")){
            response.setEmployee((Employee) host);
        } else if (hostRole.equals("ROLE_EMPLOYER")) {
            response.setEmployer((Employer) host);
        }
        return commentResponseRepository.save(response);
    }
    
    public Object saveReaction(String id, String postType, String reaction, Object host, String hostRole) throws Exception {
        Reaction reactionObject = new Reaction();
        if(List.of("inappropriate", "neutral", "appropriate").contains(reaction))
            reactionObject.setReaction(reaction);
        else
            return null;
        reactionObject.setTime(LocalDateTime.now());

        reactionObject.setHostRole(hostRole);

        Recruitment recruitment = new Recruitment();
        News news = new News();
        List<Reaction> reactions = new ArrayList<>();
        switch (postType){
            case "recruitment":
                recruitment = recruitmentRepository.findById(new ObjectId(id)).get();
                reactions = recruitment.getReactions();
                break;
            case "news":
                news = newsRepository.findById(new ObjectId(id)).get();
                reactions = news.getReactions();
                break;
            default:
                throw new Exception("Invalid postType");
        }
        switch (hostRole){
            case "ROLE_EMPLOYEE":
                reactionObject.setEmployee((Employee) host);
                reactions = reactions.stream().filter(reaction1 -> !reaction1.getHostRole().equals(hostRole) || !reaction1.getEmployee().equals((Employee) host)).toList();
                break;
            case "ROLE_EMPLOYER":
                reactionObject.setEmployer((Employer) host);
                reactions = reactions.stream().filter(reaction1 -> !reaction1.getHostRole().equals(hostRole) || !reaction1.getEmployer().equals((Employer) host)).toList();
                break;
            case "ROLE_ADMIN":
                reactionObject.setAdministrator((Administrator) host);
                reactions = reactions.stream().filter(reaction1 -> !reaction1.getHostRole().equals(hostRole) || !reaction1.getAdministrator().equals((Administrator) host)).toList();
                break;
            default:
                throw new Exception("invalid hostRole");
        }

        if(reactions == null || reactions.size() < 1)
            reactions = new ArrayList<>();
        else
            reactions = new ArrayList<>(reactions);
        reactions.add(reactionObject);

        switch (postType){
            case "recruitment":
                recruitment.setReactions(reactions);
                return recruitmentRepository.save(recruitment);
            case "news":
                news.setReactions(reactions);
                return newsRepository.save(news);
            default:
                return null;
        }
    }

    public Reaction getReaction(String id, String postType, Object host, String hostRole) throws Exception {
        Recruitment recruitment = new Recruitment();
        News news = new News();
        List<Reaction> reactions = new ArrayList<>();
        Reaction reaction = new Reaction();
        switch (postType){
            case "recruitment":
                recruitment = recruitmentRepository.findById(new ObjectId(id)).get();
                reaction = recruitment.getReactions().stream()
                        .filter(reaction1 -> {
                            switch (hostRole){
                                case "ROLE_EMPLOYEE":
                                    return reaction1.getEmployee().equals((Employee) host);
                                case "ROLE_EMPLOYER":
                                    return reaction1.getEmployer().equals((Employer) host);
                                case "ROLE_ADMIN":
                                    return reaction1.getAdministrator().equals((Administrator) host);
                                default:
                                    return false;
                            }
                        })
                        .findFirst()
                        .orElse(new Reaction());
                break;
            case "news":
                news = newsRepository.findById(new ObjectId(id)).get();
                reaction = news.getReactions().stream()
                        .filter(reaction1 -> {
                            switch (hostRole){
                                case "ROLE_EMPLOYEE":
                                    return reaction1.getEmployee().equals((Employee) host);
                                case "ROLE_EMPLOYER":
                                    return reaction1.getEmployer().equals((Employer) host);
                                case "ROLE_ADMIN":
                                    return reaction1.getAdministrator().equals((Administrator) host);
                                default:
                                    return false;
                            }
                        })
                        .findFirst()
                        .orElse(new Reaction());
                break;
            default:
                throw new Exception("Invalid postType");
        }
       return reaction;
    }


    @PostConstruct
    @Scheduled(cron = "0 0 0 * * ?") // Chạy vào lúc 00:00 hàng ngày
    public void autoCheckDueRecruitment(){
        List<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByStatus("active");
        LocalDate currentDate = LocalDate.now();
        for(Recruitment recruitment:recruitments){
            if(recruitment.getEndDate().isBefore(currentDate)){
                recruitment.setStatus("expired");
            }
        }
        recruitmentRepository.saveAll(recruitments);
    }

//    @PostConstruct
//    @Scheduled(fixedRate = 30 * 60 * 1000)
//    public void calculateCvAndRecruitmentDistance() throws IOException{
//        ProcessBuilder processBuilder = new ProcessBuilder("python", "C:\\Users\\nguye\\Documents\\Java_project\\Recruitment2\\src\\main\\python\\calculateCVAndRecruitmentDistance.py");
//        processBuilder.redirectErrorStream(true);
//        processBuilder.start();
//    }

    public List<Application> getApplicationByUserId(ObjectId id, String role, Optional<Integer> pageNum, Optional<Integer> rowLimit){
        List<Application> applications = new ArrayList<>();

        int page = pageNum.orElse(0);
        int pageSize = rowLimit.orElse(10);
        Sort sort = Sort.by(Sort.Direction.DESC, "time", "_id");
        Pageable pageable = PageRequest.of(Math.max(0, page-1), pageSize, sort);
        if(role.equals("ROLE_EMPLOYEE")){
            applications = applicationRepository.findApplicationsByCv_Employee__id(id, pageable);
        }
        else if (role.equals("ROLE_EMPLOYER")) {
            applications = applicationRepository.findApplicationsByCv_Employee__id(id, pageable);
        }
        else
            return null;
        return applications;
    }

    public List<Notification> getNotificationByUserId(ObjectId id, String role, Optional<Integer> pageNum, Optional<Integer> rowLimit){
        List<Notification> notifications = new ArrayList<>();

        int page = pageNum.orElse(1);
        int pageSize = rowLimit.orElse(10);
        Sort sort = Sort.by(Sort.Direction.DESC, "time", "_id");
        Pageable pageable = PageRequest.of(Math.max(0, page-1), pageSize, sort);
        if(role.equals("ROLE_EMPLOYEE")){
            notifications = notificationRepository.findNotificationsByEmployee__id(id, pageable);
        }
        else if (role.equals("ROLE_EMPLOYER")) {
            notifications = notificationRepository.findNotificationsByEmployer__id(id, pageable);
        }
        else
            return null;
        return notifications;
    }
}

