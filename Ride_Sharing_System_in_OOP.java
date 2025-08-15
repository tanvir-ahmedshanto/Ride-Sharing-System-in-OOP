// updated code of rafi project

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;

// ------------- EXCEPTIONS -----------------

/**
 * Custom exception class for handling ride-sharing specific errors.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class RideSharingException extends Exception implements Serializable {
    public RideSharingException(String message) {
        super(message);
    }
}

// ------------- INTERFACES & ABSTRACT CLASSES --------------

/**
 * Defines actions for a vehicle.
 * This interface is now Serializable to allow it to be part of the persisted state.
 */
interface VehicleActions extends Serializable {
    void startRide() throws RideSharingException;
    void endRide();
    double calculateFare(double distance);
}

/**
 * Abstract base class for all users.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
abstract class User implements Serializable {
    private String userId;
    private String name;
    private String phone;
    private String email;

    public User(String userId, String name, String phone, String email) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // Setters with validation
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty())
            this.name = name;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty())
            this.phone = phone;
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@"))
            this.email = email;
    }

    // Abstract method to define role
    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " [ID=" + userId + ", Name=" + name + ", Phone=" + phone + ", Email=" + email + "]";
    }
}

// ------------- USER CLASSES ----------------

/**
 * Represents a passenger in the system.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Passenger extends User implements Serializable {
    private List<Ride> rideHistory = new ArrayList<>();
    private List<Complaint> complaints = new ArrayList<>();

    public Passenger(String userId, String name, String phone, String email) {
        super(userId, name, phone, email);
    }

    // Ride history methods
    public List<Ride> getRideHistory() { return rideHistory; }
    public void addRideToHistory(Ride ride) { rideHistory.add(ride); }

    // Complaint methods
    public void addComplaint(Complaint complaint) {
        complaints.add(complaint);
    }
    public List<Complaint> getComplaints() { return complaints; }

    @Override
    public String getRole() { return "Passenger"; }
}

/**
 * Represents a driver in the system.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Driver extends User implements Serializable {
    private Vehicle vehicle;
    private boolean isVerified;
    private List<Ride> rideHistory = new ArrayList<>();
    private double totalEarnings;
    private double commissionRate = 0.2; // 20% commission
    private boolean licenseUploaded;
    private boolean vehicleDocumentsUploaded;
    private List<Complaint> complaints = new ArrayList<>();

    public Driver(String userId, String name, String phone, String email, Vehicle vehicle) {
        super(userId, name, phone, email);
        this.vehicle = vehicle;
        this.isVerified = false;
        this.licenseUploaded = false;
        this.vehicleDocumentsUploaded = false;
    }

    // Vehicle getter
    public Vehicle getVehicle() { return vehicle; }

    // Verification status
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    // Document uploads
    public void uploadLicense() { licenseUploaded = true; }
    public void uploadVehicleDocuments() { vehicleDocumentsUploaded = true; }
    public boolean hasUploadedDocuments() {
        return licenseUploaded && vehicleDocumentsUploaded;
    }

    // Ride history methods
    public List<Ride> getRideHistory() { return rideHistory; }
    public void addRideToHistory(Ride ride) { rideHistory.add(ride); }

    // Earnings methods
    public double getTotalEarnings() { return totalEarnings; }
    public void addEarnings(double fare) {
        double commission = fare * commissionRate;
        totalEarnings += (fare - commission);
    }

    // Commission rate getter/setter
    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double rate) {
        if (rate >= 0 && rate <= 1)
            commissionRate = rate;
    }

    // Complaint methods
    public void addComplaint(Complaint complaint) {
        complaints.add(complaint);
    }
    public List<Complaint> getComplaints() { return complaints; }

    @Override
    public String getRole() { return "Driver"; }

    @Override
    public String toString() {
        return super.toString() + ", Vehicle: " + vehicle + ", Verified: " + isVerified;
    }
}

/**
 * Represents an administrator in the system.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Admin extends User implements Serializable {
    public Admin(String userId, String name, String phone, String email) {
        super(userId, name, phone, email);
    }

    @Override
    public String getRole() { return "Admin"; }

    // Approve a driver after document check
    public void approveDriver(Driver driver) throws RideSharingException {
        if (!driver.hasUploadedDocuments())
            throw new RideSharingException("Driver must upload all documents.");
        driver.setVerified(true);
        System.out.println("Driver " + driver.getName() + " approved.");
    }

    // Reject a driver
    public void rejectDriver(Driver driver) {
        driver.setVerified(false);
        System.out.println("Driver " + driver.getName() + " rejected.");
    }
}

// ------------- VEHICLES -------------------

/**
 * Abstract base class for all vehicles.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
abstract class Vehicle implements VehicleActions, Serializable {
    private String vehicleId;
    private String model;
    private String color;
    private int seatCount;
    private boolean isAvailable;

    public Vehicle(String vehicleId, String model, String color, int seatCount) {
        this.vehicleId = vehicleId;
        this.model = model;
        this.color = color;
        this.seatCount = seatCount;
        this.isAvailable = true;
    }

    // Getters and setters
    public String getVehicleId() { return vehicleId; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public int getSeatCount() { return seatCount; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    // Abstract fare calculation method
    public abstract double calculateFare(double distance);

    @Override
    public void startRide() throws RideSharingException {
        if (!isAvailable) {
            throw new RideSharingException("Vehicle " + vehicleId + " not available");
        }
        isAvailable = false;
        System.out.println("Vehicle " + vehicleId + " started ride.");
    }

    @Override
    public void endRide() {
        isAvailable = true;
        System.out.println("Vehicle " + vehicleId + " ended ride.");
    }

    @Override
    public String toString() {
        return model + " (" + vehicleId + "), Color: " + color + ", Seats: " + seatCount + ", Available: " + isAvailable;
    }
}

/**
 * Represents a Car vehicle.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Car extends Vehicle implements Serializable {
    private static final double BASE_FARE_PER_KM = 12;

    public Car(String vehicleId, String model, String color, int seatCount) {
        super(vehicleId, model, color, seatCount);
    }

    // Fare with surge multiplier (method overloading)
    public double calculateFare(double distance, double surgeMultiplier) {
        return distance * BASE_FARE_PER_KM * surgeMultiplier;
    }

    @Override
    public double calculateFare(double distance) {
        return distance * BASE_FARE_PER_KM;
    }
}

/**
 * Represents a Bike vehicle.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Bike extends Vehicle implements Serializable {
    private static final double BASE_FARE_PER_KM = 7;

    public Bike(String vehicleId, String model, String color, int seatCount) {
        super(vehicleId, model, color, seatCount);
    }

    @Override
    public double calculateFare(double distance) {
        return distance * BASE_FARE_PER_KM;
    }
}

/**
 * Represents a CNG vehicle.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class CNG extends Vehicle implements Serializable {
    private static final double BASE_FARE_PER_KM = 8;

    public CNG(String vehicleId, String model, String color, int seatCount) {
        super(vehicleId, model, color, seatCount);
    }

    @Override
    public double calculateFare(double distance) {
        return distance * BASE_FARE_PER_KM;
    }
}

// ----------- RIDE & SUPPORT -------------

enum RideStatus { PENDING, ONGOING, COMPLETED, CANCELLED, NEGOTIATING }

/**
 * Represents a single ride in the system.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Ride implements Serializable {
    private static int idCounter = 1000;
    private String rideId;
    private Passenger passenger;
    private Driver driver;
    private double distanceKm;
    private RideStatus status;
    private double fare;
    private Date scheduledTime;

    // Fare negotiation
    private Double negotiatedFare;
    private boolean isFareNegotiated;

    public Ride(Passenger passenger, Driver driver, double distanceKm, Date scheduledTime) {
        this.rideId = "RIDE" + (idCounter++);
        this.passenger = passenger;
        this.driver = driver;
        this.distanceKm = distanceKm;
        this.status = RideStatus.PENDING;
        this.scheduledTime = scheduledTime;
        this.fare = 0.0;
        this.negotiatedFare = null;
        this.isFareNegotiated = false;
    }

    // New static method to set the ID counter after deserialization
    public static void setNextIdCounter(int nextId) {
        idCounter = nextId;
    }

    // Getters and setters
    public String getRideId() { return rideId; }
    public Passenger getPassenger() { return passenger; }
    public Driver getDriver() { return driver; }
    public double getDistanceKm() { return distanceKm; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus st) { status = st; }
    public double getFare() { return fare; }
    public Date getScheduledTime() { return scheduledTime; }

    public Double getNegotiatedFare() { return negotiatedFare; }
    public boolean isFareNegotiated() { return isFareNegotiated; }

    // Negotiation methods
    public void proposeFare(double fare) {
        this.negotiatedFare = fare;
        this.isFareNegotiated = false;
        this.status = RideStatus.NEGOTIATING;
    }

    public void acceptFare() {
        if (negotiatedFare != null)
            this.isFareNegotiated = true;
    }

    public void rejectFare() {
        this.negotiatedFare = null;
        this.isFareNegotiated = false;
        this.status = RideStatus.PENDING;
    }

    // Ride lifecycle methods
    public void start() throws RideSharingException {
        if (status != RideStatus.PENDING && status != RideStatus.NEGOTIATING)
            throw new RideSharingException("Ride cannot start from status " + status);
        driver.getVehicle().startRide();
        status = RideStatus.ONGOING;
        System.out.println("Ride " + rideId + " started.");
    }

    public void end() throws RideSharingException {
        if (status != RideStatus.ONGOING)
            throw new RideSharingException("Ride cannot end from status " + status);
        driver.getVehicle().endRide();

        if (isFareNegotiated && negotiatedFare != null) {
            fare = negotiatedFare;
        } else {
            fare = driver.getVehicle().calculateFare(distanceKm);
        }

        driver.addEarnings(fare);
        passenger.addRideToHistory(this);
        driver.addRideToHistory(this);
        status = RideStatus.COMPLETED;
        System.out.println("Ride " + rideId + " ended. Fare: " + fare + " Taka");
    }

    public void cancel() throws RideSharingException {
        if (status == RideStatus.COMPLETED || status == RideStatus.CANCELLED)
            throw new RideSharingException("Ride already " + status);
        status = RideStatus.CANCELLED;
        driver.getVehicle().setAvailable(true);
        System.out.println("Ride " + rideId + " cancelled.");
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String fareStr = (status == RideStatus.COMPLETED) ? String.format(", Fare: %.2f Taka", fare) : "";
        String negotiStr = (negotiatedFare != null && !isFareNegotiated) ? String.format(", Proposed Fare: %.2f Taka (Pending)", negotiatedFare) : "";
        String negotiApprovedStr = (negotiatedFare != null && isFareNegotiated) ? String.format(", Negotiated Fare: %.2f Taka (Accepted)", negotiatedFare) : "";

        return "RideID: " + rideId + ", Passenger: " + passenger.getName() + ", Driver: " + driver.getName() +
                ", Distance: " + distanceKm + "km, Status: " + status + ", Scheduled: " + sdf.format(scheduledTime) +
                fareStr + negotiStr + negotiApprovedStr;
    }
}

/**
 * Represents a complaint.
 * This class is now Serializable to allow it to be part of the persisted state.
 */
