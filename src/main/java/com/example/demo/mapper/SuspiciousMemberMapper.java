package com.example.demo.mapper;

import com.example.demo.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
public class SuspiciousMemberMapper {

    private final CountryMapper countryMapper;

    // =========================================================
    // UL
    // =========================================================
    public SetClientUL toUlRequest(Suspiciousmembers source) {
        if (source == null) {
            return null;
        }


        SetClientUL target = new SetClientUL();

        mapCommonFields(source, target);

        target.setOked(source.getPMemberoked());
        target.setOkpo(source.getPMemberokpo());
        target.setEconomySector(toInteger(source.getPESector()));
        target.setEmpCount(toInteger(source.getPEmpCount()));

        target.setIsIpdl(toInteger(source.getPIsIpdl()));
        target.setBin(Objects.toString(source.getPMembermaincode(), ""));
        target.setOpf(source.getPOrgform());

        target.setIsResident(0);
        target.setIsVip(0);

        target.setShortName("");
        target.setTransnationalName("");

        target.setRegistrationNumber("");

        target.setIpdlFio("");
        target.setIpdlRelationType("");
        target.setIpdlPost("");

        target.setFirstRegDate("");
        target.setLastRegDate("");
        target.setLastStructureDocDate("");

        target.setSupervisorFullname(Objects.requireNonNullElse(source.getPMemberurfirstheadname(), ""));
        target.setSupervisorLastname(Objects.requireNonNullElse(source.getPMemberurlastdirname(), ""));
        target.setSupervisorFirstname(Objects.requireNonNullElse(source.getPMemberurfirstdirname(), ""));
        target.setSupervisorMiddlename(Objects.requireNonNullElse(source.getPMemberurmiddledirname(), ""));

        target.setExpertBall2(0);
        target.setExpertDate3("");
        target.setExpertRemark("");

        return target;
    }


    // =========================================================
    // FL
    // =========================================================

    public SetClientFL toFlRequest(Suspiciousmembers source) {
        if (source == null) {
            return null;
        }

        SetClientFL target = new SetClientFL();

        mapCommonFields(source, target);
        target.setLastName(Objects.requireNonNullElse(source.getPMemberurlastname(), ""));
        target.setFirstName(Objects.requireNonNullElse(source.getPMemberurfirstname(), ""));
        target.setMiddleName(Objects.requireNonNullElse(source.getPMemberurmiddlename(), ""));

        // Personal data
        target.setBirthDate(toString(source.getPMemberacbirthdate()));
        target.setBirthPlace(source.getPMemberacbirthplace());

        target.setDocTypeId(String.valueOf(source.getPMemberacdoctypecode()));
        target.setDocSeries(source.getPMemberacdocseries());
        target.setDocNumber(source.getPMemberacdocnumber());
        target.setDocIssuedBy(source.getPMemberacdocwhom());
        target.setDocIssuedDate(toString(source.getPMemberacdocissuedate()));

        // IPDL
        target.setIsIpdl(toInteger(source.getPIsIpdl()));
        target.setIpdlFio(source.getPMemberurfirstheadname());

        // Default values
        target.setShortName("");
        target.setTransnationalName("");

        target.setGender(0);
        target.setIsResident(0);
        target.setEconomySector(0);

        target.setIin(Objects.toString(source.getPMembermaincode(), ""));

        target.setIpdlRelationType("");
        target.setIpdlPost("");
        target.setIpdlComment("");

        //target.setBirthCountry(source.getPMemberacbirthplace());
        target.setBirthCountry("");
        target.setCitizenship(countryMapper.getCountryCode2(source.getPMemberrescitizenship()));

        target.setPdl(0);

        target.setDocType("");
        target.setDocExpireDate("");

        target.setIsBankEmp(0);

        // Profit / employment
        target.setProfitOrgSphere("");
        target.setProfitOrgName("");
        target.setProfitOrgPost("");
        target.setProfitPeriod("");
        target.setProfitIncomeFrom(0L);
        target.setProfitIncomeTo(0L);

        target.setIsVip(0);

        // IPDL decision
        target.setIpdlSolveDateFrom("");
        target.setIpdlSolveDateTo("");
        target.setIpdlSolveNumber("");

        target.setSharePercent(0L);

        // Risk
        target.setIsFinInst(0);
        target.setHasUncertainty(0);
        target.setHasEvasion(0);
        target.setHasAddRisk(0);

        target.setRegStreetId(0L);
        target.setSeatStreetId(0L);

        target.setConfirmDocType(source.getPMemberacdoctypecode());
        target.setConfirmDocNumber(source.getPMemberacdocnumber());
        target.setConfirmDocDate("");
        target.setConfirmDocVal("");

        target.setRelativePdl(0);
        target.setCivilServant(0);

        target.setExpertDate3("");
        target.setExpertRemark("");

        return target;
    }


