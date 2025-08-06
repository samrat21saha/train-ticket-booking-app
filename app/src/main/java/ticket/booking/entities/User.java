package ticket.booking.entities;

import java.util.List;

public class User {
    private String userId;
    private String userName;
    private String password;
    private String hashedPassword;
    private List<Ticket> ticketsBooked;
}
