package com.lnikkila.oidcsample;

/**
 * Simple utility class for storing OpenID Connect configuration. This should not be used in
 * production. If you want to hide your keys, you should obfuscate them using ProGuard (with added
 * manual obfuscation), DexGuard or something else.
 *
 * See this Stack Overflow post for some suggestions:
 * https://stackoverflow.com/a/14570989
 */
public final class Config {

    // TODO: Add the information you received from your OIDC provider below.

    public static final String clientId = "sandboxy";
    public static final String clientSecret = "43eb7e7f-2f1d-4bf9-99d9-0559f6c7be72";

    //Note android emulator host can be reached via 10.0.2.2 for default emulator
    public static final String authorizationServerUrl = "http://10.0.2.2:8082/auth/realms/eurodata-test/protocol/openid-connect/auth";
    public static final String tokenServerUrl = "http://10.0.2.2:8082/auth/realms/eurodata-test/protocol/openid-connect/token";
    public static final String userInfoUrl = "http://10.0.2.2:8082/auth/realms/eurodata-test/protocol/openid-connect/userinfo";

    // This URL doesn't really have a use with native apps and basically just signifies the end
    // of the authorisation process. It doesn't have to be a real URL, but it does have to be the
    // same URL that is registered with your provider.
    public static final String redirectUrl = "app://oidcsample.lnikkila.com";

    // The `offline_access` scope enables us to request Refresh Tokens, so we don't have to ask the
    // user to authorise us again every time the tokens expire. Some providers might have an
    // `offline` scope instead. If you get an `invalid_scope` error when trying to authorise the
    // app, try changing it to `offline`.
    public static final String[] scopes = {"openid", "profile", "offline_access"};

    public enum Flows
    {
        AuthorizationCode,  //http://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth
        Implicit,           //http://openid.net/specs/openid-connect-core-1_0.html#ImplicitFlowAuth
        Hybrid              //http://openid.net/specs/openid-connect-core-1_0.html#HybridFlowAuth
    }

    // The authorization flow type that determine the response_type authorization request should use.
    // One of the supported flows AuthorizationCode, Implicit or Hybrid.
    // For more info see http://openid.net/specs/openid-connect-core-1_0.html#Authentication
    public static final Flows flowType = Flows.AuthorizationCode; // KEYCLOAK specifc -> Hybrid flow not supported yet.

}
