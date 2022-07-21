# Trouble Istio MongoDB

## 환경

- Java
- Spring Boot
- Spring Data MongoDB
- Kubernetes
- Istio (+ sidecar)
- DocumentDB (MongoDB)

## 설명

Istio Sidecar를 통해 AWS DocumentDB에 연결하는 경우 Socket 오류로 작업 실패하는 경우가 있음

AWS DocumentDB에서 발생한 문제이지만 로컬에서 테스트하기 위해 MongoDB 사용

## 원인

Istio Sidecar에 설정된 TCP `idle_timeout`으로 인해 연결이 종료되어 발생하는 문제로 추정됨

## 테스트

Istio Sidecar의 TCP `idle_timeout` 시간을 짧게 설정하여 동일한 문제가 발생하는지 테스트

EnvoyFilter로 Istio Sidecar TCP `idle_timeout`을 15초로 설정

(Envoy Proxy에서 기본으로 설정되는 TCP `idle_timeout`은 1시간)

테스트 결과 동일한 Exception은 아니지만 Socket 오류 발생

(DocumentDB는 `MongoSocketWriteException`, MongoDB는 `MongoSocketReadException`)

## 해결방안

### 1. Sidecar를 통하지 않고 직접 연결

Sidecar를 통해서 연결할 필요가 없는 경우 Sidecar를 통하지 않도록 설정
(Pod Annotation 설정)

```yaml
annotations:
  traffic.sidecar.istio.io/excludeInboundPorts: "27017"
  traffic.sidecar.istio.io/excludeOutboundPorts: "27017"
```

### 2. Client Idle Timeout을 Istio Sidecar `idle_timeout`보다 짧게 설정

```
// maxIdleTimeMS(대소문자 구별하지 않음)
mongodb://{MONGO_CONNECTION_STRING}/?maxIdleTimeMS=10000
```

### 3. Envoy Filter 설정 조정 (TCP Proxy - idle_timeout)

idle_timeout을 `0s`로 설정하면 idle_timeout을 비활성 할 수 있지만 TCP FIN 패킷 손실 등의 이유로 비정상적인 연결이 유지될 수 있음

야간이 요청이 없어서 연결이 유지되지 않는 경우라면 idle_timeout을 길게 설정하여 연결이 끊기는 것을 방지할 수 있음

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: sidecar-timeout
spec:
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: { SIDECAR_OUTBOUND | SIDECAR_INBOUND }
      listener:
        filterChain:
          filter:
            name: envoy.filters.network.tcp_proxy
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.tcp_proxy
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.tcp_proxy.v3.TcpProxy
          idle_timeout: { IDLE_TIMEOUT }
```

## 결과

MongoDB에서는 해결 가능, DocumentDB에서도 해결 가능한지 테스트 필요
