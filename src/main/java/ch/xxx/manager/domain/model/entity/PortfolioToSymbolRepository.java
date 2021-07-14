package ch.xxx.manager.domain.model.entity;

import java.util.List;

public interface PortfolioToSymbolRepository {
	List<PortfolioToSymbol> findByPortfolioId(Long portfolioId);
	List<PortfolioToSymbol> findBySymbolId(Long symbolId);
	List<PortfolioToSymbol> findByPortfolioIdAndSymbolId(Long portfolioId, Long symbolId);
}
