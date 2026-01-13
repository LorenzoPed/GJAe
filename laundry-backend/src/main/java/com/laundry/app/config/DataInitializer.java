// java
package com.laundry.app.config;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.model.Role;
import com.laundry.app.model.User;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds initial application data (users and machines) on startup if absent.
 */
@Configuration
public class DataInitializer {

    /**
     * Initialize sample data at application startup.
     *
     * Creates a manager user, a standard user and a few machines if they do not already exist.
     *
     * @param userRepository repository used to persist users
     * @param machineRepository repository used to persist machines
     * @param passwordEncoder encoder used to hash initial passwords
     * @return a CommandLineRunner that performs data seeding on application start
     */
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   MachineRepository machineRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {

            // 1. Create ADMIN user if it doesn't exist
            if (!userRepository.existsByUsername("manager")) {
                User manager = new User();
                manager.setName("Manager User");
                manager.setUsername("manager");
                manager.setEmail("admin@laundry.com");
                manager.setPassword(passwordEncoder.encode("manager123")); // Secure password
                manager.setRole(Role.MANAGER);
                userRepository.save(manager);
                System.out.println("✅ Manager user created: manager / manager123");
            }

            // 2. Create STANDARD USER if it doesn't exist
            if (!userRepository.existsByUsername("user")) {
                User user = new User();
                user.setName("Mario Rossi");
                user.setUsername("user");
                user.setEmail("user@laundry.com");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole(Role.USER);
                userRepository.save(user);
                System.out.println("✅ Standard user created: user / user123");
            }

            // 3. Create MACHINES if the database is empty
            if (machineRepository.count() == 0) {
                // Machine 1
                Machine m1 = new Machine();
                m1.setName("Washer A");
                m1.setEnabled(true);
                m1.setType(MachineType.WASHER);
                machineRepository.save(m1);

                // Machine 2
                Machine m2 = new Machine();
                m2.setName("Washer B");
                m2.setEnabled(true);
                m2.setType(MachineType.WASHER);
                machineRepository.save(m2);

                // Dryer
                Machine d1 = new Machine();
                d1.setName("Dryer Turbo");
                d1.setEnabled(true);
                d1.setType(MachineType.DRYER);
                machineRepository.save(d1);

                System.out.println("✅ Machines created");
            }
        };
    }
}
