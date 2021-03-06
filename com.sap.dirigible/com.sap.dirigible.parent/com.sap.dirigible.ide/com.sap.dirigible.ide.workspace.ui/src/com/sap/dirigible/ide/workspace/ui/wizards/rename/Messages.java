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

package com.sap.dirigible.ide.workspace.ui.wizards.rename;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.sap.dirigible.ide.workspace.ui.wizards.rename.messages"; //$NON-NLS-1$
	public static String RenameWizard_CHECK_LOGS_FOR_MORE_INFO;
	public static String RenameWizard_COULD_NOT_COMPLETE_RESOURCE_RENAME;
	public static String RenameWizard_COULD_NOT_COMPLETE_WIZARD_DUE_TO_THE_FOLLOWING_ERROR;
	public static String RenameWizard_OPERATION_ERROR;
	public static String RenameWizard_RENAME_WIZARD_TITLE;
	public static String RenameWizardModel_A_RESOURCE_WITH_THIS_NAME_ALREADY_EXISTS;
	public static String RenameWizardModel_COULD_NOT_RENAME_RESOURCE;
	public static String RenameWizardModel_INVALID_RESOURCE_NAME;
	public static String RenameWizardNamingPage_ENTER_NEW_NAME;
	public static String RenameWizardNamingPage_FILENAME_CANNOT_BE_NULL;
	public static String RenameWizardNamingPage_RENAME_WIZARD_NAMING_PAGE_DESCRIPTION;
	public static String RenameWizardNamingPage_RENAME_WIZARD_NAMING_PAGE_TITLE;
	public static String RenameWizardNamingPage_TRYING_TO_SET_FILENAME_TO_A_DISPOSED_OR;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
