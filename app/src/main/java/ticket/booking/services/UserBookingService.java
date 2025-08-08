package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
}


