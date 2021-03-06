package org.jasonjson.core.filter;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author Adam Dubiel
 */
class ClassAttributeFilter {

    private final HashSet<String> include = new HashSet<String>();

    private final HashSet<String> exclude = new HashSet<String>();

    private boolean checkIncludes = false;

    static ClassAttributeFilter empty() {
        return new ClassAttributeFilter();
    }

    ClassAttributeFilter() {
    }

    void including(String... includes) {
        include.clear();
        include.addAll(Arrays.asList(includes));
        checkIncludes = !include.isEmpty();
    }

    void excluding(String... excludes) {
        exclude.clear();
        exclude.addAll(Arrays.asList(excludes));
    }

    boolean exclude(String fieldName) {
        return exclude.contains(fieldName);
    }

    boolean include(String fieldName) {
        if (checkIncludes) {
            return include.contains(fieldName);
        }
        return true;
    }

    boolean expliciteInclude(String fieldName) {
        return include.contains(fieldName);
    }

    boolean expliciteIncludes() {
        return checkIncludes;
    }
}
