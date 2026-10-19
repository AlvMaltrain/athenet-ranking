# Muestras de resultados

Copias de `api-athenet-notifications/src/test/resources/contracts/competition/result-v1.json` y `result-v2.json`, que conservan muestras del publicador de Competencia. Incluyen nombres/ronda para probar compatibilidad hacia delante.

`ResultadoRegistradoContractTest` puede leer los archivos generados por la prueba del productor actual con `-Dranking.contract.fixture-directory=<ruta>/api-athenet-notifications/target/contracts/competition`. Primero ejecutar `CompetitionProducerContractTest` de Notificaciones con `notifications.contract.competition-source` apuntando a Competencia. Así se contrasta el mensaje real, además de las muestras versionadas.
