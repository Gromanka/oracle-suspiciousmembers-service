package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OffMember {

    private Long id;
    private Long pOperationid;

    private String pClientid;
    private String pBsclientid;
    private String pName;

    private Integer pBankClient;
    private LocalDateTime pRegopendate;
    private String pCountrycode;
    private Integer pClientType;
    private Integer pClientrole;
    private Long pClientkind;

    private String pAccount;
    private String pBank;
    private String pBankcountrycode;

    private String pBankname;
    private Integer pIpdl;

    private LocalDateTime pDateInsert;
    private LocalDateTime pDateUpdate;

    private String pUsername;
    private String pHistory;

    private LocalDateTime changedate;

    private String pBsaccount;
    private String pLastname;
    private String pFirstname;
    private String pMiddlename;
    private String pSdp;
    private String pOrgform;
    private String pBankcity;
    private String pMembertypeform;
    private String pBranch;

    private LocalDateTime pOpertime;
    private LocalDateTime pAccountopendate;

    private String pDocNumber;
    private String pFpOffshore;
    private String pFpBankOffshore;
    private String pTpOffshore;
    private String pTpBankOffshore;

    private Long kbe;
    private String offshoreZone;

    private LocalDateTime pOperationdate;
    private String pCitizenship;
    private Long pPdl;
    private String pSubaccount;
}