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

package com.sap.dirigible.ide.publish;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;

import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.common.io.StreamUtil;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.repository.RepositoryFacade;
import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.IEntity;
import com.sap.dirigible.repository.api.IRepository;

public abstract class AbstractPublisher implements IPublisher {

	private static final String PUBLISH_OF_FILE_S_BY_S_OVERRIDES_THE_PREVIOUS_MODIFICATIONS_MADE_BY_S = Messages
			.getString("AbstractPublisher.PUBLISH_OF_FILE_S_BY_S_OVERRIDES_THE_PREVIOUS_MODIFICATIONS_MADE_BY_S"); //$NON-NLS-1$
	private static final String PUBLISH_OF_FILE_S_SKIPPED = Messages
			.getString("AbstractPublisher.PUBLISH_OF_FILE_S_SKIPPED"); //$NON-NLS-1$
	private static final String FILE_WITH_THE_SAME_NAME_ALREADY_PUBLISHED_BY_ANOTHER_USER_S_DO_YOU_WANT_TO_OVERRIDE_IT_ANYWAY = Messages
			.getString("AbstractPublisher.FILE_WITH_THE_SAME_NAME_ALREADY_PUBLISHED_BY_ANOTHER_USER_S_DO_YOU_WANT_TO_OVERRIDE_IT_ANYWAY"); //$NON-NLS-1$
	private static final String PUBLISH_OF_FOLDER_S_BY_S_OVERRIDES_THE_PREVIOUS_MODIFICATIONS_MADE_BY_S = Messages
			.getString("AbstractPublisher.PUBLISH_OF_FOLDER_S_BY_S_OVERRIDES_THE_PREVIOUS_MODIFICATIONS_MADE_BY_S"); //$NON-NLS-1$
	private static final String FOLDER_WITH_THE_SAME_NAME_ALREADY_PUBLISHED_BY_ANOTHER_USER_S_DO_YOU_WANT_TO_OVERRIDE_IT_ANYWAY = Messages
			.getString("AbstractPublisher.FOLDER_WITH_THE_SAME_NAME_ALREADY_PUBLISHED_BY_ANOTHER_USER_S_DO_YOU_WANT_TO_OVERRIDE_IT_ANYWAY"); //$NON-NLS-1$
	private static final String PUBLISH_OF_FOLDER_S_SKIPPED = Messages
			.getString("AbstractPublisher.PUBLISH_OF_FOLDER_S_SKIPPED"); //$NON-NLS-1$
	
	private static final String PUBLISH = Messages.getString("AbstractPublisher.PUBLISH"); //$NON-NLS-1$
	public static final Logger logger = Logger.getLogger(AbstractPublisher.class);

	protected ICollection getTargetProjectContainer(IProject project, String registryLocation)
			throws IOException {
		final IRepository repository = RepositoryFacade.getInstance().getRepository();
		final ICollection publishContainer = repository.getCollection(registryLocation);
		final ICollection projectContainer = publishContainer.getCollection(project.getName());
		String user = getUser();
		if (projectContainer.exists()) {
			if (!checkOverridePermissionsForFolder(project.getName(), user, projectContainer)) {
				return projectContainer;
			}
		} else {
			projectContainer.create();
		}
		return projectContainer;
	}
	
	protected com.sap.dirigible.repository.api.IResource getTargetFileLocation(IFile file, String registryLocation)
			throws IOException {
		final IRepository repository = RepositoryFacade.getInstance().getRepository();
		final ICollection publishContainer = repository.getCollection(registryLocation);
		final ICollection projectContainer = publishContainer.getCollection(file.getProject().getName());
		final com.sap.dirigible.repository.api.IResource fileResource = projectContainer.getResource(file.getProjectRelativePath().removeFirstSegments(1).toString());
		return fileResource;
	}

	protected IFolder getSourceFolder(IProject project, String sourceFolderName) {
		return project.getFolder(sourceFolderName);
	}
	
