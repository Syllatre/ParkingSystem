package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static FareCalculatorService fareCalculatorService;
    private  Date outTime;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        fareCalculatorService = new FareCalculatorService(ticketDAO);

    }

    @BeforeEach
    private void setUpPerTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        int parkingPlace = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        ParkingSpot parkingSpot = new ParkingSpot(parkingPlace,ParkingType.CAR,true);
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
        parkingSpot.setAvailable(false);

        assertEquals(vehicleRegNumber, ticketDAO.getTicket(vehicleRegNumber).getVehicleRegNumber());
        assertFalse(parkingSpot.isAvailable());
    }

    @ParameterizedTest
    @MethodSource("exitTime")
    public void testParkingLotExit( Date outTime) throws Exception {
        outTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
        assertNotEquals(null, ticketDAO.getTicket(vehicleRegNumber).getPrice());
        assertNotEquals(null, ticketDAO.getTicket(vehicleRegNumber).getOutTime());
    }
    static Stream<Date> exitTime() {
        Date outTime = new Date();
        outTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        return Stream.of(outTime);
    }

    @Test
    public void testRecurringVehicle() throws Exception {
        testParkingACar();
        Date outTime = new Date();
        outTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        testParkingLotExit(outTime);
        testParkingACar();
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
        assertEquals(true, ticketDAO.isRecurringVehicle(vehicleRegNumber));

    }

}
