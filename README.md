![build](https://github.com/equidis/micronaut-grpc-availability-service/workflows/build/badge.svg) 
[![codecov](https://codecov.io/gh/equidis/micronaut-grpc-availability-service/branch/master/graph/badge.svg?token=3YIUUHBSRX)](https://codecov.io/gh/equidis/micronaut-grpc-availability-service/) 
![release](https://img.shields.io/github/v/tag/equidis/micronaut-grpc-availability-service)
![license](https://img.shields.io/github/license/equidis/micronaut-grpc-availability-service)

# Availability service

Sample microservice that features [Micronaut](https://micronaut.io/) and [GRPC](https://grpc.io/) server. The service is not relying on
reflection thanks to [Micronaut](https://micronaut.io/),
[Protobuf](https://developers.google.com/protocol-buffers) and
[Kotlinx.serialization](https://kotlinlang.org/docs/reference/serialization.html) AOT capabilities.

From a functional standpoint this delegates user management to
[micronaut-grpc-users-service](https://github.com/equidis/micronaut-grpc-users-service) using GRPC to access it's API.
