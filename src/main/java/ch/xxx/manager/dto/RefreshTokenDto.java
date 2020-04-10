package ch.xxx.manager.dto;

public class RefreshTokenDto {
	private String refreshToken;

	public RefreshTokenDto(String refreshToken) {
		super();
		this.refreshToken = refreshToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
}
