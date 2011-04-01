/*******************************************************************************
 * Copyright (c) 2010 György Orosz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     György Orosz - initial API and implementation
 ******************************************************************************/
package org.erlide.wrangler.refactoring.selection.internal;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.erlide.core.ErlangScope;
import org.erlide.core.model.erlang.IErlModel;
import org.erlide.core.model.erlang.IErlProject;
import org.erlide.wrangler.refactoring.selection.IErlSelection;

import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangString;

/**
 * Abstract class which represents an Erlang selection.
 * 
 * @author Gyorgy Orosz
 * @version %I%, %G%
 */
public abstract class AbstractErlSelection implements IErlSelection {

	protected IFile file;

	public boolean isEmpty() {
		return false;
	}

	public OtpErlangList getSearchPath() {
		final IProject project = file.getProject();
		final IErlModel model = ErlangScope.getModel();
		final IErlProject actualProject = model.getErlangProject(project);
		final IPath projectLocation = actualProject.getWorkspaceProject()
				.getLocation();

		final Collection<IPath> sourcDirs = actualProject.getSourceDirs();
		final OtpErlangString[] searchPath = new OtpErlangString[sourcDirs
				.size()];
		int i = 0;
		for (final IPath src : sourcDirs) {
			searchPath[i++] = new OtpErlangString(projectLocation.append(src)
					.toOSString());
		}
		return new OtpErlangList(searchPath);
	}

	public String getFilePath() {
		return file.getLocation().toOSString();
	}

	public IFile getFile() {
		return file;
	}

}
