/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.europeana.api2.web.controller.admin;

import eu.europeana.api2.model.admin.ApiKeyResponse;
import eu.europeana.api2.model.json.ApiNotImplementedYet;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.model.request.ApiKeyCreate;
import eu.europeana.api2.model.request.ApiKeyUpdate;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.web.exception.EmailServiceException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@RestController
@RequestMapping("/admin/apikey")
@SwaggerIgnore
public class ApiKeyController {

    private static final long DEFAULT_USAGE_LIMIT = 10000;

    @Resource
    private ApiKeyService apiKeyService;

    @RequestMapping(
            method = {RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse findAll(Principal principal) {
        return new ApiNotImplementedYet(principal.getName());
    }

    @RequestMapping(value = "/{apikey}",
            method = {RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse find(
            @PathVariable String apikey,
            Principal principal
    ) {
        ApiResponse apiResponse = new ApiKeyResponse();
        try {
            ApiKey apiKey = apiKeyService.findByID(apikey);
            // TODO Add to result
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return new ApiNotImplementedYet(principal.getName());
    }

    @RequestMapping(
            method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse createApiKey(
            @RequestBody ApiKeyCreate create,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            apiKeyService.createApiKey(
                    create.getEmail(),
                    DEFAULT_USAGE_LIMIT,
                    create.getApplication(),
                    create.getCompany(),
                    create.getFirstName(),
                    create.getLastName(),
                    create.getWebsite(),
                    create.getDescription()
            );
            response.success = true;
        } catch (DatabaseException | EmailServiceException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

    @RequestMapping(
            value = "/{apiKey}",
            method = {RequestMethod.PUT},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse updateApiKey(
            @PathVariable String apiKey,
            @RequestBody ApiKeyUpdate update,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            apiKeyService.updateApiKey(
                    apiKey,
                    update.getEmail(),
                    update.getUsageLimit() != null ? update.getUsageLimit() : DEFAULT_USAGE_LIMIT,
                    update.getApplication(),
                    update.getCompany(),
                    update.getFirstName(),
                    update.getLastName(),
                    update.getWebsite(),
                    update.getDescription()
            );
            response.success = true;
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

    @RequestMapping(
            value = "/{apiKey}/limit/{limit}",
            method = {RequestMethod.PUT},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse updateApiKeyUsageLimit(
            @PathVariable String apiKey,
            @PathVariable long limit,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            apiKeyService.changeLimit(apiKey, limit);
            response.success = true;
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

    @RequestMapping(
            value = "/{apiKey}",
            method = {RequestMethod.DELETE},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse deleteApiKey(
            @PathVariable String apiKey,
            Principal principal
    ) {
        ModificationConfirmation response = new ModificationConfirmation(principal.getName());
        try {
            apiKeyService.removeApiKey(apiKey);
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
    }

}
