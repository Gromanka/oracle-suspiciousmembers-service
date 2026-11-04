package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class SetClient {

    protected String fullName;
    protected String shortName;
    protected String transnationalName;

    protected String firstName;
    protected String middleName;
    protected String lastName;

    protected Integer gender;
    protected Integer bankClient;

    protected String resCountry;
    protected Integer isResident;
    protected Integer economySector;

    protected String taxNumber;

    protected Integer isIpdl;

    protected String birthDate;
    protected String birthCountry;
    protected String birthPlace;

    // Document
    protected String docType;
    protected String docTypeId;
    protected String docNumber;
    protected String docSeries;
    protected String docIssuedBy;
    protected String docIssuedDate;
    protected String docExpireDate;

    protected String phoneNumber;
    protected String email;

    protected String branch;

    protected Integer suspLevel;
    protected Integer isVip;
    protected Integer isArchive;

    protected Long bsId;
    protected String bsClientId;

    protected String bsDateInsert;
    protected String bsDateUpdate;

    protected Integer isFinInst;

    // Registration address
    protected String regCountry;
    protected String regArea;
    protected Long regAreaId;
    protected String regRegion;
    protected Long regRegionId;
    protected String regCity;
    protected Long regCityId;
    protected String regStreet;
    protected Long regStreetId;
    protected String regHouse;
    protected String regOffice;
    protected String regPostCode;
    protected String regAddrStr;

    // Seat address
    protected String seatCountry;
    protected String seatArea;
    protected Long seatAreaId;
    protected String seatRegion;
    protected Long seatRegionId;
    protected String seatCity;
    protected Long seatCityId;
    protected String seatStreet;
    protected Long seatStreetId;
    protected String seatHouse;
    protected String seatOffice;
    protected String seatPostCode;
    protected String seatAddrStr;

    protected Integer profileUpdated;

    protected String userName;
    protected Integer roleId;
    protected Long linkedClientId;

    protected Integer formType;
    protected Integer status;

    protected Long id;

    protected String taxCountry;
    protected Integer isResCert;

    protected Integer isLsbo;

    protected String numberSD;
    protected String dateDischarge;

    protected String authorityNumber;
    protected String authorityDate;
    protected String authorityValDate;

    protected String managerCreated;
    protected String userComments;

    protected String citizenship;
    protected String clientsRefId;

    protected Integer relativePdl;
    protected Integer civilServant;

    protected Integer expertBall2;
    protected String expertDate3;
    protected String expertRemark;
}