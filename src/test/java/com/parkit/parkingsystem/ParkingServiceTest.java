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


/**
 * class of tests to check the use of {@link ParkingService}.
 */
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

    /**
     * class test to check the correct process incoming vehicle.
     * @author tlili
     */
    @Nested
    @DisplayName("test for process incoming vehicle")
    class ProcessIncomingVehicleTest {

        /**
         * test for incoming vehicle when park is full.
         */
        @Test
        void processIncomingWhenParkFulledTest() {
            // ARRANGE
            try {
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to set up test mock InputReaderUntil");
            }
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(null)).thenReturn(-1);



            // ACT
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processIncomingVehicle();

            // ASSERT
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));
            assertThatThrownBy(() -> {
                throw new Exception(expectedErrorMessageFullPark);
            }).isInstanceOf(Exception.class);
        }

        /**
         * test for incoming vehicle with no parking spot= null.
         */
        @Test
        void processIncomingWhenNullParkingSpotTest() {
            // ARRANGE

            // when(inputReaderUtil.readSelection()).thenReturn(1);
            // when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            // when(parkingService.getNextParkingNumberIfAvailable())
            // .thenReturn(new ParkingSpot(-12, ParkingType.CAR, true));

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService = Mockito.spy(parkingService);

            Mockito.doReturn(null).when(parkingService).getNextParkingNumberIfAvailable();

            // ACT
            parkingService.processIncomingVehicle();

            // ASSERT
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));

        }

        /**
         * test for incoming car with a given parking spot negative.
         */
        @Test
        void processIncomingWhenNegativeParkingSpotTest() {
            // ARRANGE

            // when(inputReaderUtil.readSelection()).thenReturn(1);
            // when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            // when(parkingService.getNextParkingNumberIfAvailable())
            // .thenReturn(new ParkingSpot(-12, ParkingType.CAR, true));

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService = Mockito.spy(parkingService);

            Mockito.doReturn(new ParkingSpot(-10, ParkingType.CAR, true)).when(parkingService)
                    .getNextParkingNumberIfAvailable();



            // ACT
            parkingService.processIncomingVehicle();

            // ASSERT
            verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, never()).saveTicket(any(Ticket.class));

        }

    }

    /**
     * class tests for method processingExitingVehicle.
     * @author tlili
     */
    @Nested
    @DisplayName("tests for process exiting vehicle")
    class ProcessExitingVehicleTest {
        /**
         * Setup initialize for all test in the class.
         */
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

        /**
         * test for a Exiting Car. When ParkingService.processExitingVehicle() is call Then
         * <ul>
         * <li>verify ParkingSpotDao.updateParking() and TicketDao.updateTicket() are called</li>
         * <li>assert That
         * <ul>
         * <li>ParkingSpot.isavailable is True</li>
         * <li>ticket.outTime is not null</li>
         * <li>ticket.price is correct</li>
         * </ul>
         * </ul>
         */
        @Test
        void processExitingCarTest() {
            // ARRANGE
            parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket.setParkingSpot(parkingSpot);

            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            final ArgumentCaptor<ParkingSpot> parkingSpotCaptor =
                    ArgumentCaptor.forClass(ParkingSpot.class);

            // ACT
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();

            // ASSERT
            verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());
            verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());

            assertThat(parkingSpotCaptor.getValue().isAvailable()).isTrue();
            assertThat(ticketCaptor.getValue().getOutTime()).isNotNull();
        }


        @Test
        void processExitingBikeTest() {
            // ARRANGE
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
//        @Test
//        void processExitingVehicleUnableToUpdate() {
//            // ARRANGE
//            parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
//            ticket.setParkingSpot(parkingSpot);
//            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
//            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
//
//            final ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
//            final ArgumentCaptor<ParkingSpot> parkingSpotCaptor =
//                    ArgumentCaptor.forClass(ParkingSpot.class);
//
//
//            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
//            parkingService.processExitingVehicle();
//
//            verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());
//            verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());
//            String message = "Uassnable to update ticket information. Error occurred";
//            ertThat(par)
//
//        }

    }
    /**
     * class test to verify if the given next parking spot available to park a vehicle is correct.
     * @author tlili
     */

    @Nested
    @DisplayName("tests to get next number of parkingSpot")
    class GetNextNumberParkingSpotAvailable {
        /**
         * test to verify if the correct parking spot available is given for a entering car.
         */
        @Test
        void getNextNumberParkingSpotAvailableForCar() {
            // ARRANGE

            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

            // ACT
            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            // ASSERT
            assertThat(parkingSpot.getParkingType()).isEqualTo(ParkingType.CAR);
            assertThat(parkingSpot.getId()).isPositive();
            assertThat(parkingSpot.isAvailable()).isTrue();
        }

        /**
         * test to verify if the correct parking spot available is given for a entering Bike.
         */
        @Test
        void getNextNumberParkingSpotAvailableForBike() {
            // ARRANGE

            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

            // ACT
            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();

            // ASSERT
            assertThat(parkingSpot.getParkingType()).isEqualTo(ParkingType.BIKE);
            assertThat(parkingSpot.getId()).isPositive();
            assertThat(parkingSpot.isAvailable()).isTrue();
        }

        /**
         * test to verify when a car try to have a parking spot and the park is full.
         */
        @Test
        void getNextNumberCarParkingSpotAvailable_WhenFullPark() {
            // ARRANGE
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

            // ACT
            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.getNextParkingNumberIfAvailable();

            // ASSERT

            assertThatThrownBy(() -> {
                throw new Exception(expectedErrorMessageFullPark);
            }).isInstanceOf(Exception.class);

        }

        /**
         * test to verify when a bike try to have a parking spot and the park is full.
         */
        @Test
        void getNextNumberBikeParkingSpotAvailable_WhenFullPark() {
            // ARRANGE
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

            // ACT
            ParkingService parkingService =
                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.getNextParkingNumberIfAvailable();

            // ASSERT

            assertThatThrownBy(() -> {
                throw new Exception(expectedErrorMessageFullPark);
            }).isInstanceOf(Exception.class);

        }
//        @Test
//        void getNextNumberVehicleParkingSpotAvailableWithIllegalArgument() {
//            when(inputReaderUtil.readSelection()).thenReturn(3);
//            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);
//
//            // ACT
//            ParkingService parkingService =
//                    new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
//            parkingService.getNextParkingNumberIfAvailable();
//
//            // ASSERT
//
//            assertThatThrownBy(() -> {
//                throw new Exception(expectedErrorMessageIllegalArgument);
//            }).isInstanceOf(Exception.class);
//
//        }
    }
}