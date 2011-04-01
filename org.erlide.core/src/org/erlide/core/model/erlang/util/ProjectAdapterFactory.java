package org.erlide.core.model.erlang.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterFactory;
import org.erlide.core.ErlangScope;
import org.erlide.core.model.erlang.IErlProject;

@SuppressWarnings("rawtypes")
public class ProjectAdapterFactory implements IAdapterFactory {

    private static final Class[] ADAPTER_LIST = new Class[] { IErlProject.class };

    public Object getAdapter(final Object adaptableObject,
            final Class adapterType) {
        if (adapterType == IErlProject.class
                && adaptableObject instanceof IProject) {
            return ErlangScope.getModel().getErlangProject(
                    (IProject) adaptableObject);
        }
        return null;
    }

    public Class[] getAdapterList() {
        return ADAPTER_LIST;
    }

}
