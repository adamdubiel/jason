package org.bitbucket.adubiel.jason.filter;

import org.bitbucket.adubiel.jason.test.model.Parent;
import org.bitbucket.adubiel.jason.test.model.SingleChild;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author Adam Dubiel
 */
public class AttributeFilterTest {

    @Test
    public void shouldAllowFieldWhenIncludedInClassFilter() {
        // given
        AttributeFilter filter = new AttributeFilter().including(Parent.class, "name");

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isTrue();
    }

    @Test
    public void shouldAllowAllFieldsByDefault() {
        // given
        AttributeFilter filter = new AttributeFilter();

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isTrue();
    }

    @Test
    public void shouldNotAllowFieldWhenExcludedInClassFilter() {
        // given
        AttributeFilter filter = new AttributeFilter().excluding(Parent.class, "name");

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isFalse();
    }

    @Test
    public void shouldAllowOnlyFieldsFromIncludeIfDefined() {
        // given
        AttributeFilter filter = new AttributeFilter().including("name");

        // when
        boolean allow = filter.allow(Parent.class, "id");

        // then
        assertThat(allow).isFalse();
    }

    @Test
    public void shouldAllowFieldIfExplicitlyIncludedInClassEvenIfExcludedGlobally() {
        // given
        AttributeFilter filter = new AttributeFilter().excluding("name")
                .including(Parent.class, "name");

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isTrue();
    }

    @Test
    public void shouldAllowOnlyFieldFromClassWhichHasItIncluded() {
        // given
        AttributeFilter filter = new AttributeFilter()
                .including(Parent.class, "id")
                .including(SingleChild.class, "name");

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isFalse();
    }
}
