Rafi, [17-Jul-25 8:19 PM]
import java.util.*;
import java.text.SimpleDateFormat;

// ------------- EXCEPTIONS -----------------

class RideSharingException extends Exception {
    public RideSharingException(String message) {
        super(message);
    }
}

// ------------- INTERFACES & ABSTRACT CLASSES --------------

interface VehicleActions {
    void startRide() throws RideSharingException;
    void endRide();
    double calculateFare(double distance);
}

abstract class User {
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

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

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

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " [ID=" + userId + ", Name=" + name + ", Phone=" + phone + ", Email=" + email + "]";
    }
}

// ------------- USER CLASSES ----------------

class Passenger extends User {
    private List<Ride> rideHistory = new ArrayList<>();
    private List<Complaint> complaints = new ArrayList<>();

    public Passenger(String userId, String name, String phone, String email) {
        super(userId, name, phone, email);
    }

    public List<Ride> getRideHistory() { return rideHistory; }
    public void addRideToHistory(Ride ride) { rideHistory.add(ride); }

    public void addComplaint(Complaint complaint) {
        complaints.add(complaint);
    }
    public List<Complaint> getComplaints() {
        return complaints;
    }

    @Override
    public String getRole() {
        return "Passenger";
    }
}

class Driver extends User {
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

    public Vehicle getVehicle() { return vehicle; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public void uploadLicense() { licenseUploaded = true; }
    public void uploadVehicleDocuments() { vehicleDocumentsUploaded = true; }
    public boolean hasUploadedDocuments() {
        return licenseUploaded && vehicleDocumentsUploaded;
    }

    public List<Ride> getRideHistory() { return rideHistory; }
    public void addRideToHistory(Ride ride) { rideHistory.add(ride); }

    public double getTotalEarnings() { return totalEarnings; }
    public void addEarnings(double fare) {
        double commission = fare * commissionRate;
        totalEarnings += (fare - commission);
    }

    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double rate) {
        if (rate >= 0 && rate <= 1)
            commissionRate = rate;
    }

    public void addComplaint(Complaint complaint) {
        complaints.add(complaint);
    }
    public List<Complaint> getComplaints() {
        return complaints;
    }

Rafi, [17-Jul-25 8:19 PM]


    @Override
    public String getRole() {
        return "Driver";
    }

    @Override
    public String toString() {
        return super.toString() + ", Vehicle=" + vehicle + ", Verified=" + isVerified;
    }
}

class Admin extends User {
    public Admin(String userId, String name, String phone, String email) {
        super(userId, name, phone, email);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    public void approveDriver(Driver driver) throws RideSharingException {
        if (!driver.hasUploadedDocuments())
            throw new RideSharingException("Driver must upload all documents.");
        driver.setVerified(true);
        System.out.println("Driver " + driver.getName() + " approved.");
    }
    public void rejectDriver(Driver driver) {
        driver.setVerified(false);
        System.out.println("Driver " + driver.getName() + " rejected.");
    }
}

// ------------- VEHICLES -------------------

abstract class Vehicle implements VehicleActions {
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