class Complaint implements Serializable {
    private static int idCounter = 1;
    private int complaintId;
    private User reporter;
    private User reportedUser;
    private String details;
    private boolean resolved;

    public Complaint(User reporter, User reportedUser, String details) {
        this.complaintId = idCounter++;
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.details = details;
        this.resolved = false;
    }

    // New static method to set the ID counter after deserialization
    public static void setNextIdCounter(int nextId) {
        idCounter = nextId;
    }

    // Getters
    public int getComplaintId() { return complaintId; }
    public User getReporter() { return reporter; }
    public User getReportedUser() { return reportedUser; }
    public String getDetails() { return details; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    @Override
    public String toString() {
        return "Complaint #" + complaintId + " by " + reporter.getName() + " against " +
                reportedUser.getName() + ": " + details + " (Resolved: " + resolved + ")";
    }
}

// ------------- SYSTEM CENTRAL MANAGER -----------------

/**
 * Central class for managing the entire ride-sharing system.
 * This class is now Serializable and can be saved to a file.
 */
class RideSharingSystem implements Serializable {
    private Map<String, User> users = new HashMap<>();
    private Map<String, Ride> rides = new HashMap<>();
    private List<Complaint> complaints = new ArrayList<>();
    private Admin systemAdmin;

    // System config
    private double currentSurge = 1.0;

