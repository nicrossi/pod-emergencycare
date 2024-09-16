# pod-emergencycare

Trabajo Práctico Especial 1 - Sala de Emergencias

[>Ideas drawing board<](https://www.tldraw.com/r/FzBWHkJTyw66oXS636WQC?d=v-950.34.1358.1323.page)

## Concurrent main client, Fase 1
Dentro del modulo del client pueden encontrar un package `service.strategy` donde agregue una lógica inical de como me imagino que puede el cliente invocar a los distintos servicios.
A modo de test, agregue un servicio `HealthCheck` que responde un status "OK". La idea fue, usar un patron strategy para que el cliente solo tenga que validar los datos de input, tomar un thread de un pool,
y luego ejecutar el servicio segun lo que haya llegado. 
Así como esta funciona pero faltaria agregar como pasar los parametros a ejecutar, y ver si este approach nos sirve. 

Test steps: 
1. Levantan el servidor
2. Corren el cliente pasandole los argumentos `-DserverAddress=localhost:50051 -Daction=status -Dservice=HealthCheck` y deberian tener una respuesta `HealthCheck, status: OK`

Si tienen dudas si el servidor funciona, pueden probarlo con el cliente de intelliJ haciendo
```
### Health Check
GRPC localhost:50051/healthCheckService.HealthCheckService/healthCheck
Content-Type: application/grpc
```