	protected void copyAllFromTo(IContainer source, ICollection target) throws CoreException,
			IOException {

		if (!source.exists()) {
			return;
		}

		String user = getUser();

		synchronizeRepositoryWithWorkspace(source, target);

		for (IResource resource : source.members()) {
			if (resource instanceof IFolder) {
				copyFolderInto((IFolder) resource, target, user);
			}
			if (resource instanceof IFile) {
				copyFileInto((IFile) resource, target, user);
			}
		}
	}

	private void synchronizeRepositoryWithWorkspace(IContainer source, ICollection target)
			throws IOException, CoreException {
		for (IEntity repositoryResource : target.getChildren()) {
			if (repositoryResource.exists()) {
				boolean shouldDelete = true;
				String repositoryMemberPath = repositoryResource.getName();
				for (IResource member : source.members()) {
					String sourceMemberPath = member.getName();

					if (repositoryMemberPath.equals(sourceMemberPath)) {
						shouldDelete = false;
						break;
					}
				}
				if (shouldDelete) {
					repositoryResource.delete();
				}
			}
		}
	}

	protected String getUser() {
		String user = RWT.getRequest().getRemoteUser();
		if (user == null) {
			user = CommonParameters.GUEST_USER;
		}
		return user;
	}

	protected void copyFolderInto(IFolder folder, ICollection target, String user)
			throws IOException, CoreException {
		final ICollection targetFolder = target.getCollection(folder.getName());
		if (!checkOverridePermissionsForFolder(folder.getName(), user, targetFolder)) {
			return;
		}

		if (!targetFolder.exists()) {
			targetFolder.create();
		}
		copyAllFromTo(folder, targetFolder);
	}

	private boolean checkOverridePermissionsForFolder(String folderName, String user,
			final ICollection targetFolder) throws IOException {
		if (targetFolder.exists() && targetFolder.getInformation().getModifiedBy() != null
				&& !"".equals(targetFolder.getInformation().getModifiedBy()) //$NON-NLS-1$
				&& !"null".equalsIgnoreCase(targetFolder.getInformation().getModifiedBy()) //$NON-NLS-1$
				&& !targetFolder.getInformation().getModifiedBy().equalsIgnoreCase(user)) {

			boolean override = MessageDialog
					.openConfirm(
							null,
							PUBLISH,
							String.format(
									FOLDER_WITH_THE_SAME_NAME_ALREADY_PUBLISHED_BY_ANOTHER_USER_S_DO_YOU_WANT_TO_OVERRIDE_IT_ANYWAY,
									targetFolder.getInformation().getModifiedBy()));
			if (!override) {
				logger.debug(String.format(PUBLISH_OF_FOLDER_S_SKIPPED, folderName));
				return false;
			}
			logger.warn(String.format(
					PUBLISH_OF_FOLDER_S_BY_S_OVERRIDES_THE_PREVIOUS_MODIFICATIONS_MADE_BY_S,
					folderName, user, targetFolder.getInformation().getModifiedBy()));
		}
		return true;
	}

	protected void copyFileInto(IFile file, ICollection target, String user) throws IOException,
			CoreException {
		final com.sap.dirigible.repository.api.IResource targetResource = target.getResource(file
				.getName());
		if (!checkOverridePermissionsForResource(file.getName(), user, targetResource)) {
			return;
		}

		com.sap.dirigible.repository.api.IResource resource = target.getRepository().getResource(
				file.getWorkspace().getRoot().getRawLocation() + file.getFullPath().toString());

		if (targetResource.exists()) {
			Date targetResourceLastModifiedAt = targetResource.getInformation().getModifiedAt();
			Date resourceLastModifiedAt = resource.getInformation().getModifiedAt();

			if (resourceLastModifiedAt.after(targetResourceLastModifiedAt)) {
				setTargetResourceContent(file, targetResource, resource);
			}

		} else {
			setTargetResourceContent(file, targetResource, resource);
		}

	}

	private void setTargetResourceContent(IFile file,
			final com.sap.dirigible.repository.api.IResource targetResource,
			com.sap.dirigible.repository.api.IResource resource) throws IOException, CoreException {
			targetResource.setContent(readFile(file), resource.isBinary(), resource.getContentType());
	}

