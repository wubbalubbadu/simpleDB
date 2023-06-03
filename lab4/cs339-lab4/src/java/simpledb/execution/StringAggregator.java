package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.Tuple;

import simpledb.storage.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private final Op what;
    private final int gbfield;
    private final Type gbfieldtype;
    // a map of groupVal -> AggregateFields
    private final Map<String, AggregateFields> groups;

    /**
     * Aggregate constructor
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        this.what = what;
        if (what != Op.COUNT)
            throw new IllegalArgumentException("Invalid operator type " + what);
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.groups = new HashMap<>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     *
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        String groupVal = "";
        if (gbfield != NO_GROUPING) {
            groupVal = tup.getField(gbfield).toString();
        }
        AggregateFields agg = groups.get(groupVal);
        if (agg == null)
            agg = new AggregateFields(groupVal);

        agg.count++;

        groups.put(groupVal, agg);
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *         aggregateVal) if using group, or a single (aggregateVal) if no
     *         grouping. The aggregateVal is determined by the type of
     *         aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        LinkedList<Tuple> result = new LinkedList<>();
        int aggField = 1;
        TupleDesc td;

        if (gbfield == NO_GROUPING) {
            td = new TupleDesc(new Type[]{Type.INT_TYPE});
            aggField = 0;
        } else {
            td = new TupleDesc(new Type[]{gbfieldtype, Type.INT_TYPE});
        }

        // iterate over groups and create summary tuples
        for (String groupVal : groups.keySet()) {
            AggregateFields agg = groups.get(groupVal);
            Tuple tup = new Tuple(td);

            if (gbfield != NO_GROUPING) {
                if (gbfieldtype == Type.INT_TYPE)
                    tup.setField(0, new IntField(new Integer(groupVal)));
                else tup.setField(0, new StringField(groupVal, Type.STRING_LEN));
            }

            if (what == Op.COUNT) {
                tup.setField(aggField, new IntField(agg.count));
            }

            result.add(tup);
        }

        OpIterator retVal = null;
        retVal = new TupleIterator(td, Collections.unmodifiableList(result));
        return retVal;
    }

    /**
     * A helper struct to store accumulated aggregate values.
     */
    private static class AggregateFields {
        public final String groupVal;
        public int count;

        public AggregateFields(String groupVal) {
            this.groupVal = groupVal;
            count = 0;
        }
    }
}
