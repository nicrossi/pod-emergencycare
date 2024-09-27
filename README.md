# pod-emergencycare

Trabajo Práctico Especial 1 - Sala de Emergencias

## Instrucciones

### Build

1. `cd` al root del proyecto.
2. Ejecutar el comando `mvn clean install`.
3. Descomprimir los builds del cliente y el servidor con los comandos: `tar -xzf tpe1-g6/client/target/tpe1-g6-client-2024.1Q-bin.tar.gz` y `tar -xzf tpe1-g6/server/target/tpe1-g6-server-2024.1Q-bin.tar.gz`, esto creara las carpetas `tpe1-g6-client-2024.1Q` y `tpe1-g6-server-2024.1Q`

### Iniciar el servidor

1. `cd` a `tpe1-g6-server-2024.1Q`.
2. Ejecutar el comando `chmod +x ./run-server.sh`
3. Correr el servidor usando `./run-server.sh`

### Usar el cliente

1. `cd` a `tpe1-g6-client-2024.1Q`.
2. Dar permisos de ejecucion a los scripts `./administrationClient.sh`, `./waitingRoomClient.sh`, `./emergencyCareClient.sh`, `./doctorPagerClient.sh` y `./queryClient.sh`

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

## Propuesta para los Repositories

### DoctorsRepo

Los doctores tiene una tabla, la key unica seria el Name.
Nota: Idle es Available

| Name    | Level | Availability | Registered |
|---------|-------|--------------|------------|
| John    | 3     | UNAVAILABLE  | true       |
| Paul    | 5     | AVAILABLE    | false      |
| Melanie | 4     | ATTENDING    | false      |

### RoomsRepo

Las habitaciones son simplemente un id incremental, pero puede estar bueno tener el campo State para saber si algun paciente esta ocupando la habitacion sin tener que revisar la tabla de Caring, aunque es posible que sea mas simple unir esta tabla con esa.

| Room | State    |
|------|----------|
| 1    | OCCUPIED |
| 2    | FREE     |
| 3    | OCCUPIED |
| 4    | FREE     |

### PatientsRepo

Cuando se agrega un paciente nuevo, agregar su nombre al WaitingRoomRepo.

| Name | Level |
|------|-------|
| Bar  | 5     |
| Foo  | 1     |
| Baz  | 3     |

### WaitingRoomRepo

[inserte aqui ese algoritmo para elegir pacientes: leer desde esta tabla el Name, buscar en la de pacientes su level y luego en la de doctors lo que corresponda]

| PatientName |
|-------------|
| Baz         |

### CaringRepo

Tomamos los unique keys de las tablas de doctors, patients y rooms.
Cuando un paciente es atendido por un doctor en una habitacion, se inserta una row aca, se cambia el campo State para el Room a `OCCUPIED` y el campo Availability en la del Doctor a `ATTENDING` (attending y unavailable es diferente...?).
Al finalizar la atencion al paciente, se saca la row de aca, se actualiza el estado del Room a `FREE`, el Doctor de regreso a `AVAILABLE` y se elimina al Paciente de su tabla.

| PatientName | DoctorName | Room |
|-------------|------------|------|
| Bar         | Paul       | 1    |
| Foo         | Melanie    | 3    |
