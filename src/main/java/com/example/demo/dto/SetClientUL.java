package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SetClientUL extends SetClient {

    private String bin;
    private String registrationNumber;
    private String opf;

    private String oked;
    private String okpo;

    private String ipdlFio;
    private String ipdlRelationType;
    private String ipdlPost;

    private String firstRegDate;
    private String lastRegDate;
    private String lastStructureDocDate;

    private Integer isLicence;
    private Integer empCount;

    // Supervisor
    private String supervisorFullname;
    private String supervisorLastname;
    private String supervisorFirstname;
    private String supervisorMiddlename;
}
