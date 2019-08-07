/**
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.server.test;

import static com.ibm.watsonhealth.fhir.model.type.String.string;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotSame;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.watsonhealth.fhir.model.format.Format;
import com.ibm.watsonhealth.fhir.model.generator.FHIRGenerator;
import com.ibm.watsonhealth.fhir.model.generator.exception.FHIRGeneratorException;
import com.ibm.watsonhealth.fhir.model.resource.Patient;
import com.ibm.watsonhealth.fhir.model.type.Boolean;
import com.ibm.watsonhealth.fhir.model.type.Date;
import com.ibm.watsonhealth.fhir.model.type.Extension;
import com.ibm.watsonhealth.fhir.model.type.HumanName;
import com.ibm.watsonhealth.fhir.model.type.Id;
import com.ibm.watsonhealth.fhir.model.type.Instant;
import com.ibm.watsonhealth.fhir.model.type.Integer;
import com.ibm.watsonhealth.fhir.model.type.Meta;
import com.ibm.watsonhealth.fhir.model.type.Reference;
import com.ibm.watsonhealth.fhir.model.type.String;
import com.ibm.watsonhealth.fhir.server.resources.FHIRResource;
import com.ibm.watsonhealth.fhir.server.util.ReferenceMappingVisitor;

public class ReferenceMappingVistorTest {
    private Patient patient;
    private HashMap<java.lang.String , java.lang.String> localRefMap;
    @BeforeClass
    public void setUp() throws Exception {
        Id id = Id.builder().value(UUID.randomUUID().toString())
                .extension(Extension.builder()
                    .url("http://www.ibm.com/someExtension")
                    .value(string("Hello, World!"))
                    .build())
                .build();
        
        Meta meta = Meta.builder().versionId(Id.of("1"))
                .lastUpdated(Instant.now(ZoneOffset.UTC))
                .build();
        
        String given = String.builder().value("John")
                .extension(Extension.builder()
                    .url("http://www.ibm.com/someExtension")    
                    .value(String.of("value and extension"))
                    .build())
                .build();
        
        String otherGiven = String.builder()
                .extension(Extension.builder()
                    .url("http://www.ibm.com/someExtension")    
                    .value(String.of("extension only"))
                    .build())
                .build();
        
        HumanName name = HumanName.builder()
                .id("someId")
                .given(given)
                .given(otherGiven)
                .given(String.of("value no extension"))
                .family(String.of("Doe"))
                .build();
        
        java.lang.String uUID = UUID.randomUUID().toString();
                
        localRefMap = new HashMap<java.lang.String, java.lang.String>();        
        localRefMap.put("urn:uuid:" + uUID, "romote:" + uUID);
        
        Reference providerRef = Reference.builder()
                .reference(String.of("urn:uuid:" + uUID))
                .build();
                
        patient = Patient.builder()
                .id(id)
                .active(Boolean.TRUE)
                .multipleBirth(Integer.of(2))
                .meta(meta)
                .name(name)
                .birthDate(Date.of(LocalDate.now()))
                .generalPractitioner(providerRef)
                .build();   
    }
    
    
    @Test(enabled=true) 
    public void testUpdateReferences() throws FHIRGeneratorException {
        ReferenceMappingVisitor<Patient> visitor = new ReferenceMappingVisitor<Patient>(localRefMap);
        patient.accept(visitor);
        Patient result = visitor.getResult();

        assertNotSame(result, patient);

        StringWriter writer1 = new StringWriter();
        FHIRGenerator.generator(Format.JSON, true).generate(patient, writer1);
        StringWriter writer2 = new StringWriter();
        FHIRGenerator.generator(Format.JSON, true).generate(result, writer2);
        assertNotEquals(writer2.toString(), writer1.toString());

        System.out.println(writer1.toString());
        System.out.println(writer2.toString());

        assertNotEquals(result, patient);
    }
    
    
    @Test(enabled=true) 
    public void testMeta() throws Exception {
        boolean isTestGood = true;
        FHIRResource fhirResource = new FHIRResource();
        try {
            Response Meta = fhirResource.metadata();
        }catch (Exception ex) {
            isTestGood = false;
        }
        
        assertTrue(isTestGood);  
    }
}
