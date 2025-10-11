package project.swp.spring.sebt_platform.validation;

import project.swp.spring.sebt_platform.dto.object.Battery;
import project.swp.spring.sebt_platform.dto.object.Ev;
import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.model.enums.ListingType;
import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.model.enums.BatteryCondition;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Performs manual rule-based validation & normalization for CreateListingFormDTO. */
public class CreateListingValidator {

    private static final int TITLE_MIN = 3;
    private static final int TITLE_MAX = 120;
    private static final int DESC_MAX = 5000;
    private static final double EV_BATTERY_CAPACITY_MAX = 200.0; // kWh upper guard
    private static final int HEALTH_MIN = 0;
    private static final int HEALTH_MAX = 100;

    public ValidationResult validateAndNormalize(CreateListingFormDTO dto) {
        List<FieldErrorDetail> errors = new ArrayList<>();
        if (dto == null) {
            errors.add(new FieldErrorDetail("body", "Request body is null", null));
            return new ValidationResult(errors);
        }

        // --- Top-level normalization ---
        dto.setTitle(trimToNull(dto.getTitle()));
        dto.setDescription(trim(dto.getDescription()));
        dto.setCategory(upper(dto.getCategory()));
        if (dto.getListingType() == null) {
            dto.setListingType(ListingType.NORMAL);
        }

        // Title
        if (dto.getTitle() == null || dto.getTitle().length() < TITLE_MIN) {
            errors.add(new FieldErrorDetail("title", "Title must have at least " + TITLE_MIN + " characters", dto.getTitle()));
        } else if (dto.getTitle().length() > TITLE_MAX) {
            errors.add(new FieldErrorDetail("title", "Title length must be <= " + TITLE_MAX, dto.getTitle()));
        }

        // Description length (optional)
        if (dto.getDescription() != null && dto.getDescription().length() > DESC_MAX) {
            errors.add(new FieldErrorDetail("description", "Description length must be <= " + DESC_MAX, null));
        }

        // Category
        boolean isEvCategory = false;
        if (dto.getCategory() == null) {
            errors.add(new FieldErrorDetail("category", "Category is required (EV or BATTERY)", null));
        } else if (!dto.getCategory().equals("EV") && !dto.getCategory().equals("BATTERY")) {
            errors.add(new FieldErrorDetail("category", "Category must be EV or BATTERY", dto.getCategory()));
        } else {
            isEvCategory = dto.getCategory().equals("EV");
        }

        // Price
        if (dto.getPrice() <= 0) {
            errors.add(new FieldErrorDetail("price", "Price must be > 0", dto.getPrice()));
        }

        // Location
        Location loc = dto.getLocation();
        if (loc == null) {
            errors.add(new FieldErrorDetail("location", "Location object is required", null));
        } else {
            loc.setProvince(trimToNull(loc.getProvince()));
            loc.setDistrict(trimToNull(loc.getDistrict()));
            loc.setDetails(trim(loc.getDetails()));
            if (loc.getProvince() == null) errors.add(new FieldErrorDetail("location.province", "Province is required", null));
            if (loc.getDistrict() == null) errors.add(new FieldErrorDetail("location.district", "District is required", null));
        }

        Product product = dto.getProduct();
        if (product == null) {
            errors.add(new FieldErrorDetail("product", "Product object is required", null));
            return new ValidationResult(errors);
        }

        Ev ev = product.getEv();
        Battery battery = product.getBattery();

        if (ev == null && battery == null) {
            errors.add(new FieldErrorDetail("product", "At least one of EV or Battery details is required", null));
        }

        // EV validation when present
        if (ev != null) {
            ev.setBrand(trimToNull(ev.getBrand()));
            ev.setModel(trimToNull(ev.getModel()));
            ev.setName(trimToNull(ev.getName()));
            // brand & name required
            if (ev.getName() == null) errors.add(new FieldErrorDetail("product.ev.name", "EV name is required", null));
            if (ev.getBrand() == null) errors.add(new FieldErrorDetail("product.ev.brand", "EV brand is required", null));
            if (ev.getType() == null) errors.add(new FieldErrorDetail("product.ev.type", "EV type is required", null));
            if (ev.getYear() == null) {
                errors.add(new FieldErrorDetail("product.ev.year", "EV year is required", null));
            } else {
                int current = Year.now().getValue();
                if (ev.getYear() < 2010 || ev.getYear() > current + 1) {
                    errors.add(new FieldErrorDetail("product.ev.year", "Year must be between 2010 and " + (current + 1), ev.getYear()));
                }
            }
            if (ev.getMileage() != null && (ev.getMileage() < 0 || ev.getMileage() > 500_000)) {
                errors.add(new FieldErrorDetail("product.ev.mileage", "Mileage must be 0..500000", ev.getMileage()));
            }
            if (ev.getBatteryCapacity() <= 0 || ev.getBatteryCapacity() > EV_BATTERY_CAPACITY_MAX) {
                errors.add(new FieldErrorDetail("product.ev.batteryCapacity", "Battery capacity must be >0 and <= " + EV_BATTERY_CAPACITY_MAX, ev.getBatteryCapacity()));
            }
            if (ev.getConditionStatus() == null) {
                ev.setConditionStatus(VehicleCondition.GOOD);
            }
        } else if (isEvCategory) {
            errors.add(new FieldErrorDetail("product.ev", "EV data required for EV category", null));
        }

        // Battery validation when present
        if (battery != null) {
            battery.setBrand(trimToNull(battery.getBrand()));
            battery.setModel(trimToNull(battery.getModel()));
            battery.setCompatibleVehicles(trim(battery.getCompatibleVehicles()));
            if (battery.getBrand() == null) errors.add(new FieldErrorDetail("product.battery.brand", "Battery brand is required", null));
            if (battery.getModel() == null) errors.add(new FieldErrorDetail("product.battery.model", "Battery model is required", null));
            if (battery.getCapacity() <= 0 || battery.getCapacity() > EV_BATTERY_CAPACITY_MAX) {
                errors.add(new FieldErrorDetail("product.battery.capacity", "Capacity must be >0 and <= " + EV_BATTERY_CAPACITY_MAX, battery.getCapacity()));
            }
            if (battery.getHealthPercentage() < HEALTH_MIN || battery.getHealthPercentage() > HEALTH_MAX) {
                errors.add(new FieldErrorDetail("product.battery.healthPercentage", "Health % must be 0..100", battery.getHealthPercentage()));
            }
            if (battery.getConditionStatus() == null) {
                battery.setConditionStatus(BatteryCondition.GOOD);
            }
        } else if (!isEvCategory) {
            // If category BATTERY but no battery details
            if (Objects.equals(dto.getCategory(), "BATTERY")) {
                errors.add(new FieldErrorDetail("product.battery", "Battery data required for BATTERY category", null));
            }
        }

        return new ValidationResult(errors);
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
    private String trimToNull(String s) { if (s == null) return null; String t = s.trim(); return t.isEmpty()? null : t; }
    private String upper(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }

    // Result wrapper
    public static class ValidationResult {
        private final List<FieldErrorDetail> errors;
        public ValidationResult(List<FieldErrorDetail> errors) { this.errors = errors; }
        public boolean hasErrors() { return errors != null && !errors.isEmpty(); }
        public List<FieldErrorDetail> getErrors() { return errors; }
    }
}
