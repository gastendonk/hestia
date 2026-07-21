package hestia.prometheus.alert.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import hestia.git.RepositoryPersistenceException;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertGroupDAO;

/**
 * Provides CRUD operations for alert rules stored inside alert groups.
 *
 * <p>Alert rules are persisted as part of their containing alert group.
 * Every modifying operation therefore updates the corresponding alert group
 * through {@link AlertGroupDAO}. The {@code AlertGroup#getRules()} method must
 * return a mutable list.</p>
 */
public class AlertRuleDAO {
    private final AlertGroupDAO alertGroupDAO;

    /**
     * Creates a new alert rule DAO.
     *
     * @param alertGroupDAO the DAO used to load and persist alert groups
     */
    public AlertRuleDAO(AlertGroupDAO alertGroupDAO) {
        this.alertGroupDAO = Objects.requireNonNull(alertGroupDAO);
    }

    /**
     * Loads all alert rules of an alert group.
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @return an immutable copy of the alert rule list
     * @throws RepositoryPersistenceException if the alert group does not exist
     */
    public List<AlertRule> load(String environmentId, String alertGroupId) {
        AlertGroup alertGroup = loadAlertGroup(environmentId, alertGroupId);
        List<AlertRule> rules = alertGroup.getRules();
        if (rules == null || rules.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(rules));
    }

    /**
     * Loads one alert rule by its identifier.
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @param alertRuleId the alert rule identifier
     * @return the matching alert rule
     * @throws RepositoryPersistenceException if the alert rule does not exist
     */
    public AlertRule loadOne(
            String environmentId,
            String alertGroupId,
            String alertRuleId
    ) {
        Objects.requireNonNull(alertRuleId);

        return load(environmentId, alertGroupId).stream()
                .filter(rule -> Objects.equals(
                        rule.getId(),
                        alertRuleId
                ))
                .findFirst()
                .orElseThrow(() -> alertRuleNotFound(
                        environmentId,
                        alertGroupId,
                        alertRuleId
                ));
    }

    /**
     * Inserts a new alert rule into an alert group.
     *
     * <p>The containing alert group is persisted and committed after the rule
     * has been added.</p>
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @param alertRule the alert rule to insert
     * @throws RepositoryPersistenceException if the alert rule ID already exists
     */
    public void insert(
            String environmentId,
            String alertGroupId,
            AlertRule alertRule
    ) {
        Objects.requireNonNull(alertRule);
        Objects.requireNonNull(alertRule.getId());

        AlertGroup alertGroup = loadAlertGroup(
                environmentId,
                alertGroupId
        );

        List<AlertRule> rules = getMutableRules(alertGroup);

        boolean alreadyExists = rules.stream()
                .anyMatch(existingRule -> Objects.equals(
                        existingRule.getId(),
                        alertRule.getId()
                ));

        if (alreadyExists) {
            throw new RepositoryPersistenceException(
                    "Alert rule with ID '%s' already exists in alert group '%s' "
                            + "of environment '%s'"
                            .formatted(
                                    alertRule.getId(),
                                    alertGroupId,
                                    environmentId
                            )
            );
        }

        rules.add(alertRule);

        alertGroupDAO.update(environmentId, alertGroup);
    }

    /**
     * Replaces an existing alert rule.
     *
     * <p>The alert rule ID is used to locate the existing rule. The containing
     * alert group is persisted and committed after the replacement.</p>
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @param alertRule the replacement alert rule
     * @throws RepositoryPersistenceException if the alert rule does not exist
     */
    public void update(
            String environmentId,
            String alertGroupId,
            AlertRule alertRule
    ) {
        Objects.requireNonNull(alertRule);
        Objects.requireNonNull(alertRule.getId());

        AlertGroup alertGroup = loadAlertGroup(
                environmentId,
                alertGroupId
        );

        List<AlertRule> rules = getMutableRules(alertGroup);

        for (int index = 0; index < rules.size(); index++) {
            AlertRule existingRule = rules.get(index);

            if (Objects.equals(
                    existingRule.getId(),
                    alertRule.getId()
            )) {
                rules.set(index, alertRule);
                alertGroupDAO.update(environmentId, alertGroup);
                return;
            }
        }

        throw alertRuleNotFound(
                environmentId,
                alertGroupId,
                alertRule.getId()
        );
    }

    /**
     * Deletes an alert rule from an alert group.
     *
     * <p>The containing alert group is persisted and committed after the rule
     * has been removed.</p>
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @param alertRuleId the alert rule identifier
     * @throws RepositoryPersistenceException if the alert rule does not exist
     */
    public void delete(
            String environmentId,
            String alertGroupId,
            String alertRuleId
    ) {
        Objects.requireNonNull(alertRuleId);

        AlertGroup alertGroup = loadAlertGroup(
                environmentId,
                alertGroupId
        );

        List<AlertRule> rules = getMutableRules(alertGroup);

        boolean removed = rules.removeIf(rule -> Objects.equals(
                rule.getId(),
                alertRuleId
        ));

        if (!removed) {
            throw alertRuleNotFound(
                    environmentId,
                    alertGroupId,
                    alertRuleId
            );
        }

        alertGroupDAO.update(environmentId, alertGroup);
    }

    /**
     * Loads the containing alert group.
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @return the alert group
     */
    private AlertGroup loadAlertGroup(
            String environmentId,
            String alertGroupId
    ) {
        Objects.requireNonNull(environmentId);
        Objects.requireNonNull(alertGroupId);

        return alertGroupDAO.loadOne(
                environmentId,
                alertGroupId
        );
    }

    /**
     * Returns the mutable alert rule list of an alert group.
     *
     * @param alertGroup the alert group
     * @return the mutable alert rule list
     * @throws RepositoryPersistenceException if the rule list is {@code null}
     */
    private List<AlertRule> getMutableRules(AlertGroup alertGroup) {
        List<AlertRule> rules = alertGroup.getRules();

        if (rules == null) {
            throw new RepositoryPersistenceException(
                    "Alert group '%s' has no initialized alert rule list"
                            .formatted(alertGroup.getId())
            );
        }

        return rules;
    }

    /**
     * Creates an exception indicating that an alert rule was not found.
     *
     * @param environmentId the environment identifier
     * @param alertGroupId the alert group identifier
     * @param alertRuleId the alert rule identifier
     * @return the created exception
     */
    private RepositoryPersistenceException alertRuleNotFound(
            String environmentId,
            String alertGroupId,
            String alertRuleId
    ) {
        return new RepositoryPersistenceException(
                "Alert rule with ID '%s' does not exist in alert group '%s' "
                        + "of environment '%s'"
                        .formatted(
                                alertRuleId,
                                alertGroupId,
                                environmentId
                        )
        );
    }
}
