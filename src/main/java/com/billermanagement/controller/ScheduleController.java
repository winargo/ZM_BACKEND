package com.billermanagement.controller;

import com.billermanagement.services.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ScheduleController {

    @Autowired
    private SchedulerService schedulerService;

    @RequestMapping(method = RequestMethod.GET, value="/scheduler")
    @ResponseBody
    public String findById(@RequestParam(value="transId") String transId, @RequestParam(value="handler") int handler) {
        schedulerService.processRequest(transId, handler);

        return "OK";
    }

}
