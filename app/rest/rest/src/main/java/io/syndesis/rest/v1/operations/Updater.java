/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.operations;

import java.io.IOException;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.PATCH;
import io.syndesis.core.Json;
import io.syndesis.dao.manager.WithDataManager;
import io.syndesis.model.WithId;

public interface Updater<T extends WithId<T>> extends Resource, WithDataManager {

    @PUT
    @Path(value = "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    default void update(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull @Valid T obj) {
        getDataManager().update(obj);
    }

    @PATCH
    @Path(value = "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    default void patch(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull JsonNode patchJson) throws IOException {
        Class<T> modelClass = resourceKind().getModelClass();
        final T existing = getDataManager().fetch(modelClass, id);
        if( existing == null ) {
            throw new EntityNotFoundException();
        }

        JsonNode document = Json.reader().readTree(Json.writer().writeValueAsString(existing));

        // Attempt to apply the patch...
        final JsonMergePatch patch;
        try {
            patch = JsonMergePatch.fromJson(patchJson);
            document = patch.apply(document);
        } catch (JsonPatchException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }

        // Convert the Json back to an entity.
        T obj = Json.reader().forType(modelClass).readValue(Json.writer().writeValueAsBytes(document));

        // TODO: validate the updated obj before storing it/
        getDataManager().update(obj);
    }

}
