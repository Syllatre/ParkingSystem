package com.parkit.parkingsystem.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.parkit.parkingsystem.constants.DBConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import javax.inject.Inject;


@ExtendWith(MockitoExtension.class)
class TicketDAOTest {

    @Mock
    private DataBaseTestConfig dataBaseTestConfig;
    @Mock
    private Connection con;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Mock
    ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;

    Ticket ticket;
    ParkingSpot parkingSpot;


    @Mock
    private TicketDAO ticketDAO;

    @Mock
    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    public void setUp() throws Exception {
//        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
//        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        when(dataBaseTestConfig.getConnection()).thenReturn(con);
//        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);

        //on créé un ticket pour une voiture ABCDEF entrée et sortie.
        ticket = new Ticket();
//        when(preparedStatement.executeQuery()).thenReturn(resultSet);
//        when(resultSet.next()).thenReturn(true);
        ParkingSpot parkingSpot = new ParkingSpot(
                parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR),
                ParkingType.CAR, true);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticket.setInTime(inTime);
        ticket.setPrice(1.50);
        Date outTime = new Date();
        ticket.setOutTime(outTime);
    }


    @Test
    void savingValidTicket() throws SQLException, ClassNotFoundException {
        // assertTrue(ticketDAO.saveTicket(ticket));

        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;

        when(dataBaseTestConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
        Connection connection = dataBaseTestConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.SAVE_TICKET);
        when(preparedStatement.execute()).thenReturn(true);
        assertNotNull(connection);
        assertNotNull(preparedStatement);
        assertTrue(ticketDAO.saveTicket(ticket));
    }

    @Test
    void savingAticket_and_recurrentClient() {
        try {
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.getInt("count")).thenReturn(2);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        ticketDAO.saveTicket(ticket);


    }

    @Test
    void savingAticket_and_clientIsNotRecurrent() {
        try {
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.getInt("count")).thenReturn(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ticketDAO.saveTicket(ticket);

    }

    @Test
    void gettingATicket() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getString(6)).thenReturn("CAR");

        assertNotNull(ticketDAO.getTicket("ABCDEF").getId());
    }

    @Test
    void updatingATicket() {
        assertTrue(ticketDAO.updateTicket(ticket));
    }

}
