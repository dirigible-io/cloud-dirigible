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

package com.sap.dirigible.ide.db.viewer.views;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class DatabaseViewLabelProvider extends LabelProvider {

	private static final long serialVersionUID = 7440464384326831067L;

	private static final String ICONS_SEGMENT = "/icons/"; //$NON-NLS-1$

	private static final URL TYPE_SCHEMA_ICON_URL = getIconURL("icon-schema.png"); //$NON-NLS-1$

	private static final URL TYPE_TABLE_ICON_URL = getIconURL("icon-table.png"); //$NON-NLS-1$

	@SuppressWarnings("unused")
	private final DatabaseViewer databaseViewer;

	private final ResourceManager resourceManager;

	/**
	 * @param databaseViewer
	 */
	DatabaseViewLabelProvider(DatabaseViewer databaseViewer) {
		this.databaseViewer = databaseViewer;
		this.resourceManager = new LocalResourceManager(
				JFaceResources.getResources());
	}

	public String getText(Object obj) {
		return obj.toString();
	}

	public Image getImage(Object obj) {
		// String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		// if (obj instanceof TreeParent)
		// imageKey = ISharedImages.IMG_OBJ_FOLDER;
		// return PlatformUI.getWorkbench().getSharedImages()
		// .getImage(imageKey);
		if (obj instanceof TreeParent) {
			return createImage(TYPE_SCHEMA_ICON_URL);
		} else {
			return createImage(TYPE_TABLE_ICON_URL);
		}

	}

	private Image createImage(URL imageURL) {
		ImageDescriptor imageDescriptor = ImageDescriptor
				.createFromURL(imageURL);
		return resourceManager.createImage(imageDescriptor);
	}

	private static URL getIconURL(String iconName) {
		return DatabaseViewLabelProvider.class.getResource(ICONS_SEGMENT
				+ iconName);
	}

}