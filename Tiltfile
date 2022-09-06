# Watch `src` and rebuild the JAR if any source file changes
local_resource(
    "app-jar",
    cmd="sbt clean assembly",
    deps=["src"],
    labels=["scala-app"],
)

# Build the Docker image only if the application JAR changes
docker_build(
    "zeppelin/k8s-local-dev",
    context=".",
    dockerfile="deploy/Dockerfile",
    only=["./target/scala-2.13/k8s-local-dev-assembly-1.0.jar"],
)

# Apply the deployment definition with the application
k8s_yaml("deploy/k8s-resources.yaml")

# Bundles the above pieces together. Allows additional configuration, such as
# port forwarding.
k8s_resource("scala-app-deployment", labels=["scala-app"], port_forwards=8080)

# Deploy Kafka cluster
k8s_yaml("deploy/k8s-kafka-operator.yaml")
k8s_resource("strimzi-cluster-operator", labels=["kafka"])
k8s_kind('Kafka', pod_readiness="wait")
k8s_yaml("deploy/k8s-kafka-cluster.yaml")
k8s_resource("my-cluster", resource_deps=['strimzi-cluster-operator'], labels=["kafka"])

# Deploy Kafka Connect cluster
# k8s_kind('KafkaConnect', pod_readiness="wait")
# k8s_yaml("deploy/k8s-kafka-connect-cluster.yaml")
# k8s_resource("my-connect-cluster", resource_deps=['my-cluster'], labels=["kafka"])