    public RideSharingSystem() {
        systemAdmin = new Admin("ADMIN001", "SuperAdmin", "000-0000", "admin@rideshare.com");
        users.put(systemAdmin.getUserId(), systemAdmin);
    }

    // Get user by ID
    public User getUser(String id) {
        return users.get(id);
    }

    // Register new user
    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        System.out.println(user.getRole() + " registered: " + user.getName());
    }

    // Add complaint
    public void addComplaint(Complaint complaint) {
        complaints.add(complaint);
        if (complaint.getReportedUser() instanceof Driver) {
            ((Driver) complaint.getReportedUser()).addComplaint(complaint);
        } else if (complaint.getReportedUser() instanceof Passenger) {
            ((Passenger) complaint.getReportedUser()).addComplaint(complaint);
        }
    }

    // Admin getter
    public Admin getAdmin() {
        return systemAdmin;
    }

    // Get all complaints
    public List<Complaint> getAllComplaints() {
        return complaints;
    }

    // Get rides
    public List<Ride> getAllRides() {
        return new ArrayList<>(rides.values());
    }

    // Get users
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // Book ride method
    public Ride bookRide(String passengerId, String driverId, double distance, Date scheduledTime) throws RideSharingException {
        User p = users.get(passengerId);
        User d = users.get(driverId);

        if (!(p instanceof Passenger))
            throw new RideSharingException("Invalid Passenger ID");
        if (!(d instanceof Driver))
            throw new RideSharingException("Invalid Driver ID");

        Driver driver = (Driver) d;
        if (!driver.isVerified())
            throw new RideSharingException("Driver is not verified");
        if (!driver.getVehicle().isAvailable())
            throw new RideSharingException("Driver's vehicle not available");

        Ride ride = new Ride((Passenger)p, driver, distance, scheduledTime);
        rides.put(ride.getRideId(), ride);
        System.out.println("Ride " + ride.getRideId() + " booked successfully");
        return ride;
    }

