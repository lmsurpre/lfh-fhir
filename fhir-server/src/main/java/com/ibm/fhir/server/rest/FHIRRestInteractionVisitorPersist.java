/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.ibm.fhir.server.rest;

import static com.ibm.fhir.model.type.String.string;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.patch.FHIRPatch;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Bundle.Entry;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.persistence.exception.FHIRPersistenceResourceDeletedException;
import com.ibm.fhir.persistence.exception.FHIRPersistenceResourceNotFoundException;
import com.ibm.fhir.persistence.interceptor.FHIRPersistenceEvent;
import com.ibm.fhir.search.exception.FHIRSearchException;
import com.ibm.fhir.server.exception.FHIRRestBundledRequestException;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.operation.spi.FHIRRestOperationResponse;
import com.ibm.fhir.server.util.FHIRUrlParser;
import com.ibm.fhir.server.util.IssueTypeToHttpStatusMapper;

/**
 * Visitor implementation to access the persistence operations. Each operation
 * result is wrapped in a {@link FHIRRestOperationResponse} object, which is
 * what allows us to use the visitor pattern here. Note that this differs
 * slightly from the original FHIRRestHelper implementation in which operations
 * returned different types. That said, all the heavy lifting is the same,
 * performed by the implementation behind {@link FHIRResourceHelpers}.
 */
public class FHIRRestInteractionVisitorPersist extends FHIRRestInteractionVisitorBase {

    // True if the bundle type is TRANSACTION
    final boolean transaction;
    
    /**
     * Public constructor
     * @param helpers
     * @param localRefMap
     * @param responseBundleEntries
     * @param transaction
     */
    public FHIRRestInteractionVisitorPersist(FHIRResourceHelpers helpers, Map<String, String> localRefMap, Entry[] responseBundleEntries, boolean transaction) {
        super(helpers, localRefMap, responseBundleEntries);
        this.transaction = transaction;
    }

