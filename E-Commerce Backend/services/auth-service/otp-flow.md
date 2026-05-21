OTP Requests:
1st → allowed
2nd (before 5 min) → blocked
After 5 min → allowed
After 3 total → blocked for 6h
OTP Handling:
New OTP → replaces old OTP → invalid automatically
Verification:
1st wrong → wrong
2nd wrong → wrong
3rd wrong → blocked 6h
Success: user enabled Redis cleaned

docker run --name my-redis -d -p 6379:6379 redis

Distributaion transaction implemation ways
stratergy pattern
// circuit breaker exception handle flow for each servies like databse, email