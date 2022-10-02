load("ext://helm_resource", "helm_repo", "helm_resource")

# Watch `src` and rebuild the JAR if any source file changes
local_resource(
    "app-jar",
    cmd="sbt clean assembly",
    deps=["src", "build.sbt"],
    labels=["scala-app"],
)
# Build the Docker image only if the application JAR changes
docker_build(
    "kolesnikov/k8s-local-dev",
    context=".",
    dockerfile="deploy/Dockerfile.scala-app",
    only=["./target/scala-2.13/k8s-local-dev-assembly-1.0.jar"],
)
# Apply the deployment definition with the application
k8s_yaml("deploy/k8s-scala-app-deployment.yaml")
# Bundles the above pieces together. Allows additional configuration, such as
# port forwarding.
k8s_resource(
        "scala-app-deployment",
        labels=["scala-app"],
        port_forwards=[port_forward(8080, 8080, "App (8080)")])

# Deploy Kafka cluster
k8s_yaml("deploy/k8s-kafka-operator.yaml")
k8s_resource("strimzi-cluster-operator", labels=["kafka"])
k8s_yaml("deploy/k8s-kafka-cluster.yaml")
k8s_kind("Kafka$", pod_readiness="wait")
k8s_resource("my-cluster", resource_deps=['strimzi-cluster-operator'], labels=["kafka"])

# Deploy Kafka Schema Registry
helm_repo(
  name="bitnami",
  url="https://charts.bitnami.com/bitnami",
  labels=["kafka"],
  resource_name="schema-registry-repo")
helm_resource(
  name="schema-registry",
  chart="bitnami/schema-registry",
  namespace="kafka",
  flags=["--values","deploy/k8s-kafka-schema-registry-values.yaml"],
  deps=["deploy/k8s-kafka-schema-registry-values.yaml"],
  port_forwards=[port_forward(8085, 8081, "Schema Registry (8085)")],
  resource_deps=["schema-registry-repo", "my-cluster"],
  labels=["kafka"])

# Deploy Kafka Connect cluster
docker_build(
    "kolesnikov/k8s-local-dev-kafkaconnect",
    context=".",
    dockerfile="deploy/Dockerfile.kafka-connect",
    only=["deploy/connect-file-3.2.2.jar"],
)
k8s_yaml("deploy/k8s-kafka-connect-cluster.yaml")
k8s_kind("KafkaConnect$", image_json_path="{.spec.image}", pod_readiness="wait")
k8s_resource(
        "my-connect-cluster",
        resource_deps=["my-cluster", "schema-registry"],
        labels=["kafka"])
# Deploy File-Source Connector
k8s_yaml("deploy/k8s-kafka-connect-file-stream-source-connector.yaml")
k8s_kind("KafkaConnector$", pod_readiness="ignore")
k8s_resource(
        "file-stream-source-connector",
        resource_deps=['my-connect-cluster'],
        labels=["kafka"])

# Deploy AKHQ
helm_repo(
  name="akhq",
  url="https://akhq.io/",
  labels=["kafka"],
  resource_name="akhq-repo")
helm_resource(
  name="akhq",
  chart="akhq/akhq",
  namespace="kafka",
  flags=["--values","deploy/k8s-akhq-values.yaml"],
  deps=["deploy/k8s-akhq-values.yaml"],
  port_forwards=[port_forward(8081, 8080, "AKHQ Kafka WebUI (8081)")],
  resource_deps=["akhq-repo", "my-cluster", "schema-registry", "my-connect-cluster"],
  labels=["kafka"])

# Deploy MinIO S3
k8s_yaml("deploy/k8s-minio-dev.yaml")
k8s_resource(
  "minio",
  labels=["minio"],
  port_forwards=[
    port_forward(9090, 9090, "MinIO WebUI (9090)"),
    port_forward(9000, 9000, "MinIO API (9000)")])

# Deploy PostgreSQL
k8s_yaml("deploy/k8s-postgres.yaml")
k8s_resource(
        "postgres",
        labels=["postgres"],
        port_forwards=[port_forward(65432, 5432, "Postgres (65432)")])
k8s_yaml("deploy/k8s-postgres-adminer.yaml")
k8s_resource(
        "adminer",
        labels=["postgres"],
        port_forwards=[port_forward(
                        8082,
                        8080,
                        "Adminer DB WebUI (8082)",
                        link_path="/?pgsql=postgres.kafka&username=postgres&db=postgres&ns=public")])
