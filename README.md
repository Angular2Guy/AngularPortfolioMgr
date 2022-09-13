# AngularPortfolioMgr


Author: Sven Loesekann

Technologies: Angular, Angular-Cli, Angular-Material, Typescript, Spring Boot, Java, Spring Data Jpa, ArchUnit, Liquibase, Gradle, Docker, H2, Postgresql

## Articles
* [Switching on features by Spring Profile in an Angular/Spring Boot application](https://angular2guy.wordpress.com/2021/10/13/switching-on-features-by-spring-profile-in-an-angular-spring-boot-application/)
* [Angular JWT Autorefresh With Spring Boot](https://angular2guy.wordpress.com/2021/07/31/angular-jwt-autorefresh-with-spring-boot/)
* [Ngx-Simple-Charts multiline and legend support howto](https://angular2guy.wordpress.com/2021/10/02/ngx-simple-charts-multiline-and-legend-support-howto/)
* [Multiple Entry Points for the NgxSimpleCharts Angular Library](https://angular2guy.wordpress.com/2021/12/26/multiple-entry-points-for-ngxsimplecharts-angular-library/)
* [Configurable Services in the NgxSimpleCharts library](https://angular2guy.wordpress.com/2022/09/13/configurable-services-in-the-ngx-simple-charts-library/)

## What is the goal?
The goal is to provide an Angular based Web App with a Jpa backend server. The build tool is Gradle.

## Current state of the project
The project can now serve as an example of howto integrate Angular and Spring Boot with Gradle as a build tool.
* The Gradle build is done. 
* The security setup is done. It uses Jwt Tokens and has an auto refresh feature in the frontend and a rest endpoint in the backend. 
* The setup of the Angular fronted is done. 

## Monitoring
The Spring Actuator interface with Prometheus interface can be used as it is described in this article: 

[Monitoring Spring Boot with Prometheus and Grafana](https://ordina-jworks.github.io/monitoring/2020/11/16/monitoring-spring-prometheus-grafana.html)

To test the setup the application has to be started and the Docker Images for Prometheus and Grafana have to be started and configured. The scripts 'runGraphana.sh' and 'runPrometheus.sh' can be used as a starting point.

## R2DBC Implementation
A retired R2DBC backend implementation can be found in the [R2DBC-Implementation branch](https://github.com/Angular2Guy/AngularPortfolioMgr/tree/R2DBC-Implementation).
