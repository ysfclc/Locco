# Network acquisition, local-network boundaries and egress policy

**Status:** Normative security contract proposal for LOC-0004. Do not claim browser-wide network isolation until the selected engine has passed the device adversarial suite.

## 1. Separate user browsing from background acquisition

These are different authorities and execution surfaces:

- **Direct user navigation:** a visible browser action initiated by the user. The user may intentionally open public or private/local addresses (for example, a home router or NAS). Display the normalized host, scheme and security status. Local/private address access requires a clear origin/target boundary and user consent when entered through a link or cross-origin navigation.
- **Background source acquisition:** an HTTP/API/file adapter fetches material for a task without an interactive browser page. It has no ambient browser cookies, site credentials or implicit local-network authority. It is restricted to user-authorized source records and the network scope granted to that adapter.
- **Browser-page initiated requests:** subresources, forms, redirects, pop-ups, service workers, WebSockets and script fetches originate from untrusted page code. The selected engine must be evaluated for private-network request protections; a navigation callback or WebView interception hook is not assumed to mediate every request path.

A user's permission to browse one site does not automatically authorize arbitrary background crawling, API calls, access to LAN services or credential reuse.

## 2. URL and address validation for background adapters

Before every request and redirect:
1. Parse and normalize the URL with a standards-compliant parser. Reject malformed/ambiguous authorities, user-info credentials, unsupported schemes and control characters.
2. Permit only explicitly registered schemes (normally HTTPS; HTTP only where the user has explicitly accepted the transport risk). Never fetch `file:`, `content:`, `javascript:`, `data:`, `intent:`, custom app schemes or Unix/local socket targets through a generic network adapter.
3. Resolve all A and AAAA records. Reject unspecified, loopback, link-local, private, carrier-grade NAT, multicast, reserved, broadcast, IPv4-mapped IPv6 and other non-global/special-use destinations by default. Check IPv4 and IPv6 consistently.
4. Pin the connection to a validated resolution for that request where the transport permits, retain hostname-based TLS certificate validation/SNI, and repeat resolution/validation for every redirect and retry. A preflight DNS check followed by an unconstrained second lookup is vulnerable to DNS rebinding.
5. Reject redirects that change scheme, cross an unapproved origin, move into a restricted address class or exceed a small bounded redirect count. Strip Authorization, cookies and other sensitive headers on origin change; preferably never attach user credentials to generic source fetches.
6. Enforce connect/read/total deadlines, response byte and decompression limits, content-type policy, concurrency/rate budgets and cancellation. Never parse an unbounded response in memory.
7. Record the requested origin, resolved address class (not unnecessary raw LAN IP telemetry), redirect chain, adapter policy version, consent scope, response hash and typed outcome. Do not log credentials or private payload text.

Use an explicit IP classification library/test suite rather than ad hoc string prefix tests. Re-check the platform/network stack behavior when the DNS resolver, HTTP client or Android version changes.

## 3. Explicit local-network source scope

Some legitimate user tasks require a router, printer, NAS, home server or development device. Support that through a separate user-created local-network source scope:
- The user names/chooses the host or address and the intended purpose.
- The grant is bound to that host/range, protocol, port set, operation type, expiry and current device/network context.
- It is not automatically inherited by arbitrary procedures or other websites.
- Network changes, VPN changes, captive portals or interface changes invalidate or revalidate the grant.
- A public page, model, catalogue entry or generated plan cannot grant itself local-network access.
- The user can inspect and revoke the scope; every access attempt is recorded locally.

Do not offer unrestricted LAN subnet scanning as a default feature. Discovery, when explicitly requested, is separately scoped, rate-limited and visible.

## 4. Native browser private-network behavior

Ordinary web pages may make cross-origin requests, open frames, redirect and use service workers. CORS limits response access but is not a complete request-side-effect firewall.

For WebView, GeckoView or another engine:
- Evaluate public-origin pages attempting requests/navigation to loopback, link-local, private IPv4, IPv6 ULA/link-local, router endpoints and platform services.
- Include main-frame navigation, iframe, image/form, fetch/XHR, WebSocket, service worker, popup, redirect, DNS-rebinding and mixed-address tests.
- Test both request emission and response readability; a blocked read is not proof that the request was never sent.
- Do not claim `shouldInterceptRequest` or a page-world DOM observer mediates every network path unless engine-specific evidence proves it, including service-worker and renderer behavior.
- Do not expose Android debugging ports, local admin endpoints, filesystem bridges or app-private services to page content.
- Require the selected engine to pass the stated baseline for declared support versions. If the engine cannot enforce the needed boundary, label the residual risk and restrict the relevant capability rather than asserting complete protection.

Browser navigation to a local address typed directly by the user can remain available through explicit confirmation; that allowance must not authorize background source adapters or unrelated page origins.

## 5. Model and catalogue egress

- Local model inference and runtime loading are offline by default. A model file is data, not executable code.
- Catalogue lookup and model downloads use separately declared network adapters, source revision pins, explicit egress and bounded transfer rules.
- Models cannot make arbitrary network requests through the runtime or add tools/plugins.
- Optional remote inference is an explicit provider integration with its own data disclosure, credential, retention, budget and consent record. It is never silently selected as a fallback for local inference.
- Diagnostics never export prompts, evidence, private URLs, cookies, tokens or model inputs without a separate user-initiated redacted export.

## 6. Required adversarial tests

- `network.reject-loopback-ipv4`, `network.reject-loopback-ipv6`, `network.reject-link-local`, `network.reject-private-and-cgnat`.
- `network.reject-ipv4-mapped-ipv6`, `network.reject-zone-id`, `network.reject-ambiguous-authority`, `network.reject-unsupported-scheme`.
- `network.dns-rebinding-pinned-resolution`, `network.redirect-revalidate-every-hop`, `network.origin-change-strip-sensitive-headers`.
- `network.redirect-public-to-private-blocked`, `network.bounded-response-decompression`, `network.timeout-cancel-rate-limit`.
- `browser.public-origin-private-network-request` across main frame, iframe, image/form, fetch/XHR, WebSocket, service worker, popup and redirects, recording request emission as well as CORS/read outcome.
- `network.local-scope-exact-host-port-expiry`, `network.local-scope-revocation`, `network.local-scope-invalidated-on-network-change`.
- `model.local-inference-no-egress`, `model.remote-provider-explicit-consent`, `model.no-catalogue-executable-plugin`.

