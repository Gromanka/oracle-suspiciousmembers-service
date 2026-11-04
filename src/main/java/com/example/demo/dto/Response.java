package com.example.demo.dto;

import lombok.Data;

@Data
public class Response {

    private Long inId;
    private String errorMessage;
    private Integer errorCode;
    private String action;

}
