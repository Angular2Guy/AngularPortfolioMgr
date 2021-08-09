package ch.xxx.manager.usecase.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSortedMap;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.model.entity.CurrencyRepository;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CurrencyService {
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	private final CurrencyRepository currencyRepository;
	private ImmutableSortedMap<LocalDate, Collection<Currency>> currencyMap = ImmutableSortedMap.of();
	
	public CurrencyService(CurrencyRepository currencyRepository) {
		this.currencyRepository = currencyRepository;
	}
	
	@PostConstruct
	public void initCurrencyMap() {
		LOG.info("CurrencyMap updated.");
		this.currencyMap = ImmutableSortedMap.copyOf(
				this.currencyRepository.findAll().stream().collect(Collectors.groupingBy(Currency::getLocalDay)));
	}

	public ImmutableSortedMap<LocalDate, Collection<Currency>> getCurrencyMap() {
		return currencyMap;
	}
}
