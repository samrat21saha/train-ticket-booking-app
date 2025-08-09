package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {

    private User user;

    private List<User> userList;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException{
        File userDataFilePath = new File(USERS_PATH);
        userList = objectMapper.readValue(userDataFilePath, new TypeReference<List<User>>() {
        });
    }

    public boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 ->{
            return user1.getUserName().equals(user.getUserName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return  foundUser.isPresent();
    }

    public  boolean signUp(User newUserData) throws  IOException {
        userList.add(newUserData);
        saveUserListToDataFile();
        return true;
    }

    private void saveUserListToDataFile() throws IOException {
        File updatedUserData = new File(USERS_PATH);
        objectMapper.writeValue(updatedUserData, userList);
    }


    public void fetchBookings() {
        Optional<User> userBooking = userList.stream().filter(userBookingData -> {
            return userBookingData.getUserName().equals(user.getUserName()) && UserServiceUtil.checkPassword(user.getPassword(), userBookingData.getHashedPassword());
        }).findFirst();
        if (userBooking.isPresent()) {
            userBooking.get().printTickets();
        }
    }


    // Booking Cancel method
    public Boolean cancelBooking(String ticketId){
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your ticketId:");
        ticketId = in.next();

        if(ticketId == null || ticketId.isEmpty()){
            System.out.println("TicketId can't be empty:");
            return Boolean.FALSE;
        }else{
            System.out.println("Are you Sure you want to cancel ticket with id:" + ticketId + "?");
            String cancelDecision = in.nextLine().toLowerCase();
            switch (cancelDecision){
                case "yes":
                    System.out.println("Ok Cancelling ticket with id:" + ticketId + "...");
                    break;

                case "no":
                    System.out.println("Please Exit the Process!");
                    break;

                default:
                    System.out.println("Enter Valid choice: 'Yes' or 'No' ");
            }
        }

        String finalTicketId = ticketId;
        boolean removed = false;
        for (int i = 0; i < user.getTicketsBooked().size(); i++) {
            Ticket ticket = user.getTicketsBooked().get(i);
            if (ticket.getTicketId().equals(finalTicketId)) {
                user.getTicketsBooked().remove(i);
                removed = true;
                break; // Stop after removing the first matching ticket
            }
        }

        if (removed) {
            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }


    }
}


