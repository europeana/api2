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

package eu.europeana.api2.v2.web.controller;

import eu.europeana.api2.utils.JsonUtils;
import eu.europeana.api2.v2.model.json.ModificationConfirmation;
import eu.europeana.api2.v2.model.json.request.ApiKeyRegistration;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.web.exception.EmailServiceException;
import eu.europeana.corelib.web.service.EmailService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * @author Willem-Jan Boogerd (www.eledge.net/contact).
 */
@Controller
public class ApiKeyController {

    private static final long DEFAULT_USAGE_LIMIT = 10000;

    @Resource
    private ApiKeyService apiKeyService;

    @Resource
    private EmailService emailService;

    @RequestMapping(value = "/apikey",
            method = {RequestMethod.POST, RequestMethod.PUT},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView createApiKey(@RequestBody ApiKeyRegistration registration,
                                     @RequestParam(value = "wskey", required = true) String wskey,
                                     @RequestParam(value = "callback", required = false) String callback) {
        ModificationConfirmation response = new ModificationConfirmation(wskey, "/apikey (POST)");
        try {
            ApiKey apiKey = apiKeyService.createApiKey(
                    registration.getEmail(),
                    DEFAULT_USAGE_LIMIT,
                    registration.getApplication(),
                    registration.getCompany(),
                    registration.getFirstName(),
                    registration.getLastName(),
                    registration.getWebsite(),
                    registration.getDescription()
            );
            emailService.sendRegisterApiNotifyUser(apiKey, Locale.ENGLISH);
            response.success = true;
        } catch (DatabaseException | EmailServiceException e) {
            response.success = false;
            response.error = e.getMessage();
        }
        return JsonUtils.toJson(response, callback);
    }

}
