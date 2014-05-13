package org.jasonjson.core.filter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * precedence: exclude > include
 * global exclude - never show unless included in specific
 * global include - always show unless excluded in specific
 *
 * @author Adam Dubiel
 */
public class AttributeExclusionFilter implements ExclusionFilter {

    private static final ClassAttributeFilter EMPTY_FILTER = ClassAttributeFilter.empty();

    private final ClassAttributeFilter globalFilter = ClassAttributeFilter.empty();

    private final Map<Class<?>, ClassAttributeFilter> classFilters = new HashMap<Class<?>, ClassAttributeFilter>();

    public AttributeExclusionFilter() {
    }

    public AttributeExclusionFilter including(String... globalIncludes) {
        globalFilter.including(globalIncludes);
        return this;
    }

    public AttributeExclusionFilter excluding(String... globalExcludes) {
        globalFilter.excluding(globalExcludes);
        return this;
    }

    public AttributeExclusionFilter including(Class<?> targetClass, String... includes) {
        ClassAttributeFilter classFilter = getClassFilter(targetClass);
        classFilter.including(includes);
        return this;
    }

    public AttributeExclusionFilter excluding(Class<?> targetClass, String... excludes) {
        ClassAttributeFilter classFilter = getClassFilter(targetClass);
        classFilter.excluding(excludes);
        return this;
    }

    private ClassAttributeFilter getClassFilter(Class<?> targetClass) {
        ClassAttributeFilter classFilter = classFilters.get(targetClass);
        if (classFilter == null) {
            classFilter = new ClassAttributeFilter();
            classFilters.put(targetClass, classFilter);
        }
        return classFilter;
    }

    private boolean allow(Class<?> fieldClass, String fieldName) {
        ClassAttributeFilter classFilter = classFilters.get(fieldClass);
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

    @Override
    public boolean skipField(Type declaringType, String fieldName) {
        Class declaringClass;
        if(declaringType instanceof ParameterizedType) {
            declaringClass = (Class)((ParameterizedType) declaringType).getRawType();
        }
        else {
            declaringClass = (Class) declaringType;
        }

        return !allow(declaringClass, fieldName);
    }

    @Override
    public boolean skipField(Type declaringType, String fieldName, Object fieldValue) {
        return skipField(declaringType, fieldName);
    }
}
