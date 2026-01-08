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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    // RIMOSSO: private final Booking booking;  <-- Questo era l'errore

    @Autowired
    public MachineService(MachineRepository machineRepository,
                          BookingRepository bookingRepository,
                          MaintenanceRepository maintenanceRepository,
                          BookingService bookingService,
                          // RIMOSSO: Booking booking, <-- Rimosso dal costruttore
                          NotificationService notificationService) {
        this.machineRepository = machineRepository;
        this.bookingRepository = bookingRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        // RIMOSSO: this.booking = booking;
    }

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with id: " + id));
    }

    public Machine createMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    @Transactional
    public String deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new RuntimeException("Machine not found with id: " + id);
        }

        // 1. TENTATIVO DI SALVATAGGIO (Reschedule)
        BookingService.DisableMachineResult result = bookingService.handleMachineDisabled(id);

        // 2. ELIMINA LE MANUTENZIONI
        maintenanceRepository.deleteByMachineId(id);

        // 3. GESTIONE PRENOTAZIONI RIMASTE
        List<Booking> remainingBookings = bookingRepository.findByMachineId(id);

        for (Booking booking : remainingBookings) {

            // Logica Notifica
            boolean isFutureOrActive = booking.getEndTime().isAfter(LocalDateTime.now());

            if (isFutureOrActive && booking.getStatus() != BookingStatus.COMPLETED) {
                String msg = "WARNING: Your booking on " +
                        booking.getStartTime().toLocalDate() + " at " + booking.getStartTime().toLocalTime() +
                        " has been cancelled because the machine was removed from service.";

                notificationService.sendNotification(booking.getUser(), msg);
            }

            // Impostiamo lo stato cancellato e rompiamo il legame
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setMachine(null);
        }

        // Salviamo le modifiche
        bookingRepository.saveAll(remainingBookings);

        // 4. ELIMINAZIONE FISICA DELLA MACCHINA
        machineRepository.deleteById(id);

        return String.format("Machine deleted successfully. %d bookings were rescheduled, %d could not be moved and were cancelled.",
                result.getRescheduledBookings(), remainingBookings.size());
    }

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

    public Machine updateMachine(Long id, Machine machineDetails) {
        Machine machine = getMachineById(id);

        machine.setName(machineDetails.getName());
        machine.setEnabled(machineDetails.isEnabled());
        machine.setType(machineDetails.getType());

        return machineRepository.save(machine);
    }
}