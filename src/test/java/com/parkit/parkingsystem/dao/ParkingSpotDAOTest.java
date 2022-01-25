package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {

    @Mock
    private DataBaseTestConfig dataBaseTestConfig;
    @Mock
    private Connection con;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    ParkingSpot parkingSpot;
    @InjectMocks
    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    private void setUp() throws Exception {

        when(dataBaseTestConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testNextParkingSpotIsFound() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(0, result); //la méthode renvoie -1 si la connection echoue donc 0 si elle fonctionne
    }

    @Test
    void testNextParkingSpotIsFoundForACar() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(2, result);
    }

    @Test
    void testUpdateParkingSpotForACar() throws SQLException {
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenReturn(1, 1);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
    }

    @Test
    void testUpdateParkingSpotForABike() throws SQLException {
        parkingSpot = new ParkingSpot(4, ParkingType.BIKE, false);
        when(preparedStatement.executeUpdate()).thenReturn(1, 4);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
    }

    @Test
    void testNextParkingSpotIsNotFoundForACar() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(-1, result);
    }

    @Test
    void testUpdateFail() throws SQLException {
        parkingSpot = new ParkingSpot(4, ParkingType.BIKE, false);
        //when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);
        when(preparedStatement.executeUpdate()).thenReturn(0, 0);
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertFalse(result);
    }


}
