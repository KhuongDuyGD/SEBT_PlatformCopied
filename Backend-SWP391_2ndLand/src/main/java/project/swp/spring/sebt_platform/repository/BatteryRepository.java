package project.swp.spring.sebt_platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import project.swp.spring.sebt_platform.model.BatteryEntity;

@Repository
public interface BatteryRepository extends JpaRepository<BatteryEntity, Long> {
    
    /**
     * Lấy danh sách thương hiệu pin duy nhất để dùng cho filter dropdown
     * @return Danh sách thương hiệu pin sắp xếp theo thứ tự ABC
     */
    @Query("SELECT DISTINCT b.brand FROM BatteryEntity b ORDER BY b.brand ASC")
    List<String> findDistinctBrands();
    
    /**
     * Lấy danh sách tên pin duy nhất để dùng cho autocomplete
     * @return Danh sách tên pin sắp xếp theo thứ tự ABC
     */
    @Query("SELECT DISTINCT b.name FROM BatteryEntity b ORDER BY b.name ASC")
    List<String> findDistinctBatteryNames();
    
    /**
     * Lấy tất cả chuỗi compatibleVehicles để xử lý tách thành danh sách riêng biệt
     * @return Danh sách raw string chứa xe tương thích (cần parse thêm)
     */
    @Query("SELECT b.compatibleVehicles FROM BatteryEntity b WHERE b.compatibleVehicles IS NOT NULL")
    List<String> findAllCompatibleVehicles();
    
    /**
     * Lấy danh sách năm sản xuất pin duy nhất để dùng cho filter dropdown
     * @return Danh sách năm sản xuất sắp xếp giảm dần (mới nhất trước)
     */
    @Query("SELECT DISTINCT b.year FROM BatteryEntity b ORDER BY b.year DESC")
    List<Integer> findDistinctBatteryYears();
}
