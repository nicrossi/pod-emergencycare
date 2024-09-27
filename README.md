# Trabajo Práctico Especial 1 - Sala de Emergencias

Programacion de Objetos Distribuidos

## Grupo 6:

- Rossi, Nicolas - 53225
- Ricarte, Matias Agustin - 58417
- Panighini, Franco - 61258

## Instrucciones

### Build

1. `cd` al root del proyecto.
2. Ejecutar el comando `mvn clean install`.
3. Descomprimir los builds del cliente y el servidor con los comandos: `tar -xzf tpe1-g6/client/target/tpe1-g6-client-2024.1Q-bin.tar.gz` y `tar -xzf tpe1-g6/server/target/tpe1-g6-server-2024.1Q-bin.tar.gz`, esto creara las carpetas `tpe1-g6-client-2024.1Q` y `tpe1-g6-server-2024.1Q`

### Iniciar el servidor

1. `cd` a `tpe1-g6-server-2024.1Q`.
2. Ejecutar el comando `chmod +x ./run-server.sh`
3. Correr el servidor usando en un puerto determinado`./run-server.sh -Dport=50051`

### Usar el cliente

1. `cd` a `tpe1-g6-client-2024.1Q`.
2. Dar permisos de ejecucion a los scripts `./administrationClient.sh`, `./waitingRoomClient.sh`, `./emergencyCareClient.sh`, `./doctorPagerClient.sh` y `./queryClient.sh` `./healthCheck.sh`

### Ejemplos de uso de los diversos comandos y sus parametros:

#### Servicio de Administración

- Para agregar un nuevo consultorio: `./administrationClient.sh -DserverAddress=localhost:50051 -Daction=addRoom`
- Para agregar un nuevo médico: `administrationClient.sh -DserverAddress=localhost:50051 -Daction=addDoctor -Ddoctor=John -Dlevel=3`
- Para modificar la disponibilidad de un médico: `administrationClient.sh -DserverAddress=localhost:50051 -Daction=setDoctor -Ddoctor=John -Davailability=available`
- Para consultar la disponibilidad de un médico: `administrationClient.sh -DserverAddress=localhost:50051 -Daction=checkDoctor -Ddoctor=John`

#### Servicio de Sala de Espera

- Para registrar un nuevo paciente: `./waitingRoomClient.sh -DserverAddress=localhost:50051 -Daction=addPatient -Dpatient=Foo -Dlevel=3`
- Para actualizar el nivel de emergencia de un paciente: `./waitingRoomClient.sh -DserverAddress=localhost:50051 -Daction=updateLevel -Dpatient=Foo -Dlevel=3`
- Para consultar la espera aproximada de un paciente: `./waitingRoomClient.sh -DserverAddress=localhost:50051 -Daction=checkPatient -Dpatient=Foo`

#### Servicio de Atención de Emergencias

- Para iniciar la atención de una emergencia en un consultorio: `./emergencyCareClient.sh -DserverAddress=localhost:50051 -Daction=carePatient -Droom=1`
- Para iniciar la atención de emergencias en los consultorios libres: `./emergencyCareClient.sh -DserverAddress=localhost:50051 -Daction=careAllPatients`
- Para finalizar la atención de una emergencia en un consultorio: `./emergencyCareClient.sh -DserverAddress=localhost:50051 -Daction=dischargePatient -Droom=1 -Ddoctor=John -Dpatient=Foo`

#### Servicio de Notificación al Personal

- Para registrar a un médico para ser notificado: `./doctorPagerClient.sh -DserverAddress=localhost:50051 -Daction=register -Ddoctor=John`
- Para anular registrar de un médico: `./doctorPagerClient.sh -DserverAddress=localhost:50051 -Daction=unregister -Ddoctor=John`

#### Servicio de Consulta

- Para generar un reporte sobre el estado actual de los consultorios: `./queryClient.sh -DserverAddress=localhost:50051 -Daction=queryRooms -DoutPath=../queryRooms.csv`
- Para generar un reporte sobre los pacientes esperando a ser atendidos: `./queryClient.sh -DserverAddress=localhost:50051 -Daction=queryWaitingRoom -DoutPath=../queryWaiting.csv`
- Para generar un reporte sobre las atenciones finalizadas: `./queryClient.sh -DserverAddress=localhost:50051 -Daction=queryCares -DoutPath=../queryCares.csv`

### HealthCheck

A modo de test, el servicio `HealthCheck` responde un status "OK". Para asegurarse que el servidor esta funcionando correctamente.

#### Test steps:

1. Iniciar el servidor
2. `./healthCheck.sh -DserverAddress=localhost:50051 -Daction=status`