    // Get ride by ID
    public Ride getRide(String rideId) throws RideSharingException {
        Ride ride = rides.get(rideId);
        if(ride == null) throw new RideSharingException("Ride not found");
        return ride;
    }

    // Cancel ride
    public void cancelRide(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        ride.cancel();
    }

    // Start ride
    public void startRide(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        ride.start();
    }

    // End ride
    public void endRide(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        ride.end();
    }

    // Show admin dashboard
    public void showAdminDashboard() {
        System.out.println("=== Admin Dashboard ===");
        System.out.println("Total Users: " + users.size());
        long totalRides = rides.size();
        long ongoing = rides.values().stream().filter(r -> r.getStatus() == RideStatus.ONGOING).count();
        long completed = rides.values().stream().filter(r -> r.getStatus() == RideStatus.COMPLETED).count();
        long cancelled = rides.values().stream().filter(r -> r.getStatus() == RideStatus.CANCELLED).count();
        System.out.println("Rides: Total=" + totalRides + ", Ongoing=" + ongoing + ", Completed=" + completed + ", Cancelled=" + cancelled);
        double earnings = users.values().stream()
                .filter(u -> u instanceof Driver)
                .mapToDouble(u -> ((Driver) u).getTotalEarnings())
                .sum();
        System.out.println("Total Earnings (Driver's share): " + earnings + " Taka");
        System.out.println("Active Complaints: " + complaints.stream().filter(c -> !c.isResolved()).count());
        System.out.println("=======================");
    }

    // Show users by role
    public void showUsersByRole(String role) {
        System.out.println("== List of " + role + "s ==");
        users.values().stream()
                .filter(u -> u.getRole().equalsIgnoreCase(role))
                .forEach(u -> System.out.println(u));
    }

    // Show complaints
    public void showComplaints() {
        System.out.println("=== Complaints List ===");
        complaints.forEach(System.out::println);
    }

    // Resolve complaint by ID
    public void resolveComplaint(int complaintId) {
        for(Complaint comp : complaints) {
            if(comp.getComplaintId() == complaintId) {
                comp.setResolved(true);
                System.out.println("Complaint #" + complaintId + " marked as resolved.");
                return;
            }
        }
        System.out.println("Complaint ID not found.");
    }

    // Update surge pricing
    public void updateSurgePricing(double surge) {
        if (surge >= 1.0) {
            currentSurge = surge;
            System.out.println("Surge pricing updated to x" + surge);
        } else {
            System.out.println("Invalid surge multiplier. Must be >= 1.0");
        }
    }

    public double getCurrentSurge() {
        return currentSurge;
    }

    // ------------ Fare negotiation methods ------------

    // Passenger proposes fare offer (Taka)
    public void proposeFare(String rideId, double fare) throws RideSharingException {
        Ride ride = getRide(rideId);
        if (ride.getStatus() != RideStatus.PENDING && ride.getStatus() != RideStatus.NEGOTIATING)
            throw new RideSharingException("Fare can be negotiated only on pending or negotiating rides");

        ride.proposeFare(fare);
        System.out.println("Fare of " + fare + " Taka proposed for ride " + rideId);
    }

    // Driver accepts proposed fare
    public void acceptFare(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        if (ride.getStatus() != RideStatus.NEGOTIATING)
            throw new RideSharingException("Fare can be accepted only on negotiating rides");

        if (ride.getNegotiatedFare() == null)
            throw new RideSharingException("No fare proposal to accept");

        ride.acceptFare();
        // The ride status is changed back to PENDING so the driver can choose to start or cancel.
        ride.setStatus(RideStatus.PENDING);
        System.out.println("Negotiated fare accepted for ride " + rideId + ": " + ride.getNegotiatedFare() + " Taka");
    }

