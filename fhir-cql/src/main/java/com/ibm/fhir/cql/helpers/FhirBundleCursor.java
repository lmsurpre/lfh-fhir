package com.ibm.fhir.cql.helpers;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Resource;

public class FhirBundleCursor implements Iterable<Object> {

    public static interface PageLoader extends Function<String, Bundle> {
    };

    private PageLoader pageLoader;
    private Bundle results;

    public FhirBundleCursor(PageLoader pageLoader, Bundle results) {
        this.pageLoader = pageLoader;
        this.results = results;
    }

    public Iterator<Object> iterator() {
        return new FhirBundleIterator(pageLoader, results);
    }

    private static class FhirBundleIterator implements Iterator<Object> {

        private PageLoader pageLoader;
        private Bundle results;
        private List<? extends Resource> currentEntry;

        public FhirBundleIterator(PageLoader pageLoader, Bundle results) {
            this.pageLoader = pageLoader;
            this.results = results;
            this.currentEntry = getEntry();
        }

        public boolean hasNext() {
            boolean hasNext = this.currentEntry.size() > 0;
            if( ! hasNext && this.getLink().isPresent() ) {
                try {
                    String url = getLink().get();
                    this.results = pageLoader.apply(url);
                    this.currentEntry = getEntry();
                    
                    hasNext = this.currentEntry.size() > 0;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            return hasNext;
        }

        private List<? extends Resource> getEntry() {
            if (this.results.getEntry() != null) {
                return this.results.getEntry().stream().map(e -> e.getResource()).collect(Collectors.toCollection(LinkedList::new));
            } else {
                return Collections.emptyList();
            }
        }

        private Optional<String> getLink() {
            return ModelHelper.getLinkByType(this.results, "next");
        }

        public Object next() {
            return this.currentEntry.remove(0);
        }
    }
}
