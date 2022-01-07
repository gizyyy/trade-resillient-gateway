# trade-resillient-gateway

This is a gateway project developed by using S0pring Could Gateway and Resillience4j.

Due to application.yml and GatewayConfig, the calls coming to gateway will be either forwarded to TraderClient overview or TraderServer main functionalities.

It's a good practice just to open one project to outside world, in order to handle cross cutting concerns in just one place.

If the overview call fails, gateway forwards to overview-fallback in resillient way, so the caller application can show a welcome page rather than an overview page.
