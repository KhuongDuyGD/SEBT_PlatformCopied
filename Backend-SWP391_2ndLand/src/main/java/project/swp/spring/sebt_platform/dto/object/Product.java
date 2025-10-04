package project.swp.spring.sebt_platform.dto.object;

public class Product {
    private Ev ev;
    private Battery battery;

    // Default constructor
    public Product() {
        // Don't initialize ev or battery by default
        // They will be set based on productType from frontend
    }

    // Constructor with parameters
    public Product(Ev ev, Battery battery) {
        this.ev = ev;
        this.battery = battery;
    }

    // Getters and Setters với lazy initialization
    public Ev getEv() {
        return ev;
    }
    public void setEv(Ev ev) {
        this.ev = ev;
    }

    public Battery getBattery() {
        return battery;
    }
    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    /**
     * Ensure nested object is initialized for Spring binding
     */
    public void ensureEvInitialized() {
        if (ev == null) {
            ev = new Ev();
        }
    }

    /**
     * Ensure nested object is initialized for Spring binding
     */
    public void ensureBatteryInitialized() {
        if (battery == null) {
            battery = new Battery();
        }
    }
}
