package ch.xxx.manager.adapter.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@Repository
public class SymbolRepositoryBean implements SymbolRepository {
	private final JpaSymbolRepository jpaSymbolRepository;
	
	public SymbolRepositoryBean(JpaSymbolRepository jpaSymbolRepository) {
		this.jpaSymbolRepository = jpaSymbolRepository;
	}

	@Override
	public List<Symbol> findBySymbol(String symbol) {
		return this.jpaSymbolRepository.findBySymbol(symbol);
	}

	@Override
	public List<Symbol> findBySymbolSingle(String symbol) {
		return this.jpaSymbolRepository.findBySymbolSingle(symbol);
	}

	@Override
	public List<Symbol> findByName(String name) {
		return this.jpaSymbolRepository.findByName(name);
	}

	@Override
	public List<Symbol> findByPortfolioId(Long portfolioId) {
		return this.jpaSymbolRepository.findByPortfolioId(portfolioId);
	}
	
	@Override
	public List<Symbol> findAll() {
		return this.jpaSymbolRepository.findAll();
	}

	@Override
	public Symbol save(Symbol symbol) {
		return this.jpaSymbolRepository.save(symbol);
	}
	
}
