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

package com.sap.dirigible.ide.publish.ui.view;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.sap.dirigible.ide.publish.IPublisher;
import com.sap.dirigible.ide.publish.PublishManager;
import com.sap.dirigible.ide.ui.widget.extbrowser.ExtendedBrowser;

public class WebViewerView extends ViewPart {

	private static final String REFRESH = Messages.RuntimeViewerView_REFRESH;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final URL DIRIGIBLE_REFRESH_ICON_URL = WebViewerView.class
			.getResource("/resources/icons/refresh.png"); //$NON-NLS-1$

	private final ISelectionListener selectionListener = new SelectionListenerImpl();

	private ExtendedBrowser browser = null;

	private final ResourceManager resourceManager;

	private Text pageUrlText;

	public WebViewerView() {
		super();
		resourceManager = new LocalResourceManager(
				JFaceResources.getResources());
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		attachSelectionListener(site);
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		final Composite holder = new Composite(parent, SWT.NONE);
		holder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		holder.setLayout(new GridLayout(2, false));

		createUrlAddressField(holder);
		createRefreshButton(holder);

		browser = new ExtendedBrowser(parent, SWT.NONE);
		browser.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		setUrl(EMPTY_STRING);
	}

	private void createRefreshButton(final Composite holder) {
		Button goButton = new Button(holder, SWT.PUSH);
		goButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		goButton.setToolTipText(REFRESH);
		goButton.setImage(createImage(DIRIGIBLE_REFRESH_ICON_URL));
		goButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 5640314767414360517L;

			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		});
	}
	
	public void refresh() {
		browser.setUrl(pageUrlText.getText());
		browser.refresh();
	}

	private void createUrlAddressField(Composite parent) {
		pageUrlText = new Text(parent, SWT.BORDER);
		pageUrlText
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void dispose() {
		detachSelectionListener(getSite());
		browser = null;
		super.dispose();
	}

	private void attachSelectionListener(IWorkbenchPartSite site) {
		if (site == null) {
			return;
		}
		final ISelectionService selectionService = getSelectionService(site);
		if (selectionService != null) {
			selectionService.addSelectionListener(selectionListener);
		}
	}

	private void detachSelectionListener(IWorkbenchPartSite site) {
		if (site == null) {
			return;
		}
		final ISelectionService selectionService = getSelectionService(site);
		if (selectionService != null) {
			selectionService.removeSelectionListener(selectionListener);
		}
	}

	private ISelectionService getSelectionService(IWorkbenchPartSite site) {
		final IWorkbenchWindow window = site.getWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getSelectionService();
	}

	private void handleElementSelected(Object element) {

		if (element instanceof IFile) {
			IFile file = (IFile) element;
			IPublisher publisher = getPublisher(file);
			if (publisher != null) {
				String activePerspectiveId = getActivePerspective();
				String endpoint = null;
				if ("debug".equals(activePerspectiveId)) {
					endpoint = publisher.getDebugEndpoint(file);
				} else {
					endpoint = publisher.getActivatedEndpoint(file);
				}
				
				if (endpoint != null) {
					setUrl(endpoint);
				}
			}

		}
	}

	private String getActivePerspective() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IPerspectiveDescriptor perspective = page.getPerspective();
		String id = perspective.getId();
		return id;
	}

	private IPublisher getPublisher(IFile file) {
		final List<IPublisher> publishers = PublishManager.getPublishers();

		for (Iterator<IPublisher> iterator = publishers.iterator(); iterator
				.hasNext();) {
			IPublisher publisher = (IPublisher) iterator.next();
			if (publisher.recognizedFile(file)) {
				return publisher;
			}
		}
		return null;
	}

	private class SelectionListenerImpl implements ISelectionListener {

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				selectionChanged((IStructuredSelection) selection);
			}
		}

		private void selectionChanged(IStructuredSelection selection) {
			Object element = selection.getFirstElement();
			if (element != null) {
				handleElementSelected(element);
			}
		}

	}

	private Image createImage(URL imageURL) {
		// TODO - cached?
		ImageDescriptor imageDescriptor = ImageDescriptor
				.createFromURL(imageURL);
		return resourceManager.createImage(imageDescriptor);
	}

	protected void setUrl(String text) {
		if (text == null || EMPTY_STRING.equals(text)) {
			return;
		}
		try {
			String url = RWT.getRequest().getScheme()
					+ "://" + RWT.getRequest().getServerName() //$NON-NLS-1$
					+ ":" //$NON-NLS-1$
					+ RWT.getRequest().getServerPort() + text;
			pageUrlText.setText(url);
			browser.setUrl(url);
			browser.refresh();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
