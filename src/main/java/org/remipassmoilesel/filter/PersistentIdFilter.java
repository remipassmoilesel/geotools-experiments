package org.remipassmoilesel.filter;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.AbstractFilter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;

import java.util.Set;

/**
 * Custom ID filter using persistent ID
 */
public class PersistentIdFilter extends AbstractFilter implements Id {

    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory();
    private final Set<Identifier> ids;

    public PersistentIdFilter(Set<Identifier> ids) {
        this.ids = ids;
    }

    @Override
    public boolean evaluate(Object feature) {

        if (feature == null) {
            return false;
        }

        String evaluate = ff.property("persistent_id").evaluate(feature, String.class);
        if (evaluate == null) {
            return false;
        } else {
            return ids.contains(evaluate);
        }

    }

    @Override
    public Object accept(FilterVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

    @Override
    public Set getIDs() {
        return ids;
    }

    @Override
    public Set<Identifier> getIdentifiers() {
        return ids;
    }
}
