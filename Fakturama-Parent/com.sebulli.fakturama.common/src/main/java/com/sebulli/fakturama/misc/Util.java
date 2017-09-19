package com.sebulli.fakturama.misc;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;

public final class Util {

    /**
     * If it is possible to adapt the given object to the given type, this
     * returns the adapter. Performs the following checks:
     * 
     * <ol>
     * <li>Returns <code>sourceObject</code> if it is an instance of the
     * adapter type.</li>
     * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
     * <li>If sourceObject is not an instance of PlatformObject (which would have
     * already done so), the adapter manager is queried for adapters</li>
     * </ol>
     * 
     * Otherwise returns null.
     * 
     * @param sourceObject
     *            object to adapt, or null
     * @param adapterType
     *            type to adapt to
     * @return a representation of sourceObject that is assignable to the
     *         adapter type, or null if no such representation exists
     */
    public static Object getAdapter(Object sourceObject, Class adapterType) {
    	Assert.isNotNull(adapterType);
        if (sourceObject == null) {
            return null;
        }
        if (adapterType.isInstance(sourceObject)) {
            return sourceObject;
        }

        if (sourceObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) sourceObject;

            Object result = adaptable.getAdapter(adapterType);
            if (result != null) {
                // Sanity-check
                Assert.isTrue(adapterType.isInstance(result));
                return result;
            }
        } 
        
        if (!(sourceObject instanceof PlatformObject)) {
            Object result = Platform.getAdapterManager().getAdapter(sourceObject, adapterType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static void arrayCopyWithRemoval(Object [] src, Object [] dst, int idxToRemove) {
    	if (src == null || dst == null || src.length - 1 != dst.length || idxToRemove < 0 || idxToRemove >= src.length) {
			throw new IllegalArgumentException();
		}
    	
    	if (idxToRemove == 0) {
    		System.arraycopy(src, 1, dst, 0, src.length - 1);    		
    	}
    	else if (idxToRemove == src.length - 1) {
    		System.arraycopy(src, 0, dst, 0, src.length - 1);
    	}
    	else {
    		System.arraycopy(src, 0, dst, 0, idxToRemove);
    		System.arraycopy(src, idxToRemove + 1, dst, idxToRemove, src.length - idxToRemove - 1);
    	}    	
    }

    private Util() {
    }	
}
