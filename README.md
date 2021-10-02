# AngularPortfolioMgr


Author: Sven Loesekann

Technologies: Angular, Angular-Cli, Angular-Material, Typescript, Spring Boot, Java, Spring Data Jpa, Liquibase, Gradle, Docker, H2, Postgresql

## Articles
* [Angular JWT Autorefresh With Spring Boot](https://angular2guy.wordpress.com/2021/07/31/angular-jwt-autorefresh-with-spring-boot/)
* [Ngx-Simple-Charts multiline and legend support howto](https://angular2guy.wordpress.com/2021/10/02/ngx-simple-charts-multiline-and-legend-support-howto/)

## What is the goal?
The goal is to provide an Angular based Web App with a Jpa backend server. The build tool is Gradle.

## Current state of the project
The project can now serve as an example of howto integrate Angular and Spring Boot with Gradle as a build tool.
* The Gradle build is done. 
* The security setup is done. It uses Jwt Tokens and has an auto refresh feature in the frontend and a rest endpoint in the backend. 
* The setup of the Angular fronted is done. 

## R2DBC Implementation
A retired R2DBC backend implementation can be found in the 'R2DBC-Implementation' branch.
