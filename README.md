# AngularPortfolioMgr


Author: Sven Loesekann

Technologies: Angular, Angular-Cli, Angular-Material, Typescript, Spring Boot, Java, Spring Data Jpa, ArchUnit, Liquibase, Gradle, Docker, H2, Postgresql, Apache Kafka, Tink

[![CodeQL](https://github.com/Angular2Guy/AngularPortfolioMgr/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/Angular2Guy/AngularPortfolioMgr/actions/workflows/codeql-analysis.yml)

## Articles
* [Using Java Stream Gatherers to improve Stateful Operations](https://angular2guy.wordpress.com/2025/05/03/using-java-stream-gatherers-to-improve-stateful-operations/)
* [Spring RestClient with Delays on Virtual Threads](https://angular2guy.wordpress.com/2024/08/30/restclient-with-delays-on-virtual-threads/)
* [Using KRaft Kafka for development and Kubernetes deployment](https://angular2guy.wordpress.com/2024/08/17/using-kraft-kafka-for-development-and-kubernetes-deployment/)
* [Using RSS News Feeds with the Rome Library and Angular/Spring Boot](https://angular2guy.wordpress.com/2024/06/29/implementing-rss-news-feeds-with-the-rome-library-and-angular-spring-boot/)
* [A scrolling Date/Timeline Chart with Angular Material Components](https://angular2guy.wordpress.com/2023/07/01/a-scrolling-date-timeline-chart-with-angular-material-components/)
* [Autoscaling Kubernetes setup for Spring Boot Apps with Database and Kafka](https://angular2guy.wordpress.com/2023/03/23/autoscaling-kubernetes-setup-for-spring-boot-apps-with-a-database-and-kafka/)
* [Angular Drag’n Drop with Query Components and Form Validation](https://angular2guy.wordpress.com/2022/12/21/angular-dragn-drop-with-query-components-and-form-validation/)
* [An Angular Component Tree with included Tables and a flexible Jpa Criteria backend](https://angular2guy.wordpress.com/2022/12/24/an-angular-component-tree-with-included-tables-and-a-flexible-jpa-criteria-backend/)
* [Spring Boot 3 update experience](https://angular2guy.wordpress.com/2022/11/15/spring-boot-3-update-experience/)
* [Spring Kafka errorhandling](https://angular2guy.wordpress.com/2022/11/11/spring-kafka-error-handling/)
* [Switching on features by Spring Profile in an Angular/Spring Boot application](https://angular2guy.wordpress.com/2021/10/13/switching-on-features-by-spring-profile-in-an-angular-spring-boot-application/)
* [Angular JWT Autorefresh With Spring Boot](https://angular2guy.wordpress.com/2021/07/31/angular-jwt-autorefresh-with-spring-boot/)
* [Ngx-Simple-Charts multiline and legend support howto](https://angular2guy.wordpress.com/2021/10/02/ngx-simple-charts-multiline-and-legend-support-howto/)
* [Multiple Entry Points for the NgxSimpleCharts Angular Library](https://angular2guy.wordpress.com/2021/12/26/multiple-entry-points-for-ngxsimplecharts-angular-library/)
* [Configurable Services in the NgxSimpleCharts library](https://angular2guy.wordpress.com/2022/09/13/configurable-services-in-the-ngx-simple-charts-library/)

## What is the goal?
The goal is to provide a Portfolio Management Application to compare different portfolios for there quality over time. It is an Angular based Web App with a Jpa backend server. The build tool is Gradle. Apache Kafka can be used for scalable Jwt token revokation. 

## Current state of the project
The project can now serve as an example of howto integrate Angular and Spring Boot with Gradle as a build tool.
* The Gradle build is done. 
* The security setup is done. It uses Jwt Tokens and has an auto refresh feature in the frontend and a rest endpoint in the backend. Apache Kafka can be used for Jwt Token revocation. The error handling for problems with Kafka events is implemented with transaction rollback.
* The Angular fronted displays several D3 charts and statistics. Comparison indexes have been added to the to the charts and statistics.
* The import of the Kaggle SEC Filings dataset is implemented and optimized. 
* The UI for the search in the SEC Filings data set uses Drag and Drop and displays the results in a Angular Components table or in an Angular Components tree of Angular Components tables.
* The search of the SEC Filings uses the Jpa Criteria Api to dynamicaly generate the query requested by the frontend.
* The Helm chart for a Kubernetes deployment in the helm directory can deploy the project with Kafka, Zookeeper and Postgresql.
* Currently Liquibase needs '-Dliquibase.duplicateFileMode=WARN' as VM Parameter at startup.
* The Helm chart supports horizontal autoscaling of the app with Keda.
* The scrolling Dateline chart is added to show how a portfolio evolves over time.
* Support for a separate Api Key pair for each user is implemented to support the import/update for the quotes.
* The User Api Keys are encrypted with Tink. 

The current state of the project is that a multi user portfolio management is implemented and is ready for use. 

## C4 Architecture Diagrams
The project has a [System Context Diagram](structurizr/diagrams/structurizr-1-SystemContext.svg), a [Container Diagram](structurizr/diagrams/structurizr-1-Containers.svg) and a [Component Diagram](structurizr/diagrams/structurizr-1-Components.svg). The Diagrams have been created with Structurizr. The file runStructurizr.sh contains the commands to use Structurizr and the directory structurizr contains the dsl file.

## Portfolio Manager setup
To use the Portfolio Manager 2 Apikeys are needed for the application and for each user to import the stock quotes and the company information. 
* the Alphavantage Apikey is available [here](https://www.alphavantage.co/support/#api-key) and needs to be put in the property file with key: api.key=
* the RapidApi Apikey ist availabe [here](https://rapidapi.com/apidojo/api/yh-finance/) (An account with a valid email address can be created.) and need to be  put in the property file with the key: api.key.rapidapi=

That will enable the Portfolio Manager to import the stock quotes for the portfolio each user has to provide Alphavantage/RapidApi keys. The keys are stored with each user and enable the import/update of the quotes by multiple users of the application. The nightly quote import uses the keys to update the quotes.
The Portfolio Manager starts with a H2 in memory database that will lose its content with each application termination. To have persistent data in the database the postgresql support needs to be used. It is activated in the 'prod' profile and needs a local [postgresql database](https://www.postgresql.org/) or an available [docker image](https://hub.docker.com/_/postgres) of the database. The properties to setup the database access are in the [application-prod.properties](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/backend/src/main/resources/application-prod.properties) or the [application-prod-kafka.properties](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/backend/src/main/resources/application-prod-kafka.properties). 

## SEC Filings analysis setup
To use the Portfolio Manager for Sec Filings analysis. The data has to be imported first. It can be downloaded(large download) [here](https://www.kaggle.com/datasets/wbqrmgmcia7lhhq/sec-financial-statement-data-in-json). Then the 'path.financial-data=' property in the [application.properties](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/backend/src/main/resources/application.properties) file needs to point to a directory(like '/tmp/') where the application can find/access the zip file. The downloaded file has to be copied in that directory. The Jupyter Notebook to create the data set can be found [here](https://github.com/Angular2Guy/DataScience/tree/master/SecFinancialStatementConverter).

## Usage of the Portfolio Manager
The Portfolio Manage imports the stock symbols and comparision indexes every night. To trigger the import the 'Import Symbols' button in the header can be used. During the import a new Portfolio can be created. The portfolio elements can be added after the symbols import is finished. 

The different portfolios are shown in a table with their stocks and perfomance data. A click on the portfolio name in the side bar opens the portfolio statistics component. A click on the table opens the portfolio details component with charts of the portfolio and its stocks with comparison indexes. The company info for each stock is shown after a second click on the portfolio stock. 

## Usage of the SEC Filings analysis
The SEC Filings analysis can be opened with the 'To Financial Data' button. To import the zip file with the financial data the 'Import Financials' button can be used. A dialog opens and the name of the file can be entered and the import can be started. The process will take a long time(perhaps an hour but that depends on the Db version and the IO system) due to the large amount of data. The progress/finish is shown in the logs. 

After the data import the form for the company query to select a period, symbol or quarter can be used. The items of the filings can be queried with the query items where the 'concept' is the query filing name(like 'revenue') and the value is the 'concept' monetary amount. By default the query items have an 'and' relation. Different relations are available if the query items are enclosed with 'Term Start' and 'Term End' items where 'Term Start' item specifies the relation. Adding and removing query items works with Drag and Drop between the boxes. Both queries can be combined.

The results are show next to the query in a table or in a tree component. The results are limited if a large amount of entries match the query. Due to the large amount of data the queries can take long (again depends on Db version and IO system). Be patient with the spinner.

## Kubernetes deployment
The project has a Docker image that can be deployed on Kubernetes. It has been tested in Minikube. The Helm chart for the deployment can be found in the directory [angularportfoliomgr](https://github.com/Angular2Guy/AngularPortfolioMgr/tree/master/helm/angularportfoliomgr). Commands for the Minikube setup can be found in the [minikubeSetup.sh](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/helm/minikubeSetup.sh) script. The commands for the Helm chart are in the [helmCommand.sh](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/helm/helmCommand.sh) script. The Keda install commands are in the [helmCommand.sh](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/helm/helmCommand.sh) script and the configuration is in the 'ScaledObject' section in the [kubTemplate.yaml](https://github.com/Angular2Guy/AngularPortfolioMgr/blob/master/helm/angularportfoliomgr/templates/kubTemplates.yaml)

The 'angularportfoliomgr' Helm chart will deploy Kafka, Zookeeper, Postgresql and the PortfolioMgr. Kafka is used to provide a horizontaly scalable secure logout. The Kafka usage will grow in the future for nightly quote imports and other distributed data. Zookeeper is deployed for Kafka. The Postgresql Database provides persistence for the AngularPortfolio project. Keda is used to provide horizontal scaling for the AngularPortfolioMgr.

## Monitoring
The Spring Actuator interface with Prometheus interface can be used as it is described in this article: 

[Monitoring Spring Boot with Prometheus and Grafana](https://ordina-jworks.github.io/monitoring/2020/11/16/monitoring-spring-prometheus-grafana.html)

To test the setup the application has to be started and the Docker Images for Prometheus and Grafana have to be started and configured. The scripts 'runGraphana.sh' and 'runPrometheus.sh' can be used as a starting point.

## R2DBC Implementation
A retired R2DBC backend implementation can be found in the [R2DBC-Implementation branch](https://github.com/Angular2Guy/AngularPortfolioMgr/tree/R2DBC-Implementation).
