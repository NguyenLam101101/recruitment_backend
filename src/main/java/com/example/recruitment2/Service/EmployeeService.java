package com.example.recruitment2.Service;

import com.example.recruitment2.Entity.*;
import com.example.recruitment2.Form.ApplicationForm;
import com.example.recruitment2.Form.EmployeeSignupForm;
import com.example.recruitment2.Repository.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService{
    @Value("${upload.cv.path}")
    private String uploadCvDir;
    @Autowired
    private UserService userService;
    @Autowired
    private AppService appService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CvRepository cvRepository;
    @Autowired
    private CvRecruitmentDistanceRepository cvRecruitmentDistanceRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Transactional(rollbackFor={Exception.class,Throwable.class})
    public int saveEmployee(EmployeeSignupForm form){
        if(accountRepository.findAccountByEmail(form.getEmail()) != null){
            return -2;
        }
        Account account=new Account();
        account.setEmail(form.getEmail());
        account.setPassword(form.getPassword());
        account.setRole("ROLE_EMPLOYEE");
        Account savedAccount = userService.saveAccount(account);
        Employee employee=new Employee();
        employee.setFirstName(form.getFirstName());
        employee.setLastName(form.getLastName());
        employee.setGender(form.getGender());
        employee.setYearOfBirth(form.getYearOfBirth());
        employee.setAccount(savedAccount);
        employeeRepository.save(employee);
        return 1;
    }

    public Employee findEmployeeByAccount_Email(String email){
        Account account = accountRepository.findAccountByEmail(email);
        if(account == null)
            return null;
        Employee employee = employeeRepository.findEmployeeByAccount__id(account.get_id());
        return employee;
    }

    public Application apply(ApplicationForm form, Employee employee) throws IOException{
        Application application = new Application();
        //recruitment
        Recruitment recruitment = recruitmentRepository.findById(new ObjectId(form.getRecruitmentId())).get();
        application.setRecruitment(recruitment);
        //Cv
        Cv cv = cvRepository.findById(new ObjectId(form.getCvId())).get();
        application.setCv(cv);
        //History
        Mail mail= new Mail();
        mail.setHead(form.getHead());
        mail.setBody(form.getBody());
        mail.setPhase("ứng tuyển");
        mail.setStatus("processing");
        //attachment
        List<UserFile> attachments = new ArrayList<>();
        for(MultipartFile attachment : form.getAttachedFiles()){
            String path = appService.saveFile(attachment, appService.getAttachmentDir());
            UserFile userFile = new UserFile();
            userFile.setSource(path);
            userFile.setOriginalName(attachment.getOriginalFilename());
            attachments.add(userFile);
        }
        mail.setAttachments(attachments);
        application.setCurrentMail(mail);
        //save
        Application savedApplication = applicationRepository.save(application);
        //info employer
        Notification notification = new Notification();
        notification.setMessage(employee.getFirstName() + " " + employee.getLastName() +
                                " đã ứng tuyển " + recruitment.getName());
        notification.setTime(LocalDateTime.now());
        notification.setType("application");
        notification.setEmployer(recruitment.getEmployer());
        notification.setHostRole("ROLE_EMPLOYER");
        notification.setObjectId(application.get_id().toString());
        notificationRepository.save(notification);

        return savedApplication;
    }

    public List<Recruitment> findMostSuitableRecruitmentsByEmployee(Employee employee, Optional<Integer> limit){
        List<Cv> cvs = cvRepository.findCvsByEmployee(employee);
        Sort sort = Sort.by("distance").ascending();
        Pageable pageable = PageRequest.of(0,limit.orElse(200), sort);
        List<CvRecruitmentDistance> cvRecruitmentDistances = cvRecruitmentDistanceRepository.findCvRecruitmentDistancesByCvIn(cvs, pageable);
        List<Recruitment> recruitments = cvRecruitmentDistances.stream().map(cvRecruitmentDistance -> cvRecruitmentDistance.getRecruitment()).toList();
        return recruitments;
    }
}
