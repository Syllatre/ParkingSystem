package com.parkit.parkingsystem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;



@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static ParkingSpot parkingSpot;
    private static Ticket ticket;


    private static String expectedErrorMessageFullPark =
            "Error fetching parking number from DB. Parking slots might be full";
    private static String expectedErrorMessageIllegalArgument =
            "Error parsing user input for type of vehicle";
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;


    @Nested
    @DisplayName("test for process incoming vehicle")
    class ProcessIncomingVehicleTest {

        @Test
        void processIncomingWhenParkFulledTest() throws Exception {

            try {
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to set up test mock InputReaderUntil");
            }
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(null)).thenReturn(-1);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processIncomingVehicle();

            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));
            assertThatThrownBy(() -> {
                throw new Exception(expectedErrorMessageFullPark);
            }).isInstanceOf(Exception.class);
        }

        @Test
        void processIncomingWhenNullParkingSpotTest() throws Exception {

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService = Mockito.spy(parkingService);

            Mockito.doReturn(null).when(parkingService).getNextParkingNumberIfAvailable();

            parkingService.processIncomingVehicle();

            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));

        }

        @Test
        void processIncomingWhenNegativeParkingSpotTest() throws Exception {

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService = Mockito.spy(parkingService);

            Mockito.doReturn(new ParkingSpot(-10, ParkingType.CAR, true)).when(parkingService)
                    .getNextParkingNumberIfAvailable();

            parkingService.processIncomingVehicle();

            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));

        }

    }

    @Nested
    @DisplayName("tests for process exiting vehicle")
    class ProcessExitingVehicleTest {

        @BeforeEach
        public void setUpTest() {
            try {
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to set up test mock InputReaderUntil");
            }

            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        }

        @Test
        void processExitingCarTest() {
            parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket.setParkingSpot(parkingSpot);

            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            final ArgumentCaptor<ParkingSpot> parkingSpotCaptor =
                    ArgumentCaptor.forClass(ParkingSpot.class);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();

            verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());
            verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());

            assertThat(parkingSpotCaptor.getValue().isAvailable()).isTrue();
            assertThat(ticketCaptor.getValue().getOutTime()).isNotNull();
        }


        @Test
        void processExitingBikeTest() {
            parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
            ticket.setParkingSpot(parkingSpot);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            final ArgumentCaptor<ParkingSpot> parkingSpotCaptor =
                    ArgumentCaptor.forClass(ParkingSpot.class);


            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();

            verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());
            verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());

            assertThat(parkingSpotCaptor.getValue().isAvailable()).isTrue();
            assertThat(ticketCaptor.getValue().getOutTime()).isNotNull();

        }
        @Test
        void processExitingVehicleUnableToUpdate() {
                parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
                ticket.setParkingSpot(parkingSpot);
                when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

                parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
                parkingService.processExitingVehicle();

            assertThatThrownBy(() -> {
                throw new Exception("Unable to process incoming vehicle");
            }).isInstanceOf(Exception.class);
        }
    }


    @Nested
    @DisplayName("tests to get next number of parkingSpot")
    class GetNextNumberParkingSpotAvailable {

        @Test
        void getNextNumberParkingSpotAvailableForCar() {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertThat(parkingSpot.getParkingType()).isEqualTo(ParkingType.CAR);
            assertThat(parkingSpot.getId()).isPositive();
            assertThat(parkingSpot.isAvailable()).isTrue();
        }


        @Test
        void getNextNumberParkingSpotAvailableForBike() {
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertThat(parkingSpot.getParkingType()).isEqualTo(ParkingType.BIKE);
            assertThat(parkingSpot.getId()).isPositive();
            assertThat(parkingSpot.isAvailable()).isTrue();
        }

        @Test
        void getNextNumberParkingSpotNotAvailableForVehicle() {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(null)).thenReturn(0);

            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertThatThrownBy(() -> {
                throw new Exception("Error parsing user input for type of vehicle");
            }).isInstanceOf(Exception.class);
        }

        @Test
        void getVehicleTypeException() {
            when(inputReaderUtil.readSelection()).thenReturn(3);

            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            assertThatThrownBy(() -> {
                throw new IllegalArgumentException("Entered input is invalid");
            }).isInstanceOf(IllegalArgumentException.class);
        }


        @Test
        void getNextNumberCarParkingSpotAvailable_WhenFullPark() {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);


            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.getNextParkingNumberIfAvailable();

            assertThatThrownBy(() -> {
                throw new Exception(expectedErrorMessageFullPark);
            }).isInstanceOf(Exception.class);

        }

        @Test
        void getNextNumberBikeParkingSpotAvailable_WhenFullPark() {
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.getNextParkingNumberIfAvailable();

            assertThatThrownBy(() -> {
                throw new Exception(expectedErrorMessageFullPark);
            }).isInstanceOf(Exception.class);

        }
    }
}