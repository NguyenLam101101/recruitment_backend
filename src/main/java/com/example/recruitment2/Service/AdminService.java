package com.example.recruitment2.Service;

import com.example.recruitment2.Entity.Account;
import com.example.recruitment2.Entity.Administrator;
import com.example.recruitment2.Repository.AccountRepository;
import com.example.recruitment2.Repository.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AdministratorRepository administratorRepository;
    public Administrator findAdminByAccount_Email(String email){
        Account account = accountRepository.findAccountByEmail(email);
        Administrator administrator = administratorRepository.findAdministratorByAccount__id(account.get_id());
        return administrator;
    }
}
