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
import java.util.concurrent.Future;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.patch.FHIRPatch;
import com.ibm.fhir.model.resource.Bundle.Entry;
import com.ibm.fhir.model.type.Instant;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.model.util.ReferenceMappingVisitor;
import com.ibm.fhir.persistence.exception.FHIRPersistenceResourceDeletedException;
import com.ibm.fhir.persistence.exception.FHIRPersistenceResourceNotFoundException;
import com.ibm.fhir.search.exception.FHIRSearchException;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.server.exception.FHIRRestBundledRequestException;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.operation.spi.FHIRRestOperationResponse;
import com.ibm.fhir.server.util.FHIRUrlParser;
import com.ibm.fhir.server.util.IssueTypeToHttpStatusMapper;

/**
 * Visitor used to update references in an incoming resource prior to persistence
 */
public class FHIRRestInteractionVisitorReferenceMapping extends FHIRRestInteractionVisitorBase {
    
    // True if there's a bundle-level transaction, null otherwise
    final boolean transaction;
    
    /**
     * Public constructor
     * @param helpers
     */
    public FHIRRestInteractionVisitorReferenceMapping(boolean transaction, FHIRResourceHelpers helpers, Map<String, String> localRefMap, Entry[] responseBundleEntries) {
        super(helpers, localRefMap, responseBundleEntries);
        this.transaction = transaction;
    }

    @Override
    public FHIRRestOperationResponse doSearch(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String compartment, String compartmentId,
        MultivaluedMap<String, String> queryParameters, String requestUri, Resource contextResource, boolean checkInteractionAllowed) throws Exception {
        // NOP. Nothing to do
        return null;
    }

    @Override
    public FHIRRestOperationResponse doVRead(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, String versionId, MultivaluedMap<String, String> queryParameters)
        throws Exception {
        // NOP for now. TODO: when offloading payload, start an async optimistic read of the id/version payload
        return null;
    }

    @Override
    public FHIRRestOperationResponse doRead(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, boolean throwExcOnNull, boolean includeDeleted, Resource contextResource,
        MultivaluedMap<String, String> queryParameters, boolean checkInteractionAllowed) throws Exception {
        // NOP for now. TODO: when offloading payload, try an optimistic async read of the latest payload
        return null;
    }

    @Override
    public FHIRRestOperationResponse doHistory(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, MultivaluedMap<String, String> queryParameters, String requestUri)
        throws Exception {
        // NOP for now. TODO: optimistic async reads, if we can scope them properly
        return null;
    }

    @Override
    public FHIRRestOperationResponse doCreate(int entryIndex, List<Issue> warnings, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, Resource resource, String ifNoneExist, String localIdentifier) throws Exception {
        
        // Use doOperation so we can implement common exception handling in one place
        return doOperation(entryIndex, requestDescription, initialTime, () -> {
                
            // Convert any local references found within the resource to their corresponding external reference.
            ReferenceMappingVisitor<Resource> visitor = new ReferenceMappingVisitor<Resource>(localRefMap);
            resource.accept(visitor);
            final Resource finalResource = visitor.getResult();
            
            // Resource processing is now complete and from this point on, we can treat the resource
            // as immutable.
            
            // TODO: If the persistence layer supports offloading, we can store now. The offloadResponse
            // will be null if offloading is not supported
            int newVersionNumber = Integer.parseInt(finalResource.getMeta().getVersionId().getValue());
            Instant lastUpdated = finalResource.getMeta().getLastUpdated();
            Future<FHIRRestOperationResponse> offloadResponse = storePayload(finalResource, finalResource.getId(), 
                newVersionNumber, lastUpdated);
            
            // Pass back the updated resource so it can be used in the next phase if required
            return new FHIRRestOperationResponse(finalResource, finalResource.getId(), newVersionNumber, lastUpdated, offloadResponse);
        });
    }

    @Override
    public FHIRRestOperationResponse doUpdate(int entryIndex, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, 
        long initialTime, String type, String id, Resource resource, Resource prevResource, String ifMatchValue, String searchQueryString,
        boolean skippableUpdate, String localIdentifier, List<Issue> warnings, boolean isDeleted) throws Exception {

        // Use doOperation for common exception handling
        return doOperation(entryIndex, requestDescription, initialTime, () -> {
            
            // Convert any local references found within the resource to their corresponding external reference.
            ReferenceMappingVisitor<Resource> visitor = new ReferenceMappingVisitor<Resource>(localRefMap);
            resource.accept(visitor);
            Resource newResource = visitor.getResult();
            
            if (localIdentifier != null && localRefMap.get(localIdentifier) == null) {
                addLocalRefMapping(localIdentifier, newResource);
            }
            
            // Pass back the updated resource so it can be used in the next phase
            return new FHIRRestOperationResponse(null, null, newResource);
        });
    }

    @Override
    public FHIRRestOperationResponse doPatch(int entryIndex, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, long initialTime, 
        String type, String id, Resource resource, Resource prevResource, FHIRPatch patch, String ifMatchValue, String searchQueryString,
        boolean skippableUpdate, List<Issue> warnings, String localIdentifier) throws Exception {
        // Use doOperation for common exception handling
        return doOperation(entryIndex, requestDescription, initialTime, () -> {
            
            // Convert any local references found within the resource to their corresponding external reference.
            ReferenceMappingVisitor<Resource> visitor = new ReferenceMappingVisitor<Resource>(localRefMap);
            resource.accept(visitor);
            Resource newResource = visitor.getResult();
            
            if (localIdentifier != null && localRefMap.get(localIdentifier) == null) {
                addLocalRefMapping(localIdentifier, newResource);
            }
            
            // Pass back the updated resource so it can be used in the next phase
            return new FHIRRestOperationResponse(null, null, newResource);
        });
    }

    @Override
    public FHIRRestOperationResponse doInvoke(String method, int entryIndex, Entry validationResponseEntry, String requestDescription, FHIRUrlParser requestURL, long initialTime, FHIROperationContext operationContext, String resourceTypeName, String logicalId,
        String versionId, String operationName, Resource resource, MultivaluedMap<String, String> queryParameters) throws Exception {
        // NOP
        return null;
    }

    @Override
    public FHIRRestOperationResponse doDelete(int entryIndex, String requestDescription, FHIRUrlParser requestURL, long initialTime, String type, String id, String searchQueryString) throws Exception {
        // NOP
        return null;
    }

    @Override
    public FHIRRestOperationResponse validationResponse(int entryIndex, Entry validationResponseEntry, String requestDescription, long initialTime) throws Exception {
        // NOP
        return null;
    }
    
    @Override
    public FHIRRestOperationResponse issue(int entryIndex, String requestDescription, long initialTime, Status status, Entry responseEntry) throws Exception {
        // NOP
        return null;
    }
    
    /**
     * If payload offloading is supported by the persistence layer, store the given resource. This
     * can be an async operation which we resolve at the end just prior to the transaction being
     * committed.
     * TODO: use a dedicated class instead of FHIRRestOperationResponse
     * @param resource
     * @param logicalId
     * @param newVersionNumber
     * @param lastUpdated
     * @return
     */
    protected Future<FHIRRestOperationResponse> storePayload(Resource resource, String logicalId, int newVersionNumber, Instant lastUpdated) {
       return helpers.storePayload(resource, logicalId, newVersionNumber, lastUpdated); 
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
    private FHIRRestOperationResponse doOperation(int entryIndex, String requestDescription, long initialTime, Callable<FHIRRestOperationResponse> v) throws Exception {
        final boolean failFast = transaction;
        try {
            return v.call();
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
        
        return null;
    }
}