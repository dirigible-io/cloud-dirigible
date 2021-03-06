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

package com.sap.dirigible.runtime.content;

import java.io.IOException;

import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.ext.db.DsvUpdater;
import com.sap.dirigible.repository.ext.db.DatabaseUpdater;
import com.sap.dirigible.repository.ext.extensions.ExtensionUpdater;
import com.sap.dirigible.repository.ext.security.SecurityUpdater;
import com.sap.dirigible.runtime.repository.RepositoryFacade;

public class ContentPostImportUpdater {

	private IRepository repository;

	public ContentPostImportUpdater(IRepository repository) {
		this.repository = repository;
	}

	public IRepository getRepository() {
		return repository;
	}

	public void update() throws IOException, Exception {
		// 1. Execute the real database "create or update"
		DatabaseUpdater databaseUpdater = new DatabaseUpdater(getRepository(),
				RepositoryFacade.getInstance().getDataSource(),
				DatabaseUpdater.REGISTRY_DATA_STRUCTURES_DEFAULT);
		databaseUpdater.applyUpdates();

		// 2. Execute the real security "create or update"
		SecurityUpdater securityUpdater = new SecurityUpdater(getRepository(),
				RepositoryFacade.getInstance().getDataSource(),
				SecurityUpdater.REGISTRY_SECURITY_CONSTRAINTS_DEFAULT);
		securityUpdater.applyUpdates();
		
		// 3. Execute the real import from DSV files
		DsvUpdater dsvUpdater = new DsvUpdater(getRepository(),
				RepositoryFacade.getInstance().getDataSource(),
				DatabaseUpdater.REGISTRY_DATA_STRUCTURES_DEFAULT);
		dsvUpdater.applyUpdates();
		
		// 4. Extensions
		ExtensionUpdater extensionUpdater = new ExtensionUpdater(getRepository(),
				RepositoryFacade.getInstance().getDataSource(),
				ExtensionUpdater.REGISTRY_EXTENSION_DEFINITIONS_DEFAULT);
		extensionUpdater.applyUpdates();
		
	}

}
