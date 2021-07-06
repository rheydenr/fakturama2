package org.fakturama.wizards.activities;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class WorkbenchActivityHelper {
	/**
	 * The ID of the trigger point that only returns activities with core
	 * expressions.
	 *
	 * @since 3.4
	 */
	public static final String TRIGGER_PRE_UI_POINT = "org.eclipse.ui.workbenchModel"; //$NON-NLS-1$

//	private static ITriggerPoint getTriggerPoint(String id) {
//		return PlatformUI.getWorkbench().getActivitySupport().getTriggerPointManager().getTriggerPoint(id);
//	}


	/**
	 * Returns an array with those objects of the argument array that pass the
	 * {@link #restrictUseOf(Object)} test.
	 *
	 * @param array the input array
	 * @return a new array of the same type as the argument array, containing
	 *         objects that pass the test
	 *
	 * @since 3.4
	 */
	public static Object[] restrictArray(Object[] array) {
		ArrayList<Object> list = new ArrayList<>(array.length);
		for (Object e : array) {
			if (!restrictUseOf(e)) {
				list.add(e);
			}
		}
		return list.toArray((Object[]) Array.newInstance(array.getClass().getComponentType(), list.size()));
	}

//	/**
//	 * Answers whether a given contribution is allowed to be used based on activity
//	 * enablement. If it is currently disabled, then a dialog is opened and the user
//	 * is prompted to activate the required activities. If the user declines their
//	 * activation then false is returned. In all other cases <code>true</code> is
//	 * returned.
//	 *
//	 * @param triggerPoint the trigger point being hit
//	 * @param object       the contribution to test.
//	 * @return whether the contribution is allowed to be used based on activity
//	 *         enablement.
//	 */
//	public static boolean allowUseOf(ITriggerPoint triggerPoint, Object object) {
//		if (!isFiltering()) {
//			return true;
//		}
//		if (triggerPoint == null) {
//			return true;
//		}
//		if (object instanceof IPluginContribution) {
//			IPluginContribution contribution = (IPluginContribution) object;
//			IIdentifier identifier = getIdentifier(contribution);
//			return allow(triggerPoint, identifier);
//		}
//		return true;
//	}

	/**
	 * Restrict the use of the object only if it is matched by an activity with a
	 * core expression. A normal disabled activity will not restrict the use of this
	 * object.
	 *
	 * @param object the object to restrict
	 * @return <code>true</code> if this object is matched by a disabled activity
	 *         with an expression.
	 * @since 3.4
	 */
	public static boolean restrictUseOf(Object object) {
		return false; // !allowUseOf(getTriggerPoint(TRIGGER_PRE_UI_POINT), object);
	}


	/**
	 * Answers whether the provided object should be filtered from the UI based on
	 * activity state. Returns <code>false</code> except when the object is an
	 * instance of <code>IPluginContribution</code> whos unified id matches an
	 * <code>IIdentifier</code> that is currently disabled.
	 *
	 * @param object the object to test
	 * @return whether the object should be filtered
	 * @see #createUnifiedId(IPluginContribution)
	 */
	public static boolean filterItem(Object object) {
//		if (object instanceof IPluginContribution) {
//			IPluginContribution contribution = (IPluginContribution) object;
//			IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
//			IIdentifier identifier = workbenchActivitySupport.getActivityManager()
//					.getIdentifier(createUnifiedId(contribution));
//			if (!identifier.isEnabled()) {
//				return true;
//			}
//		}
		return false;
	}

}
