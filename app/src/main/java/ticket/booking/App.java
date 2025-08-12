package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {
    // This will hold the currently logged-in user. It's null until a successful login.
    private static User currentUser = null;

    public static void main(String[] args) throws IOException {

        System.out.println("Welcome to Bharat Rail Ticket Booking App!");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;

        try {
            // Initialize UserBookingService once at the start of the application
            // This service will manage all user and train data from files
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Error initializing application: " + ex.getMessage());
            System.out.println("Please ensure data files are accessible. Exiting.");
            return; // Exit if service cannot be initialized
        }

        // trainSelectedForBooking should be initialized to null and updated when a train is selected.
        Train trainSelectedForBooking = null;

        while (option != 7) {
            System.out.println("\n--- Choose an option ---");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch My Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel My Booking");
            System.out.println("7. Exit the App");
            System.out.print("Your choice: ");

            // Consume the newline character left by nextInt() to prevent issues with nextLine()
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1: // Sign Up
                    System.out.println("--- Sign Up ---");
                    System.out.print("Enter a new username: ");
                    String nameToSignUp = scanner.nextLine(); // Use nextLine for full line input
                    System.out.print("Enter a password: ");
                    String passwordToSignUp = scanner.nextLine();

                    // Create a new User object with all necessary details for signup
                    User userToSignup = new User(
                            nameToSignUp,
                            passwordToSignUp, // Plain password (not stored)
                            UserServiceUtil.hashPassword(passwordToSignUp), // Hashed password (to be stored)
                            new ArrayList<>(), // Empty list for new user's tickets
                            UUID.randomUUID().toString() // Generate a unique ID for the new user
                    );

                    if (userBookingService.signUp(userToSignup)) {
                        System.out.println("Sign up successful! Please log in.");
                    } else {
                        System.out.println("Sign up failed. Username might already exist or file error.");
                    }
                    break;

                case 2: // Login
                    System.out.println("--- Login ---");
                    System.out.print("Enter your username: ");
                    String nameToLogin = scanner.nextLine();
                    System.out.print("Enter your password: ");
                    String passwordToLogin = scanner.nextLine();

                    // Call the loginUser method which now returns an Optional<User>
                    // Pass only username and password, don't create a full User object here for login
                    Optional<User> loggedInUserOptional = userBookingService.loginUser(nameToLogin, passwordToLogin);

                    if (loggedInUserOptional.isPresent()) {
                        currentUser = loggedInUserOptional.get(); // Set the static currentUser
                        // You might want to update the user object within UserBookingService as well if it needs to hold it internally
                        // userBookingService.setCurrentUser(currentUser); // Uncomment if you add this setter to UserBookingService
                        System.out.println("Login successful! Welcome, " + currentUser.getUserName() + "!");
                    } else {
                        System.out.println("Login failed. Invalid username or password.");
                        currentUser = null; // Ensure currentUser is null on failed login
                    }
                    break;

                case 3: // Fetch Bookings
                    System.out.println("--- Fetch Bookings ---");
                    if (currentUser == null) {
                        System.out.println("Please log in first to view your bookings (Option 2).");
                        break;
                    }
                    // Pass the current logged-in user to the service method
                    userBookingService.fetchBookings(currentUser); // Assuming fetchBookings takes a User
                    break;

                case 4: // Search Trains
                    System.out.println("--- Search Trains ---");
                    System.out.print("Type your source station: ");
                    String source = scanner.nextLine();
                    System.out.print("Type your destination station: ");
                    String dest = scanner.nextLine();

                    List<Train> trains = userBookingService.getTrains(source, dest);

                    if (trains.isEmpty()) {
                        System.out.println("No trains found for this route.");
                        trainSelectedForBooking = null; // Reset selection
                    } else {
                        System.out.println("Available Trains:");
                        for (int i = 0; i < trains.size(); i++) {
                            Train t = trains.get(i);
                            System.out.println((i + 1) + ". Train ID: " + t.getTrainId());
                            System.out.println("   Stations and Times:");
                            // Ensure stationArrivalTimes is not null and iterate
                            if (t.getStationArrivalTimes() != null) {
                                for (Map.Entry<String, String> entry : t.getStationArrivalTimes().entrySet()) {
                                    System.out.println("      " + entry.getKey() + ": " + entry.getValue());
                                }
                            }
                        }
                        System.out.print("Select a train by typing its number (e.g., 1, 2, 3...): ");
                        int trainChoice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        if (trainChoice > 0 && trainChoice <= trains.size()) {
                            trainSelectedForBooking = trains.get(trainChoice - 1); // Get correct train by index
                            System.out.println("You selected Train: " + trainSelectedForBooking.getTrainId());
                        } else {
                            System.out.println("Invalid train selection.");
                            trainSelectedForBooking = null; // Reset selection
                        }
                    }
                    break;

                case 5: // Book a Seat
                    System.out.println("--- Book a Seat ---");
                    if (currentUser == null) {
                        System.out.println("Please log in first to book a seat (Option 2).");
                        break;
                    }
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please search and select a train first (Option 4).");
                        break;
                    }

                    System.out.println("Seats for Train " + trainSelectedForBooking.getTrainId() + ":");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    for (List<Integer> row : seats) {
                        for (Integer val : row) {
                            System.out.print(val + " "); // Print 0 for available, 1 for booked
                        }
                        System.out.println();
                    }

                    System.out.print("Enter the row number (0-indexed): ");
                    int row = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    System.out.print("Enter the column number (0-indexed): ");
                    int col = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    System.out.println("Attempting to book your seat....");
                    // Pass the current user for associating the ticket
                    Boolean booked = userBookingService.bookTrainSeat(currentUser, trainSelectedForBooking, row, col);

                    if (booked.equals(Boolean.TRUE)) {
                        System.out.println("Booked! Enjoy your journey!");
                        // You might want to refresh currentUser's tickets here from the service
                        // For example, by calling currentUser = userBookingService.loginUser(currentUser.getUserName(), currentUser.getPassword()).get();
                    } else {
                        System.out.println("Can't book this seat. It might be taken or invalid coordinates.");
                    }
                    break;

                case 6: // Cancel My Booking
                    System.out.println("--- Cancel Booking ---");
                    if (currentUser == null) {
                        System.out.println("Please log in first to cancel a booking (Option 2).");
                        break;
                    }
                    System.out.print("Enter the Ticket ID you wish to cancel: ");
                    String ticketIdToCancel = scanner.nextLine();

                    Boolean canceled = userBookingService.cancelBooking(ticketIdToCancel);
                    // The cancelBooking method already prints messages, so just need to ensure the logic flows.
                    // This method now handles confirmation internally.
                    if (canceled.equals(Boolean.TRUE)) {
                        // Optional: Re-fetch user data to update in-memory tickets list after cancellation
                        // This might be done by calling loginUser again or having a refresh method in UserBookingService
                    }
                    break;

                case 7: // Exit
                    System.out.println("Thank you for using Bharat Rail Ticket Booking App. Goodbye!");
                    break;

                default: // Invalid Option
                    System.out.println("Invalid option. Please choose a number between 1 and 7.");
                    break;
            }
        }
        scanner.close(); // Close the scanner when the app exits
    }
}