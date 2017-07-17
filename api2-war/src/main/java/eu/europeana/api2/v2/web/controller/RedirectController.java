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

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for redirects urls (e.g. values of edmIsShownAt and edmIsShownBy fields)
 */
@Controller
public class RedirectController {

    /**
     * Handles redirects urls (e.g. values of edmIsShownAt and edmIsShownBy fields)
     *
     * @param apiKey    optional is not checked at the moment
     * @param isShownAt required, the url where to redirect
     * @return
     * @throws IllegalArgumentException
     */
    @RequestMapping(value = {"/{apiKey}/redirect", "/{apiKey}/redirect.json", "/v2/{apiKey}/redirect", "/v2/{apiKey}/redirect.json"},
            method = RequestMethod.GET)
    public String handleRedirect(
            @PathVariable String apiKey,
            @RequestParam(value = "shownAt", required = true) String isShownAt) {

        if (StringUtils.isBlank(isShownAt)) {
            throw new IllegalArgumentException(
                    "Empty 'shownAt' in the request URL");
        }
        // Disabled while awaiting better implementation (ticket #1742)
        // apiLogService.logApiRequest(wskey, id, RecordType.REDIRECT, profile);

        // TODO this does a 302, but a 303 may be better!?
        return "redirect:" + isShownAt;
    }

}
