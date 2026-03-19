/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo;

import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class TransformResVO {

    private int id;
    private String transformId;
    private String method;
    private String name;
    private String type;
    private String url;
    private String flowType;
    private String fileRequest;
    private String fileResponse;
    private String fileCallback;
}
