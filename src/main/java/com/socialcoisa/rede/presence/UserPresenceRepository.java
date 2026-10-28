package com.socialcoisa.rede.presence;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserPresenceRepository extends JpaRepository<UserPresence, String> {
}
