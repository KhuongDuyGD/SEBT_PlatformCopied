package project.swp.spring.sebt_platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import project.swp.spring.sebt_platform.model.enums.ConfigDataType;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.repository.*;
import project.swp.spring.sebt_platform.service.AdminService;
import project.swp.spring.sebt_platform.service.AuthService;

@Configuration
public class initializerBaseData implements CommandLineRunner {

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EvVehicleRepository evVehicleRepository;

    @Autowired
    private BatteryRepository batteryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PostRequestRepository postRequestRepository;

    @Override
    public void run(String... args) throws Exception {

        adminService.addConfig("POST_LISTING_NORMAL_FEE", "50000", ConfigDataType.NUMBER, "Normal listing fee");
        adminService.addConfig("POST_LISTING_PREMIUM_FEE", "800000", ConfigDataType.NUMBER, "Premium listing fee");
        adminService.addConfig("POST_LISTING_FEATURED_FEE", "100000", ConfigDataType.NUMBER, "Featured listing fee");
        adminService.addConfig("PRICING_FEE", "20000", ConfigDataType.NUMBER, "Fee for using pricing service");

        authService.register("admin123", "noreplysebtplatform@gmail.com", UserRole.ADMIN);
        authService.register("123456", "ducminh852005@gmail.com", UserRole.MEMBER);

    }
}
