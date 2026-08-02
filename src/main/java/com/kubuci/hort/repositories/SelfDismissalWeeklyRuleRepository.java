package com.kubuci.hort.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kubuci.hort.models.SelfDismissalWeeklyRule;

public interface SelfDismissalWeeklyRuleRepository extends JpaRepository<SelfDismissalWeeklyRule, UUID> {
	List<SelfDismissalWeeklyRule> findBySelfDismissal_Id(UUID selfDismissalId);

	List<SelfDismissalWeeklyRule> findBySelfDismissal_IdIn(List<UUID> selfDismissalIds);
}
