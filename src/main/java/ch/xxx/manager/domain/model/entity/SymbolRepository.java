package ch.xxx.manager.domain.model.entity;

import java.util.List;

public interface SymbolRepository {
	List<Symbol> findBySymbol(String symbol);
	List<Symbol> findBySymbolSingle(String symbol);
	List<Symbol> findByName(String name);
	List<Symbol> findByPortfolioId(Long portfolioId);
}
