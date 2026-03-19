package com.billermanagement.services;

import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.Role;
import com.billermanagement.persistance.domain.User;
import com.billermanagement.persistance.repository.RoleRepository;
import com.billermanagement.persistance.repository.UserRepository;
import com.billermanagement.vo.LoginReqVO;
import com.billermanagement.vo.UserReqVO;
import com.billermanagement.vo.UserResVO;
import com.billermanagement.vo.UserVO;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.List;
//import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    //@Autowired
    //EmailUtil emailUtil;

    //Logger logger = LoggerFactory.getLogger(UserService.class);

//    @Autowired
//    private JavaMailSender javaMailSender;
//
//    void sendEmail(String to, String subject, String text) {
//
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo(to);
//
//        msg.setSubject(subject);
//        msg.setText(text);
//
//        javaMailSender.send(msg);
//
//    }

    public UserResVO add(UserReqVO vo){
        User existing=repository.findByUsername(vo.getUsername());
        if(existing!=null){
            throw new NostraException("Username already used. Please user another username",StatusCode.ERROR);
        }
        User user = new User();
        user.setUsername(vo.getUsername());
        user.setEmail(vo.getEmail());
        user.setRoles(vo.getRole());
        User savedUser=repository.save(user);
        UserResVO result=new UserResVO();
        result.setId(savedUser.getId().toString());
        result.setUsername(savedUser.getUsername());
        result.setEmail(savedUser.getEmail());
        Role role = roleRepository.findByRoleId(vo.getRole());
        result.setRole(role);

        return result;
    }

    public UserResVO update(UserReqVO vo){
        User user = repository.findByUserId(vo.getId());
        if(user==null){
            throw new NostraException("User not found", StatusCode.DATA_NOT_FOUND);
        }
        user.setRoles(vo.getRole());
        User savedUser=repository.save(user);

        UserResVO result=new UserResVO();
        result.setId(savedUser.getId().toString());
        result.setUsername(savedUser.getUsername());
        result.setEmail(savedUser.getEmail());
        Role role = roleRepository.findByRoleId(vo.getRole());
        result.setRole(role);

        return result;
    }

    public List<UserResVO> getUser(){
        List<User> users=repository.findAll();
        List<UserResVO> result=new ArrayList<>();

        for(User user:users){
            UserResVO vo = new UserResVO();
            vo.setId(user.getId().toString());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            Role role = roleRepository.findByRoleId(user.getRoles());
            vo.setRole(role);
            result.add(vo);
        }
        return result;
    }

    public UserResVO getUserDetail(String id){
        User user=repository.findByUserId(id);
        if(user==null){
            throw new NostraException("User not found", StatusCode.DATA_NOT_FOUND);
        }
        UserResVO result=new UserResVO();

        result.setId(user.getId().toString());
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());
        Role role = roleRepository.findByRoleId(user.getRoles());
        result.setRole(role);

        return result;
    }

    public UserResVO getUserByEmail(String email) {
        User user=repository.findByEmail(email);
        if(user==null){
            throw new NostraException("User not found", StatusCode.DATA_NOT_FOUND);
        }
        UserResVO result=new UserResVO();

        result.setId(user.getId().toString());
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());
        Role role = roleRepository.findByRoleId(user.getRoles());
        result.setRole(role);

        return result;
    }

    public UserVO validateUser(LoginReqVO vo){
        User user=repository.findByEmail(vo.getEmail());
        if(user==null){
            throw new NostraException("Invalid Email",StatusCode.ERROR);
        }else{
            /*byte[] decodedBytes = Base64.getDecoder().decode(user.getPassword());
            String password = new String(decodedBytes);
            if(!password.equals(vo.getPassword())){
                throw new NostraException("Invalid Password",StatusCode.ERROR);
            }else {*/
                UserVO userVO = new UserVO();
                userVO.setUsername(user.getUsername());
                userVO.setEmail(user.getEmail());
                userVO.setRoles(user.getRoles());
                //userVO.setToken(Base64.getEncoder().encodeToString((user.getEmail()+":"+password).getBytes()));
                return userVO;
            //}
        }
    }

    /*public boolean validateToken(String authorization) {
        try {
            String decoded = new String(Base64.getDecoder().decode(authorization));

            String[] parts = decoded.split(":");
            String email = parts[0];
            String pass = parts[1];

            User user=repository.findByEmail(email);
            if(user==null){
                return false;
            }else {
                byte[] decodedBytes = Base64.getDecoder().decode(user.getPassword());
                String password = new String(decodedBytes);
                return password.equals(pass);
            }
        } catch (Exception e) {
            return false;
        }
    }*/

//    @Async
//    public CompletableFuture<List<User>> saveUsers(MultipartFile file) throws Exception {
//        long start = System.currentTimeMillis();
//        List<User> users = parseCSVFile(file);
//        logger.info("saving list of users of size {}", users.size(), "" + Thread.currentThread().getName());
//        users = repository.saveAll(users);
//        long end = System.currentTimeMillis();
//        logger.info("Total time {}", (end - start),"ms");
//        return CompletableFuture.completedFuture(users);
//    }
//    @Async
//    public CompletableFuture<List<User>> findAllUsers(){
//        logger.info("get list of user by "+Thread.currentThread().getName());
//        List<User> users=repository.findAll();
//        return CompletableFuture.completedFuture(users);
//    }
//
//    private List<User> parseCSVFile(final MultipartFile file) throws Exception {
//        final List<User> users = new ArrayList<>();
//        try {
//            try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    final String[] data = line.split(",");
//                    final User user = new User();
//                    user.setName(data[0]);
//                    user.setEmail(data[1]);
//                    user.setGender(data[2]);
//                    users.add(user);
//                }
//                return users;
//            }
//        } catch (final IOException e) {
//            logger.error("Failed to parse CSV file {}", e);
//            throw new Exception("Failed to parse CSV file {}", e);
//        }
//    }

}
