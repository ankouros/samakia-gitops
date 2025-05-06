# samakia-gitops

This repository contains GitOps manifests for the samakia cluster.

## Structure

- `.argocd/root-app.yaml`: ArgoCD root Application-of-Apps pointing to the dev environment.
- `infrastructure/`: Cluster-level manifests & Helm values.
- `services/`: Microservices (users, species, birds, pairs, portal).
- `environments/dev/apps-argo.yaml`: Dev environment Application-of-Apps.
- `environments/prod/apps-argo.yaml`: Prod environment Application-of-Apps.
