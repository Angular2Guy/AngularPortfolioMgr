package ch.xxx.manager.entity;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("symbol")
public class CurrencyEntity {
	@Id
	private Long id;
	private String from_curr;
	private String to_curr;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	
	public CurrencyEntity() {
	}
	
	public CurrencyEntity(String from_curr, String to_curr, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal close) {
		super();
		this.from_curr = from_curr;
		this.to_curr = to_curr;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFrom_curr() {
		return from_curr;
	}

	public void setFrom_curr(String from_curr) {
		this.from_curr = from_curr;
	}

	public String getTo_curr() {
		return to_curr;
	}

	public void setTo_curr(String to_curr) {
		this.to_curr = to_curr;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}
}
