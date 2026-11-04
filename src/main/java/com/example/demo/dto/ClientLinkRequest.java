package com.example.demo.dto;

import lombok.Data;

@Data
public class ClientLinkRequest {
    private Long clientId;
    private Integer roleId;
    private Long linkedClientId;
    private Integer linkStatus;
}
