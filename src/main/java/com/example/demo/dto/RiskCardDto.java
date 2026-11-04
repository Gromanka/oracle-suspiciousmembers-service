package com.example.demo.dto;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RiskCardDto {

    private Long id;
    private Long cardId;

    private Long statOrgForm;
    private Long statRegCountry;
    private Long statSubjectType;
    private Long statOked;
    private Long statIinBin;
    private Long statIndustry;
    private Long statTelephone;
    private Long statEmail;

    private Long statAdrUrCountry;
    private Long statAdrUrCity;
    private Long statAdrUrStreet;
    private Long statAdrUrHome;
    private Long statAdrUrOffice;
    private Long statAdrUrPostIndex;

    private Long statAdrFactCountry;
    private Long statAdrFactCity;
    private Long statAdrFactStreet;
    private Long statAdrFactHome;
    private Long statAdrFactOffice;
    private Long statAdrFactPostIndex;

    private Long statDocType;
    private Long statDocNumber;
    private Long statDocIssuedBy;
    private Long statDocIssuedDate;

    private Long statRiskBall;

    private Long messagesFm;
    private Long messagesSusp;
    private Long messagesBall;

    private Long productBall;
    private Long expertSolution;
    private Long expertBall;

    private LocalDateTime expertDate;
    private String expertRemark;

    private Long summaryBall;
    private Long summaryRisk;
    private String summaryRemark;
    private LocalDateTime summaryDate;

    private Long statSuspiciousBool;
    private Long statIsFictCompany;
    private Long statAccountmoneysources;
    private Long statMainbusinesstype;
    private Long statMainincomesources;
    private Long statGovlicense;
    private Long statEmpBank;

    private String productRemark;
    private String messagesFmRemark;
    private String messagesSuspRemark;

    private String calcRemark;

    private Long manualRisk;
    private String expertUser;

    private Long serviceBall;
    private String serviceRemark;

    private Long founderCountryBall;
    private String founderCountryRemark;

    private Long statRegCitizen;

    private Long beneficiaryCountryBall;
    private String beneficiaryCountryRemark;

    private Long classification;
}