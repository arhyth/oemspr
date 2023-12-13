# Open Exchange Rates mirror (Java)

This is a Java implementation of the Open Exchange Rates free plan API (US base currency only) using [Spring Boot](https://github.com/spring-projects/spring-boot) and [Undertow](https://github.com/undertow-io/undertow).

The repository contains 3 modules:
1. `fetcher`, the mini binary to fetch and insert rates. This does not include retry logic and is designed to run "at most once" when run as a k8s cron job.
2. `server`, the OER mirror server.
3. `rates`, the common library code.

:exclamation: This code has only been tested to be successfully built on Java 17.