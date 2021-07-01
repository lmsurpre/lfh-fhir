package com.ibm.fhir.cql.engine.retrieve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.fhir.cql.engine.searchparam.IQueryParameter;
import com.ibm.fhir.cql.engine.searchparam.IQueryParameterAnd;
import com.ibm.fhir.cql.engine.searchparam.IQueryParameterOr;

public class SearchParameterMap implements Serializable {

    private static final long serialVersionUID = 2302049000744568377L;

    private Map<String, List<List<? extends IQueryParameter>>> parameters = new LinkedHashMap<>();
    private Integer count;

    public void count(Integer count) {
        this.count = count;
    }

    public Integer count() {
        return this.count;
    }

    public void put(String key, IQueryParameterOr<? extends IQueryParameter> orParam) {
        List<List<? extends IQueryParameter>> list = parameters.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(orParam.getParameterValues());
    }

    public void put(String key, IQueryParameterAnd<? extends IQueryParameterOr<?>> andParam) {
        andParam.getParameterValues().forEach(p -> {
            put(key, p);
        });
    }

    public void put(String key, IQueryParameter param) {
        List<List<? extends IQueryParameter>> list = parameters.computeIfAbsent(key, k -> new ArrayList<>());
        List<IQueryParameter> sublist = new ArrayList<>();
        sublist.add(param);
        list.add(sublist);
    }

    public boolean containsKey(String key) {
        return this.parameters.containsKey(key);
    }

    public Set<String> keySet() {
        return this.parameters.keySet();
    }

    public boolean isEmpty() {
        return this.parameters.isEmpty();
    }

    public Set<Map.Entry<String, List<List<? extends IQueryParameter>>>> entrySet() {
        return this.parameters.entrySet();
    }

    public List<List<? extends IQueryParameter>> remove(String theName) {
        return this.parameters.remove(theName);
    }
    
    public String toString() {
        return parameters.toString();
    }
}
