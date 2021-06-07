package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.engine.model.ModelUtil.fhirstring;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class LibraryEvaluate extends AbstractCqlOperation {

    private static Logger logger = Logger.getLogger(LibraryEvaluate.class.getName());

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("OperationDefinition-cpg-library-evaluate.json")) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
        String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper)
        throws FHIROperationException {

        Parameters result = null;

        Map<String, Parameter> paramMap = indexParametersByName(parameters);

        try {
            Library primaryLibrary = null;
            if (operationContext.getType().equals(FHIROperationContext.Type.INSTANCE)) {
                SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.LIBRARY.getValue(), logicalId, true, false, null);
                primaryLibrary = (Library) readResult.getResource();
            } else if (operationContext.getType().equals(FHIROperationContext.Type.RESOURCE_TYPE)) {
                Parameter param = getRequiredParameter(paramMap, "library");
                String canonicalURL = ((com.ibm.fhir.model.type.Uri) param.getValue()).getValue();
                primaryLibrary = FHIRRegistry.getInstance().getResource(canonicalURL, Library.class);
            } else {
                throw new UnsupportedOperationException("This operation must be invoked in the context of the Library resource");
            }

            if (primaryLibrary == null) {
                throw new IllegalArgumentException("failed to resolve library");
            } else {
                result = doEvaluation(resourceHelper, paramMap, primaryLibrary);
            }

        } catch (FHIROperationException fex) {
            throw fex;
        } catch (IllegalArgumentException iex) {
            logger.log(Level.SEVERE, "Bad Request", iex);
            throw new FHIROperationException(iex.getMessage(), iex).withIssue(Issue.builder().severity(IssueSeverity.ERROR).code(IssueType.INVALID).details(CodeableConcept.builder().text(fhirstring(iex.getMessage())).build()).build());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Evaluation failed", ex);
            throw new FHIROperationException("Evaluation failed", ex);
        }

        return result;
    }
}