    // =========================================================
    // IP
    // =========================================================

    public SetClientIP toIPRequest(Suspiciousmembers source) {
        if (source == null) {
            return null;
        }

        SetClientIP target = new SetClientIP();

        mapCommonFields(source, target);
        target.setLastName(Objects.requireNonNullElse(source.getPMemberurlastname(), ""));
        target.setFirstName(Objects.requireNonNullElse(source.getPMemberurfirstname(), ""));
        target.setMiddleName(Objects.requireNonNullElse(source.getPMemberurmiddlename(), ""));

        target.setOked(source.getPMemberoked());

        // Personal data
        target.setBirthDate(toString(source.getPMemberacbirthdate()));
        target.setBirthPlace(source.getPMemberacbirthplace());

        target.setDocTypeId(String.valueOf(source.getPMemberacdoctypecode()));
        target.setDocSeries(source.getPMemberacdocseries());
        target.setDocNumber(source.getPMemberacdocnumber());
        target.setDocIssuedBy(source.getPMemberacdocwhom());
        target.setDocIssuedDate(toString(source.getPMemberacdocissuedate()));

        // IPDL
        target.setIsIpdl(toInteger(source.getPIsIpdl()));

        // Default values
        target.setTransnationalName("");
        target.setShortName("");

        target.setIsResident(0);
        //target.setBin(source.getPMembermaincode());
        target.setBin(Objects.toString(source.getPMembermaincode(), ""));

        target.setKbe(0);
        target.setGender(0);
        target.setEconomySector(0);

        target.setBirthCountry("");

        target.setDocType("");
        target.setDocExpireDate("");

        target.setJobPlace("");
        target.setJobName("");

        target.setProfileUpdated(0);

        target.setFormType(0);

        target.setStatus(0);

        target.setRegStreetId(0L);
        target.setSeatStreetId(0L);

        // Actual address
        target.setActCountry("");
        target.setActArea("");
        target.setActAreaId(0L);
        target.setActRegion("");
        target.setActRegionId(0L);
        target.setActCity("");
        target.setActCityId(0L);
        target.setActStreet("");
        target.setActStreetId(0L);
        target.setActHouse("");
        target.setActOffice("");
        target.setActPostCode("");
        target.setActAddrStr("");

        // Registration
        target.setRegDocNumber("");
        target.setFirstRegDate("");
        target.setLastStructureDocDate("");
        target.setRegAuthority("");

        target.setWorkersAmount(0L);
        target.setRating("");

        target.setIsLsbo(0);

        target.setNumberSD("");
        target.setDateDischarge("");

        target.setAuthorityNumber("");
        target.setAuthorityDate("");
        target.setAuthorityValDate("");

        target.setTaxCountry("");
        target.setIsResCert(0);

        target.setKrp("");
        target.setLicActivityType("");

        target.setOwnershipType("");
        target.setActsOn("");

        target.setAuthCapital(0L);

        target.setCitizenship(countryMapper.getCountryCode2(source.getPMemberrescitizenship()));

        target.setRelativePdl(0);
        target.setCivilServant(0);

        target.setClientRiskBs(0);

        target.setExpertDate3("");
        target.setExpertRemark("");

        return target;
    }


