package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Suspiciousmembers {
    private Long id;

    private String pClientid;
    private String pBsclientid;
    private String pMemberurname;
    private Integer pMembertype;
    private Integer pBankClient;
    private String pMemberrescountrycode;
    private String pMembertaxcode;
    private String pMemberoked;
    private String pMemberokpo;
    private String pMembermaincode;
    private String pMemberphone;
    private String pMemberemail;
    private String pMemberforeignextra;
    private String pMemberurfirstheadname;

    private LocalDateTime pMemberacbirthdate;
    private String pMemberacbirthplace;
    private Integer pMemberacdoctypecode;
    private String pMemberacdocseries;
    private String pMemberacdocnumber;
    private String pMemberacdocwhom;
    private LocalDateTime pMemberacdocissuedate;

    private String pMemberregfulladdress;
    private String pMemberregcountrycode;
    private String pMemberregarea;
    private String pMemberregregion;
    private String pMemberregcity;
    private String pMemberregstreet;
    private String pMemberreghouse;
    private String pMemberregoffice;
    private String pMemberregpostcode;

    private String pMemberseatfulladdress;
    private String pMemberseatcountrycode;
    private String pMemberseatarea;
    private String pMemberseatregion;
    private String pMemberseatcity;
    private String pMemberseatstreet;
    private String pMemberseathouse;
    private String pMemberseatoffice;
    private String pMemberseatpostcode;

    private String pBranch;
    private String pESector;

    private Long pEmpCount;
    private Long pEmpBank;
    private Integer pSuspiciousBool;

    private String pMembercomments;

    private LocalDateTime pDateInsert;
    private LocalDateTime pDateUpdate;

    private String pUsername;

    private LocalDateTime changedate;

    private String pHistory;

    private Integer pApprove;
    private String pAmountOfAccounts;
    private Integer pMemberRisk;
    private String pMemberaddinfo;
    private String pOrgform;

    private String pMemberurlastdirname;
    private String pMemberurfirstdirname;
    private String pMemberurmiddledirname;
    private String pMemberurlastname;
    private String pMemberurfirstname;
    private String pMemberurmiddlename;

    private Long regKatoId;
    private Long seatKatoId;
    private Long regAreaKatoId;
    private Long seatAreaKatoId;
    private Long regRegionKatoId;
    private Long seatRegionKatoId;

    private Long bsId;
    private Long version;
    private Long dx;

    private Long productStatus;
    private Long pClassKyc;
    private Long pIsTerr;
    private Long isInGrayList;
    private Long pIsIpdl;
    private Long pIpdlId;

    private Long clientCardClosed;
    private Long noCalcRisk;
    private Long pIssuedbid;

    private LocalDateTime kycCardAcceptDate;
    private LocalDateTime kycCardFutureUpdDate;

    private String pMemberrescitizenship;

    private Long privatebankclient;
    private Long tenCard;

    private LocalDateTime tenCardDate;
}
