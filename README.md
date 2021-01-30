# AngularPortfolioMgr (retired)


![Build Status](https://travis-ci.org/Angular2Guy/AngularPortfolioMgr.svg?branch=master)

Author: Sven Loesekann

Technologies: Angular, Angular-Cli, Angular-Material, Typescript, Spring Boot, Java, Spring Webflux, Spring Data R2DBC, Gradle, Docker, H2, Postgresql

## What is the goal?
The goal is to provide an Angular based Web App with a reactive backend server that uses Webflux and Spring R2DBC technology. The build tool is Gradle.

## Current state of the project
The project can now serve as an example of howto integrate Angular and Spring Boot with Gradle as a build tool.
* The Gradle build is done. 
* The security setup is done. It uses Jwt Tokens and has an auto refresh feature in the frontend and a rest endpoint in the backend. 
* The setup of the Angular fronted is done. 
* The portfolio calculation is done.
* The project is retired

# Experiences gained from the project
The project is now retired and has provided a valueable experience. I tried to use reactive programming with R2DBC and a normalized data model. The technology works and is Ok. The developers of R2DBC have done a good job.

I have implemented the PortfolioCalculationService to get the combined value of a portfolio. The calculation needs values of several tables and combines them to the collective value. The code I ended up with is not easy to read and not what I would consider maintainable. Then I tried to implement the comparison of portfolios to standard indexes in the PortfolioToIndexService. It is not done and will not be done with R2DBC. For me the code is hard to read and hard to debug. 

If there a people out there that disagree with my experience please send me a pull request or show me a fork on how to do a better implementation. 

As a conclusion I would say that I have not found a solution to use a normalized data model and reactive programming in a maintainable manner. I will use for normalized data models Jpa in the future and accept that its model is less efficient. 

A final thougth: Unsuccessful experiments provide important knowledge too. Either the result is lets use something like Jpa or an alternative approach to use R2DBC can evolve.

# Future plans
In some point in the future the backend will be replaced with a Jpa implementation. The current backend will remain availiable in the branch R2DBC-Implementation
