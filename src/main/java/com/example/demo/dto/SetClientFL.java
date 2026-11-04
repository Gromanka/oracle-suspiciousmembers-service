package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SetClientFL extends SetClient{

    private String iin;

    private String ipdlFio;
    private String ipdlRelationType;
    private String ipdlPost;
    private String ipdlComment;

    private Integer pdl;

    private Integer isBankEmp;

    // Profit / employment
    private String profitOrgSphere;
    private String profitOrgName;
    private String profitOrgPost;
    private String profitPeriod;

    private Long profitIncomeFrom;
    private Long profitIncomeTo;

    // IPDL decision
    private String ipdlSolveDateFrom;
    private String ipdlSolveDateTo;
    private String ipdlSolveNumber;

    private Long sharePercent;

    // Risk
    private Integer hasUncertainty;
    private Integer hasEvasion;
    private Integer hasAddRisk;

    // Confirm document
    private Integer confirmDocType;
    private String confirmDocNumber;
    private String confirmDocDate;
    private String confirmDocVal;
}
