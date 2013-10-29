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
        AttributeFilter filter = new AttributeFilter(null, null).filteringClass(Parent.class, new String[]{"name"}, null);

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isTrue();
    }

    @Test
    public void shouldAllowAllFieldsByDefault() {
        // given
        AttributeFilter filter = new AttributeFilter(null, null);

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isTrue();
    }

    @Test
    public void shouldNotAllowFieldWhenExcludedInClassFilter() {
        // given
        AttributeFilter filter = new AttributeFilter(null, null).filteringClass(Parent.class, null, new String[]{"name"});

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isFalse();
    }

    @Test
    public void shouldAllowOnlyFieldsFromIncludeIfDefined() {
        // given
        AttributeFilter filter = new AttributeFilter(new String[]{"name"}, null);

        // when
        boolean allow = filter.allow(Parent.class, "id");

        // then
        assertThat(allow).isFalse();
    }

    @Test
    public void shouldAllowFieldIfExplicitlyIncludedInClassEvenIfExcludedGlobally() {
        // given
        AttributeFilter filter = new AttributeFilter(null, new String[]{"name"})
                .filteringClass(Parent.class, new String[]{"name"}, null);

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isTrue();
    }

    @Test
    public void shouldAllowOnlyFieldFromClassWhichHasItIncluded() {
        // given
        AttributeFilter filter = new AttributeFilter(null, null)
                .filteringClass(Parent.class, new String[]{"id"}, null)
                .filteringClass(SingleChild.class, new String[]{"name"}, null);

        // when
        boolean allow = filter.allow(Parent.class, "name");

        // then
        assertThat(allow).isFalse();
    }
}
