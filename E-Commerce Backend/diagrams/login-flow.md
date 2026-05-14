            HTTP Request
                ↓
    Filter (UsernamePasswordAuthenticationFilter)
                ↓
    Authentication Token (unauthenticated)
                ↓
        AuthenticationManager
                ↓
        AuthenticationProvider
                ↓
    UserDetailsService + Password check
                ↓
    Authenticated Token returned
                ↓
    SecurityContextHolder updated
                ↓
    Request continues as AUTHENTICATED