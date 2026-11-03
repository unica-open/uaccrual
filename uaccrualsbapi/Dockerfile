FROM docker-base.ecosis.csi.it/maven:3.9.2-j17-csi-r1 as builder
COPY . /usr/src/mymaven
WORKDIR /usr/src/mymaven
RUN env
RUN cat .mvn/conf/settings.xml
RUN mvn package --settings .mvn/conf/settings.xml -Dmaven.test.skip=true
WORKDIR /usr/src/mymaven/target


FROM docker-trusted.ecosis.csi.it/eclipse-temurin:17.0.8.1_1-jdk
LABEL it.csi.codice_progetto="siac" \
      it.csi.codice_componente="accrualsbapi"
RUN mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments
COPY  ./run-java.sh /deployments/run-java.sh

RUN chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh 


COPY --from=builder /usr/src/mymaven/target/*.jar /deployments/app.jar
EXPOSE 8080
USER 1001

ENTRYPOINT ["/deployments/run-java.sh"]