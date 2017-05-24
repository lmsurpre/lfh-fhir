/**
 * (C) Copyright IBM Corp. 2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.replication.api;

import com.ibm.watsonhealth.fhir.exception.FHIRException;

@FunctionalInterface
public interface Unmarshaller<T> {
	static final String ISO_8601_GMT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	/**
     * Unmarshalls json string to object T.
     * @throws FHIRPersistenceException
     */
	T unmarshall(String json) throws FHIRException;
}
