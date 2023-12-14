kubectl create namespace mandelbrot

kubectl apply -f process-service/kubernetes/deployment.yaml -n mandelbrot
kubectl apply -f process-service/kubernetes/service.yaml -n mandelbrot
kubectl apply -f process-service/kubernetes/ingress.yaml -n mandelbrot

kubectl apply -f ui-service/kubernetes/ui-service-service.yaml -n mandelbrot
kubectl apply -f ui-service/kubernetes/ui-service-ingress.yaml -n mandelbrot
kubectl expose deployment/ui-service-service --type=LoadBalancer --port 8180 --target-port 8080 --name=ui-service-service -n mandelbrot
kubectl expose deployment/ui-service-ingress --type=LoadBalancer --port 8280 --target-port 8080 --name=ui-service-ingress -n mandelbrot