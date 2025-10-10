# Create Listing Field Mapping & Validation

This document defines the canonical mapping between Frontend form fields and Backend DTO / Entities for the `POST /api/listings/create` endpoint (multipart/form-data). It also marks which fields are REQUIRED, how they are validated/normalized, and their importance for AI price suggestion.

## Legend
- R = Required (backend validator rejects if missing / invalid)
- O = Optional (accepted if present, may enhance AI quality)
- D = Defaulted (backend will assign a default when null)
- AI Weight: HIGH (strong impact), MED (moderate), LOW (minor), NONE

## Top Level (CreateListingFormDTO)
| FE Field | Backend Field | R/O/D | Validation / Normalization | AI Weight | Notes |
|----------|---------------|-------|-----------------------------|-----------|-------|
| title | title | R | length 3..120, trim | LOW | Auto-suggest from EV/Battery info recommended |
| description | description | O | <= 5000 chars | MED | More descriptive context → better future ML (not strong now) |
| category | category ("EV" or "BATTERY") | R | uppercased, must match enum set | HIGH | Drives which product block is required |
| listingType | listingType | D | default NORMAL if null | NONE | Future tiers (PROMO, PREMIUM) |
| price | price | R | > 0 | (N/A) | Can be empty on FE before suggestion & populated after AI |
| images[] | images | R | at least 1 file | NONE | First file becomes thumbnail |

## Location (Location)
| FE Field | Backend Field | R/O/D | Validation | AI Weight | Notes |
|----------|---------------|-------|-----------|-----------|-------|
| location.province | location.province | R | not blank | LOW | Regional pricing later |
| location.district | location.district | R | not blank | LOW | |
| location.details | location.details | O | trim | NONE | Free-form address lines |

## EV Product (Product.ev) – Required when category=EV
| FE Field | Backend Field | R/O/D | Validation | AI Weight | Notes |
|----------|---------------|-------|-----------|-----------|-------|
| ev.type | product.ev.type | R | enum VehicleType | MED | Helps segment (car / scooter / etc.) |
| ev.brand | product.ev.brand | R | not blank, trim | HIGH | Baseline pricing anchor |
| ev.model | product.ev.model | O (but strongly recommended) | trim | HIGH | Combined with brand to map model overrides |
| ev.name | product.ev.name | R | not blank | MED | Display name (may duplicate brand+model) |
| ev.year | product.ev.year | R | 2010 .. current+1 | HIGH | Depreciation factor |
| ev.mileage | product.ev.mileage | O | 0 .. 500000 | HIGH | Mileage depreciation |
| ev.batteryCapacity (kWh) | product.ev.batteryCapacity | R | >0 & <=200 | HIGH | Capacity-based adjustments |
| ev.conditionStatus | product.ev.conditionStatus | D | default GOOD | MED | Adjusts value band |

## Battery Product (Product.battery) – Required when category=BATTERY
| FE Field | Backend Field | R/O/D | Validation | AI Weight | Notes |
|----------|---------------|-------|-----------|-----------|-------|
| battery.brand | product.battery.brand | R | not blank | HIGH | Base anchor |
| battery.model | product.battery.model | R | not blank | HIGH | Variant specificity |
| battery.capacity (kWh) | product.battery.capacity | R | >0 & <=200 | HIGH | Major factor |
| battery.healthPercentage | product.battery.healthPercentage | R | 0..100 | HIGH | Degradation proxy |
| battery.compatibleVehicles | product.battery.compatibleVehicles | O | trim | LOW | Informational |
| battery.conditionStatus | product.battery.conditionStatus | D | default GOOD | MED | Condition impact |

## Internal Normalization (Backend)
- category uppercased.
- Trim all string fields; blank -> null for required checks.
- Default listingType = NORMAL.
- EV/Battery condition default GOOD if omitted.
- Validate numeric ranges before persistence.

## Field Error JSON Structure
Example response (HTTP 400):
```json
{
  "timestamp": "2025-10-10T10:10:10Z",
  "path": "/api/listings/create",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "code": "VALIDATION_ERROR",
  "fieldErrors": [
    { "field": "product.ev.batteryCapacity", "message": "Battery capacity must be >0 and <= 200", "rejectedValue": 0 },
    { "field": "location.province", "message": "Province is required", "rejectedValue": null }
  ]
}
```

## Recommended FE Behaviors
1. Dynamic form sections: show EV or Battery block based on category.
2. Auto Title Builder: `<brand> <model> <year> - <mileage> km - <batteryCapacity> kWh` (hide missing tokens).
3. Price Suggestion Button: call `/api/pricing/suggest` with sanitized fields before user final submit.
4. Real-time validation: show messages matching backend rules; prevent submit if any required field invalid.
5. Normalization before send:
   - Trim strings; collapse multiple spaces.
   - Convert capacity, mileage, health to numbers; clamp health to 0..100.
   - Uppercase category; keep enums as defined.

## Future Enhancements (Deferred)
- Accept & parse region code (VN province code) for better pricing segmentation.
- Add optional images alt text for accessibility / search.
- Provide enumeration endpoint for brands/models for guided selection.
- Add server-side detection of inconsistent brand-model pairs (heuristic mapping).

## AI Usage Notes
- Baselines rely mainly on brand + model + year + mileage + batteryCapacity + healthPercentage (battery-only) + condition.
- Missing model forces fallback to brand default, decreasing accuracy.
- Mileage=0 is treated as “no wear”; FE should encourage user to input actual mileage.

---
Cập nhật: tài liệu này phải được chỉnh sửa khi thêm field mới hoặc thay đổi luật validation.
