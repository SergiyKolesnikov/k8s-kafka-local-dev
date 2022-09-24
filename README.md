# Kubernetes (K8s) Local Dev Environment

This is a local K8s dev environment for learning and experimenting with
K8s. After setting up and running the environment, you will get a K8s cluster
running locally on you machine and a simple HTTP server application deployed
and running in the cluster.

Project contents:
.
├── build.sbt                         - SBT build script
├── deploy                            - This directory contains configuration files for the local dev environment
│   ├── Dockerfile.kafka-connect      - Dockerfile for a worker of the Kafka Connect cluster
│   ├── Dockerfile.scala-app          - Dockerfile for the HTTP server application
│   ├── deploy/connect-file-3.2.2.jar - FileStreamSourceConnector for the Kafka Connect cluster
│   ├── k8s-*.yaml                    - Definitions of resources created in the K8s cluster
│   ├── kind-cluster.yaml             - Cluster configuration
│   └── mounts                        - This directory is mounted in all K8s nodes
│       └── message.txt               - Text file served by the HTTP server
├── project                           - Scala project related settings
│   ├── build.properties
│   └── plugins.sbt
├── README.md
├── run                               - Task runner for bringing up and tearing down the dev environment
├── src
│   └── main
│       └── scala
│           └── hello
│               └── Main.scala        - A simple HTTP server logic that will run in K8s
└── Tiltfile                          - Tilt configureation

## Setup

Required tools:
* Java >= 8
* [Scala](https://get-coursier.io/docs/cli-installation) == 2.13.x
* [Docker](https://docs.docker.com/get-docker/)
* [Kind](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
* [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
* [ctlptl](https://github.com/tilt-dev/ctlptl#how-do-i-install-it)
* [Helm](https://helm.sh/docs/intro/install/)
* [Tilt](https://docs.tilt.dev/install.html)

Recommended tools:
* [K9s](https://github.com/derailed/k9s/releases)

## Run

Bring up the local dev environment with `./run up`. 

The relevant information, such as the URL of the Tilt Web UI, will be output to
the console. 

Tilt is configured such that any change to the source code of the example
application, Dockerfile, or definitions of the K8s resources will automatically
trigger a rebuild and redeployment of the corresponding artifacts. That is, you
will immediately see the corresponding change.

Tear down the local dev environment with `./run down`.

## Example Application: HTTP server

After the start, the HTTP server is available at http://localhost:8080/.

The root endpoint will print the text form `deploy/mounts/message.txt`.

The http://localhost:8080/sleep-short endpoint will return a message after
seeping for some short time.

The http://localhost:8080/sleep-long endpoint will return a message after
seeping for some long time.

The `/sleep-short` and `/sleep-long` endpoints are meant to demonstrate that the
HTTP server can run blocking operations without blocking itself, because those
operation are started in different threads.

## TODO

* Add S3 compatible service (MinIO)
* Use helm to install Strimzi Kafka operator ()
* Add PostgreSQL service

## Notes

### Strimzi (Kafka)

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

### MinIO

https://min.io/docs/minio/kubernetes/upstream/index.html?ref=docs-redirect#procedure

``` shell
minio_yaml="deploy/k8s-minio-dev.yaml"
wget https://raw.githubusercontent.com/minio/docs/master/source/extra/examples/minio-dev.yaml -O $minio_yaml
```

Open `$minio_yaml` and delete the namespace definition, because we will deploy MinIO in the
existing `kafka` namespace.

``` shell
sed -i 's/namespace: minio-dev/namespace: kafka/' $minio_yaml
sed -i 's_path: /mnt/disk1/data_path: /mnt/host-volumes/minio_' $minio_yaml
sed -i -e '/nodeSelector:$/d' -e '/kubernetes.io\/hostname: kubealpha.local/d' $minio_yaml
```
