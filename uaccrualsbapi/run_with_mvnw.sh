#!/bin/sh
./mvnw spring-boot:run "-Dspring-boot.run.jvmArguments=\"-Dspring.profiles.active=dev\"  \"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9009\""