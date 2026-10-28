package com.socialcoisa.rede.presence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_presence")
class UserPresence {
	@Id
	@Column(name = "user_id", nullable = false, length = 64)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 24)
	private PresenceStatus status;

	@Column(name = "last_seen_at", nullable = false)
	private Instant lastSeenAt;

	protected UserPresence() {
	}

	UserPresence(String userId, PresenceStatus status, Instant lastSeenAt) {
		this.userId = userId;
		this.status = status;
		this.lastSeenAt = lastSeenAt;
	}

	PresenceStatus status() {
		return status;
	}

	Instant lastSeenAt() {
		return lastSeenAt;
	}

	void update(PresenceStatus status, Instant seenAt) {
		this.status = status;
		this.lastSeenAt = seenAt;
	}

	void touch(Instant seenAt) {
		this.lastSeenAt = seenAt;
	}
}
