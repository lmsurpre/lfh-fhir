/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.ibm.fhir.persistence.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of search parameters extracted from a FHIR resource
 * held in a form that is easy to serialize/deserialize to a wire format
 * (e.g. JSON) for sending to a remote/async indexing service.
 * @implNote because we want to serialize/deserialize this object
 * as JSON, we need to keep it simple
 */
public class SearchParametersTransport {

    // The FHIR resource type name
    private String resourceType;

    // The logical id of the resource
    private String logicalId;

    // The database identifier assigned to this resource
    private long logicalResourceId;

    // The key value used for sharding the data when using a distributed database
    private Short shardKey;

    private List<StringParameter> stringValues;
    private List<NumberParameter> numberValues;
    private List<QuantityParameter> quantityValues;
    private List<TokenParameter> tokenValues;
    private List<DateParameter> dateValues;
    private List<LocationParameter> locationValues;

    /**
     * Factory method to create a {@link Builder} instance
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder to make it easier to construct a {@link SearchParametersTransport}
     */
    public static class Builder {
        private List<StringParameter> stringValues = new ArrayList<>();
        private List<NumberParameter> numberValues = new ArrayList<>();
        private List<QuantityParameter> quantityValues = new ArrayList<>();
        private List<TokenParameter> tokenValues = new ArrayList<>();
        private List<DateParameter> dateValues = new ArrayList<>();
        private List<LocationParameter> locationValues = new ArrayList<>();
    
        private String resourceType;
        private String logicalId;
        private long logicalResourceId = -1;
        private Short shardKey;

        /**
         * Set the resourceType
         * @param resourceType
         * @return
         */
        public Builder withResourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        /**
         * Set the logicalId
         * @param logicalId
         * @return
         */
        public Builder withLogicalId(String logicalId) {
            this.logicalId = logicalId;
            return this;
        }

        /**
         * Set the logicalResourceId
         * @param logicalResourceId
         * @return
         */
        public Builder withLogicalResourceId(long logicalResourceId) {
            this.logicalResourceId = logicalResourceId;
            return this;
        }

        /**
         * Set the shardKey
         * @param shardKey
         * @return
         */
        public Builder withShardKey(Short shardKey) {
            this.shardKey = shardKey;
            return this;
        }

        /**
         * Add a string parameter value
         * @param value
         * @return
         */
        public Builder addStringValue(StringParameter value) {
            stringValues.add(value);
            return this;
        }

        /**
         * Add a number parameter value
         * @param value
         * @return
         */
        public Builder addNumberValue(NumberParameter value) {
            numberValues.add(value);
            return this;
        }

        /**
         * Add a quantity parameter value
         * @param value
         * @return
         */
        public Builder addQuantityValue(QuantityParameter value) {
            quantityValues.add(value);
            return this;
        }

        /**
         * Add a token parameter value
         * @param value
         * @return
         */
        public Builder addTokenValue(TokenParameter value) {
            tokenValues.add(value);
            return this;
        }

        /**
         * Add a date parameter value
         * @param value
         * @return
         */
        public Builder addDateValue(DateParameter value) {
            dateValues.add(value);
            return this;
        }

        /**
         * Add a location parameter value
         * @param value
         * @return
         */
        public Builder addLocationValue(LocationParameter value) {
            locationValues.add(value);
            return this;
        }

        /**
         * Builder a new {@link SearchParametersTransport} instance based on the current state
         * of this {@link Builder}.
         * @return
         */
        public SearchParametersTransport build() {
            if (this.logicalResourceId < 0) {
                throw new IllegalStateException("Must set logicalResourceId");
            }
            if (this.resourceType == null) {
                throw new IllegalStateException("Must set resourceType");
            }
            if (this.logicalId == null) {
                throw new IllegalStateException("Must set logicalId");
            }

            SearchParametersTransport result = new SearchParametersTransport();
            result.resourceType = this.resourceType;
            result.logicalId = this.logicalId;
            result.logicalResourceId = this.logicalResourceId;
            result.shardKey = this.shardKey;

            if (this.stringValues.size() > 0) {
                result.stringValues = new ArrayList<>(this.stringValues);
            }
            if (this.numberValues.size() > 0) {
                result.numberValues = new ArrayList<>(this.numberValues);
            }
            if (this.quantityValues.size() > 0) {
                result.quantityValues = new ArrayList<>(this.quantityValues);
            }
            if (this.tokenValues.size() > 0) {
                result.tokenValues = new ArrayList<>(this.tokenValues);
            }
            if (this.dateValues.size() > 0) {
                result.dateValues = new ArrayList<>(this.dateValues);
            }
            if (this.locationValues.size() > 0) {
                result.locationValues = new ArrayList<>(this.locationValues);
            }
            return result;
        }
    }
    
    /**
     * @return the resourceType
     */
    public String getResourceType() {
        return resourceType;
    }

    
    /**
     * @param resourceType the resourceType to set
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    
    /**
     * @return the logicalId
     */
    public String getLogicalId() {
        return logicalId;
    }

    
    /**
     * @param logicalId the logicalId to set
     */
    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }

    
    /**
     * @return the logicalResourceId
     */
    public long getLogicalResourceId() {
        return logicalResourceId;
    }

    
    /**
     * @param logicalResourceId the logicalResourceId to set
     */
    public void setLogicalResourceId(long logicalResourceId) {
        this.logicalResourceId = logicalResourceId;
    }

    
    /**
     * @return the stringValues
     */
    public List<StringParameter> getStringValues() {
        return stringValues;
    }

    
    /**
     * @param stringValues the stringValues to set
     */
    public void setStringValues(List<StringParameter> stringValues) {
        this.stringValues = stringValues;
    }

    
    /**
     * @return the numberValues
     */
    public List<NumberParameter> getNumberValues() {
        return numberValues;
    }

    
    /**
     * @param numberValues the numberValues to set
     */
    public void setNumberValues(List<NumberParameter> numberValues) {
        this.numberValues = numberValues;
    }

    
    /**
     * @return the quantityValues
     */
    public List<QuantityParameter> getQuantityValues() {
        return quantityValues;
    }

    
    /**
     * @param quantityValues the quantityValues to set
     */
    public void setQuantityValues(List<QuantityParameter> quantityValues) {
        this.quantityValues = quantityValues;
    }

    
    /**
     * @return the tokenValues
     */
    public List<TokenParameter> getTokenValues() {
        return tokenValues;
    }

    
    /**
     * @param tokenValues the tokenValues to set
     */
    public void setTokenValues(List<TokenParameter> tokenValues) {
        this.tokenValues = tokenValues;
    }

    
    /**
     * @return the dateValues
     */
    public List<DateParameter> getDateValues() {
        return dateValues;
    }

    
    /**
     * @param dateValues the dateValues to set
     */
    public void setDateValues(List<DateParameter> dateValues) {
        this.dateValues = dateValues;
    }

    
    /**
     * @return the locationValues
     */
    public List<LocationParameter> getLocationValues() {
        return locationValues;
    }

    
    /**
     * @param locationValues the locationValues to set
     */
    public void setLocationValues(List<LocationParameter> locationValues) {
        this.locationValues = locationValues;
    }
}
