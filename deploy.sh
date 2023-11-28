kubectl create namespace mandelbrot

kubectl apply -f process-service/kubernetes/deployment.yaml -n mandelbrot
kubectl apply -f process-service/kubernetes/service.yaml -n mandelbrot
kubectl apply -f process-service/kubernetes/ingress.yaml -n mandelbrot

kubectl apply -f ui-service/kubernetes/deployment.yaml -n mandelbrot
kubectl expose deployment/ui-service --type=LoadBalancer --port 8180 --target-port 8080 --name=ui-service -n mandelbrot