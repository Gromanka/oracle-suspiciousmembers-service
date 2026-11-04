package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SetClientIP extends SetClient {

    private String bin;

    private Integer kbe;

    private String jobPlace;
    private String jobName;

    // Actual address
    private String actCountry;
    private String actArea;
    private Long actAreaId;
    private String actRegion;
    private Long actRegionId;
    private String actCity;
    private Long actCityId;
    private String actStreet;
    private Long actStreetId;
    private String actHouse;
    private String actOffice;
    private String actPostCode;
    private String actAddrStr;

    // Registration
    private String regDocNumber;
    private String firstRegDate;
    private String lastStructureDocDate;
    private String regAuthority;

    private Long workersAmount;

    private String rating;
    private String oked;

    private String krp;

    private Integer isLicence;
    private String licActivityType;

    private String ownershipType;
    private String actsOn;

    private Long authCapital;

    private Integer empBank;
    private Integer clientRiskBs;
}
