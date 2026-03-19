package com.billermanagement.controller;

import com.billermanagement.vo.LoginReqVO;
import com.billermanagement.vo.UserReqVO;
import com.billermanagement.vo.ResultVO;
import com.billermanagement.vo.UserResVO;
import com.billermanagement.persistance.domain.User;
import com.billermanagement.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/User")
public class UserController {
    @Autowired
    private UserService service;

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Get")
    @ResponseBody
    public ResponseEntity<ResultVO> findAll() {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getUser();
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/GetDetail")
    @ResponseBody
    public ResponseEntity<ResultVO> findDetail(@RequestParam(value="id", required = true) String id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getUserDetail(id);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE,
        value="/GetByEmail")
    @ResponseBody
    public ResponseEntity<ResultVO> findByEmail(@RequestParam(value="email", required = true) String email) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getUserByEmail(email);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Add")
    @ResponseBody
    public ResponseEntity<ResultVO> add(@RequestBody final UserReqVO vo) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.add(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Validate")
    @ResponseBody
    public ResponseEntity<ResultVO> validate(@RequestBody final LoginReqVO vo) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.validateUser(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Update")
    @ResponseBody
    public ResponseEntity<ResultVO> udpate(@RequestBody final UserReqVO vo) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.update(vo);
            }
        };
        return handler.getResult();
    }

//    @PostMapping(value = "/users", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
//    public ResponseEntity saveUsers(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
//        for (MultipartFile file : files) {
//            service.saveUsers(file);
//        }
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
//
//    @GetMapping(value = "/users", produces = "application/json")
//    public CompletableFuture<ResponseEntity> findAllUsers() {
//        return  service.findAllUsers().thenApply(ResponseEntity::ok);
//    }
//
//
//    @GetMapping(value = "/getUsersByThread", produces = "application/json")
//    public  ResponseEntity getUsers(){
//        CompletableFuture<List<User>> users1=service.findAllUsers();
//        CompletableFuture<List<User>> users2=service.findAllUsers();
//        CompletableFuture<List<User>> users3=service.findAllUsers();
//        CompletableFuture.allOf(users1,users2,users3).join();
//        return  ResponseEntity.status(HttpStatus.OK).build();
//    }
}
