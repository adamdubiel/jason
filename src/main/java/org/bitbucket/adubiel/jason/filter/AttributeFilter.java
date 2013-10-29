package org.bitbucket.adubiel.jason.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * precedence: exclude > include
 * global exclude - never show unless included in specific
 * global include - always show unless excluded in specific
 *
 * @author Adam Dubiel
 */
public class AttributeFilter {

    private static final AttributeFilterOptions emptyFilter = AttributeFilterOptions.empty();

    private final AttributeFilterOptions globalFilter;

    private final Map<Class<?>, AttributeFilterOptions> classFilters = new HashMap<Class<?>, AttributeFilterOptions>();

    public AttributeFilter() {
        globalFilter = emptyFilter;
    }

    public AttributeFilter(String[] globalIncludes, String[] globalExcludes) {
        globalFilter = new AttributeFilterOptions(globalIncludes, globalExcludes);
    }

    public AttributeFilter filteringClass(Class<?> targetClass, String[] includes, String[] excludes) {
        classFilters.put(targetClass, new AttributeFilterOptions(includes, excludes));
        return this;
    }

    public boolean allow(Class<?> fieldClass, String fieldName) {
        AttributeFilterOptions classFilter = classFilters.get(fieldClass);
        if (classFilter == null) {
            classFilter = emptyFilter;
        }

        if (globalFilter.exclude(fieldName)) {
            return classFilter.expliciteInclude(fieldName);
        }

        if (globalFilter.include(fieldName) && !classFilter.expliciteIncludes()) {
            return !classFilter.exclude(fieldName);
        }

        if (globalFilter.expliciteIncludes()) {
            return classFilter.expliciteInclude(fieldName);
        }

        return !classFilter.exclude(fieldName) && classFilter.include(fieldName);
    }
}
