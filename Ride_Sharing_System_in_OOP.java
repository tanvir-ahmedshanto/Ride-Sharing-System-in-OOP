

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Interface for vehicles
interface Vehicle {
    void startRide();
    void stopRide();
    String getVehicleInfo();
}

// Interface for payment methods
interface PaymentMethod {
    void processPayment(double amount) throws PaymentException;
}

// Custom exception for payment failures
class PaymentException extends Exception {
    public PaymentException(String message) {
        super(message);
    }
}


// Abstract user class
abstract class User {
    protected String userId;
    protected String name;
    protected String phone;
    protected String password;

    public User(String userId, String name, String phone, String password) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.password = password;
    }

    public abstract void displayProfile();
    
    public void contactSupport() {
        System.out.println(name + " is contacting support...");
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}

// Rider class
class Rider extends User {
    private PaymentMethod paymentMethod;
    private Ride currentRide;
    private List<Ride> rideHistory;

    public Rider(String userId, String name, String phone, String password, PaymentMethod paymentMethod) {
        super(userId, name, phone, password);
        this.paymentMethod = paymentMethod;
        this.rideHistory = new ArrayList<>();
    }

    @Override
    public void displayProfile() {
        System.out.println("\nPassenger Profile:");
        System.out.println("ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Phone: " + phone);
        System.out.println("Total Rides: " + rideHistory.size());
    }

    public void requestRide(Driver driver, String pickup, String destination, double distance) {
        currentRide = new Ride(this, driver, pickup, destination, distance);
        System.out.println(name + " requested a ride from " + pickup + " to " + destination);
    }

    public void completeRide() throws PaymentException {
        if (currentRide != null) {
            currentRide.complete(paymentMethod);
            rideHistory.add(currentRide);
            currentRide = null;
        }
    }

    public Ride getCurrentRide() {
        return currentRide;
    }
}

// Driver class
class Driver extends User {
    private Vehicle vehicle;
    private boolean available;
    private List<Ride> completedRides;

    public Driver(String userId, String name, String phone, String password, Vehicle vehicle) {
        super(userId, name, phone, password);
        this.vehicle = vehicle;
        this.available = true;
        this.completedRides = new ArrayList<>();
    }

    @Override
    public void displayProfile() {
        System.out.println("\nDriver Profile:");
        System.out.println("ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Phone: " + phone);
        System.out.println("Vehicle: " + vehicle.getVehicleInfo());
        System.out.println("Completed Rides: " + completedRides.size());
        System.out.println("Status: " + (available ? "Available" : "On Ride"));
    }

    public void acceptRide(Ride ride) {
        if (available) {
            System.out.println(name + " accepted ride from " + ride.getRider().name);
            ride.setDriver(this);
            available = false;
            vehicle.startRide();
        }
    }

    public void completeRide(Ride ride) {
        vehicle.stopRide();
        completedRides.add(ride);
        available = true;
        System.out.println(name + " completed the ride");
    }

    public boolean isAvailable() {
        return available;
    }
}

// Ride class
class Ride {
    private static final double BASE_FARE = 20.50;
    private static final double PER_MILE_RATE = 3;
    
    private Rider rider;
    private Driver driver;
    private String pickupLocation;
    private String destination;
    private double distance;
    private boolean completed;

    public Ride(Rider rider, Driver driver, String pickup, String destination, double distance) {
        this.rider = rider;
        this.driver = driver;
        this.pickupLocation = pickup;
        this.destination = destination;
        this.distance = distance;
        this.completed = false;
    }

    public double calculateFare() {
        return BASE_FARE + (distance * PER_MILE_RATE);
    }

    public void complete(PaymentMethod paymentMethod) throws PaymentException {
        double fare = calculateFare();
        paymentMethod.processPayment(fare);
        driver.completeRide(this);
        completed = true;
        if (paymentMethod instanceof HandCashPayment) {
            System.out.println("Ride completed. Please pay " + fare + " Taka to the driver");
        } else {
            System.out.println("Ride completed. Fare: " + fare + " Taka");  
        }
    }

    public Rider getRider() {
        return rider;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}

// Vehicle implementations
class Car implements Vehicle {
    private String licensePlate;
    private String model;

    public Car(String licensePlate, String model) {
        this.licensePlate = licensePlate;
        this.model = model;
    }

    @Override
    public void startRide() {
        System.out.println(model + " car (" + licensePlate + ") started the ride");
    }

    @Override
    public void stopRide() {
        System.out.println(model + " car (" + licensePlate + ") stopped the ride");
    }

    @Override
    public String getVehicleInfo() {
        return model + " (License: " + licensePlate + ")";
    }
}

class Bike implements Vehicle {
    private String registrationNumber;
    private String type;

    public Bike(String registrationNumber, String type) {
        this.registrationNumber = registrationNumber;
        this.type = type;
    }

    @Override
    public void startRide() {
        System.out.println(type + " bike (" + registrationNumber + ") started the ride");
    }

    @Override
    public void stopRide() {
        System.out.println(type + " bike (" + registrationNumber + ") stopped the ride");
    }

