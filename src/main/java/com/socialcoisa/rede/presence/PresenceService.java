package com.socialcoisa.rede.presence;

import java.time.Clock;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresenceService {
	private static final Duration ONLINE_WINDOW = Duration.ofMinutes(2);

	private final UserPresenceRepository repository;
	private final Clock clock;

	PresenceService(UserPresenceRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public PresenceStatus ownStatus(String userId) {
		return repository.findById(userId).map(UserPresence::status).orElse(PresenceStatus.AVAILABLE);
	}

	@Transactional
	public PresenceStatus setStatus(String userId, PresenceStatus status) {
		var presence = repository.findById(userId)
			.orElseGet(() -> new UserPresence(userId, status, clock.instant()));
		presence.update(status, clock.instant());
		repository.save(presence);
		return status;
	}

	@Transactional
	public PresenceStatus heartbeat(String userId) {
		var presence = repository.findById(userId)
			.orElseGet(() -> new UserPresence(userId, PresenceStatus.AVAILABLE, clock.instant()));
		presence.touch(clock.instant());
		repository.save(presence);
		return presence.status();
	}

	/** O modo invisível e sessões sem atividade recente nunca revelam atividade a terceiros. */
	@Transactional(readOnly = true)
	public String publicStatus(String userId) {
		return repository.findById(userId)
			.filter(p -> p.status() != PresenceStatus.INVISIBLE)
			.filter(p -> !p.lastSeenAt().isBefore(clock.instant().minus(ONLINE_WINDOW)))
			.map(p -> p.status().name())
			.orElse("OFFLINE");
	}

	/** Não perturbar silencia o push, mas preserva a notificação dentro do aplicativo. */
	@Transactional(readOnly = true)
	public boolean allowPush(String userId) {
		return ownStatus(userId) != PresenceStatus.DO_NOT_DISTURB;
	}
}
