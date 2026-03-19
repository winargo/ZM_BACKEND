package com.billermanagement.services;

import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.Role;
import com.billermanagement.persistance.repository.RoleRepository;
import com.billermanagement.vo.RoleReqVO;
import com.billermanagement.vo.RoleResVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RoleService {

    @Autowired
    RoleRepository rRepo;

    Logger logger = LoggerFactory.getLogger(Role.class);

    public Role addRole(RoleReqVO vo){
        Role role = new Role();
        role.setName(vo.getName());
        String pages="";
        for(String page:vo.getPages()){
            pages=pages+page+"|";
        }
        role.setPages(pages);
        Role savedRole=rRepo.save(role);
        return savedRole;
    }

    public Role updateRole(RoleResVO vo){
        Role role = rRepo.findByRoleId(Integer.toString(vo.getId()));
        if(role==null){
            throw new NostraException("Role is not found", StatusCode.DATA_NOT_FOUND);
        }
        role.setName(vo.getName());
        String pages="";
        for(String page:vo.getPages()){
            pages=pages+page+"|";
        }
        role.setPages(pages);
        Role savedRole=rRepo.save(role);
        return savedRole;
    }

    public List<RoleResVO> getAllRole(){
        List<Role>roles=rRepo.findAll();
        List<RoleResVO> result= new ArrayList<>();
        for(Role role:roles) {
            String[] pages = role.getPages().split("\\|");
            RoleResVO vo = new RoleResVO();
            vo.setId(role.getId());
            vo.setName(role.getName());
            vo.setPages(pages);
            result.add(vo);
        }
        return result;
    }

    public RoleResVO getDetailRole(String id){
        Role role=rRepo.findByRoleId(id);
        if(role==null){
            throw new NostraException("Role is not found", StatusCode.DATA_NOT_FOUND);
        }
        RoleResVO vo= new RoleResVO();
        String[] pages = role.getPages().split("\\|");
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setPages(pages);
        return vo;
    }


}
