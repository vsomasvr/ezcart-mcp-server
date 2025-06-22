package com.example.ezcart.mcp.server.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuth2TokenService {

    private final ReactiveOAuth2AuthorizedClientManager clientManager;
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ClientCredentialsOAuth2AuthorizedClientProvider clientCredentialTokenProvider = new ClientCredentialsOAuth2AuthorizedClientProvider();

    private static final String CLIENT_REGISTRATION_ID = "ezcart-backend";

    public OAuth2TokenService(ReactiveOAuth2AuthorizedClientManager clientManager, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientManager = clientManager;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal("ezcart-mcp-server")
                .build();

//        @TODO: troubleshoot issue with client manager
//        OAuth2AuthorizedClient authorizedClient = clientManager.authorize(authorizeRequest);
        OAuth2AuthorizedClient authorizedClient = getClientCredentialsAccessToken();
        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            return "Bearer " + authorizedClient.getAccessToken().getTokenValue();
        }
        throw new IllegalStateException("Failed to obtain access token");
    }

    private OAuth2AuthorizedClient getClientCredentialsAccessToken() {
        var clientRegistration = this.clientRegistrationRepository
                .findByRegistrationId(CLIENT_REGISTRATION_ID);

        var authRequest = OAuth2AuthorizationContext.withClientRegistration(clientRegistration.block())
                .principal(new AnonymousAuthenticationToken("client-credentials-client", "client-credentials-client",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
                .build();
        return this.clientCredentialTokenProvider.authorize(authRequest);
    }
}