    @Override
    public FHIRRestOperationResponse doSearch(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String compartment, String compartmentId,
        MultivaluedMap<String, String> queryParameters, String requestUri, Resource contextResource, boolean checkInteractionAllowed) throws Exception {
        
        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            Bundle searchResults = helpers.doSearch(type, compartment, compartmentId, queryParameters, requestUri, contextResource, checkInteractionAllowed);
            
            return Bundle.Entry.builder()
                    .resource(searchResults)
                    .response(Entry.Response.builder()
                        .status(SC_OK_STRING)
                        .build())
                    .build();
        });
        
        return null;
    }

    @Override
    public FHIRRestOperationResponse doVRead(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, String versionId, MultivaluedMap<String, String> queryParameters)
        throws Exception {
        
        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            // Perform the VREAD and return the result entry we want in the response bundle
            Resource resource = helpers.doVRead(type, id, versionId, queryParameters);
            return Entry.builder()
                    .response(Entry.Response.builder()
                        .status(SC_OK_STRING)
                        .build())
                    .resource(resource)
                    .build();
        });
        
        return null;
    }

    @Override
    public FHIRRestOperationResponse doRead(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, boolean throwExcOnNull, boolean includeDeleted, Resource contextResource,
        MultivaluedMap<String, String> queryParameters, boolean checkInteractionAllowed) throws Exception {

        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            // Perform the VREAD and return the result entry we want in the response bundle
            SingleResourceResult<? extends Resource> readResult = helpers.doRead(type, id, throwExcOnNull, includeDeleted, contextResource, queryParameters);
            return Entry.builder()
                    .response(Entry.Response.builder()
                        .status(SC_OK_STRING)
                        .build())
                    .resource(readResult.getResource())
                    .build();
        });
        return null;
    }

    @Override
    public FHIRRestOperationResponse doHistory(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, MultivaluedMap<String, String> queryParameters, String requestUri)
        throws Exception {
        
        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            Bundle bundle = helpers.doHistory(type, id, queryParameters, requestUri);
            return Entry.builder()
                    .response(Entry.Response.builder()
                        .status(SC_OK_STRING)
                        .build())
                    .resource(bundle)
                    .build();
        });
        return null;
    }

    @Override
    public FHIRRestOperationResponse doCreate(int entryIndex, FHIRPersistenceEvent event, List<Issue> warnings, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, Resource resource, String ifNoneExist, String localIdentifier) throws Exception {

        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            FHIRRestOperationResponse ior = helpers.doCreatePersist(event, warnings, resource);
        
            OperationOutcome validationOutcome = null;
            if (validationResponseEntry != null && validationResponseEntry.getResponse() != null) {
                validationOutcome = validationResponseEntry.getResponse().getOutcome().as(OperationOutcome.class);
            }
    
            return buildResponseBundleEntry(ior, validationOutcome, requestDescription, initialTime);
        });
        
        // No need to return anything. doOperation injects the entry to the responseBundle
        return null;
    }

    @Override
    public FHIRRestOperationResponse doUpdate(int entryIndex, FHIRPersistenceEvent event, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, 
        long initialTime, String type, String id, Resource newResource, Resource prevResource, String ifMatchValue, String searchQueryString,
        boolean skippableUpdate, String localIdentifier, List<Issue> warnings, boolean isDeleted) throws Exception {

        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            
            FHIRRestOperationResponse ior = helpers.doPatchOrUpdatePersist(event, type, id, false, newResource, prevResource, warnings, isDeleted);
            OperationOutcome validationOutcome = null;
            if (validationResponseEntry != null && validationResponseEntry.getResponse() != null) {
                validationOutcome = validationResponseEntry.getResponse().getOutcome().as(OperationOutcome.class);
            }
            return buildResponseBundleEntry(ior, validationOutcome, requestDescription, initialTime);        
        });
        
        return null;
    }

    @Override
    public FHIRRestOperationResponse doPatch(int entryIndex, FHIRPersistenceEvent event, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, long initialTime, 
        String type, String id, Resource newResource, Resource prevResource, FHIRPatch patch, String ifMatchValue, String searchQueryString,
        boolean skippableUpdate, List<Issue> warnings, String localIdentifier) throws Exception {

        // For patch, if the original resource was deleted, we'd have already thrown an error.
        // Note that the patch will have already been applied to the resource...so this is
        // really just an update as far as the persistence layer is concerned
        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            FHIRRestOperationResponse ior = helpers.doPatchOrUpdatePersist(event, type, id, true, newResource, prevResource,  
                warnings, false);
            OperationOutcome validationOutcome = null;
            if (validationResponseEntry != null && validationResponseEntry.getResponse() != null) {
                validationOutcome = validationResponseEntry.getResponse().getOutcome().as(OperationOutcome.class);
            }
            return buildResponseBundleEntry(ior, validationOutcome, requestDescription, initialTime);
        });
        
        return null;
    }

    @Override
    public FHIRRestOperationResponse doInvoke(String method, int entryIndex, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, long initialTime, FHIROperationContext operationContext, String resourceTypeName, String logicalId,
        String versionId, String operationName, Resource resource, MultivaluedMap<String, String> queryParameters) throws Exception {
        
        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            Resource response = helpers.doInvoke(operationContext, resourceTypeName, logicalId, versionId, operationName, resource, queryParameters);
            return Entry.builder()
                    .response(Entry.Response.builder()
                        .status(SC_OK_STRING)
                        .build())
                    .resource(response)
                    .build();
        });
        
        return null;
    }

    @Override
    public FHIRRestOperationResponse doDelete(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, String searchQueryString) throws Exception {

        doInteraction(entryIndex, requestDescription, initialTime, () -> {
            FHIRRestOperationResponse ior = helpers.doDelete(type, id, searchQueryString);
        
            int httpStatus = ior.getStatus().getStatusCode();
            OperationOutcome oo = ior.getOperationOutcome();
    
            return Entry.builder()
                    .response(Entry.Response.builder()
                        .status(string(Integer.toString(httpStatus)))
                        .outcome(oo)
                        .build())
                    .build();
        });
        
        return null;
    }

    @Override
    public FHIRRestOperationResponse validationResponse(int entryIndex, Entry validationResponseEntry, String requestDescription, long initialTime) throws Exception {
        setEntryComplete(entryIndex, validationResponseEntry, requestDescription, initialTime);
        return null;
    }
    
    @Override
    public FHIRRestOperationResponse issue(int entryIndex, String requestDescription, long initialTime, Status status, Entry responseEntry) throws Exception {
        setEntryComplete(entryIndex, responseEntry, requestDescription, initialTime);
        return null;
    }

    /**
     * Unified exception handling for each of the operation calls
     * @param entryIndex
     * @param v
     * @param failFast
     * @param requestDescription
     * @param initialTime
     * @throws Exception
     */
    private void doInteraction(int entryIndex, String requestDescription, long initialTime, Callable<Entry> v) throws Exception {
        // To make it clear, we fail immediately on exception if we're processing bundleType == TRANSACTION
        final boolean failFast = transaction;
        try {
            // If we already have a response entry for the given index then the entry has already 
            // completed or failed in a previous phase so we skip it here
            if (getResponseEntry(entryIndex) == null) {
                Entry entry = v.call();
                setEntryComplete(entryIndex, entry, requestDescription, initialTime);
            }
        } catch (FHIRPersistenceResourceNotFoundException e) {
            if (failFast) {
                String msg = "Error while processing request bundle.";
                throw new FHIRRestBundledRequestException(msg, e).withIssue(e.getIssues());
            }

            // Record the error as an entry in the result bundle
            Entry entry = Entry.builder()
                    .resource(FHIRUtil.buildOperationOutcome(e, false))
                    .response(Entry.Response.builder()
                        .status(SC_NOT_FOUND_STRING)
                        .build())
                    .build();
            setEntryComplete(entryIndex, entry, requestDescription, initialTime);
        } catch (FHIRPersistenceResourceDeletedException e) {
            if (failFast) {
                String msg = "Error while processing request bundle.";
                throw new FHIRRestBundledRequestException(msg, e).withIssue(e.getIssues());
            }

            Entry entry = Entry.builder()
                    .resource(FHIRUtil.buildOperationOutcome(e, false))
                    .response(Entry.Response.builder()
                        .status(SC_GONE_STRING)
                        .build())
                    .build();
            setEntryComplete(entryIndex, entry, requestDescription, initialTime);
        } catch (FHIROperationException e) {
            if (failFast) {
                String msg = "Error while processing request bundle.";
                throw new FHIRRestBundledRequestException(msg, e).withIssue(e.getIssues());
            }

            Status status;
            if (e instanceof FHIRSearchException) {
                status = Status.BAD_REQUEST;
            } else {
                status = IssueTypeToHttpStatusMapper.issueListToStatus(e.getIssues());
            }

            Entry entry = Entry.builder()
                    .resource(FHIRUtil.buildOperationOutcome(e, false))
                    .response(Entry.Response.builder()
                        .status(string(Integer.toString(status.getStatusCode())))
                        .build())
                    .build();
            setEntryComplete(entryIndex, entry, requestDescription, initialTime);
        }
    }
}