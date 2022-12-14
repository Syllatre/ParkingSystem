package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
    TicketDAO recurringVehicle;

    public FareCalculatorService(TicketDAO recurringVehicle) {
        this.recurringVehicle = recurringVehicle;
    }

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        boolean recurringRegNumber = recurringVehicle.isRecurringVehicle(ticket.getVehicleRegNumber());
        double discount = 1;
        if (recurringRegNumber){
            discount =0.95;
        }


        /** we used getTime to transform time in milliseconds and transform it another
         * time in hour
         */

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();


        double duration = (double)(outHour - inHour)/ (1000*60*60);

        // we applied free half hour in price
        final double FREE_TIME = 0.5;
        if(duration <FREE_TIME)
            duration = 0;
        else
            duration = duration - FREE_TIME;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice((double)Math.round(duration * Fare.CAR_RATE_PER_HOUR * discount*100)/100);
                break;
            }
            case BIKE: {
               ticket.setPrice((double)Math.round(duration * Fare.BIKE_RATE_PER_HOUR * discount*100)/100);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
   }
}