	private boolean checkOverridePermissionsForResource(String fileName, String user,
			final com.sap.dirigible.repository.api.IResource targetResource) throws IOException {
		if (targetResource.exists() && targetResource.getInformation().getModifiedBy() != null
				&& !"".equals(targetResource.getInformation().getModifiedBy()) //$NON-NLS-1$
				&& !"null".equalsIgnoreCase(targetResource.getInformation().getModifiedBy()) //$NON-NLS-1$
				&& !targetResource.getInformation().getModifiedBy().equalsIgnoreCase(user)) {

			boolean override = MessageDialog
					.openConfirm(
							null,
							PUBLISH,
							String.format(
									FILE_WITH_THE_SAME_NAME_ALREADY_PUBLISHED_BY_ANOTHER_USER_S_DO_YOU_WANT_TO_OVERRIDE_IT_ANYWAY,
									targetResource.getInformation().getModifiedBy()));
			if (!override) {
				logger.debug(String.format(PUBLISH_OF_FILE_S_SKIPPED, fileName));
				return false;
			}

			logger.warn(String.format(
					PUBLISH_OF_FILE_S_BY_S_OVERRIDES_THE_PREVIOUS_MODIFICATIONS_MADE_BY_S,
					fileName, user, targetResource.getInformation().getModifiedBy()));
		}
		return true;
	}

	protected byte[] readFile(IFile file) throws IOException, CoreException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final InputStream in = file.getContents();
		try {
			StreamUtil.transferData(in, out);
		} finally {
			in.close();
		}
		return out.toByteArray();
	}

	protected String generatePublishedPath(IFile file) {
		IProject project = file.getProject();
		IPath path = file.getProjectRelativePath();
		if (path != null && path.segmentCount() > 1) {
			path = path.removeFirstSegments(1);
			return IPath.SEPARATOR + project.getName() + IPath.SEPARATOR + path.toString();
		}
		return null;
	}

	public String getPublishedLocation(IFile file) {
		return CommonParameters.getRuntimeUrl() + IPath.SEPARATOR + CommonParameters.REGISTRY
				+ IPath.SEPARATOR + getFolderType() + generatePublishedPath(file);
	}
	
	public String getActivatedLocation(IFile file) {
		return CommonParameters.getRuntimeUrl() + IPath.SEPARATOR + CommonParameters.SANDBOX
				+ IPath.SEPARATOR + getFolderType() + generatePublishedPath(file);
	}

	public String getPublishedEndpoint(IFile file) {
		if (getPublishedContainerMapping(file) == null) {
			// no real container at runtime
			return null;
		}
		return CommonParameters.getRuntimeUrl() + getPublishedContainerMapping(file)
				+ generatePublishedPath(file);
	}
	
	public String getActivatedEndpoint(IFile file) {
		if (getActivatedContainerMapping(file) == null) {
			// no real container at runtime
			return null;
		}
		return (IRepository.SEPARATOR.equals(CommonParameters.getRuntimeUrl()) ? "" : CommonParameters.getRuntimeUrl()) 
				+ getActivatedContainerMapping(file)
				+ generatePublishedPath(file);
	}

	protected boolean checkFolderType(IFile file) {
		IPath path = file.getProjectRelativePath();
		if (path != null && path.segmentCount() > 0) {
			String folderTypeSegment = path.segment(0);
			if (getFolderType().equals(folderTypeSegment)) {
				return true;
			}
		}
		return false;
	}
	
	protected abstract String getSandboxLocation();	

	@Override
	public void activateFile(IFile file) throws PublishException {
		if (!recognizedFile(file)) {
			return;
		}
		try {
			final com.sap.dirigible.repository.api.IResource targetFile = getTargetFileLocation(file,
					getSandboxLocation());
			if (file.exists()) {
				copyFileInto(file, targetFile.getParent(), getUser());
			} else {
				if (targetFile.exists()) {
					targetFile.delete();
				}
			}
		} catch (Exception ex) {
			throw new PublishException(ex.getMessage(), ex);
		}
	}
	
	@Override
	public String getDebugEndpoint(IFile file) {
		return null;
	}

}
