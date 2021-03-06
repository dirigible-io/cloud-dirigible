/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.ide.workspace.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.repository.ui.view.IRefreshableView;
import com.sap.dirigible.ide.workspace.RemoteResourcesPlugin;
import com.sap.dirigible.ide.workspace.ui.viewer.WorkspaceViewer;
import com.sap.dirigible.ide.workspace.ui.viewer.WorkspaceViewerUtils;

public class WorkspaceExplorerView extends ViewPart implements IRefreshableView {
	
	public static final String VIEW_ID = "com.sap.dirigible.ide.workspace.ui.view.WorkspaceExplorerView";	
	
	private static final String CHECK_LOGS_FOR_MORE_INFO = Messages.WorkspaceExplorerView_CHECK_LOGS_FOR_MORE_INFO;

	private static final String COULD_NOT_EXECUTE_OPEN_COMMAND_DUE_TO_THE_FOLLOWING_ERROR = Messages.WorkspaceExplorerView_COULD_NOT_EXECUTE_OPEN_COMMAND_DUE_TO_THE_FOLLOWING_ERROR;

	private static final String OPERATION_FAILED = Messages.WorkspaceExplorerView_OPERATION_FAILED;

	private static final String COULD_NOT_EXECUTE_OPEN_COMMAND = Messages.WorkspaceExplorerView_COULD_NOT_EXECUTE_OPEN_COMMAND;

	private static final Logger logger = Logger
			.getLogger(WorkspaceExplorerView.class);

	private static final String OPEN_COMMAND_ID = "com.sap.dirigible.ide.workspace.ui.commands.OpenHandler"; //$NON-NLS-1$

	private WorkspaceViewer viewer = null;

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new WorkspaceViewer(parent, SWT.MULTI);
		viewer.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				onWorkspaceViewerDoubleClicked(event);
			}
		});
		getSite().setSelectionProvider(viewer.getSelectionProvider());
		getSite().registerContextMenu(
				"com.sap.dirigible.ide.workspace.ui.view.Menu", //$NON-NLS-1$
				viewer.getMenuManager(), viewer.getSelectionProvider());

		setSelectedProjectFromRequest();

	}

	private void setSelectedProjectFromRequest() {
		try {
			String projectName = RWT.getRequest().getParameter(
					CommonParameters.PARAMETER_PROJECT);
			if (projectName != null) {
				List<Object> selected = new ArrayList<Object>();
				TreeItem[] treeItems = viewer.getViewer().getTree().getItems();
				for (int i = 0; i < treeItems.length; i++) {
					TreeItem treeItem = treeItems[i];
					Object treeObject = treeItem.getData();
					if (treeObject instanceof IProject) {
						if (projectName.equals(((IProject) treeObject)
								.getName())) {
							selected.add(treeObject);
							break;
						}
					}
				}

				viewer.getViewer().setExpandedElements(
						selected.toArray(new Object[] {}));
			}
		} catch (Exception e) {
			// do nothing - just usability feature, which should not bother user
			// when breaks
		}
	}

	private void onWorkspaceViewerDoubleClicked(DoubleClickEvent event) {
		ICommandService commandService = (ICommandService) getSite()
				.getService(ICommandService.class);
		IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);
		Command command = commandService.getCommand(OPEN_COMMAND_ID);
		ExecutionEvent executionEvent = handlerService.createExecutionEvent(
				command, null);
		try {
			command.executeWithChecks(executionEvent);
		} catch (Exception ex) {
			logger.error(COULD_NOT_EXECUTE_OPEN_COMMAND, ex);
			MessageDialog.openError(null, OPERATION_FAILED,
					COULD_NOT_EXECUTE_OPEN_COMMAND_DUE_TO_THE_FOLLOWING_ERROR
							+ ex.getMessage() + CHECK_LOGS_FOR_MORE_INFO);
		}
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Object element : structuredSelection.toArray()) {
				if (element instanceof IFolder
						|| element instanceof IProject) {
					WorkspaceViewerUtils.doubleClickedElement(element);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFocus() {
		viewer.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	public void refresh() {
		viewer.refresh();
	}
	
	public WorkspaceViewer getViewer() {
		return viewer;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IRefreshableView.class.equals(adapter)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	protected IWorkspace getWorkspace() {
		return RemoteResourcesPlugin.getWorkspace();
	}

}
