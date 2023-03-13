workspace "AngularPortfolioMgr" "This is project manage and compare different portfolios with an Angular Frontend and Spring Boot Backend." {

    model {
        portfolioUser = person "Portfolio User" "Analyse and compare portfolios."
        finDataUser = person "Financial Data User" "Analyse financial data."
        angularPortfolioMgrSystem = softwareSystem "AngularPortfolio Manager System" "Manage portfolios and search financial data." {
        	angularPortfolioMgr = container "AngularPortfolio Manager" "Multiple instances possible. Angular Frontend and Spring Boot Backend integrated." {
	        	angularFrontend = component "Angular Frontend" "The SPA to manage portfolios and search financial data." tag "Browser" 
	        	backendPortfolioClients = component "Import portfolio clients" "The clients to import portfolio quotes and descriptions."
	        	backendPortfolioControllers = component "Portfolio controllers" "The controllers to provide the rest interfaces."
	        	backendFinDataController = component "Financial Data Controller" "The controller to provide the rest interfaces."
	        	backendAuthController = component "Auth Controller" "Provides the rest interfaces for Login / Signin / Logout."
	        	backendPortfolioServices = component "Portfolio services" "The services implement the portfolio logic."
	        	backendFinDataService = component "Financial Data service" "The service implements the financial data logic."
	        	backendUserService = component "AppUser Service" "AppUser Service provides the logic for login / signin / logout."
	        	backendScheduler = component "Scheduler" "Scheduler to start the cron jobs." tag "Scheduler"
	        	backendKafkaConsumer = component "Kafka Consumer" "Consume the Kafka events." tag "Consumer"
        	    backendKafkaProducer = component "Kafka Producer" "Produce the Kafka events." tag "Consumer"
        	    backendPortfolioRepos = component "Portfolio Repositories" "Portfolio related Repositories."
        	    backendFinDataRepository = component "Financial Data Repository" "Repository implementing the dynamic queries."
        	    backendUserRepository = component "AppUser Repository" "Repository for the AppUsers."
        	     
        	}
        	kafka = container "Kafka Event System(Optional)" "Kafka provides the events between multiple deployed AngularPortfolioMgr applications."
        	database = container "Postgresql Db" "Postgresql stores all the data of the system." tag "Database"
        }
		alphavatageSystem = softwareSystem "Alphavatage System"
		yahooFinanceSystem = softwareSystem "YahooFinance System" 
		rapidApiSystem = softwareSystem "RapidApi System"
		
		# relationships people / software systems
        portfolioUser -> angularPortfolioMgrSystem "uses portfolio data"
        finDataUser -> angularPortfolioMgrSystem "uses financial data"
        angularPortfolioMgrSystem -> alphavatageSystem "import quotes"
        angularPortfolioMgrSystem -> yahooFinanceSystem "import quotes"
        angularPortfolioMgrSystem -> rapidApiSystem "import descriptions"
        
        # relationships containers
        portfolioUser -> angularPortfolioMgr "uses portfolio data"
        finDataUser -> angularPortfolioMgr "uses financial data"
        angularPortfolioMgr -> kafka
        kafka -> angularPortfolioMgr
        angularPortfolioMgr -> database
        angularPortfolioMgr -> alphavatageSystem "rest import of quotes"
        angularPortfolioMgr -> yahooFinanceSystem "rest import of quotes"        
        angularPortfolioMgr -> rapidApiSystem  "rest import of descriptions"
        
        # relationships components
        angularFrontend -> backendPortfolioControllers "rest requests"
        angularFrontend -> backendFinDataController "rest requests"
        angularFrontend -> backendAuthController "rest requests"
        backendPortfolioControllers -> backendPortfolioServices
        backendFinDataController -> backendFinDataService
        backendAuthController -> backendUserService
        backendScheduler -> backendPortfolioServices "trigger symbol imports"
        backendScheduler -> backendUserService "trigger logged out user cleanup"
        backendKafkaConsumer -> backendUserService "process kafka events" 
        backendUserService -> backendKafkaProducer "send kafka events"
        backendUserService -> backendUserRepository
        backendFinDataService -> backendFinDataRepository
        backendPortfolioServices -> backendPortfolioRepos
        backendPortfolioServices -> backendPortfolioClients
    }

    views {
        systemContext angularPortfolioMgrSystem "SystemContext" {
            include *
            autoLayout
        }
        
        container angularPortfolioMgrSystem "Containers" {
        	include *
            autoLayout
        }
        
        component angularPortfolioMgr "Components" {
        	include *
            autoLayout
        }  
        
        styles {
        	element "Person" {            
            	shape Person
        	}
        	element "Database" {
                shape Cylinder                
            }
            element "Browser" {
                shape WebBrowser
            }
            element "Consumer" {
            	shape Pipe
            }
            element "Scheduler" {
            	shape Circle
            }
        }
    }

}