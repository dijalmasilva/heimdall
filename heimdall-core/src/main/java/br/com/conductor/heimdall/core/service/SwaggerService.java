package br.com.conductor.heimdall.core.service;

/*-
 * =========================LICENSE_START==================================
 * heimdall-core
 * ========================================================================
 * Copyright (C) 2018 Conductor Tecnologia SA
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==========================LICENSE_END===================================
 */

import br.com.conductor.heimdall.core.dto.ResourceDTO;
import br.com.conductor.heimdall.core.entity.Api;
import br.com.conductor.heimdall.core.entity.Operation;
import br.com.conductor.heimdall.core.entity.Resource;
import br.com.conductor.heimdall.core.enums.HttpMethod;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.parser.Swagger20Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides methods to import and export Swagger.
 *
 * @author Filipe Germano
 * @author <a href="https://dijalmasilva.github.io" target="_blank">Dijalma Silva</a>
 */
@Service
public class SwaggerService {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private OperationService operationService;

    public Api importApiFromSwaggerJSON(Api api, String swaggerAsString, boolean override) throws IOException {

        List<Resource> resources;

        if (override) {
            resources = new ArrayList<>();
        } else {
            resources = resourceService.list(api.getId(), new ResourceDTO());
        }

        Swagger swagger = new Swagger20Parser().parse(swaggerAsString);

        readTags(swagger.getTags(), resources, api.getId());
        readPaths(swagger.getPaths(), api.getBasePath(), resources, api.getId());

        api.setResources(resources);

        return api;
    }

    private void readTags(List<Tag> tags, List<Resource> resources, Long apiId) {
        tags.forEach(tag -> {
            if (resourceThisTagNotExist(tag, resources)) {
                Resource resourceCreated = findResourceByTagOrCreate(tag, resources);
                resourceCreated = resourceService.save(apiId, resourceCreated);
                resources.add(resourceCreated);
            }
        });
    }

    private void readPaths(Map<String, Path> paths, String basePath, List<Resource> resources, Long apiId) {

        paths.forEach(((valuePath, pathItem) -> {
            io.swagger.models.Operation get = pathItem.getGet();
            io.swagger.models.Operation put = pathItem.getPut();
            io.swagger.models.Operation post = pathItem.getPost();
            io.swagger.models.Operation patch = pathItem.getPatch();
            io.swagger.models.Operation delete = pathItem.getDelete();

            if (Objects.nonNull(get)) {
                readOperation(valuePath, basePath, get, HttpMethod.GET, resources, apiId);
            }

            if (Objects.nonNull(put)) {
                readOperation(valuePath, basePath, put, HttpMethod.PUT, resources, apiId);
            }

            if (Objects.nonNull(post)) {
                readOperation(valuePath, basePath, post, HttpMethod.POST, resources, apiId);
            }

            if (Objects.nonNull(patch)) {
                readOperation(valuePath, basePath, patch, HttpMethod.PATCH, resources, apiId);
            }

            if (Objects.nonNull(delete)) {
                readOperation(valuePath, basePath, delete, HttpMethod.DELETE, resources, apiId);
            }

        }));
    }

    private boolean resourceThisTagNotExist(Tag tag, List<Resource> resources) {
        return resources.stream().noneMatch(resource -> tag.getName().equalsIgnoreCase(resource.getName()));
    }

    private Resource findResourceByTagOrCreate(Tag tag, List<Resource> resources) {
        return resources.stream().filter(r -> r.getName().equalsIgnoreCase(tag.getName())).findFirst().orElse(createResourceByTag(tag, null));
    }

    private Resource createResourceByTag(Tag tag, List<Operation> operations) {
        Resource resource = new Resource();
        resource.setName(tag.getName());
        resource.setDescription(tag.getDescription());
        resource.setOperations(Objects.nonNull(operations) ? operations : new ArrayList<>());

        return resource;
    }

    private boolean operationNotExist(io.swagger.models.Operation operation, HttpMethod method, String path, List<Operation> operations) {
        return operations.stream().noneMatch(op -> op.getDescription().equalsIgnoreCase(operation.getSummary()) && op.getMethod() == method && op.getPath().equalsIgnoreCase(path));
    }

    private Operation findOperationByOperationSwaggerOrCreate(io.swagger.models.Operation operation, HttpMethod method, String path, List<Operation> operations) {
        return operations.stream().filter(op -> op.getDescription().equalsIgnoreCase(operation.getSummary()) && op.getMethod() == method && op.getPath().equalsIgnoreCase(path))
                .findFirst()
                .orElse(createOperationByOperationSwagger(path, method, operation));
    }

    private Operation createOperationByOperationSwagger(String path, HttpMethod method, io.swagger.models.Operation operation) {
        Operation op = new Operation();
        op.setPath(path);
        op.setMethod(method);
        op.setDescription(operation.getSummary());

        return op;
    }

    private void readOperation(String valuePath, String basePath, io.swagger.models.Operation verb, HttpMethod method, List<Resource> resources, Long apiId) {

        verb.getTags().forEach(tagName -> {
            Tag tag = new Tag().name(tagName);
            Resource resource = findResourceByTagOrCreate(tag, resources);
            if (resourceThisTagNotExist(tag, resources)) {
                resource = resourceService.save(apiId, resource);
                resources.add(resource);
            }

            List<Operation> operations = resource.getOperations();
            String path = valuePath.replace(basePath, "");

            Operation operation = findOperationByOperationSwaggerOrCreate(verb, method, path, operations);

            if (operationNotExist(verb, method, path, operations)) {
                operation = operationService.save(apiId, resource.getId(), operation);
                operations.add(operation);
            }
        });
    }
}
