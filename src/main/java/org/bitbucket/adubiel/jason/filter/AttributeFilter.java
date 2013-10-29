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

    private static final AttributeFilterOptions EMPTY_FILTER = AttributeFilterOptions.empty();

    private final AttributeFilterOptions globalFilter = EMPTY_FILTER;

    private final Map<Class<?>, AttributeFilterOptions> classFilters = new HashMap<Class<?>, AttributeFilterOptions>();

    public AttributeFilter() {
    }

    public AttributeFilter including(String... globalIncludes) {
        globalFilter.including(globalIncludes);
        return this;
    }

    public AttributeFilter excluding(String... globalExcludes) {
        globalFilter.excluding(globalExcludes);
        return this;
    }

    public AttributeFilter including(Class<?> targetClass, String... includes) {
        AttributeFilterOptions classFilter = getClassFilter(targetClass);
        classFilter.including(includes);
        return this;
    }

    public AttributeFilter excluding(Class<?> targetClass, String... excludes) {
        AttributeFilterOptions classFilter = getClassFilter(targetClass);
        classFilter.excluding(excludes);
        return this;
    }

    private AttributeFilterOptions getClassFilter(Class<?> targetClass) {
        AttributeFilterOptions classFilter = classFilters.get(targetClass);
        if (classFilter == null) {
            classFilter = new AttributeFilterOptions();
            classFilters.put(targetClass, classFilter);
        }
        return classFilter;
    }

    public boolean allow(Class<?> fieldClass, String fieldName) {
        AttributeFilterOptions classFilter = classFilters.get(fieldClass);
        if (classFilter == null) {
            classFilter = EMPTY_FILTER;
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
