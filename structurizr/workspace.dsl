workspace "AngularPortfolioMgr" "This is project manage and compare different portfolios with an Angular Frontend and Spring Boot Backend." {

    model {
        portfolioUser = person "Portfolio User" "Analyse and compare portfolios."
        finDataUser = person "Financial Data User" "Analyse finacial company data."
        angularPortfolioMgrSystem = softwareSystem "AngularPortfolio Management System"
		alphavatageSystem = softwareSystem "Alphavatage System"
		yahooFinanceSystem = softwareSystem "YahooFinance System" 
		rapidApiSystem = softwareSystem "RapidApi System"
		
		# relationships people / software systems
        portfolioUser -> angularPortfolioMgrSystem "Uses portfolio data"
        finDataUser -> angularPortfolioMgrSystem "Uses financial data"
        angularPortfolioMgrSystem -> alphavatageSystem "import quotes"
        angularPortfolioMgrSystem -> yahooFinanceSystem "import quotes"
        angularPortfolioMgrSystem -> rapidApiSystem "import descriptions"
    }

    views {
        systemContext angularPortfolioMgrSystem "SystemContext" {
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