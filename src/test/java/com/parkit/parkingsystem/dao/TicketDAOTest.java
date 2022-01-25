package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


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





    Ticket ticket;


    @InjectMocks
    private TicketDAO ticketDAO;


    @BeforeEach
    public void setUp() {


        ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(
                1,
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
    void savingValidTicket() throws Exception {
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
    void saveANonValidTicket() throws Exception {
        when(dataBaseTestConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(false);
        Connection connection = dataBaseTestConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.SAVE_TICKET);
        ticketDAO.saveTicket(ticket);


        assertThatThrownBy(() -> {
            throw new Exception("Error fetching next available slot");
        }).isInstanceOf(Exception.class);
    }

//    @Test
//    void getNonValidTicket() throws Exception {
//        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
//        when(preparedStatement.executeQuery()).thenReturn(resultSet);
//        Connection connection = dataBaseTestConfig.getConnection();
//        PreparedStatement ps = connection.prepareStatement(DBConstants.GET_TICKET);
//        ResultSet rs =  ps.executeQuery();
//
//
//
//        ticketDAO.getTicket("ABCDE");
//
//
//        assertThatThrownBy(() -> {
//            throw new Exception("Error fetching next available slot");
//        }).isInstanceOf(Exception.class);
//    }

    @Test
    void isRecurringVehicleException() throws Exception {
        when(dataBaseTestConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Connection connection = dataBaseTestConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(DBConstants.RECURRING_VEHICLE);
        ResultSet rs =  ps.executeQuery();

        ticketDAO.isRecurringVehicle("adsd");

        assertThatThrownBy(() -> {
            throw new Exception("Error fetching next available slot");
        }).isInstanceOf(Exception.class);
    }

    @Test
    void insideException() throws Exception {
        when(dataBaseTestConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Connection connection = dataBaseTestConfig.getConnection();
        PreparedStatement ps = connection.prepareStatement(DBConstants.INSIDE);
        ResultSet rs =  ps.executeQuery();

        ticketDAO.inside("adsd");


        assertThatThrownBy(() -> {
            throw new Exception("Error fetching next available slot");
        }).isInstanceOf(Exception.class);
    }

    @Test
    void updateNotValidTicket() throws Exception {
        when(dataBaseTestConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
        Connection connection = dataBaseTestConfig.getConnection();
        when(preparedStatement.execute()).thenReturn(false);

        PreparedStatement preparedStatement = connection.prepareStatement(DBConstants.UPDATE_TICKET);
        ticketDAO.updateTicket(ticket);


        assertThatThrownBy(() -> {
            throw new Exception("Error saving ticket info");
        }).isInstanceOf(Exception.class);
    }

}
