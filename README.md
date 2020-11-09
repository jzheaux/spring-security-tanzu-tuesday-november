Spring Security for SPAs
------------------------

This repo demonstrates three main security concerns when building a Single-Page Application:

1. Cross-Origin requests with CORS
2. Cross-Site Request Forgery protection
3. OAuth 2.0-based Authentication and Authorization

To see it working, please start the three different applications, like so:

* To start the Authorization Server, run: `cd authz && ./mvnw spring-boot:run`
* To start the Resource Server (the REST API), run `cd sentiment && ./mvnw spring-boot:run`
* And, to start the SPA, run `cd app && ./mvnw spring-boot:run`

