package eu.europeana.api2.web.controller.admin;

import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.api2.model.request.admin.ApiKeyCreate;
import eu.europeana.api2.model.request.admin.ApiKeyUpdate;
import eu.europeana.api2.model.response.admin.ApiKeyResponse;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.web.swagger.SwaggerIgnore;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.web.exception.EmailServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@RestController
@RequestMapping("/api/admin/apikey")
@SwaggerIgnore
public class ApiKeyAdminController {

    private static final long DEFAULT_USAGE_LIMIT = 10000;

    @Resource
    private ApiKeyService apiKeyService;

    @RequestMapping(value = "/{term:.+}",
            method = {RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse find(
            @PathVariable String term,
            Principal principal
    ) {
        ApiKeyResponse response = new ApiKeyResponse(principal.getName());
        try {
            if (StringUtils.contains(term, "@")) {
                for (ApiKey apiKey : apiKeyService.findByEmail(term)) {
                    response.getApiKeys().add(toEntity(apiKey));
                }
            } else {
                ApiKey apiKey = apiKeyService.findByID(term);
                if (apiKey != null) {
                    response.getApiKeys().add(toEntity(apiKey));
                }
            }
        } catch (DatabaseException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return response;
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

         System.out.println("here createApiKey");
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
                    update.getUsageLimit(),
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

    private ApiKeyResponse.ApiKey toEntity(ApiKey apiKey) {
        if (apiKey != null) {
            ApiKeyResponse.ApiKey entity = new ApiKeyResponse().new ApiKey();
            entity.setPublicKey(apiKey.getId());
            entity.setPrivateKey(apiKey.getPrivateKey());
            entity.setApplication(apiKey.getApplicationName());
            entity.setCompany(apiKey.getCompany());
            entity.setDescription(apiKey.getDescription());
            entity.setEmail(apiKey.getEmail());
            entity.setFirstName(apiKey.getFirstName());
            entity.setLastName(apiKey.getLastName());
            entity.setWebsite(apiKey.getWebsite());
            entity.setUsageLimit(apiKey.getUsageLimit());
            entity.setLevel(apiKey.getLevel().toString());
            return entity;
        }
        return null;
    }

}
