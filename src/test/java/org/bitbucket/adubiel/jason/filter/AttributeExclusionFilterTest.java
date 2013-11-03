package org.bitbucket.adubiel.jason.filter;

import org.bitbucket.adubiel.jason.test.model.Parent;
import org.bitbucket.adubiel.jason.test.model.SingleChild;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author Adam Dubiel
 */
public class AttributeExclusionFilterTest {

    @Test
    public void shouldAllowFieldWhenIncludedInClassFilter() {
        // given
        AttributeExclusionFilter filter = new AttributeExclusionFilter().including(Parent.class, "name");

        // when
        boolean skip = filter.skipField(Parent.class, "name");

        // then
        assertThat(skip).isFalse();
    }

    @Test
    public void shouldAllowAllFieldsByDefault() {
        // given
        AttributeExclusionFilter filter = new AttributeExclusionFilter();

        // when
        boolean skip = filter.skipField(Parent.class, "name");

        // then
        assertThat(skip).isFalse();
    }

    @Test
    public void shouldNotAllowFieldWhenExcludedInClassFilter() {
        // given
        AttributeExclusionFilter filter = new AttributeExclusionFilter().excluding(Parent.class, "name");

        // when
        boolean skip = filter.skipField(Parent.class, "name");

        // then
        assertThat(skip).isTrue();
    }

    @Test
    public void shouldAllowOnlyFieldsFromIncludeIfDefined() {
        // given
        AttributeExclusionFilter filter = new AttributeExclusionFilter().including("name");

        // when
        boolean skip = filter.skipField(Parent.class, "id");

        // then
        assertThat(skip).isTrue();
    }

    @Test
    public void shouldAllowFieldIfExplicitlyIncludedInClassEvenIfExcludedGlobally() {
        // given
        AttributeExclusionFilter filter = new AttributeExclusionFilter().excluding("name")
                .including(Parent.class, "name");

        // when
        boolean skip = filter.skipField(Parent.class, "name", null);

        // then
        assertThat(skip).isFalse();
    }

    @Test
    public void shouldAllowOnlyFieldFromClassWhichHasItIncluded() {
        // given
        AttributeExclusionFilter filter = new AttributeExclusionFilter()
                .including(Parent.class, "id")
                .including(SingleChild.class, "name");

        // when
        boolean skip = filter.skipField(Parent.class, "name");

        // then
        assertThat(skip).isTrue();
    }
}
