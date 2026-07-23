package hestia.environment;

import hestia.persist.Identifiable;

/**
 * Environment saves environment name and also customer data.
 */
public class Environment implements Identifiable {
    private String id;
    private String name;
    private String customer;
    /** This is the secret key that the customer instance uses to retrieve data from the cloud instance. */
    private String customerKey;
    private boolean active = true;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
