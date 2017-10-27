docker build .

docker run -it --rm -p 8989:8989 ${image name}

docker run -it --rm -e SERVICE_BOOTSTRAP_OPTS="-Dakka.deployment.applicationId=akka-nucleus -Dakka.deployment.stack=stack -Dakka.deployment.environment=dev -Dakka.deployment.region=region -Dakka.deployment.datacenter=datacenter" -p 8989:8989 ${image name}

remote
- akka.actor.provider = remote
- tcp
    - plain
        - akka.remote.enabled-transports = ["akka.remote.netty.tcp"]
        - akka.remote.netty.tcp.hostname = 0.0.0.0
        - akka.remote.netty.tcp.port = 2552
    - ssl
        - akka.remote.enabled-transports = ["akka.remote.netty.ssl"]
        - akka.remote.netty.ssl.hostname = 0.0.0.0
        - akka.remote.netty.ssl.port = 2552
        - akka.remote.netty.ssl.security.key-store = ""
        - akka.remote.netty.ssl.security.trust-store = ""
        - akka.remote.netty.ssl.security.key-store-password = ""
        - akka.remote.netty.ssl.security.key-password = ""
        - akka.remote.netty.ssl.security.trust-store-password = ""
        - akka.remote.netty.ssl.security.protocol = "TLSv1.2"
        - akka.remote.netty.ssl.security.enabled-algorithms = [TLS_DHE_RSA_WITH_AES_128_GCM_SHA256]
        - akka.remote.netty.ssl.security.random-number-generator = "AES128CounterSecureRNG"
- artery
    - akka.remote.artery.enabled = on
    - akka.remote.artery.canonical.host = 0.0.0.0
    - akka.remote.artery.canonical.port = 2552
    - INFO : Remoting started; listening on address: [akka://akka-nucleus@0.0.0.0:25520] with UID [3326379590819384954]

cluster
- akka.actor.provider = cluster
- akka.extensions = ["akka.cluster.client.ClusterClientReceptionist"]
- akka.cluster.seed-nodes = ["akka.ssl.tcp://akka-nucleus@0.0.0.0:2552"]
- akka.cluster.client.initial-contacts = ["akka.ssl.tcp://akka-nucleus@0.0.0.0:2552/system/receptionist"]
- server
    - Cluster Node [akka://akka-nucleus@0.0.0.0:25520] - Starting up...
    - Cluster Node [akka://akka-nucleus@0.0.0.0:25520] - Started up successfully
- client
    - INFO : Connected to [akka://akka-nucleus@0.0.0.0:25520/system/receptionist]

security
- akka.remote.untrusted-mode = on
- akka.remote.trusted-selection-paths = ["placeholder"]