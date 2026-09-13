
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;



abstract class Vehicle {

    protected String registrationNumber;
    protected String make;
    protected double mileage;

    public Vehicle(String registrationNumber, String make, double mileage) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.mileage = mileage;
    }

    public abstract double calculateOperatingCost();

    public abstract void displayDetails();

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public double getMileage() {
        return mileage;
    }
}

// ---------------- Part B: Interface ----------------
interface Maintainable {

    void performMaintenance();

    boolean requiresMaintenance();
}

// ---------------- Part C: Car ----------------
class Car extends Vehicle implements Maintainable {

    private int numberOfPassengers;
    private double fuelConsumption; // litres per 100 km

    private static final double FUEL_PRICE_PER_LITRE = 25.0;
    private static final double MAINTENANCE_THRESHOLD = 10000;

    public Car(String registrationNumber,
               String make,
               double mileage,
               int numberOfPassengers,
               double fuelConsumption) {

        super(registrationNumber, make, mileage);

        this.numberOfPassengers = numberOfPassengers;
        this.fuelConsumption = fuelConsumption;
    }

    @Override
    public double calculateOperatingCost() {
        double fuelUsed = (mileage / 100) * fuelConsumption;
        return fuelUsed * FUEL_PRICE_PER_LITRE;
    }

    @Override
    public boolean requiresMaintenance() {
        return mileage >= MAINTENANCE_THRESHOLD;
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Car " + registrationNumber
                + " (" + make + "). Mileage reset checks complete.");
    }

    @Override
    public void displayDetails() {
        System.out.println("---- Car Details ----");
        System.out.println("Registration Number : " + registrationNumber);
        System.out.println("Make                : " + make);
        System.out.println("Mileage             : " + mileage + " km");
        System.out.println("Passengers          : " + numberOfPassengers);
        System.out.println("Fuel Consumption    : " + fuelConsumption + " L/100km");
    }
}

// ---------------- Part D: Bus ----------------
class Bus extends Vehicle implements Maintainable {

    private int passengerCapacity;
    private double fuelConsumption;

    private static final double FUEL_PRICE_PER_LITRE = 25.0;
    private static final double MAINTENANCE_THRESHOLD = 15000;

    public Bus(String registrationNumber,
               String make,
               double mileage,
               int passengerCapacity,
               double fuelConsumption) {

        super(registrationNumber, make, mileage);

        this.passengerCapacity = passengerCapacity;
        this.fuelConsumption = fuelConsumption;
    }

    @Override
    public double calculateOperatingCost() {
        double fuelUsed = (mileage / 100) * fuelConsumption;
        return fuelUsed * FUEL_PRICE_PER_LITRE;
    }

    @Override
    public boolean requiresMaintenance() {
        return mileage >= MAINTENANCE_THRESHOLD;
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Bus " + registrationNumber
                + " (" + make + "). Brakes and tyres inspected.");
    }

    @Override
    public void displayDetails() {
        System.out.println("---- Bus Details ----");
        System.out.println("Registration Number : " + registrationNumber);
        System.out.println("Make                : " + make);
        System.out.println("Mileage             : " + mileage + " km");
        System.out.println("Passenger Capacity  : " + passengerCapacity);
        System.out.println("Fuel Consumption    : " + fuelConsumption + " L/100km");
    }
}

// ---------------- Part E: Truck ----------------
class Truck extends Vehicle implements Maintainable {

    private double loadCapacity; // tonnes
    private double fuelConsumption;

    private static final double FUEL_PRICE_PER_LITRE = 25.0;
    private static final double SURCHARGE_PER_TONNE = 500.0;
    private static final double MAINTENANCE_THRESHOLD = 20000;

    public Truck(String registrationNumber,
                 String make,
                 double mileage,
                 double loadCapacity,
                 double fuelConsumption) {

        super(registrationNumber, make, mileage);

        this.loadCapacity = loadCapacity;
        this.fuelConsumption = fuelConsumption;
    }

    @Override
    public double calculateOperatingCost() {
        double fuelUsed = (mileage / 100) * fuelConsumption;
        double fuelCost = fuelUsed * FUEL_PRICE_PER_LITRE;
        double loadSurcharge = loadCapacity * SURCHARGE_PER_TONNE;
        return fuelCost + loadSurcharge;
    }