    // Driver rejects proposed fare
    public void rejectFare(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        if (ride.getStatus() != RideStatus.NEGOTIATING)
            throw new RideSharingException("Fare can be rejected only on negotiating rides");

        if (ride.getNegotiatedFare() == null)
            throw new RideSharingException("No fare proposal to reject");

        ride.rejectFare();
        System.out.println("Negotiated fare rejected for ride " + rideId);
    }

    // Method to get a list of pending rides for a specific passenger
    public List<Ride> getPendingRidesForPassenger(Passenger passenger) {
        List<Ride> pending = new ArrayList<>();
        for (Ride ride : rides.values()) {
            if (ride.getPassenger().equals(passenger) && (ride.getStatus() == RideStatus.PENDING || ride.getStatus() == RideStatus.NEGOTIATING)) {
                pending.add(ride);
            }
        }
        return pending;
    }

    // New methods for data persistence

    /**
     * Saves the entire RideSharingSystem object to a file using serialization.
     * @param filename The name of the file to save to.
     */
    public void saveDataToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("System state saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Loads the entire RideSharingSystem object from a file using deserialization.
     * @param filename The name of the file to load from.
     * @return The loaded RideSharingSystem object, or null if the file doesn't exist or an error occurs.
     */
    public static RideSharingSystem loadDataFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            RideSharingSystem loadedSystem = (RideSharingSystem) ois.readObject();
            System.out.println("System state loaded from " + filename);
            // After loading, we need to reset the ID counters to prevent duplicates
            // This is a simple but effective way to handle it for this app.
            int maxRideId = loadedSystem.getAllRides().stream()
                    .mapToInt(r -> Integer.parseInt(r.getRideId().substring(4)))
                    .max().orElse(1000);
            Ride.setNextIdCounter(maxRideId + 1);

            int maxComplaintId = loadedSystem.getAllComplaints().stream()
                    .mapToInt(Complaint::getComplaintId)
                    .max().orElse(1);
            Complaint.setNextIdCounter(maxComplaintId + 1);

            return loadedSystem;
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found. Starting a new system.");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return null;
        }
    }
}

// --------------- CONSOLE UI ------------------

class ConsoleUI {
    private Scanner scanner = new Scanner(System.in);
    private RideSharingSystem system;
    private static final String DATA_FILE = "rideshare_data.ser";

    public ConsoleUI(RideSharingSystem system) {
        this.system = system;
    }

