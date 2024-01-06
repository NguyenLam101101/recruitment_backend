package com.example.recruitment2.Service;

import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.*;
import com.example.recruitment2.Repository.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployerService {
//    @Value("${upload.image.path}")
//    private String uploadImageDir;
    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional(rollbackFor={Exception.class, Throwable.class})
    public int saveEmployer(EmployerSignupForm form){
        if(accountRepository.findAccountByEmail(form.getEmail()) != null){
            return -2;
        }
        Account account = new Account();
        account.setEmail(form.getEmail());
        account.setPassword(form.getPassword());
        account.setRole("ROLE_EMPLOYER");
        Account savedAccount = userService.saveAccount(account);
        Employer employer = new Employer();
        employer.setFirstName(form.getFirstName());
        employer.setLastName(form.getLastName());
        employer.setPhoneNumber(form.getAreaCode()+form.getPhoneNumber());
        employer.setAccount(savedAccount);
        employerRepository.save(employer);
        return 1;
    }

    public Employer findEmployerByAccount_Email(String email){
        Account account = accountRepository.findAccountByEmail(email);
        if(account == null)
            return null;
        Employer employer = employerRepository.findEmployerByAccount__id(account.get_id());
        return employer;
    }

    //cần điều chỉnh lại code vò đang chạy 2 vòng for lông nhau
    public List<Application> findApplicationByEmployer(Employer employer){
        List<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByEmployer__id(employer.get_id());
        List<Application> applications = new ArrayList<>();
        for(Recruitment recruitment: recruitments){
            List<Application> applications1 = applicationRepository.findApplicationsByRecruitment__id(recruitment.get_id());
            applications.addAll(applications1);
        }
        return applications;
    }

    @Transactional(rollbackFor = {Exception.class, Throwable.class})
    public void approveApplications(ApprovalForm form) throws IOException{
        Application application = applicationRepository.findById(new ObjectId(form.getApplicationId())).get();
        //build history
        if(!form.getAction().equals("reject")){
            application.getCurrentMail().setStatus("passed");
        }
        else {
            application.getCurrentMail().setStatus("failed");
        }
        if(application.getApplicationHistories() == null){
            application.setApplicationHistories(new ArrayList<Mail>());
        }
        application.getApplicationHistories().add(application.getCurrentMail());

        //set new current phase
        Mail newCurMail = new Mail();
        newCurMail.setHead(form.getHead());
        newCurMail.setBody(form.getBody());
        newCurMail.setTime(LocalDateTime.now());
        newCurMail.setPhase(form.getAction());
        newCurMail.setStatus("processing");
        newCurMail.setAttachments(form.getAttachments());

        if(form.getAction().equals("reject")) {
            newCurMail.setStatus("failed");
            newCurMail.setPhase("Từ chối");
        }
        application.setCurrentMail(newCurMail);
        applicationRepository.save(application);

        //info employer
        Notification notification = new Notification();
        notification.setMessage(application.getRecruitment().getEmployer().getFirstName() + " " +
                application.getRecruitment().getEmployer().getLastName() +
                " vừa gửi thư thông báo kết quả ứng tuyển của bạn");
        notification.setTime(LocalDateTime.now());
        notification.setType("approval");
        notification.setEmployee(application.getCv().getEmployee());
        notification.setHostRole("ROLE_EMPLOYEE");
        notification.setObjectId(application.get_id().toString());
        notificationRepository.save(notification);
    }

//    public List<Recruitment> findRecruitmentsByEmployerId(String id){
//        List<Recruitment> recruitments = recruitmentRepository.findRecruitmentsByEmployer_IdOrderByIdDesc(id);
//        List<Recruitment> transformedRecruitments = new ArrayList<>();
//        recruitments.forEach(recruitment -> {
//            try {
//                recruitment.setBanner(appService.pathToBase64(recruitment.getBanner(), "image/png"));
//            } catch (IOException e){
//                System.out.println(e);
//            }
//            try {
//                recruitment.getEmployer().getCompany().setLogo(appService.pathToBase64(recruitment.getEmployer().getCompany().getLogo(), "image/png"));
//            } catch (IOException e) {
//                System.out.println(e);
//            }
//        });
//        return recruitments;
//    }
}