    @Override
    public String getVehicleInfo() {
        return type + " bike (Reg: " + registrationNumber + ")";
    }
}

// Payment method implementations
class CreditCardPayment implements PaymentMethod {
    private String cardNumber;
    private String expiry;

    public CreditCardPayment(String cardNumber, String expiry) {
        this.cardNumber = cardNumber;
        this.expiry = expiry;
    }

    @Override
    public void processPayment(double amount) throws PaymentException {
        if (amount <= 0) {
            throw new PaymentException("Invalid payment amount: " + amount + " Taka");
        }
        System.out.println("Processing credit card payment of " + amount + " Taka");
        System.out.println("Payment successful with card ending in " + 
                          cardNumber.substring(cardNumber.length() - 4));
    }
}

class DigitalWalletPayment implements PaymentMethod {
    private String walletId;

    public DigitalWalletPayment(String walletId) {
        this.walletId = walletId;
    }

    @Override
    public void processPayment(double amount) throws PaymentException {
        if (amount <= 0) {
            throw new PaymentException("Invalid payment amount: " + amount + " Taka");
        }
        System.out.println("Processing digital wallet payment of " + amount +" Taka");
        System.out.println("Payment successful with wallet: " + walletId);
    }
}

// Hand Cash Payment implementation
class HandCashPayment implements PaymentMethod {
    @Override
    public void processPayment(double amount) throws PaymentException {
        if (amount <= 0) {
            throw new PaymentException("Invalid payment amount: " + amount + " Taka");
        }
        System.out.println("Processing hand cash payment of " + amount + " Taka");
        System.out.println("Please pay " + amount + "Taka directly to the driver");
    }
}

// Admin class
class Admin extends User {
    private List<User> allUsers;

    public Admin(String userId, String name, String phone, String password, List<User> allUsers) {
        super(userId, name, phone, password);
        this.allUsers = allUsers;
    }

    @Override
    public void displayProfile() {
        System.out.println("\nAdmin Profile:");
        System.out.println("ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Phone: " + phone);
    }

    public void viewAllUsers() {
        System.out.println("\nAll Registered Users:");
        for (User user : allUsers) {
            user.displayProfile();
            System.out.println("-------------------");
        }
    }
}

// Ride Sharing System with Menu
public class RideSharingSystem {
    private static List<User> users = new ArrayList<>();
    private static List<Driver> drivers = new ArrayList<>();
    private static List<Rider> riders = new ArrayList<>();
    private static Admin admin;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeSystem();
        showMainMenu();
    }

    private static void initializeSystem() {
        // Create default admin
        admin = new Admin("A100", "EI MAMA", "01831650978", "admin123", users);
        users.add(admin);

        // Create some default drivers
        Vehicle sedan = new Car("ABC123", "Toyota Camry");
        Vehicle suv = new Car("XYZ789", "Honda CR-V");
        Vehicle scooter = new Bike("BIKE001", "TVS");

        Driver driver1 = new Driver("D100", "Abdur Rahim", "01735537376", "driver1", sedan);
        Driver driver2 = new Driver("D101", "Abdul Karim", "0175550102", "driver2", suv);
        Driver driver3 = new Driver("D102", "Suleman", "0175550103", "driver3", scooter);

        drivers.add(driver1);
        drivers.add(driver2);
        drivers.add(driver3);
        users.add(driver1);
        users.add(driver2);
        users.add(driver3);

        // Create some default riders
        PaymentMethod card = new CreditCardPayment("4111111111111111", "12/25");
        PaymentMethod wallet = new DigitalWalletPayment("mary@payapp.com");
        PaymentMethod cash = new HandCashPayment();

        Rider rider1 = new Rider("R100", "Tanvir", "01303910166", "rider1", card);
        Rider rider2 = new Rider("R101", "Tuser", "01760049326", "rider2", wallet);
        Rider rider3 = new Rider("R102", "Tousiq", "01712345678", "rider3", cash);

        riders.add(rider1);
        riders.add(rider2);
        riders.add(rider3);
        users.add(rider1);
        users.add(rider2);
        users.add(rider3);
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1) Admin Panel");
            System.out.println("2) Driver Panel");
            System.out.println("3) Passenger Panel");
            System.out.println("4) Register New User");
            System.out.println("0) Exit");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    adminLogin();
                    break;
                case 2:
                    driverLogin();
                    break;
                case 3:
                    passengerLogin();
                    break;
                case 4:
                    registerNewUser();
                    break;
                case 0:
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void adminLogin() {
        System.out.print("\nEnter Admin ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (id.equals(admin.userId) && admin.authenticate(password)) {
            adminPanel();
        } else {
            System.out.println("Invalid admin credentials!");
        }
    }

