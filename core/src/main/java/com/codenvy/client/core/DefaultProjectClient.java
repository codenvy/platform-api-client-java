/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.client.Entity.text;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.fromStatusCode;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codenvy.client.ProjectClient;
import com.codenvy.client.Request;
import com.codenvy.client.core.RequestResponseAdaptor.Adaptor;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultProject;
import com.codenvy.client.model.Project;

/**
 * The Codenvy project API client.
 *
 * @author Kevin Pollet
 * @author Stéphane Daviet
 */
public class DefaultProjectClient extends AbstractClient implements ProjectClient {
    /**
     * Constructs an instance of {@link DefaultProjectClient}.
     *
     * @param url the Codenvy platform URL.
     * @param authenticationManager the {@link AuthenticationManager}.
     * @throws NullPointerException if url or authenticationManager parameter is {@code null}.
     */
    DefaultProjectClient(String url, AuthenticationManager authenticationManager) {
        super(url, "project", authenticationManager);
    }

    /**
     * Retrieves all workspace {@link Project}.
     *
     * @param workspaceId the workspace id.
     * @return the workspace {@link Project} list never {@code null}.
     * @throws NullPointerException if workspaceId parameter is {@code null}.
     */
    @Override
    public Request<List<? extends Project>> getWorkspaceProjects(String workspaceId) {
        checkNotNull(workspaceId);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<List<? extends Project>>(request, new GenericType<List<DefaultProject>>() {
        }, getAuthenticationManager());
    }

    /**
     * Creates a {@link Project} in the given workspace.
     *
     * @param project the {@link Project} to create.
     * @return the new {@link Project}, never {@code null}.
     * @throws NullPointerException if project parameter is {@code null}.
     */
    @Override
    public Request<? extends Project> create(Project project) {
        checkNotNull(project);

        final Invocation request = getWebTarget().path(project.workspaceId())
                                                 .queryParam("name", project.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(json(project));

        return new SimpleRequest<>(request, DefaultProject.class, getAuthenticationManager());
    }

    /**
     * Exports a resource in the given {@link Project}.
     *
     * @param project the {@link Project}.
     * @param resourcePath the path of the resource to export, must be a folder.
     * @return the resource {@link ZipInputStream} or {@code null} if the resource is not found.
     * @throws NullPointerException if project parameter is {@code null}.
     */
    @Override
    public Request<ZipInputStream> exportResources(Project project, String resourcePath) {
        checkNotNull(project);

        final Invocation request = getWebTarget().path(project.workspaceId())
                                                 .path("export")
                                                 .path(project.name())
                                                 .path(resourcePath == null ? "" : resourcePath)
                                                 .request()
                                                 .accept("application/zip")
                                                 .buildGet();

        return new RequestResponseAdaptor<>(new SimpleRequest<>(request, InputStream.class, getAuthenticationManager()),
                                            new Adaptor<ZipInputStream, InputStream>() {
                                                @Override
                                                public ZipInputStream adapt(InputStream response) {
                                                    return new ZipInputStream(response);
                                                }
                                            });
    }

    /**
     * Deletes a resource in the given {@link Project}.
     * 
     * @param project the {@link Project}.
     * @param resourcePath the path of the resource to delete.
     * @return the {@link Request} pointing to a {@link Void} result.
     * @throws NullPointerException if project parameter is {@code null}.
     */
    @Override
    public Request<Void> deleteResources(Project project, String resourcePath) {
        checkNotNull(project);

        final Invocation request = getWebTarget().path(project.workspaceId())
                                                 .path(project.name())
                                                 .path(resourcePath == null ? "" : resourcePath)
                                                 .request()
                                                 .buildDelete();

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Upload a local ZIP folder.
     *
     * @param workspaceId the workspace id in which the ZIP folder will be imported.
     * @param project the pre-exisiting {@link Project} in which the archive content should be imported.
     * @param archiveInputStream the archive {@link InputStream}.
     * @return the {@link Request} pointing to a {@link Void} result.
     * @throws NullPointerException if workspaceId, projectName or archiveInputStrem parameters are {@code null}.
     */
    @Override
    public Request<Void> importArchive(String workspaceId, Project project, InputStream archiveInputStream) {
        checkNotNull(workspaceId);
        checkNotNull(project);
        checkNotNull(archiveInputStream);

        final Invocation request = getWebTarget().path(workspaceId)
                                                 .path("import")
                                                 .path(project.name())
                                                 .request()
                                                 .buildPost(entity(archiveInputStream, "application/zip"));

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Updates a resource in the given {@link Project}.
     *
     * @param project the {@link Project}.
     * @param filePath the path to the file to update.
     * @param fileInputStream the file {@link InputStream}.
     * @throws NullPointerException if project, filePath or fileInputStream parameter is {@code null}.
     */
    @Override
    public Request<Void> updateFile(Project project, String filePath, InputStream fileInputStream) {
        checkNotNull(project);
        checkNotNull(filePath);
        checkNotNull(fileInputStream);

        final Invocation request = getWebTarget().path(project.workspaceId())
                                                 .path("file")
                                                 .path(project.name())
                                                 .path(filePath)
                                                 .request()
                                                 .buildPut(text(fileInputStream));

        return new SimpleRequest<>(request, Void.class, getAuthenticationManager());
    }

    /**
     * Gets file content in the given {@link Project}.
     *
     * @param project the {@link Project}.
     * @param filePath the file path.
     * @return the file {@link InputStream} or {@code null} if not found.
     */
    @Override
    public Request<InputStream> getFile(Project project, String filePath) {
        checkNotNull(project);
        checkNotNull(filePath);

        final Invocation request = getWebTarget().path(project.workspaceId())
                                                 .path("file")
                                                 .path(project.name())
                                                 .path(filePath)
                                                 .request()
                                                 .accept(TEXT_PLAIN)
                                                 .buildGet();

        return new SimpleRequest<>(request, InputStream.class, getAuthenticationManager());
    }

    /**
     * Returns if the given resource exists in the given {@link Project}.
     *
     * @param project the {@link Project}.
     * @param resourcePath the resource path.
     * @return {@code true} if the given resource exists in the Codenvy project, {@code false} otherwise.
     * @throws NullPointerException if project or resourcePath parameter is {@code null}.
     */
    @Override
    public Request<Boolean> isResource(Project project, String resourcePath) {
        checkNotNull(project);
        checkNotNull(resourcePath);

        final Invocation request = getWebTarget().path(project.workspaceId())
                                                 .path("file")
                                                 .path(project.name())
                                                 .path(resourcePath)
                                                 .request()
                                                 .build("HEAD");

        return new RequestResponseAdaptor<>(new SimpleRequest<>(request, Response.class, getAuthenticationManager()),
                                            new Adaptor<Boolean, Response>() {
                                                @Override
                                                public Boolean adapt(Response response) {
                                                    // TODO check if better, bad request response is sent if resourcePath is a folder
                                                    final Status status = fromStatusCode(response.getStatus());
                                                    return status == Status.OK || status == Status.BAD_REQUEST;
                                                }
                                            });
    }
}