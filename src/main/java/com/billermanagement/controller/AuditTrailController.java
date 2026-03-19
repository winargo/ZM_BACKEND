package com.billermanagement.controller;

import com.billermanagement.services.AuditTrailService;
import com.billermanagement.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditTrailController {

  @Autowired
  private AuditTrailService auditTrailService;

  @GetMapping(value = "/api/v1/trail/{ownerId}", produces = "application/json")
  public ResponseEntity<ResultVO> findNameByPrefix(@PathVariable(value = "ownerId") int ownerId) {
    AbstractRequestHandler handler = new AbstractRequestHandler() {
      @Override
      public Object processRequest() {
        return auditTrailService.findByOwnerId(ownerId);
      }
    };
    return handler.getResult();
  }
}
