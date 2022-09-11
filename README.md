# Kubernetes (K8s) Local Dev Environment

This is a local K8s dev environment for learning and experimenting with
K8s. After setting up and running the environment, you will get a K8s cluster
running locally on you machine and a simple HTTP server application deployed
and running in the cluster.

Project contents:
.
├── build.sbt                  - SBT build script
├── deploy                     - This directory contains configuration files for the local dev environment
│   ├── Dockerfile             - Dockerfile for the HTTP server
│   ├── k8s-resources.yaml     - Definitions of resources created in the K8s cluster
│   ├── kind-cluster.yaml      - Cluster configuration
│   └── mounts                 - This directory is mounted in all K8s nodes
│       └── message.txt        - Text file served by the HTTP server
├── project                    - Scala project related settings
│   ├── build.properties
│   └── plugins.sbt
├── README.md
├── run                        - Task runner for bringing up and tearing down the dev environment
├── src
│   └── main
│       └── scala
│           └── hello
│               └── Main.scala - A simple HTTP server logic that will run in K8s
└── Tiltfile                   - Tilt configureation

## Setup

Required tools:
* Java >= 8
* [Scala](https://get-coursier.io/docs/cli-installation) == 2.13.x
* [Docker](https://docs.docker.com/get-docker/)
* [Kind](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
* [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
* [ctlptl](https://github.com/tilt-dev/ctlptl#how-do-i-install-it)
* [helm](https://helm.sh/docs/intro/install/)
* [Tilt](https://docs.tilt.dev/install.html)

Recommended tools:
* [K9s](https://github.com/derailed/k9s/releases)

## Run

Bring up the local dev environment with `./run up`. 

The relevant information, such as the URL of the Tilt Web UI, will be output to
the console. The HTTP server is available at http://localhost:8080.

Tilt is configured such that any change to the source code of the HTTP server,
Dockerfile, or definitions of the K8s resources will automatically trigger a
rebuild and redeployment of the corresponding artifacts. That is, you will
immediately see the corresponding change.

Tear down the local dev environment with `./run down`.

## TODO

* Add Kafka service
* Add Kafka-Connect cluster (https://strimzi.io/blog/2021/03/29/connector-build/ )
* Add Kafka Schema Registry
* Add PostgreSQL service
* Add S3 compatible service (MinIO)
* Add AKHQ Kafka WebUI (https://github.com/tchiotludo/akhq/tree/dev/helm/akhq )

## Notes

### Strimzi (Kafka) installation instructions

https://strimzi.io/quickstarts/

Think about how to automate it in Tilt including downloading the latest definitions on `tilt up`.

Create the kafka namespace:
``` yaml
---
apiVersion: v1
kind: Namespace
metadata:
  name: kafka

```
YAML definitions:
* Operator: https://strimzi.io/install/latest?namespace=kafka
* Kafka CRD (had to add "namespace: kafka" manually): https://strimzi.io/examples/latest/kafka/kafka-ephemeral.yaml

Examples of other custom resource definitions: https://github.com/strimzi/strimzi-kafka-operator/tree/0.31.0/examples
