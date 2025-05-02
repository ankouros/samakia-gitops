#!/bin/bash

BASE_DIR="argo/apps"
OUTPUT_DIR="${BASE_DIR}/_generated"
GIT_REPO="git@github.com:ankouros/samakia-gitops.git"
BRANCH="main"
DEST_SERVER="https://kubernetes.default.svc"

mkdir -p "$OUTPUT_DIR"

for dir in "$BASE_DIR"/*/ ; do
    APP_NAME=$(basename "$dir")
    cat > "${OUTPUT_DIR}/${APP_NAME}.yaml" <<EOF
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: ${APP_NAME}
  namespace: argocd
spec:
  project: default
  source:
    repoURL: ${GIT_REPO}
    targetRevision: ${BRANCH}
    path: ${BASE_DIR}/${APP_NAME}
  destination:
    server: ${DEST_SERVER}
    namespace: ${APP_NAME}
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
EOF
    echo "Generated: ${OUTPUT_DIR}/${APP_NAME}.yaml"
done
