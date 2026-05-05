exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");

exchange.getResponse().getHeaders().add(
"Content-Security-Policy",
"default-src 'self'"
);