## Kubernetes Dashboard

### Installation and configuration

Install Kubernetes Dashboard:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```

Patch the dashboard to allow skipping login:
```bash
kubectl patch deployment kubernetes-dashboard -n kubernetes-dashboard --type 'json' -p '[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--enable-skip-login"}]'
```

RBAC configuration:
```bash
kubectl delete clusterrolebinding serviceaccount-cluster-admin
```
```bash
kubectl create clusterrolebinding serviceaccount-cluster-admin --clusterrole=cluster-admin --user=system:serviceaccount:kubernetes-dashboard:kubernetes-dashboard
```

Viewing the Kubernetes Dashboard:
```bash
kubectl proxy
```
Dashboard URL: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/


### Installing Kubernetes Metrics Server (optional)

Take the latest release from [the project's GitHub page](https://github.com/kubernetes-sigs/metrics-server/releases/).
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.6.3/components.yaml
```

This installs a variety of resources in your cluster, but again, we need to patch one of the deployments.
We need to add the --kubelet-insecure-tls argument to the metrics-server deployment, otherwise you'll see an error
saying something like unable to fetch metrics from node docker-desktop. The following command patches the deployment:
```bash
kubectl patch deployment metrics-server -n kube-system --type 'json' -p '[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--kubelet-insecure-tls"}]'
```

### Delete

Delete Kubernetes Dashboard:

```bash
kubectl delete -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```