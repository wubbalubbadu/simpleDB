package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    private Type[] typeAr;
    private String[] fieldAr;

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */

    public Iterator<TDItem> iterator() {
        return new Iterator<TupleDesc.TDItem>() {
            private TDItem[] items = new TDItem[typeAr.length];
            {
                for (int i = 0; i < typeAr.length; i++) {
                    items[i] = new TDItem(typeAr[i], fieldAr[i]);
                }
            }
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < items.length;
            }

            @Override
            public TDItem next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return items[index++];
            }
        };
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        if (typeAr.length < 1){
            throw new IllegalArgumentException("typeAr must contain at least one entry");
        }
        if (typeAr.length != fieldAr.length){
            throw new IllegalArgumentException("typeAr and fieldAr must have the same length");
        }
        this.typeAr = typeAr;
        this.fieldAr = fieldAr;
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        if (typeAr.length < 1){
            throw new IllegalArgumentException("typeAr must contain at least one entry");
        }
        this.typeAr = typeAr;
        this.fieldAr = new String[typeAr.length];
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return typeAr.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        if (i < 0 || i >= fieldAr.length){
            throw new NoSuchElementException("i is not a valid field reference");
        }
        return fieldAr[i];
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        if (i < 0 || i >= typeAr.length){
            throw new NoSuchElementException("i is not a valid type reference");
        }
        return typeAr[i];
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        for (int i = 0; i < numFields(); i++){
            if (fieldAr[i]!=null && fieldAr[i].equals(name)){
                return i;
            }
        }
        throw new NoSuchElementException("no field with a matching name is found");
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        int size = 0;
        for (Type type : typeAr){
            size += type.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        Type[] typeAr = new Type[td1.numFields() + td2.numFields()];
        String[] fieldAr = new String[td1.numFields() + td2.numFields()];
        for (int i = 0; i < td1.numFields(); i++){
            typeAr[i] = td1.getFieldType(i);
            fieldAr[i] = td1.getFieldName(i);
        }
        for (int i = 0; i < td2.numFields(); i++){
            typeAr[i + td1.numFields()] = td2.getFieldType(i);
            fieldAr[i + td1.numFields()] = td2.getFieldName(i);
        }
        return new TupleDesc(typeAr, fieldAr);

    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        if (o == null){
            return false;
        }
        if (o.getClass() != this.getClass()){
            return false;
        }
        TupleDesc other = (TupleDesc) o;
        if (other.numFields() != this.numFields()){
            return false;
        }
        for (int i = 0; i < this.numFields(); i++){
            if (other.getFieldType(i) != this.getFieldType(i)){
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        Map<Type, String> hashMap = new HashMap<>();
        for (int i = 0; i < numFields(); i++){
            hashMap.put(getFieldType(i), getFieldName(i));
        }
        return hashMap.hashCode();
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        String result = "";
        for (int i = 0; i < numFields(); i++){
            result += getFieldType(i) + "(" + getFieldName(i) + ")";
            if (i != numFields() - 1){
                result += ", ";
            }
        }
        return result;
    }
}
