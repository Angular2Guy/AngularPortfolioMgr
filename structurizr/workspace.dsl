workspace "AngularPortfolioMgr" "This is project manage and compare different portfolios with an Angular Frontend and Spring Boot Backend." {

    model {
        portfolioUser = person "Portfolio User" "Analyse and compare portfolios."
        finDataUser = person "Financial Data User" "Analyse financial data."
        angularPortfolioMgrSystem = softwareSystem "AngularPortfolio Manager System" {
        	angularPortfolioMgr = container "AngularPortfolio Manager" "Multiple instances possible. Angular Frontend and Spring Boot Backend integrated." {
	        	angularFrontend = component "Angular Frontend" "The SPA to manage portfolios and search financial data." tag "Browser"    	
        	}
        	kafka = container "Kafka Event System(Optional)" "Kafka provides the events between multiple deployed AngularPortfolioMgr applications."
        	database = container "Postgresql Db" "Postgresql stores all the data of the system." tag "Database"
        }
		alphavatageSystem = softwareSystem "Alphavatage System"
		yahooFinanceSystem = softwareSystem "YahooFinance System" 
		rapidApiSystem = softwareSystem "RapidApi System"
		
		# relationships people / software systems
        portfolioUser -> angularPortfolioMgrSystem "Uses portfolio data"
        finDataUser -> angularPortfolioMgrSystem "Uses financial data"
        angularPortfolioMgrSystem -> alphavatageSystem "import quotes"
        angularPortfolioMgrSystem -> yahooFinanceSystem "import quotes"
        angularPortfolioMgrSystem -> rapidApiSystem "import descriptions"
        
        # relationships containers
        portfolioUser -> angularPortfolioMgr
        finDataUser -> angularPortfolioMgr
        angularPortfolioMgr -> kafka
        kafka -> angularPortfolioMgr
        angularPortfolioMgr -> database
        angularPortfolioMgr -> alphavatageSystem
        angularPortfolioMgr -> yahooFinanceSystem
        angularPortfolioMgr -> rapidApiSystem 
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
        }
    }

}