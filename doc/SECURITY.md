# Securing Connector Endpoints

Security in Basic DataApp is implemented via Spring Security mechanism. This framework is responsible for login user and also for response headers.

SpringSecurity:

```
.headers().xssProtection().and().contentTypeOptions().and().frameOptions().sameOrigin()
```

Example for the response headers are:

```
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
X-Frame-Options: SAMEORIGIN
```

Execution Core Container (and TRUE Connector) has implemented basic security mechanisms for all "public" endpoints (those endpoints that will be reached from outside world).

There are 2 types of users:

## API user

This user is used to manipulate with connector Self Description document (endpoints behind)

```
/api/*
```

 and to get connector public self description document.
 
```
/  (root context)
```

Details about this user can be found in property file:

```
#API management credentials
application.user.api.username=apiUser
# 'password' encoded value
application.user.api.password=$2a$10$MQ5grDaIqDpBjMlG78PFduv.AMRe9cs0CNm/V4cgUubrqdGTFCH3m

```

Further more, there is a lock mechanism in place, that will lock user (requests originating from same IP) if there are several consecutive requests with failed authorization. This logic can be configured, and following properties can be used for that purpose:

```
#number of consecutive failed attempts
application.user.lock.maxattempts=5
# duration for how long user will be locked
application.user.lock.duration=30
# time unit used for locking user, possible values are: SECONDS,MINUTES,HOURS,DAYS
application.user.lock.unit=MINUTES
```

## Change default password

Using following endpoint, with API user, you can generate new password. 

```
/api/password/{password}
```

Once you get new encoded string, update password property and restart connector.

This endpoint can be used to reset password for both API and Connector user.


There is password validation in place, which will check if password is strong enough. Following properties can be used to configure "strength".

```
#Password length should be in between (for example) 8 and 16 characters, minLength = 0 to disable the rule, maxLength mandatory
application.password.validator.min-length=8
application.password.validator.maxLength=16
#minimum number of Upper-case characters allowed, 0 to disable the rule
application.password.validator.min-upper-case=1
#minimum number of Lower-case characters allowed, 0 to disable the rule
application.password.validator.min-lower-case=1
#minimum number of digit allowed, 0 to disable the rule
application.password.validator.min-digit=1
#minimum number of special characters allowed, 0 to disable the rule
application.password.validator.min-special=1
```