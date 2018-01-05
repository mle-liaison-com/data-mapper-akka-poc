# Akka Nucleus

Template project for Akka-based services

## User Guide

Users are strongly discouraged from making changes to all modules directly except service-implementation in their own service.
It is highly recommended to submit changes to akka-nucleus for non service-implementation changes, 
then pull those changes into forked/mirrored projects.

### Creating a New Service

In order to create a new service from this template project, user needs to first fork or 
[mirror/duplicate](https://help.github.com/articles/duplicating-a-repository/) akka-nucleus.

Once template project is forked/mirrored, then modify following files
- settings.gradle
- k8s.d/deployment.json
- service-implementation

### Working With Remote Repository

User of this template project is expected to constantly pull from base project to keep all modules up-to-date.
This can be simply achieved by configuring remote git repository.
- git remote add base git@github.com:LiaisonTechnologies/g2-akka-nucleus.git
- git remote update
- git pull base develop

### How to Run

Akka-nucleus is expected to be fully docker-containerized. However, user has an option to run it in isolation.

- Set environment (not system properties) variables needed by [ConfigManager](https://github.com/LiaisonTechnologies/g2-akka-nucleus/blob/develop/service-core/src/main/java/com/liaison/service/akka/core/config/ConfigManager.java)
    - ![IntelliJ example](https://github.com/LiaisonTechnologies/g2-akka-nucleus/blob/develop/docs/ide_environment_variables.png)
- Set required configurations (see [ServiceBoostrap](https://github.com/LiaisonTechnologies/g2-akka-nucleus/blob/develop/service-bootstrap/src/main/java/com/liaison/service/akka/bootstrap/ServiceBootstrap.java))
    ```
    akka {
        remote {
            untrusted-mode = on
            trusted-selection-paths = ["/user/entry"]
        }
    }
    com {
        liaison {
            service {
                akka {
                    bootstrap {
                        class = "FQCN"
                    }
                }
            }
        }
    }
    ```
    ```java
    public class BootstrapModuleImpl implements BootstrapModule {
    
        @Override
        public void configure(ActorSystem system) {
          ...
        }
    }
    ```
- Run main in ServiceBootstrap class

#### Containerization

- docker build --build-arg APPLICATION_ID=... .
- docker run -it --rm -e "STACK=..." -e "ENVIRONMENT=..." -p 8989:8989 ${image name}

### Configuration Notes
```
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
```