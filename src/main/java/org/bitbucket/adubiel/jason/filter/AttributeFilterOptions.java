package org.bitbucket.adubiel.jason.filter;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author Adam Dubiel
 */
class AttributeFilterOptions {

    private final HashSet<String> include = new HashSet<String>();

    private final HashSet<String> exclude = new HashSet<String>();

    private final boolean checkIncludes;

    static AttributeFilterOptions empty() {
        return new AttributeFilterOptions(null, null);
    }

    AttributeFilterOptions(String[] includes, String[] excludes) {
        if (includes != null) {
            include.addAll(Arrays.asList(includes));
        }
        checkIncludes = !include.isEmpty();

        if (excludes != null) {
            exclude.addAll(Arrays.asList(excludes));
        }
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
