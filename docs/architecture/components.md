# Architecture Components

This document describes each major component in the **samakia** GitOps-driven Kubernetes cluster, its purpose, and key configuration highlights.


---

## 1. Flannel (CNI)

**Role:** Provides a simple, performant overlay network for Pods.

* **Version**: v0.25.0
* **Configuration**:
  * Pod network CIDR: `10.244.0.0/16`
  * Deployed via the official \[kube-flannel.yml\] manifest.
* **Key Files**:
  * `infrastructure/networking/kustomization.yaml`
  * Flannel YAML from `https://raw.githubusercontent.com/flannel-io/flannel/v0.25.0/Documentation/kube-flannel.yml`


---

## 2. MetalLB (LoadBalancer)

**Role:** Implements a bare-metal LoadBalancer in L2 mode for Service `type=LoadBalancer`.

* **Version**: v0.13.8 (native manifests)
* **IPAddressPool**: `192.168.11.200–192.168.11.250`
* **Advertisement**: L2 advertisement for ARP/NDP announcements.
* **Key Files**:
  * `infrastructure/networking/metallb-config.yaml`
  * `infrastructure/networking/kustomization.yaml`


---

## 3. NFS (Network File System)

**Role:** Provides shared file storage for PVCs via an NFS server on the master.

* **Export Path**: `/mnt/global_storage` on `samakia` (192.168.11.30)
* **Provisioner**: [example.com/nfs](http://example.com/nfs) CSI driver
* **Reclaim Policy**: `Delete`
* **Key File**:
  * `infrastructure/storage/nfs-storageclass.yaml`


---

## 4. Longhorn (Distributed Block Storage)

**Role:** Highly-available block storage with replication and self-healing.

* **Installation**: Official Helm chart in `longhorn-system` namespace.
* **Default Class**: Enabled via `longhorn-helm-values.yaml`
* **Key File**:
  * `infrastructure/storage/longhorn-helm-values.yaml`


---

## 5. Traefik (Ingress Controller)

**Role:** Edge proxy for HTTP(S) traffic, handling TLS termination and routing.

* **Installation**: Helm chart in `traefik` namespace.
* **TLS Resolver**: ACME TLS-01 via mkcert-generated certs.
* **Key File**:
  * `infrastructure/ingress/traefik-helm-values.yaml`


---

## 6. mkcert (Local Certificate Authority)

**Role:** Generates a root CA and TLS certificates for `*.samakia.lab`.

* **Tool**: mkcert (with `libnss3-tools`)
* **Usage**:

  
  1. `mkcert -install`
  2. `mkcert *.samakia.lab`
* **Integration**: Certificates stored in Kubernetes Secrets consumed by Traefik and service Ingresses.


---

## 7. ArgoCD (GitOps Controller)

**Role:** Continuously syncs Git repository state into the cluster.

* **Pattern**: “App-of-Apps” root in `.argocd/root-app.yaml` pointing at `environments/dev` or `prod`.
* **Sync Policy**:
  * `automated.selfHeal: true`
  * `prune: false` (to preserve removed resources history)
* **Key File**:
  * `.argocd/root-app.yaml`
  * `environments/*/apps-argo.yaml`


---

## 8. Prometheus (Metrics)

**Role:** Scrapes and stores time series metrics from Kubernetes and applications.

* **Helm Chart**: `prometheus-community/prometheus`
* **Scrape Interval**: 15s globally
* **Key File**:
  * `infrastructure/monitoring/prometheus-helm-values.yaml`


---

## 9. Loki (Logging)

**Role:** Aggregates and indexes application logs in a scalable, label-based store.

* **Helm Chart**: `grafana/loki-stack`
* **Retention**: 14 days
* **Persistence**: 10 Gi
* **Key File**:
  * `infrastructure/monitoring/loki-helm-values.yaml`


---

## 10. Grafana (Visualization)

**Role:** Dashboards for metrics & logs, pre-configured with Prometheus data source.

* **Helm Chart**: `grafana/grafana`
* **Admin Credentials**:
  * User: `admin`
  * Password: `PAparia!23`
* **Datasources**: Prometheus via `datasources.yaml`
* **Key File**:
  * `infrastructure/monitoring/grafana-helm-values.yaml`


---

## 11. KubeVirt (Virtual Machines)

**Role:** Runs legacy and integration VMs as Kubernetes Custom Resources.

* **Helm Chart**: `kubevirt/kubevirt` with `dev` mode enabled
* **Sample VM**: Defined in `infrastructure/kubevirt/vm-dev.yaml`
* **Use Case**: Simulate additional “continents” or run non-containerized workloads.


---

## 12. Kong (API Gateway)

**Role:** Ingress-style API gateway for routing, auth, rate-limiting.

* **Helm Chart**: `kong/kong` in `kong` namespace
* **CRDs**: API Gateway CRDs installed separately or via manual step.
* **Key File**:
  * `infrastructure/platform-crds/kong-helm-values.yaml`


---

## 13. Keycloak (Identity & Access Management)

**Role:** OAuth2/OpenID Connect provider for securing APIs & UIs.

* **Helm Chart**: `codecentric/keycloakx` in `keycloak` namespace
* **Replicas**: 1 (can be scaled for high availability)
* **Admin Credentials**:
  * User: `admin`
  * Password: `PAparia!23`
* **Key File**:
  * `infrastructure/platform-crds/keycloak-helm-values.yaml`


---

*End of Components*