/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.path.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.linuxforhealth.fhir.model.resource.Patient;
import org.linuxforhealth.fhir.path.ClassInfo;
import org.linuxforhealth.fhir.path.ClassInfoElement;
import org.linuxforhealth.fhir.path.FHIRPathType;
import org.linuxforhealth.fhir.path.SimpleTypeInfo;
import org.linuxforhealth.fhir.path.TupleTypeInfo;
import org.linuxforhealth.fhir.path.TupleTypeInfoElement;
import org.linuxforhealth.fhir.path.TypeInfo;
import org.linuxforhealth.fhir.path.util.FHIRPathUtil;

public class TypeInfoTest {
    @Test
    public void testSimpleTypeInfo() {
        TypeInfo actual = FHIRPathUtil.buildSimpleTypeInfo(FHIRPathType.SYSTEM_STRING);
        TypeInfo expected = new SimpleTypeInfo("System", "String", "System.Any");
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testClassInfo() {
        TypeInfo actual = FHIRPathUtil.buildClassInfo(FHIRPathType.FHIR_ADDRESS);
        List<ClassInfoElement> element = new ArrayList<>();
        element.add(new ClassInfoElement("use", "code", true));
        element.add(new ClassInfoElement("type", "code", true));
        element.add(new ClassInfoElement("text", "FHIR.string", true));
        element.add(new ClassInfoElement("line", "FHIR.string", false));
        element.add(new ClassInfoElement("city", "FHIR.string", true));
        element.add(new ClassInfoElement("district", "FHIR.string", true));
        element.add(new ClassInfoElement("state", "FHIR.string", true));
        element.add(new ClassInfoElement("postalCode", "FHIR.string", true));
        element.add(new ClassInfoElement("country", "FHIR.string", true));
        element.add(new ClassInfoElement("period", "Period", true));
        TypeInfo expected = new ClassInfo("FHIR", "Address", "FHIR.Element", element);
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testTupleTypeInfo() {
        TypeInfo actual = FHIRPathUtil.buildTupleTypeInfo(Patient.Contact.class);
        List<TupleTypeInfoElement> element = new ArrayList<>();
        element.add(new TupleTypeInfoElement("relationship", "FHIR.CodeableConcept", false));
        element.add(new TupleTypeInfoElement("name", "FHIR.HumanName", true));
        element.add(new TupleTypeInfoElement("telecom", "FHIR.ContactPoint", false));
        element.add(new TupleTypeInfoElement("address", "FHIR.Address", true));
        element.add(new TupleTypeInfoElement("gender", "FHIR.code", true));
        element.add(new TupleTypeInfoElement("organization", "FHIR.Reference", true));
        element.add(new TupleTypeInfoElement("period", "FHIR.Period", true));
        TypeInfo expected = new TupleTypeInfo(element);
        Assert.assertEquals(actual, expected);
    }
}