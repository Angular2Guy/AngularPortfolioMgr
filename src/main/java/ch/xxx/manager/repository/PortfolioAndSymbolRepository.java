package ch.xxx.manager.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.entity.PortfolioAndSymbolEntity;
import reactor.core.publisher.Flux;

@Repository
public class PortfolioAndSymbolRepository {
	@Autowired
	private DatabaseClient client;

	public Flux<PortfolioAndSymbolEntity> findPortfolioCalcEntitiesByPorfolioId(Long portfolioId) {
		return client
				.execute("select s.symbol as symbol, s.name as symbol_name, s.curr as curr, s.id as symbol_id, "
						+ "pts.changed_at as changed_at, pts.removed_at as removed_at, pts.weight as weight, "
						+ "p.id as id, p.user_id as user_id, p.created_at as created_at, p.name as portfolio_name "
						+ "from portfolio p, portfolio_to_symbol pts, symbol s "
						+ "where p.id = pts.portfolio_id and pts.symbol_id = s.id and p.id = :portfolioId")
				.bind("portfolioId", portfolioId).as(PortfolioAndSymbolEntity.class).fetch().all();
	}
}
