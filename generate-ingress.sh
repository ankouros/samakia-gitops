#!/bin/bash

BASE_DIR="argo/apps"
TLS_SECRET="samakia-wildcard-tls"
DOMAIN="samakia.lab"

for dir in "$BASE_DIR"/*/ ; do
    APP_NAME=$(basename "$dir")
    cat > "${dir}/ingress.yaml" <<EOF
apiVersion: traefik.containo.us/v1alpha1
kind: IngressRoute
metadata:
  name: ${APP_NAME}
  namespace: ${APP_NAME}
spec:
  entryPoints:
    - websecure
  routes:
    - match: Host(\`${APP_NAME}.${DOMAIN}\`)
      kind: Rule
      services:
        - name: ${APP_NAME}
          port: 80
  tls:
    secretName: ${TLS_SECRET}
EOF
    echo "Created IngressRoute: ${dir}/ingress.yaml"
done