    // Main application loop
    public void start() {
        System.out.println("Welcome to RideSharing Java Console App");

        // Load data on startup
        RideSharingSystem loadedSystem = RideSharingSystem.loadDataFromFile(DATA_FILE);
        if (loadedSystem != null) {
            this.system = loadedSystem;
        }

        while (true) {
            showMainMenu();
            int choice = readInt("Choose option");
            switch (choice) {
                case 1:
                    handleAdminLogin();
                    break;
                case 2:
                    handleDriverLogin();
                    break;
                case 3:
                    handlePassengerLogin();
                    break;
                case 4:
                    registerUserMenu();
                    break;
                case 0:
                    // Save data on exit
                    this.system.saveDataToFile(DATA_FILE);
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid input, try again.");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\nMain Menu");
        System.out.println("1) Admin Panel");
        System.out.println("2) Driver Panel");
        System.out.println("3) Passenger Panel");
        System.out.println("4) Register New User");
        System.out.println("0) Exit and Save");
    }

    private void handleAdminLogin() {
        System.out.print("Enter Admin ID: ");
        String id = scanner.nextLine().trim();
        User user = system.getUser(id);
        if (user != null && user instanceof Admin) {
            adminPanel((Admin)user);
        } else {
            System.out.println("Admin not found.");
        }
    }

    private void handleDriverLogin() {
        System.out.print("Enter Driver ID: ");
        String id = scanner.nextLine().trim();
        User user = system.getUser(id);
        if (user != null && user instanceof Driver) {
            driverPanel((Driver)user);
        } else {
            System.out.println("Driver not found.");
        }
    }

    private void handlePassengerLogin() {
        System.out.print("Enter Passenger ID: ");
        String id = scanner.nextLine().trim();
        User user = system.getUser(id);
        if (user != null && user instanceof Passenger) {
            passengerPanel((Passenger)user);
        } else {
            System.out.println("Passenger not found.");
        }
    }

    // User registration menu
    private void registerUserMenu() {
        System.out.println("User Registration");
        System.out.println("1) Passenger");
        System.out.println("2) Driver");
        int choice = readInt("Choose user type");
        String id = generateUserId(choice == 1 ? "P" : "D");

        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        if (choice == 1) {
            Passenger p = new Passenger(id, name, phone, email);
            system.registerUser(p);
            System.out.println("Passenger registered with id: " + id);
        } else {
            System.out.println("Vehicle Registration");
            System.out.print("Vehicle ID: ");
            String vId = scanner.nextLine();
            System.out.print("Model: ");
            String model = scanner.nextLine();
            System.out.print("Color: ");
            String color = scanner.nextLine();
            int seats = readInt("Seat count");

            Vehicle vehicle = chooseVehicleType(vId, model, color, seats);
            Driver d = new Driver(id, name, phone, email, vehicle);
            system.registerUser(d);
            System.out.println("Driver registered with id: " + id);
            System.out.println("Please upload license and vehicle documents to proceed for approval.");
        }
    }

    // Choose vehicle type helper
    private Vehicle chooseVehicleType(String vId, String model, String color, int seats) {
        System.out.println("Choose vehicle type:");
        System.out.println("1) Car");
        System.out.println("2) Bike");
        System.out.println("3) CNG");
        int choice = readInt("Your choice");

        switch (choice) {
            case 1: return new Car(vId, model, color, seats);
            case 2: return new Bike(vId, model, color, seats);
            case 3: return new CNG(vId, model, color, seats);
            default:
                System.out.println("Invalid choice. Defaulting to Car.");
                return new Car(vId, model, color, seats);
        }
    }

    // Generate user ID helper
    private String generateUserId(String prefix) {
        Random r = new Random();
        // A better way to ensure uniqueness, though the current random approach is ok for this console app.
        return prefix + (1000 + r.nextInt(9000));
    }

    // Admin panel
    private void adminPanel(Admin admin) {
        System.out.println("Welcome, Admin " + admin.getName());
        while(true) {
            System.out.println("\nAdmin Panel Menu");
            System.out.println("1) View Dashboard");
            System.out.println("2) Approve/Reject Drivers");
            System.out.println("3) View Users");
            System.out.println("4) View Complaints");
            System.out.println("5) Resolve Complaint");
            System.out.println("6) Configure Surge Pricing");
            System.out.println("0) Logout");

            int choice = readInt("Enter option");
            try {
                switch(choice) {
                    case 1:
                        system.showAdminDashboard();
                        break;
                    case 2:
                        manageDriverApprovals();
                        break;
                    case 3:
                        viewUsersMenu();
                        break;
                    case 4:
                        system.showComplaints();
                        break;
                    case 5:
                        int compId = readInt("Enter Complaint ID to resolve");
                        system.resolveComplaint(compId);
                        break;
                    case 6:
                        double surge = readDouble("Enter new surge multiplier (>=1)");
                        system.updateSurgePricing(surge);
                        break;
                    case 0:
                        System.out.println("Logging out admin...");
                        return;
                    default:
                        System.out.println("Invalid input. Try again.");
                }
            } catch (RideSharingException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Manage driver approvals
    private void manageDriverApprovals() throws RideSharingException {
        List<User> drivers = new ArrayList<>();
        for(User u : system.getAllUsers()) {
            if(u instanceof Driver) {
                drivers.add(u);
            }
        }
        List<Driver> unapproved = new ArrayList<>();
        for(User u : drivers) {
            Driver d = (Driver) u;
            if(!d.isVerified()) unapproved.add(d);
        }
        if(unapproved.isEmpty()) {
            System.out.println("No unapproved drivers at the moment.");
            return;
        }
        for(Driver d : unapproved) {
            System.out.println(d);
            System.out.println("Uploaded Docs? " + (d.hasUploadedDocuments() ? "Yes" : "No"));
            System.out.println("1) Approve  2) Reject  0) Skip");
            int choice = readInt("Your choice");

            if(choice == 1) {
                system.getAdmin().approveDriver(d);
            } else if(choice == 2) {
                system.getAdmin().rejectDriver(d);
            } else {
                System.out.println("Skipped driver " + d.getName());
            }
        }
    }

    // View users menu
    private void viewUsersMenu() {
        System.out.println("View Users by Role");
        System.out.println("1) Passengers");
        System.out.println("2) Drivers");
        System.out.println("3) All Users");
        System.out.println("0) Back");

        int choice = readInt("Your choice");
        switch(choice) {
            case 1:
                system.showUsersByRole("Passenger");
                break;
            case 2:
                system.showUsersByRole("Driver");
                break;
            case 3:
                system.getAllUsers().forEach(System.out::println);
                break;
            case 0:
            default:
                break;
        }
    }

    // Driver panel
    private void driverPanel(Driver driver) {
        System.out.println("Welcome, Driver " + driver.getName());
        while(true) {
            System.out.println("\nDriver Panel");
            System.out.println("1) Upload License");
            System.out.println("2) Upload Vehicle Documents");
            System.out.println("3) View Vehicle Info");
            System.out.println("4) View Ride History");
            System.out.println("5) View Earnings");
            System.out.println("6) Accept Ride Request");
            System.out.println("7) View Complaints");
            System.out.println("0) Logout");

            int choice = readInt("Choose option");
            switch(choice) {
                case 1:
                    driver.uploadLicense();
                    System.out.println("License uploaded.");
                    break;
                case 2:
                    driver.uploadVehicleDocuments();
                    System.out.println("Vehicle documents uploaded.");
                    break;
                case 3:
                    System.out.println(driver.getVehicle());
                    System.out.println("Verified: " + driver.isVerified());
                    break;
                case 4:
                    List<Ride> rides = driver.getRideHistory();
                    if(rides.isEmpty()) System.out.println("No rides yet.");
                    else rides.forEach(System.out::println);
                    break;
                case 5:
                    System.out.println("Total Earnings: " + driver.getTotalEarnings() + " Taka");
                    System.out.println("Commission Rate: " + driver.getCommissionRate());
                    break;
                case 6:
                    acceptRideRequest(driver);
                    break;
                case 7:
                    List<Complaint> complaints = driver.getComplaints();
                    if(complaints.isEmpty()) System.out.println("No complaints.");
                    else complaints.forEach(System.out::println);
                    break;
                case 0:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    // Accept ride request with fare negotiation options
    private void acceptRideRequest(Driver driver) {
        System.out.println("--- Pending rides for this driver ---");
        List<Ride> pendingRides = new ArrayList<>();

        for (Ride r : system.getAllRides()) {
            // Check for both PENDING and NEGOTIATING rides for this driver
            if (r.getDriver().getUserId().equals(driver.getUserId()) && (r.getStatus() == RideStatus.PENDING || r.getStatus() == RideStatus.NEGOTIATING)) {
                pendingRides.add(r);
            }
        }

        if (pendingRides.isEmpty()) {
            System.out.println("No pending ride requests.");
            return;
        }

        pendingRides.forEach(System.out::println);

        System.out.print("Enter RideID to respond to: ");
        String rideId = scanner.nextLine().trim();

        try {
            Ride r = system.getRide(rideId);
            // Re-check status to handle concurrent changes (though not an issue in this single-threaded app)
            if (r.getDriver() != driver || (r.getStatus() != RideStatus.PENDING && r.getStatus() != RideStatus.NEGOTIATING)) {
                System.out.println("Invalid ride ID or ride not pending/negotiating for you.");
                return;
            }

            if (r.getNegotiatedFare() != null && !r.isFareNegotiated()) {
                System.out.println("Fare negotiation pending. Passenger's proposed fare: " + r.getNegotiatedFare() + " Taka");

                System.out.println("1) Accept fare");
                System.out.println("2) Reject fare");
                System.out.println("3) Propose counter fare");
                System.out.println("4) Cancel ride");

                int choice = readInt("Your choice");

                switch(choice) {
                    case 1:
                        system.acceptFare(rideId);
                        System.out.println("Fare accepted. Ride status is now pending start.");
                        break;
                    case 2:
                        system.rejectFare(rideId);
                        System.out.println("Fare rejected. Ride is now pending without a proposed fare.");
                        break;
                    case 3:
                        double counterFare = readDouble("Enter your counter fare in Taka");
                        system.proposeFare(rideId, counterFare);
                        System.out.println("Counter fare proposed. Waiting for passenger's response.");
                        break;
                    case 4:
                        system.cancelRide(rideId);
                        System.out.println("Ride cancelled.");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else { // No negotiation is active, regular start/cancel options
                System.out.println("No fare negotiation active.");
                System.out.println("1) Start ride");
                System.out.println("2) Cancel ride");
                int choice = readInt("Your choice");

                if (choice == 1) {
                    system.startRide(rideId);
                    System.out.println("Ride started.");
                } else if (choice == 2) {
                    system.cancelRide(rideId);
                    System.out.println("Ride cancelled.");
                } else {
                    System.out.println("Invalid choice.");
                }
            }
        } catch (RideSharingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Passenger panel
    private void passengerPanel(Passenger passenger) {
        System.out.println("Welcome, Passenger " + passenger.getName());
        while (true) {
            System.out.println("\nPassenger Panel");
            System.out.println("1) Book a Ride");
            System.out.println("2) View Ride History");
            System.out.println("3) View Pending Rides & Negotiations");
            System.out.println("4) Submit Complaint");
            System.out.println("0) Logout");

            int choice = readInt("Choose option");
            switch (choice) {
                case 1:
                    bookRideFlow(passenger);
                    break;
                case 2:
                    List<Ride> rides = passenger.getRideHistory();
                    if(rides.isEmpty()) System.out.println("No rides yet.");
                    else rides.forEach(System.out::println);
                    break;
                case 3:
                    handlePendingRidesForPassenger(passenger);
                    break;
                case 4:
                    submitComplaint(passenger);
                    break;
                case 0:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    // New method to handle passenger's pending rides and negotiations
    private void handlePendingRidesForPassenger(Passenger passenger) {
        System.out.println("--- Your Pending Rides & Negotiations ---");
        List<Ride> pending = system.getPendingRidesForPassenger(passenger);

        if (pending.isEmpty()) {
            System.out.println("No pending rides or negotiations at the moment.");
            return;
        }

        pending.forEach(System.out::println);

        System.out.print("Enter RideID to view details or respond: ");
        String rideId = scanner.nextLine().trim();

        try {
            Ride r = system.getRide(rideId);
            if (r.getPassenger() != passenger || (r.getStatus() != RideStatus.PENDING && r.getStatus() != RideStatus.NEGOTIATING)) {
                System.out.println("Invalid ride ID.");
                return;
            }

            // If the driver has made a counter-offer
            if (r.getStatus() == RideStatus.NEGOTIATING) {
                System.out.println("Driver has proposed a counter-fare: " + r.getNegotiatedFare() + " Taka");
                System.out.println("1) Accept counter-fare");
                System.out.println("2) Reject counter-fare");
                System.out.println("3) Cancel ride");

                int choice = readInt("Your choice");

                switch (choice) {
                    case 1:
                        // Accept the driver's counter-offer
                        system.acceptFare(rideId);
                        System.out.println("Counter-fare accepted. The ride is now confirmed and pending start.");
                        break;
                    case 2:
                        // Reject the counter-offer, which reverts to a standard pending ride
                        system.rejectFare(rideId);
                        System.out.println("Counter-fare rejected. The ride is now pending without a fare proposal.");
                        break;
                    case 3:
                        system.cancelRide(rideId);
                        System.out.println("Ride cancelled.");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            } else {
                // If the ride is just pending, offer to cancel or wait
                System.out.println("This ride is pending driver acceptance. What would you like to do?");
                System.out.println("1) Cancel ride");
                System.out.println("0) Back to menu");

                int choice = readInt("Your choice");
                if (choice == 1) {
                    system.cancelRide(rideId);
                    System.out.println("Ride cancelled.");
                }
            }
        } catch (RideSharingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Book ride flow with optional fare proposal
    private void bookRideFlow(Passenger passenger) {
        System.out.println("--- Available Drivers ---");
        system.showUsersByRole("Driver");

        System.out.print("Enter Driver ID to book with: ");
        String driverId = scanner.nextLine().trim();

        if (system.getUser(driverId) == null || !(system.getUser(driverId) instanceof Driver)) {
            System.out.println("Invalid driver ID.");
            return;
        }

        double dist = readDouble("Enter distance in km");
        Date scheduledDate = new Date(); // current time

        try {
            Ride ride = system.bookRide(passenger.getUserId(), driverId, dist, scheduledDate);
            System.out.println("Ride booked with ID: " + ride.getRideId());

            System.out.print("Do you want to propose a fare in Taka for negotiation? (y/n): ");
            String yn = scanner.nextLine().trim().toLowerCase();

            if (yn.equals("y")) {
                double fareProposal = readDouble("Enter fare amount in Taka to propose");
                system.proposeFare(ride.getRideId(), fareProposal);
                System.out.println("Fare proposed successfully. Waiting for driver's response. Check 'View Pending Rides' for updates.");
            } else {
                System.out.println("Standard fare applied. Waiting for driver to accept.");
            }
        } catch (RideSharingException e) {
            System.out.println("Failed to book ride: " + e.getMessage());
        }
    }

    // Submit complaint
    private void submitComplaint(Passenger passenger) {
        System.out.print("Enter user ID to complain about (Driver or Passenger): ");
        String reportedId = scanner.nextLine().trim();

        User reported = system.getUser(reportedId);
        if(reported == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter complaint details: ");
        String details = scanner.nextLine();

        Complaint c = new Complaint(passenger, reported, details);
        system.addComplaint(c);

        System.out.println("Complaint submitted successfully.");
    }

    // Helper method to read int input
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    // Helper method to read double input
    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
}

// -------------- MAIN ---------------
public class RideSharingApp {
    public static void main(String[] args) {
        RideSharingSystem system = new RideSharingSystem();

        // The system will try to load data from the file system.
        // If the file doesn't exist, a new system is created.

        ConsoleUI ui = new ConsoleUI(system);
        ui.start();
    }
}
