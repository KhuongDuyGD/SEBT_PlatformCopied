package project.swp.spring.sebt_platform.pricing.baseline;

import java.util.List;

/**
 * Lớp wrapper (bao bọc) cho cấu trúc dữ liệu JSON gốc.
 * Chứa danh sách các đối tượng {@link BaselinePriceEntry}.
 */
public class BaselinePriceData {
    private List<BaselinePriceEntry> baselinePrices;

    public List<BaselinePriceEntry> getBaselinePrices() { return baselinePrices; }
    public void setBaselinePrices(List<BaselinePriceEntry> baselinePrices) { this.baselinePrices = baselinePrices; }
}