package com.laundry.app.service;

import com.laundry.app.model.Booking;
import com.laundry.app.model.BookingStatus;
import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.MaintenanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing machines: CRUD operations and cleanup when machines are deleted/disabled.
 */
@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final BookingService bookingService;

    /**
     * Construct the machine service with necessary repositories and booking service.
     *
     * @param machineRepository repository used to manage Machine entities
     * @param bookingRepository repository used to manage Booking entities
     * @param maintenanceRepository repository used to manage Maintenance entities
     * @param bookingService service used to handle booking reschedule/cancel operations
     */
    @Autowired
    public MachineService(MachineRepository machineRepository,
                          BookingRepository bookingRepository,
                          MaintenanceRepository maintenanceRepository,
                          BookingService bookingService) {
        this.machineRepository = machineRepository;
        this.bookingRepository = bookingRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.bookingService = bookingService;
    }

    /**
     * Return all machines.
     *
     * @return list of machines
     */
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    /**
     * Return a machine by id, throwing if not found.
     *
     * @param id machine id
     * @return machine
     */
    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " + id));
    }

    /**
     * Create and persist a new machine.
     *
     * @param machine machine entity to create
     * @return saved machine
     */
    public Machine createMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    /**
     * Delete a machine and handle impacted bookings / maintenances.
     *
     * @param id id of the machine to delete
     * @return summary message about performed actions
     */
    @Transactional
    public String deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new RuntimeException("Machine not found with id: " + id);
        }

        // 1. Call to Booking Service method to handle reservations
        BookingService.DisableMachineResult result = bookingService.handleMachineDisabled(id);

        // 2. Delete mantainances
        maintenanceRepository.deleteByMachineId(id);

        //Get remaining bookings (e.g. in the past)
        List<Booking> remainingBookings = bookingRepository.findByMachineId(id);

        for (Booking booking : remainingBookings) {

            if (booking.getStatus() != BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.COMPLETED) {
                booking.setStatus(BookingStatus.CANCELLED);
            }

            // machine_id = NULL
            booking.setMachine(null);
        }

        // Salviamo lo scollegamento
        bookingRepository.saveAll(remainingBookings);

        // 4. ELIMINAZIONE FISICA DELLA MACCHINA
        machineRepository.deleteById(id);

        return String.format("Machine deleted successfully. %d bookings were rescheduled, %d were cancelled.",
                result.getRescheduledBookings(), result.getCancelledBookings());
    }

    /**
     * Update only the machine name and type.
     *
     * @param id machine id
     * @param name new name
     * @param type new machine type
     * @return updated machine
     */
    public Machine updateMachineNameAndType(Long id, String name, MachineType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Machine name cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Machine type is required.");
        }

        Machine machine = getMachineById(id);
        machine.setName(name.trim());
        machine.setType(type);

        return machineRepository.save(machine);
    }

    /**
     * Update a machine; if disabling, delegates to bookingService to reschedule/cancel bookings.
     *
     * @param id machine id
     * @param machineDetails new machine data
     * @return updated machine
     */
    @Transactional
    public Machine updateMachine(Long id, Machine machineDetails) {
        Machine existingMachine = getMachineById(id);

        if (existingMachine.isEnabled() && !machineDetails.isEnabled()) {

            //Move bookings
            bookingService.handleMachineDisabled(id);
        }

        existingMachine.setName(machineDetails.getName());
        existingMachine.setEnabled(machineDetails.isEnabled());
        existingMachine.setType(machineDetails.getType());

        return machineRepository.save(existingMachine);
    }
}