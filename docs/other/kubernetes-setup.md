# Настройка Kubernetes

В работе используется автономный сервер и клиент Kubernetes из Docker Desktop:

> https://docs.docker.com/desktop/kubernetes/#:~:text=It%20runs%20within%20a%20Docker,not%20affect%20your%20other%20workloads.

Данный сервер не содержит пользовательского интерфейса (Kubernetes Dashboard) и требует его отдельной установки и настроки. В самой работе Kubernetes Dashboard не требуется, однако он может упростить некоторые действия, например чтение логов и т.п. 

## Установка Kubernetes Dashboard

> Установка Kubernetes Dashboard требует наличие kubectl (командной строки Kubernetes)

```txt
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
kubectl patch deployment kubernetes-dashboard -n kubernetes-dashboard --type 'json' -p '[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--enable-skip-login"}]'
kubectl delete clusterrolebinding serviceaccount-cluster-admin
kubectl create clusterrolebinding serviceaccount-cluster-admin --clusterrole=cluster-admin --user=system:serviceaccount:kubernetes-dashboard:kubernetes-dashboard
```

## Запуск Kubernetes Dashboard

Для того, чтобы можно было попать в кластер необходимо выполнить:
```txt
kubectl proxy
```

Kubectl proxy — это простая утилита командной строки, которая обеспечивает доступ к серверу API Kubernetes из кластера. Это помогает получить доступ к серверу API из модуля или из удаленного местоположения за пределами кластера.

После чего Kubernetes Dashboard будет доступен по следующему адресу:
http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/