    // =========================================================
    // Common fields
    // =========================================================

    private void mapCommonFields(
            Suspiciousmembers source,
            SetClient target
    ) {

        target.setId(0L);

        target.setBsId(source.getBsId());
        target.setBsClientId(source.getPBsclientid());

        target.setFullName(source.getPMemberurname());

        target.setBankClient(source.getPBankClient());
        target.setResCountry(countryMapper.getCountryCode2(source.getPMemberrescountrycode()));
        target.setTaxNumber(source.getPMembertaxcode());

        target.setBranch(source.getPBranch());

        target.setPhoneNumber(source.getPMemberphone());
        target.setEmail(source.getPMemberemail());

        target.setUserComments(source.getPMembercomments());

        target.setBsDateInsert(toString(source.getPDateInsert()));
        target.setBsDateUpdate(toString(source.getPDateUpdate()));

        target.setUserName(source.getPUsername());

        target.setIsArchive(
                source.getClientCardClosed() != null
                        ? source.getClientCardClosed().intValue()
                        : 0
        );

        target.setSuspLevel(source.getPSuspiciousBool());

        target.setProfileUpdated(0);

        target.setStatus(source.getPApprove());

        target.setTaxCountry("");
        target.setIsResCert(0);

        target.setIsLsbo(
                source.getPClassKyc() != null
                        ? source.getPClassKyc().intValue()
                        : 0
        );

        target.setNumberSD("");
        target.setDateDischarge("");

        target.setManagerCreated(source.getPUsername());
        target.setClientsRefId(source.getPClientid());

        mapAddresses(source, target);
    }


    // =========================================================
    // Addresses
    // =========================================================

    private void mapAddresses(
            Suspiciousmembers source,
            SetClient target
    ) {

        // Registration address

        target.setRegCountry(countryMapper.getCountryCode2(source.getPMemberregcountrycode()));
        target.setRegArea(source.getPMemberregarea());
        target.setRegRegion(source.getPMemberregregion());
        target.setRegCity(source.getPMemberregcity());
        target.setRegStreet(source.getPMemberregstreet());
        target.setRegHouse(source.getPMemberreghouse());
        target.setRegOffice(source.getPMemberregoffice());
        target.setRegPostCode(source.getPMemberregpostcode());
        target.setRegAddrStr(source.getPMemberregfulladdress());

        target.setRegAreaId(source.getRegAreaKatoId());
        target.setRegRegionId(source.getRegRegionKatoId());
        target.setRegCityId(source.getRegKatoId());

        // Seat address

        target.setSeatCountry(countryMapper.getCountryCode2(source.getPMemberseatcountrycode()));
        target.setSeatArea(source.getPMemberseatarea());
        target.setSeatRegion(source.getPMemberseatregion());
        target.setSeatCity(source.getPMemberseatcity());
        target.setSeatStreet(source.getPMemberseatstreet());
        target.setSeatHouse(source.getPMemberseathouse());
        target.setSeatOffice(source.getPMemberseatoffice());
        target.setSeatPostCode(source.getPMemberseatpostcode());
        target.setSeatAddrStr(source.getPMemberseatfulladdress());

        target.setSeatAreaId(source.getSeatAreaKatoId());
        target.setSeatRegionId(source.getSeatRegionKatoId());
        target.setSeatCityId(source.getSeatKatoId());
    }


    // =========================================================
    // FIO
    // =========================================================





    // =========================================================
    // Converters
    // =========================================================

    private static Integer toInteger(Long value) {
        return value != null
                ? value.intValue()
                : null;
    }
    private static Integer toInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toString(LocalDateTime date) {
        if (date == null) {
            return null;
        }

        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }


}