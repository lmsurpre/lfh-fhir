package com.ibm.fhir.cql.engine.searchparam;

import java.util.List;

public interface IQueryParameterOr<T extends IQueryParameter> {

    public List<T> getParameterValues();
}
