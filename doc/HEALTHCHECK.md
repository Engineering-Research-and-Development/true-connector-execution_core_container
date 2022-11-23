# TRUEConnector Health Check

TRUEConnector has logic to verify internal and external health check. This means that for internal health check availability of internal components availability is check and for audit log volume, free space is check. For external health check, availability (reachability) of external services is checked - DAPS and Clearing House.

If health check logic is turned on, by setting up the property

```
application.healthcheck.enabled=true
```
Then both internal and external health check must be evaluated as true; otherwise connector will not be in operational state and every IDS request will result in Rejection message with rejection reason - Temporary unavailable.

Once enabled, health check is executed in scheduled intervals. To change frequency of health evaluation, following property needs to be updated:

```
application.healthcheck.cron.expression=0 */5 * ? * *
```

Default value is configured to trigger logic on every 5th minute.

Each health evaluation is logged in log file, and you can find the reason which evaluation resulted in unhealthy status, and proceed with resolving that issue.


## Internal health check

Following health check logic includes following:

 * audit log volume free space
 * dataApp availability
 * usage control dataApp availability
 
### Audit log volume free space
 
When running in dockerized environment, volume for audit logs can be mounted, and TRUEConnector will check the amount of remaining space on that volume. Logic will read audit **file** location from logback.xml file

```xml
<appender name="FILE" class="ch.qos.logback.core.FileAppender">
	<file>/log/true_connector_audit.log</file>
	<append>true</append>
	<encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

Following property is used in logic for audit free space to set threshold, in percentage, when audit logic should be evaluated as false. This means that when threshold of 5% of free volume space is reached, TRUEConnecto will become unhealthy.

```
## Threshold in percentages - from max value
application.healthcheck.threshold.audit=5
```

If you set value for audit file to be relative and not absolute, then this logic for audit volume free space will be skipped, and evaluated as true.

### DataApp availability

DataApp availability logic relies on property:

```
application.healthcheck.dataapp=https://localhost:8083/about/version
```

This URL will be used to "ping" dataApp, and if response of HTTP status 200 is received, logic will be evaluated as true, otherwise as false.


### Usage control dataApp availability

Like with dataApp health check, same principle is used to evaluate Usage control availability, and for that logic 
 
```
application.healthcheck.usagecontrol=https://localhost:8080/platoontec/PlatoonDataUsage/1.0/about/version
```

### Events

Upon successful health check evaluation, TRUEConnector will fire **CONNECTOR_INTERNAL_HEALTHY** event, meaning *"Connector internal state is healthy"*, and upon evaluating health check as false - **CONNECTOR_INTERNAL_UNHEALTHY**, meaning *"Connector internal state is unhealthy"*.

## External health check

Following health check logic includes following:

 * DAPS availability
 * Clearing House availability

### DAPS availability

When DAPS interaction is enabled, logic for evaluating DAPS availability will be executed, and property:

```
application.healthcheck.daps=${application.dapsJWKSUrl}
```

will be used to send request and check response - HTTP status 200, meaning DAPS can be reached. If other response than 200 is received, it will report that DAPS is not available, and check will be evaluated as false.

Default URL uses link for DAPS jwks. If needed, this value can be changed and used some other URL for checking DAPS avilability.

If DAPS interaction is disabled - check will be evaluated as true.

### Clearing House availability

When Clearing House interaction is enabled, logic for evaluating Clearing House availability will be executed, and property:

```
application.healthcheck.clearinghouse=
```

will be used to send GET request and check response - HTTP status 200, meaning Clearing House can be reached. If other response than 200 is received, it will report that Clearing House is not available, and check will be evaluated as false.

If Clearing House interaction is disabled - check will be evaluated as true.

### Events

Upon successful health check evaluation, TRUEConnector will fire **CONNECTOR_EXTERNAL_HEALTHY** event, meaning *"Connector external state is healthy"*, and upon evaluating health check as false - **CONNECTOR_EXTERNAL_UNHEALTHY**, meaning *"Connector external state is unhealthy"*.
