package ch.xxx.manager.adapter.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.PortfolioToSymbol;
import ch.xxx.manager.domain.model.entity.PortfolioToSymbolRepository;

@Repository
public class PortfolioToSymbolRepositoryBean implements PortfolioToSymbolRepository{
	private final JpaPortfolioToSymbolRepository jpaPortfolioToSymbolRepository;
	
	public PortfolioToSymbolRepositoryBean(JpaPortfolioToSymbolRepository jpaPortfolioToSymbolRepository) {
		this.jpaPortfolioToSymbolRepository = jpaPortfolioToSymbolRepository;
	}

	@Override
	public List<PortfolioToSymbol> findByPortfolioId(Long portfolioId) {
		return this.jpaPortfolioToSymbolRepository.findByPortfolioId(portfolioId);
	}

	@Override
	public List<PortfolioToSymbol> findBySymbolId(Long symbolId) {
		return this.jpaPortfolioToSymbolRepository.findBySymbolId(symbolId);
	}

	@Override
	public List<PortfolioToSymbol> findByPortfolioIdAndSymbolId(Long portfolioId, Long symbolId) {
		return this.jpaPortfolioToSymbolRepository.findByPortfolioIdAndSymbolId(portfolioId, symbolId);
	}
	
	
}