    public String getVehicleId() { return vehicleId; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public int getSeatCount() { return seatCount; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    // Abstract method fare calculation
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

class Car extends Vehicle {
    private static final double BASE_FARE_PER_KM = 12;

    public Car(String vehicleId, String model, String color, int seatCount) {
        super(vehicleId, model, color, seatCount);
    }

    // Method overloading for surge pricing
    public double calculateFare(double distance, double surgeMultiplier) {
        return distance * BASE_FARE_PER_KM * surgeMultiplier;
    }

    @Override
    public double calculateFare(double distance) {
        return distance * BASE_FARE_PER_KM;
    }
}

class Bike extends Vehicle {
    private static final double BASE_FARE_PER_KM = 7;

    public Bike(String vehicleId, String model, String color, int seatCount) {
        super(vehicleId, model, color, seatCount);
    }

    @Override
    public double calculateFare(double distance) {
        return distance * BASE_FARE_PER_KM;
    }
}

class CNG extends Vehicle {
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

enum RideStatus { PENDING, ONGOING, COMPLETED, CANCELLED }

class Ride {
    private static int idCounter = 1000;
    private String rideId;
    private Passenger passenger;
    private Driver driver;
    private double distanceKm;
    private RideStatus status;
    private double fare;
    private Date scheduledTime;

Rafi, [17-Jul-25 8:19 PM]


    public Ride(Passenger passenger, Driver driver, double distanceKm, Date scheduledTime) {
        this.rideId = "RIDE" + (idCounter++);
        this.passenger = passenger;
        this.driver = driver;
        this.distanceKm = distanceKm;
        this.status = RideStatus.PENDING;
        this.scheduledTime = scheduledTime;
        this.fare = 0.0;
    }

    public String getRideId() { return rideId; }
    public Passenger getPassenger() { return passenger; }
    public Driver getDriver() { return driver; }
    public double getDistanceKm() { return distanceKm; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus st) { status = st; }
    public double getFare() { return fare; }
    public Date getScheduledTime() { return scheduledTime; }

    public void start() throws RideSharingException {
        if (status != RideStatus.PENDING)
            throw new RideSharingException("Ride cannot start from status " + status);
        driver.getVehicle().startRide();
        status = RideStatus.ONGOING;
        System.out.println("Ride " + rideId + " started.");
    }

    public void end() throws RideSharingException {
        if (status != RideStatus.ONGOING)
            throw new RideSharingException("Ride cannot end from status " + status);
        driver.getVehicle().endRide();
        fare = driver.getVehicle().calculateFare(distanceKm);
        driver.addEarnings(fare);
        passenger.addRideToHistory(this);
        driver.addRideToHistory(this);
        status = RideStatus.COMPLETED;
        System.out.println("Ride " + rideId + " ended. Fare: " + fare);
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
        return "RideID: " + rideId + ", Passenger: " + passenger.getName() + ", Driver: " + driver.getName() +
                ", Distance: " + distanceKm + "km, Status: " + status + ", Scheduled: " + sdf.format(scheduledTime);
    }
}

class Complaint {
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

class RideSharingSystem {
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

    public User getUser(String id) {
        return users.get(id);
    }

Rafi, [17-Jul-25 8:19 PM]


    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        System.out.println(user.getRole() + " registered: " + user.getName());
    }

    public void addComplaint(Complaint complaint) {
        complaints.add(complaint);
        if (complaint.getReportedUser() instanceof Driver) {
            ((Driver) complaint.getReportedUser()).addComplaint(complaint);
        } else if (complaint.getReportedUser() instanceof Passenger) {
            ((Passenger) complaint.getReportedUser()).addComplaint(complaint);
        }
    }

    public Admin getAdmin() {
        return systemAdmin;
    }

    public List<Complaint> getAllComplaints() {
        return complaints;
    }

    public List<Ride> getAllRides() {
        return new ArrayList<>(rides.values());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Ride bookRide(String passengerId, String driverId, double distance, Date scheduledTime) throws RideSharingException {
        User p = users.get(passengerId);
        User d = users.get(driverId);

        if (!(p instanceof Passenger))
            throw new RideSharingException("Invalid Passenger ID");
        if (!(d instanceof Driver))
            throw new RideSharingException("Invalid Driver ID");

        Driver driver = (Driver) d;
        if (!driver.isVerified()) throw new RideSharingException("Driver is not verified");
        if (!driver.getVehicle().isAvailable()) throw new RideSharingException("Driver's vehicle not available");

        Ride ride = new Ride((Passenger)p, driver, distance, scheduledTime);
        rides.put(ride.getRideId(), ride);
        System.out.println("Ride " + ride.getRideId() + " booked successfully");
        return ride;
    }

    public Ride getRide(String rideId) throws RideSharingException {
        Ride ride = rides.get(rideId);
        if(ride == null) throw new RideSharingException("Ride not found");
        return ride;
    }

    public void cancelRide(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        ride.cancel();
    }

    public void startRide(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        ride.start();
    }

    public void endRide(String rideId) throws RideSharingException {
        Ride ride = getRide(rideId);
        ride.end();
    }

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
        System.out.println("Total Earnings (Driver's share): " + earnings);
        System.out.println("Active Complaints: " + complaints.stream().filter(c -> !c.isResolved()).count());
        System.out.println("=======================");
    }

    public void showUsersByRole(String role) {
        System.out.println("== List of " + role + "s ==");
        users.values().stream()
                .filter(u -> u.getRole().equalsIgnoreCase(role))
                .forEach(u -> System.out.println(u));
    }

    public void showComplaints() {
        System.out.println("=== Complaints List ===");
        complaints.forEach(System.out::println);
    }

Rafi, [17-Jul-25 8:19 PM]


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
}

// --------------- CONSOLE UI ------------------

class ConsoleUI {
    private Scanner scanner = new Scanner(System.in);
    private RideSharingSystem system;

    public ConsoleUI(RideSharingSystem system) {
        this.system = system;
    }

    public void start() {
        System.out.println("Welcome to RideSharing Java Console App");
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
        System.out.println("0) Exit");
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

Rafi, [17-Jul-25 8:19 PM]


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

    private String generateUserId(String prefix) {
        Random r = new Random();
        return prefix + (1000 + r.nextInt(9000));
    }

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

Rafi, [17-Jul-25 8:19 PM]


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

Rafi, [17-Jul-25 8:19 PM]


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
                    System.out.println("Total Earnings: " + driver.getTotalEarnings());
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

    private void acceptRideRequest(Driver driver) {
        System.out.println("--- Pending rides for this driver ---");
        List<Ride> pendingRides = new ArrayList<>();
        for (Ride r : system.getAllRides()) {
            if (r.getDriver().getUserId().equals(driver.getUserId()) && r.getStatus() == RideStatus.PENDING) {
                pendingRides.add(r);
            }
        }
        if (pendingRides.isEmpty()) {
            System.out.println("No pending ride requests.");
            return;
        }
        pendingRides.forEach(System.out::println);
        System.out.print("Enter RideID to accept or cancel: ");
        String rideId = scanner.nextLine().trim();
        try {
            Ride r = system.getRide(rideId);
            if (r.getStatus() != RideStatus.PENDING) {
                System.out.println("Ride status not pending.");
                return;
            }
            System.out.println("Accept(1) or Cancel(2) ride?");
            int choice = readInt("Choice");
            if (choice == 1) {
                system.startRide(rideId);
                System.out.println("Ride started.");
            } else if (choice == 2) {
                system.cancelRide(rideId);
                System.out.println("Ride cancelled.");
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (RideSharingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void passengerPanel(Passenger passenger) {
        System.out.println("Welcome, Passenger " + passenger.getName());
        while (true) {
            System.out.println("\nPassenger Panel");
            System.out.println("1) Book a Ride");
            System.out.println("2) View Ride History");
            System.out.println("3) Submit Complaint");
            System.out.println("0) Logout");

Rafi, [17-Jul-25 8:19 PM]


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
        Date scheduledDate = new Date(); // current date/time
        try {
            Ride ride = system.bookRide(passenger.getUserId(), driverId, dist, scheduledDate);
            System.out.println("Ride booked with ID: " + ride.getRideId());
        } catch (RideSharingException e) {
            System.out.println("Failed to book ride: " + e.getMessage());
        }
    }

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

    // Helper read int
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                int val = Integer.parseInt(scanner.nextLine());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    // Helper read double
    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                double val = Double.parseDouble(scanner.nextLine());
                return val;
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

        // Pre-register admin
        Admin admin = system.getAdmin();

        ConsoleUI ui = new ConsoleUI(system);
        ui.start();
    }
}