    private static void adminPanel() {
        while (true) {
            System.out.println("\nAdmin Panel");
            System.out.println("1) View All Users");
            System.out.println("2) View All Drivers");
            System.out.println("3) View All Passengers");
            System.out.println("0) Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    admin.viewAllUsers();
                    break;
                case 2:
                    System.out.println("\nAll Drivers:");
                    for (Driver driver : drivers) {
                        driver.displayProfile();
                        System.out.println("-------------------");
                    }
                    break;
                case 3:
                    System.out.println("\nAll Passengers:");
                    for (Rider rider : riders) {
                        rider.displayProfile();
                        System.out.println("-------------------");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void driverLogin() {
        System.out.print("\nEnter Driver ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        for (Driver driver : drivers) {
            if (driver.userId.equals(id) && driver.authenticate(password)) {
                driverPanel(driver);
                return;
            }
        }
        System.out.println("Invalid driver credentials or driver not found!");
    }

    private static void driverPanel(Driver driver) {
        while (true) {
            System.out.println("\nDriver Panel - " + driver.name);
            System.out.println("1) View Profile");
            System.out.println("2) View Available Rides");
            System.out.println("0) Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    driver.displayProfile();
                    break;
                case 2:
                    System.out.println("\nAvailable Rides:");
                    if (!riders.isEmpty()) {
                        System.out.println("1. Ride from Mirpur to Asulia (10.2 miles)");
                        System.out.println("2. Ride from Asulia to Uttara State (12.4 miles)");
                    } else {
                        System.out.println("No available rides at the moment.");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void passengerLogin() {
        System.out.print("\nEnter Passenger ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        for (Rider rider : riders) {
            if (rider.userId.equals(id) && rider.authenticate(password)) {
                passengerPanel(rider);
                return;
            }
        }
        System.out.println("Invalid passenger credentials or passenger not found!");
    }

    private static void passengerPanel(Rider rider) {
        while (true) {
            System.out.println("\nPassenger Panel - " + rider.name);
            System.out.println("1) View Profile");
            System.out.println("2) Request Ride");
            System.out.println("0) Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    rider.displayProfile();
                    break;
                case 2:
                    if (!drivers.isEmpty()) {
                        System.out.print("Enter pickup location: ");
                        String pickup = scanner.nextLine();
                        System.out.print("Enter destination: ");
                        String destination = scanner.nextLine();
                        System.out.print("Enter distance (miles): ");
                        double distance = scanner.nextDouble();
                        scanner.nextLine(); // consume newline

                        // In a real system, you would match with an available driver
                        Driver availableDriver = drivers.get(0);
                        rider.requestRide(availableDriver, pickup, destination, distance);
                        availableDriver.acceptRide(rider.getCurrentRide());

                        System.out.println("Press any key to complete ride...");
                        scanner.nextLine();
                        try {
                            rider.completeRide();
                        } catch (PaymentException e) {
                            System.out.println("Payment failed: " + e.getMessage());
                        }
                    } else {
                        System.out.println("No drivers available at the moment.");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void registerNewUser() {
        System.out.println("\nRegister New User");
        System.out.println("1) Register as Driver");
        System.out.println("2) Register as Passenger");
        System.out.println("0) Back to Main Menu");
        System.out.print("Choose option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                registerDriver();
                break;
            case 2:
                registerRider();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void registerDriver() {
        System.out.print("\nEnter Driver ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        System.out.print("Enter Vehicle Type (car/bike): ");
        String vehicleType = scanner.nextLine();
        System.out.print("Enter License/Registration: ");
        String license = scanner.nextLine();
        System.out.print("Enter Model: ");
        String model = scanner.nextLine();

        Vehicle vehicle;
        if (vehicleType.equalsIgnoreCase("car")) {
            vehicle = new Car(license, model);
        } else {
            vehicle = new Bike(license, model);
        }

        Driver newDriver = new Driver(id, name, phone, password, vehicle);
        drivers.add(newDriver);
        users.add(newDriver);
        System.out.println("Driver registration successful!");
    }

    private static void registerRider() {
        System.out.print("\nEnter Passenger ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        System.out.println("Select Payment Method:");
        System.out.println("1) Credit Card");
        System.out.println("2) Digital Wallet");
        System.out.println("3) Hand Cash Payment");
        System.out.print("Choose option: ");
        int paymentChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        PaymentMethod paymentMethod;
        if (paymentChoice == 1) {
            System.out.print("Enter Card Number: ");
            String cardNumber = scanner.nextLine();
            System.out.print("Enter Expiry (MM/YY): ");
            String expiry = scanner.nextLine();
            paymentMethod = new CreditCardPayment(cardNumber, expiry);
        } else if (paymentChoice == 2) {
            System.out.print("Enter Wallet ID: ");
            String walletId = scanner.nextLine();
            paymentMethod = new DigitalWalletPayment(walletId);
        } else if (paymentChoice == 3) {
            paymentMethod = new HandCashPayment();
        } else {
            System.out.println("Invalid choice. Defaulting to Hand Cash Payment.");
            paymentMethod = new HandCashPayment();
        }

        Rider newRider = new Rider(id, name, phone, password, paymentMethod);
        riders.add(newRider);
        users.add(newRider);
        System.out.println("Passenger registration successful!");
    }
}
