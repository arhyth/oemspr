# Open Exchange Rates mirror (Java)

This is a Java implementation of the Open Exchange Rates free plan API (US base currency only) using [Spring Boot](https://github.com/spring-projects/spring-boot) and [Undertow](https://github.com/undertow-io/undertow).

The repository contains code for 2 binaries -- (1) the fetcher which is implemented as a simple command to be run as a kubernetes cronjob, and (2) the OER mirror server.