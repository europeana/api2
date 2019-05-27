package eu.europeana.api2.web.controller;

import eu.europeana.corelib.db.service.ApiKeyService;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller for retrieving the model for and displaying the confirmation page for access to a protected resource.
 * @deprecated 2018-01-09 old MyEuropeana functionality*
 */
@Controller
@SessionAttributes("authorizationRequest")
@Deprecated
public class AccessConfirmationController {

    @Resource
    private ApiKeyService apiKeyService;

    @Resource
    private ApprovalStore approvalStore;

    @Resource(name = "api2_oauth2_clientDetailsService")
    private ClientDetailsService clientDetailsService;

    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model, Principal principal) throws Exception {
        AuthorizationRequest clientAuth = (AuthorizationRequest) model.remove("authorizationRequest");
        ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
        ApiKey key = apiKeyService.findByID(client.getClientId());
        model.put("auth_request", clientAuth);
        model.put("client", client);
        model.put("appName", StringUtils.defaultIfBlank(key.getApplicationName(), StringUtils.defaultIfBlank(key.getCompany(), key.getId())));
        Map<String, String> scopes = new LinkedHashMap<>();
        for (String scope : clientAuth.getScope()) {
            scopes.put(OAuth2Utils.SCOPE_PREFIX + scope, "false");
        }
        approvalStore.getApprovals(principal.getName(), client.getClientId()).stream()
                .filter(approval -> clientAuth.getScope().contains(approval.getScope()))
                .forEach(approval -> scopes.put(OAuth2Utils.SCOPE_PREFIX + approval.getScope(),
                        approval.getStatus() == Approval.ApprovalStatus.APPROVED ? "true" : "false"));
        model.put("scopes", scopes);
        return new ModelAndView("user/authorize", model);
    }

    @RequestMapping("/oauth/error")
    public String handleError(Map<String, Object> model) throws Exception {
        // We can add more stuff to the model here for JSP rendering. If the client was a machine then
        // the JSON will already have been rendered.
        model.put("message", "There was a problem with the OAuth2 protocol");
        return "user/error";
    }

    @RequestMapping(value = "/oAuthLogin")
    public String loginUserForm() {
        return "user/login";
    }

}
