/**
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.jdbc.dto;

import java.sql.Timestamp;

import com.ibm.watsonhealth.fhir.search.Parameter.Type;

/**
 * This class defines the Data Transfer Object representing a row in the FHIR Parameter table.
 * @author markd
 *
 */
public class Parameter {
    
    private long id;
    private long resourceId;
    private Type type;
    private String resourceType;
    private String name;
    private String valueString;
    private Timestamp valueDate;
    private Timestamp valueDateStart;
    private Timestamp valueDateEnd;
    private Double valueNumber;
    private Double valueNumberLow;
    private Double valueNumberHigh;
    private Double valueLongitude;
    private Double valueLatitude;
    private String valueSystem;
    private String valueCode;

    public Parameter() {
    	super();
    }

    public Timestamp getValueDateStart() {
        return valueDateStart;
    }

    public void setValueDateStart(Timestamp valueDateStart) {
        this.valueDateStart = valueDateStart;
    }

    public Timestamp getValueDateEnd() {
        return valueDateEnd;
    }

    public void setValueDateEnd(Timestamp valueDateEnd) {
        this.valueDateEnd = valueDateEnd;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Timestamp getValueDate() {
        return valueDate;
    }

    public void setValueDate(Timestamp valueDate) {
        this.valueDate = valueDate;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
    }

    public String getValueSystem() {
        return valueSystem;
    }

    public void setValueSystem(String valueSystem) {
        this.valueSystem = valueSystem;
    }

    public String getValueCode() {
        return valueCode;
    }

    public void setValueCode(String valueCode) {
        this.valueCode = valueCode;
    }

    public Double getValueNumberLow() {
        return valueNumberLow;
    }

    public void setValueNumberLow(Double valueNumberLow) {
        this.valueNumberLow = valueNumberLow;
    }

    public Double getValueNumberHigh() {
        return valueNumberHigh;
    }

    public void setValueNumberHigh(Double valueNumberHigh) {
        this.valueNumberHigh = valueNumberHigh;
    }

    public Double getValueLongitude() {
        return valueLongitude;
    }

    public void setValueLongitude(Double valueLongitude) {
        this.valueLongitude = valueLongitude;
    }

    public Double getValueLatitude() {
        return valueLatitude;
    }

    public void setValueLatitude(Double valueLatitude) {
        this.valueLatitude = valueLatitude;
    }

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
