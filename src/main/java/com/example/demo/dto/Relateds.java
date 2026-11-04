package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Relateds {


    private Long id;
    private Long pSuspiciousmemberid;

    private Integer pMembertype;
    private Integer bsId;
    private Integer roleId;

    private String name;
    private String firstName;
    private String lastName;
    private String middleName;

    private String pFounderopf;
    private String iin;
    private String pBsclientid;

    private Integer pBankClient;

    private String pMemberrescountrycode;
    private String pMemberrescitizenship;

    private String pMembertaxcode;
    private String pMembermaincode;
    private String pMemberphone;
    private String pMemberemail;

    private String pMemberacdoctypecode;
    private String pMemberacdocseries;
    private String pMemberacdocnumber;
    private String pMemberacdocwhom;
    private LocalDateTime pMemberacdocissuedate;

    private LocalDateTime pMemberacbirthdate;
    private String pMemberacbirthplace;

    private String pBranch;
    private String pESector;
    private String pEmpBank;
    private String pUsername;
    private String pApprove;

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

    private Long regKatoId;
    private Long seatKatoId;
    private Long regAreaKatoId;
    private Long seatAreaKatoId;
    private Long regRegionKatoId;
    private Long seatRegionKatoId;
}
