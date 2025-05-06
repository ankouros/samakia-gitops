# Architecture Overview

This repository implements a fully GitOps-driven Kubernetes cluster deployed on the physical host **samakia** (Ubuntu 22.04) with three KubeVirt-based worker nodes to simulate a multi-continent setup.

## Cluster Topology

* **Master node**: `samakia` (192.168.11.30)
* **Worker nodes**:
  * `asia`
  * `africa`
  * `america`
* All nodes run Kubernetes v1.33.0 managed via **kubeadm**.

## Networking

* **Flannel CNI** providing Pod network on `10.244.0.0/16`.
* **MetalLB** in L2 mode offering a load-balancer IP range: `192.168.11.200–250`.
* Optional Tailscale VPN peering for cross-site connectivity (if enabled).

## Storage

* **NFS** server on `samakia` exporting `/mnt/global_storage` for persistent volumes.
* **Longhorn** CSI for distributed block storage with automatic replication & recovery.

## Ingress & TLS

* **Traefik** Ingress Controller configured via Helm.
* Local CA using **mkcert** to generate `*.samakia.lab` certificates.
* Ingress rules defined in service overlays (e.g. `portal.samakia.lab` for the Next.js front-end).

## GitOps Workflow

* **ArgoCD** root App-of-Apps declared in `.argocd/root-app.yaml`.
* Separate **infrastructure/** and **services/** folders contain Kustomize/Helm manifests.
* **environments/dev** and **environments/prod** slice overlays per environment.
* Automated sync (self-heal) with `prune: false` to preserve historical resources.

## Monitoring & Logging

* **Prometheus** scraping at 15s intervals.
* **Loki** for log aggregation with 14-day retention.
* **Grafana** dashboards (ID 1337) for visualizing metrics & logs.

## Virtualization

* **KubeVirt** for running legacy/integration VMs.


* Sample VM defined in `infrastructure/kubevirt/vm-dev.yaml`.


---

*For full details and extension points, see individual folders under* `infrastructure/` and `services/`.