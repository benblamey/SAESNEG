package benblamey.saesneg.model.datums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is a disaster zone set in stone (thanks to serialization)...
 *
 * @author Ben
 *
 */
public class DatumCollection extends ArrayList<Datum> // This should be implements ArrayList<Datum> - but we leave it as it is to prevent serialization from breaking, and force override of all the List members.
{

    Map<Long, Datum> _map = new TreeMap<Long, Datum>();

    @Override
    public boolean add(Datum e) {

        _map.put(e.getNetworkID(), e);

        return super.add(e);
    }

    public boolean containsObjectWithNetworkID(Long datumID) {
        return _map.containsKey(datumID);
    }

    public Datum getObjectWithNetworkID(Long datumID) {
        return _map.get(datumID);
    }

    @Override
    public boolean remove(Object o) {
        Datum datum = (Datum) o;
        _map.remove(datum.getNetworkID()); // The underlying map removes based on key.
        return super.remove(o);
    }

    /**
     * Don't trust the return code.
     *
     * @param c
     * @return
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean didRemove = false;
        for (Object o : c) {
            if (remove(o)) {
                didRemove = true;
            }
        }
        return didRemove;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public Datum remove(int index) {
        return super.remove(index);
    }

    public void afterDeserlializationFix() {
        // Bizarrely, because of old serialized data -- we can end up with STRINGS instead of LONGS!
        // (i.e. old style 
        // This breaks the key-lookup.
        
        // Rather than faffing with the broken, simply re-build the index afresh.
        _map.clear();
        for (Datum d : this) {
            _map.put(d.getNetworkID(), d);
        }
    }
}
