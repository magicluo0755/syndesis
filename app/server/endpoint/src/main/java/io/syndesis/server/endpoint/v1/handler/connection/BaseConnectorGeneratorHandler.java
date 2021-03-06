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
package io.syndesis.server.endpoint.v1.handler.connection;

import java.util.function.BiFunction;

import javax.persistence.EntityNotFoundException;

import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;

import org.springframework.context.ApplicationContext;

abstract class BaseConnectorGeneratorHandler extends BaseHandler {

    final ApplicationContext context;

    protected BaseConnectorGeneratorHandler(final DataManager dataMgr, final ApplicationContext context) {
        super(dataMgr);
        this.context = context;
    }

    final <T> T withGeneratorAndTemplate(final String templateId,
        final BiFunction<ConnectorGenerator, ConnectorTemplate, T> callback) {
        final ConnectorTemplate connectorTemplate = getDataManager().fetch(ConnectorTemplate.class, templateId);

        if (connectorTemplate == null) {
            throw new EntityNotFoundException("Connector template: " + templateId);
        }

        final ConnectorGenerator connectorGenerator = context.getBean(templateId, ConnectorGenerator.class);
        if (connectorGenerator == null) {
            throw new EntityNotFoundException(
                "Unable to find connector generator for connector template with id: " + templateId);
        }

        return callback.apply(connectorGenerator, connectorTemplate);
    }
}
