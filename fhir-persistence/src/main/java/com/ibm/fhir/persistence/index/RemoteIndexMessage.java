/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.ibm.fhir.persistence.index;

/**
 * The Kafka message we send to the remote index service
 */
public class RemoteIndexMessage {
    private String tenantId;
    private SearchParametersTransport data;
    
    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }
    
    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    /**
     * @return the data
     */
    public SearchParametersTransport getData() {
        return data;
    }
    
    /**
     * @param data the data to set
     */
    public void setData(SearchParametersTransport data) {
        this.data = data;
    }
    
}
