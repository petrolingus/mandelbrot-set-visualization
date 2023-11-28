# Результаты исследования

## Клиент вне кластера

### Архитектура с Kubernetes Service

Для доступа к обработчикам необходимо создать сервис с помощью команды:

```shell
kubectl expose deployment/process-service --type=LoadBalancer --port 8181 --target-port 8080 --name=process-service -n mandelbrot
```

Документация:
https://kubernetes.io/docs/tutorials/kubernetes-basics/expose/expose-intro/
https://kubernetes.io/docs/tutorials/stateless-application/expose-external-ip-address/

Тип сервиса может быть один из:

* LoadBalancer
* ClusterIP (по умолчанию) - не подходит
* NodePort

Пдюсы/минусы
График

QoS Class: BestEffort


QoS Class: -----




### Архитектура с Kubernetes Ingress

Плюсы/минусы
График




## Клиент внутри кластера

Архитектура с сервисом
Плюсы/минусы
График
Архитектура с ингрессом
Плюсы/ммнусы
График





Вероятность ошибки
Статистика по запросам(всего/успешных/ошибок) для сервиса и ингресса