package it.eng.idsa.businesslogic.service.user;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {
	
	private int maxAttempts;
	private LoadingCache<String, Integer> attemptsCache;

	public LoginAttemptService(@Value("${application.user.lock.duration}") int lockDuration,
			@Value("${application.user.lock.unit}") String lockUnit,
			@Value("${application.user.lock.maxattempts:5}") int maxAttempts) {
		super();
		attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(lockDuration, TimeUnit.valueOf(lockUnit))
				.build(new CacheLoader<String, Integer>() {
					public Integer load(String key) {
						return 0;
					}
				});
		this.maxAttempts = maxAttempts;
	}

	public void loginSucceeded(String key) {
		attemptsCache.invalidate(key);
	}

	public void loginFailed(String key) {
		int attempts = 0;
		try {
			attempts = attemptsCache.get(key);
		} catch (ExecutionException e) {
			attempts = 0;
		}
		attempts++;
		attemptsCache.put(key, attempts);
	}

	public boolean isBlocked(String key) {
		try {
			return attemptsCache.get(key) >= maxAttempts;
		} catch (ExecutionException e) {
			return false;
		}
	}
}