    @Override
    public boolean requiresMaintenance() {
        return mileage >= MAINTENANCE_THRESHOLD;
    }

    @Override
    public void performMaintenance() {
        System.out.println("Performing maintenance on Truck " + registrationNumber
                + " (" + make + "). Load-bearing components checked.");
    }

    @Override
    public void displayDetails() {
        System.out.println("---- Truck Details ----");
        System.out.println("Registration Number : " + registrationNumber);
        System.out.println("Make                : " + make);
        System.out.println("Mileage             : " + mileage + " km");
        System.out.println("Load Capacity       : " + loadCapacity + " tonnes");
        System.out.println("Fuel Consumption    : " + fuelConsumption + " L/100km");
    }
}

// ---------------- Part F: Custom exception ----------------
class InvalidVehicleException extends Exception {

    public InvalidVehicleException(String message) {
        super(message);
    }
}

// ---------------- Main application ----------------
public class SmartTransportSystem {

    private static Scanner scanner = new Scanner(System.in);

    // Part G: holds every vehicle, regardless of type (dynamic binding)
    private static ArrayList<Vehicle> vehicles = new ArrayList<>();

    public static void main(String[] args) {

        int choice = -1;

        while (choice != 10) {

            printMenu();
            choice = readMenuOption();

            try {
                switch (choice) {
                    case 1:
                        addCar();
                        break;
                    case 2:
                        addBus();
                        break;
                    case 3:
                        addTruck();
                        break;
                    case 4:
                        displayAllVehicles();
                        break;
                    case 5:
                        displayOperatingCosts();
                        break;
                    case 6:
                        displayVehiclesRequiringMaintenance();
                        break;
                    case 7:
                        performMaintenanceMenu();
                        break;
                    case 8:
                        searchVehicle();
                        break;
                    case 9:
                        removeVehicle();
                        break;
                    case 10:
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("ERROR: Please choose a valid option (1-10).");
                }
            } catch (Exception e) {
                // Safety net so an unexpected error never terminates the program
                System.out.println("ERROR: An unexpected problem occurred: " + e.getMessage());
            } finally {
                System.out.println();
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("      SMART TRANSPORT SYSTEM");
        System.out.println();
        System.out.println("1. Add Car");
        System.out.println("2. Add Bus");
        System.out.println("3. Add Truck");
        System.out.println("4. Display All Vehicles");
        System.out.println("5. Display Operating Costs");
        System.out.println("6. Display Vehicles Requiring Maintenance");
        System.out.println("7. Perform Maintenance");
        System.out.println("8. Search Vehicle");
        System.out.println("9. Remove Vehicle");
        System.out.println("10. Exit");
        System.out.print("Enter option: ");
    }

    // ---------- Input helpers (Part J: robust exception handling) ----------

    private static int readMenuOption() {
        try {
            String line = scanner.nextLine();
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter a valid number.");
            return -1;
        }
    }

    private static String readNonEmptyString(String prompt) throws InvalidVehicleException {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            throw new InvalidVehicleException("Registration number cannot be empty.");
        }
        return value;
    }

    private static double readNonNegativeDouble(String prompt, String fieldName) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value < 0) {
                    System.out.println("ERROR: " + fieldName + " cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid number.");
            }
        }
    }

    private static double readPositiveDouble(String prompt, String fieldName) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.println("ERROR: " + fieldName + " must be greater than zero.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid number.");
            }
        }
    }

    private static int readNonNegativeInt(String prompt, String fieldName) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value < 0) {
                    System.out.println("ERROR: " + fieldName + " cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid number.");
            }
        }
    }

    // ---------- Menu actions ----------

    private static void addCar() {
        try {
            String reg = readNonEmptyString("Enter registration number: ");
            System.out.print("Enter make: ");
            String make = scanner.nextLine().trim();
            double mileage = readNonNegativeDouble("Enter mileage: ", "Mileage");
            int passengers = readNonNegativeInt("Enter number of passengers: ", "Passenger count");
            double fuelConsumption = readPositiveDouble("Enter fuel consumption (L/100km): ", "Fuel consumption");

            Car car = new Car(reg, make, mileage, passengers, fuelConsumption);
            vehicles.add(car);
            System.out.println("Car added successfully.");

        } catch (InvalidVehicleException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void addBus() {
        try {
            String reg = readNonEmptyString("Enter registration number: ");
            System.out.print("Enter make: ");
            String make = scanner.nextLine().trim();
            double mileage = readNonNegativeDouble("Enter mileage: ", "Mileage");
            int capacity = readNonNegativeInt("Enter passenger capacity: ", "Passenger capacity");
            double fuelConsumption = readPositiveDouble("Enter fuel consumption (L/100km): ", "Fuel consumption");

            Bus bus = new Bus(reg, make, mileage, capacity, fuelConsumption);
            vehicles.add(bus);
            System.out.println("Bus added successfully.");

        } catch (InvalidVehicleException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void addTruck() {
        try {
            String reg = readNonEmptyString("Enter registration number: ");
            System.out.print("Enter make: ");
            String make = scanner.nextLine().trim();
            double mileage = readNonNegativeDouble("Enter mileage: ", "Mileage");
            double loadCapacity = readNonNegativeDouble("Enter load capacity (tonnes): ", "Load capacity");
            double fuelConsumption = readPositiveDouble("Enter fuel consumption (L/100km): ", "Fuel consumption");

            Truck truck = new Truck(reg, make, mileage, loadCapacity, fuelConsumption);
            vehicles.add(truck);
            System.out.println("Truck added successfully.");

        } catch (InvalidVehicleException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // Part G: dynamic binding - each vehicle's own displayDetails() runs
    private static void displayAllVehicles() {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles have been added yet.");
            return;
        }

        for (Vehicle vehicle : vehicles) {
            vehicle.displayDetails();
            System.out.println("Operating Cost: R" + String.format("%.2f", vehicle.calculateOperatingCost()));
            System.out.println();
        }
    }

    private static void displayOperatingCosts() {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles have been added yet.");
            return;
        }

        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle.getRegistrationNumber() + " -> R"
                    + String.format("%.2f", vehicle.calculateOperatingCost()));
        }
    }

    // Part H: only vehicles that implement Maintainable can go in this list.
    // Every Car, Bus and Truck implements Maintainable, so all current
    // vehicle types qualify; a future vehicle type that does NOT implement
    // Maintainable simply could not be added to this list (compile error).
    private static ArrayList<Maintainable> getMaintainableVehicles() {
        ArrayList<Maintainable> maintainableVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle instanceof Maintainable) {
                maintainableVehicles.add((Maintainable) vehicle);
            }
        }
        return maintainableVehicles;
    }

    private static void displayVehiclesRequiringMaintenance() {
        ArrayList<Maintainable> maintainableVehicles = getMaintainableVehicles();
        boolean found = false;

        for (int i = 0; i < maintainableVehicles.size(); i++) {
            Maintainable m = maintainableVehicles.get(i);
            if (m.requiresMaintenance()) {
                found = true;
                Vehicle v = (Vehicle) m;
                System.out.println(v.getRegistrationNumber() + " (" + v.getClass().getSimpleName()
                        + ") requires maintenance. Mileage: " + v.getMileage());
            }
        }

        if (!found) {
            System.out.println("No vehicles currently require maintenance.");
        }
    }

    private static void performMaintenanceMenu() {
        ArrayList<Maintainable> maintainableVehicles = getMaintainableVehicles();
        boolean performedAny = false;

        for (Maintainable vehicle : maintainableVehicles) {
            if (vehicle.requiresMaintenance()) {
                vehicle.performMaintenance();
                performedAny = true;
            }
        }

        if (!performedAny) {
            System.out.println("No vehicles required maintenance.");
        }
    }

    private static void searchVehicle() {
        System.out.print("Enter registration number to search: ");
        String reg = scanner.nextLine().trim();

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getRegistrationNumber().equalsIgnoreCase(reg)) {
                vehicle.displayDetails();
                System.out.println("Operating Cost: R" + String.format("%.2f", vehicle.calculateOperatingCost()));
                return;
            }
        }

        System.out.println("ERROR: Vehicle not found.");
    }

    private static void removeVehicle() {
        System.out.print("Enter registration number to remove: ");
        String reg = scanner.nextLine().trim();

        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getRegistrationNumber().equalsIgnoreCase(reg)) {
                vehicles.remove(i);
                System.out.println("Vehicle removed successfully.");
                return;
            }
        }

        System.out.println("ERROR: Vehicle not found.");
    }
}
    
