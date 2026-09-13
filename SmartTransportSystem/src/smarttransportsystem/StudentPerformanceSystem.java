
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



class Student {

    private String studentNumber;
    private String name;
    private String surname;
    private String course;
    private ArrayList<Double> marks;

    public Student(String studentNumber,
                   String name,
                   String surname,
                   String course,
                   ArrayList<Double> marks) {

        this.studentNumber = studentNumber;
        this.name = name;
        this.surname = surname;
        this.course = course;
        this.marks = marks;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getCourse() {
        return course;
    }

    public ArrayList<Double> getMarks() {
        return marks;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setMarks(ArrayList<Double> marks) {
        this.marks = marks;
    }

    public double calculateAverage() {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (double mark : marks) {
            total += mark;
        }
        return total / marks.size();
    }

    public double getHighestMark() {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }
        double highest = marks.get(0);
        for (double mark : marks) {
            if (mark > highest) {
                highest = mark;
            }
        }
        return highest;
    }

    public double getLowestMark() {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }
        double lowest = marks.get(0);
        for (double mark : marks) {
            if (mark < lowest) {
                lowest = mark;
            }
        }
        return lowest;
    }
}

// ---------------- Custom exception for malformed JSON ----------------
class InvalidJsonException extends Exception {
    public InvalidJsonException(String message) {
        super(message);
    }
}

// ---------------- Part D: JSON file manager ----------------
class StudentFileManager {

    private final String FILE_NAME = "students.json";
    private final String BACKUP_NAME = "students_backup.json";

    // Regex patterns for pulling fields out of one JSON object's text
    private static final Pattern STUDENT_NUMBER_PATTERN =
            Pattern.compile("\"studentNumber\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern SURNAME_PATTERN =
            Pattern.compile("\"surname\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern COURSE_PATTERN =
            Pattern.compile("\"course\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern MARKS_PATTERN =
            Pattern.compile("\"marks\"\\s*:\\s*\\[([^\\]]*)\\]");

    /**
     * Reads students.json and returns the list of students.
     * Creates the file with an empty array if it does not exist.
     * Throws InvalidJsonException if the file's contents cannot be parsed.
     */
    public ArrayList<Student> readStudents() throws InvalidJsonException {

        File file = new File(FILE_NAME);
        ArrayList<Student> students = new ArrayList<>();

        try {
            if (!file.exists()) {
                Files.write(file.toPath(), "[]".getBytes());
                return students;
            }

            String content = new String(Files.readAllBytes(file.toPath())).trim();

            if (content.isEmpty()) {
                return students;
            }

            if (!content.startsWith("[") || !content.endsWith("]")) {
                throw new InvalidJsonException("The students.json file contains invalid JSON.");
            }

            String inner = content.substring(1, content.length() - 1).trim();

            if (inner.isEmpty()) {
                return students;
            }

            for (String objectText : splitTopLevelObjects(inner)) {
                students.add(parseStudent(objectText));
            }

            return students;

        } catch (IOException e) {
            throw new InvalidJsonException("Could not read students.json: " + e.getMessage());
        }
    }

    // Splits the array's inner content into the text of each {...} object,
    // tracking brace depth so nested [ ] inside "marks" doesn't confuse it.
    private ArrayList<String> splitTopLevelObjects(String inner) throws InvalidJsonException {
        ArrayList<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    if (start == -1) {
                        throw new InvalidJsonException("The students.json file contains invalid JSON.");
                    }
                    objects.add(inner.substring(start, i + 1));
                    start = -1;
                } else if (depth < 0) {
                    throw new InvalidJsonException("The students.json file contains invalid JSON.");
                }
            }
        }

        if (depth != 0) {
            throw new InvalidJsonException("The students.json file contains invalid JSON.");
        }

        return objects;
    }

    private Student parseStudent(String objectText) throws InvalidJsonException {

        String studentNumber = extractString(STUDENT_NUMBER_PATTERN, objectText, "studentNumber");
        String name = extractString(NAME_PATTERN, objectText, "name");
        String surname = extractString(SURNAME_PATTERN, objectText, "surname");
        String course = extractString(COURSE_PATTERN, objectText, "course");
        ArrayList<Double> marks = extractMarks(objectText);

        return new Student(studentNumber, name, surname, course, marks);
    }

    private String extractString(Pattern pattern, String text, String fieldName)
            throws InvalidJsonException {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            throw new InvalidJsonException("The students.json file contains invalid JSON "
                    + "(missing field: " + fieldName + ").");
        }
        return matcher.group(1);
    }

    private ArrayList<Double> extractMarks(String text) throws InvalidJsonException {
        Matcher matcher = MARKS_PATTERN.matcher(text);
        ArrayList<Double> marks = new ArrayList<>();

        if (!matcher.find()) {
            throw new InvalidJsonException("The students.json file contains invalid JSON (missing field: marks).");
        }

        String marksText = matcher.group(1).trim();
        if (marksText.isEmpty()) {
            return marks;
        }

        for (String part : marksText.split(",")) {
            try {
                marks.add(Double.parseDouble(part.trim()));
            } catch (NumberFormatException e) {
                throw new InvalidJsonException("The students.json file contains invalid JSON (bad mark value).");
            }
        }

        return marks;
    }

    /**
     * Saves the full list, backing up the previous file first (Part M/N).
     */
    public void saveStudents(ArrayList<Student> students) {

        try {
            createBackup();

            StringBuilder json = new StringBuilder();
            json.append("[\n");

            for (int i = 0; i < students.size(); i++) {
                json.append(toJson(students.get(i)));
                if (i < students.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("]");

            // Write to a temp file first, then replace, so a failed write
            // never leaves students.json half-written (transaction-style).
            File tempFile = new File(FILE_NAME + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(json.toString());
            }

            Files.move(tempFile.toPath(), Path.of(FILE_NAME),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.out.println("ERROR: Could not save students.json: " + e.getMessage());
            System.out.println("The previous data on disk has not been changed.");
        }
    }

    private void createBackup() {
        try {
            File original = new File(FILE_NAME);
            if (original.exists()) {
                Files.copy(original.toPath(), Path.of(BACKUP_NAME),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println("WARNING: Could not create backup file: " + e.getMessage());
        }
    }

    private String toJson(Student s) {
        StringBuilder sb = new StringBuilder();
        sb.append("    {\n");
        sb.append("        \"studentNumber\": \"").append(escape(s.getStudentNumber())).append("\",\n");
        sb.append("        \"name\": \"").append(escape(s.getName())).append("\",\n");
        sb.append("        \"surname\": \"").append(escape(s.getSurname())).append("\",\n");
        sb.append("        \"course\": \"").append(escape(s.getCourse())).append("\",\n");
        sb.append("        \"marks\": [");

        ArrayList<Double> marks = s.getMarks();
        for (int i = 0; i < marks.size(); i++) {
            sb.append(formatMark(marks.get(i)));
            if (i < marks.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]\n");
        sb.append("    }");
        return sb.toString();
    }

    private String formatMark(double mark) {
        if (mark == Math.floor(mark)) {
            return String.valueOf((long) mark);
        }
        return String.valueOf(mark);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public void addStudent(Student student) throws InvalidJsonException {
        ArrayList<Student> students = readStudents();

        for (Student s : students) {
            if (s.getStudentNumber().equalsIgnoreCase(student.getStudentNumber())) {
                System.out.println("ERROR: Student number already exists.");
                return;
            }
        }

        students.add(student);
        saveStudents(students);
        System.out.println("Student added successfully.");
    }

    public boolean deleteStudent(String studentNumber) throws InvalidJsonException {
        ArrayList<Student> students = readStudents();

        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentNumber().equalsIgnoreCase(studentNumber)) {
                students.remove(i);
                saveStudents(students);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a student exists (used before showing the update
     * sub-menu in the main program, which performs the actual field edits
     * and then calls saveStudents()).
     */
    public boolean updateStudent(String studentNumber) throws InvalidJsonException {
        ArrayList<Student> students = readStudents();
        for (Student s : students) {
            if (s.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                return true;
            }
        }
        return false;
    }
}

// ---------------- Main application ----------------
public class StudentPerformanceSystem {

    private static Scanner scanner = new Scanner(System.in);
    private static StudentFileManager fileManager = new StudentFileManager();

    public static void main(String[] args) {

        int choice = -1;

        while (choice != 9) {

            printMenu();
            choice = readMenuOption();

            try {
                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        displayAllStudents();
                        break;
                    case 3:
                        searchStudent();
                        break;
                    case 4:
                        updateStudent();
                        break;
                    case 5:
                        deleteStudent();
                        break;
                    case 6:
                        studentStatistics();
                        break;
                    case 7:
                        sortStudents();
                        break;
                    case 8:
                        createBackup();
                        break;
                    case 9:
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("ERROR: Please choose a valid option (1-9).");
                }
            } catch (InvalidJsonException e) {
                System.out.println("ERROR: " + e.getMessage());
                System.out.println("The application cannot load student records.");
            } catch (Exception e) {
                System.out.println("ERROR: An unexpected problem occurred: " + e.getMessage());
            } finally {
                System.out.println();
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("     STUDENT PERFORMANCE SYSTEM");
        System.out.println();
        System.out.println("1. Add Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Student Statistics");
        System.out.println("7. Sort Students");
        System.out.println("8. Create Backup");
        System.out.println("9. Exit");
        System.out.print("Enter option: ");
    }

    private static int readMenuOption() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter a valid number.");
            return -1;
        }
    }

    // ---------- Input helpers (Part K) ----------

    private static String readNonEmptyStudentNumber() {
        while (true) {
            System.out.print("Enter student number: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                System.out.println("ERROR: Student number cannot be empty.");
                continue;
            }
            return value;
        }
    }

    private static ArrayList<Double> readMarks() {
        ArrayList<Double> marks = new ArrayList<>();
        System.out.print("How many marks would you like to enter? ");
        int count;
        try {
            count = Integer.parseInt(scanner.nextLine().trim());
            if (count < 0) {
                System.out.println("ERROR: Please enter a valid number. Using 0 marks.");
                count = 0;
            }
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Please enter a valid number. Using 0 marks.");
            count = 0;
        }

        for (int i = 0; i < count; i++) {
            marks.add(readSingleMark(i + 1));
        }
        return marks;
    }

    private static double readSingleMark(int markNumber) {
        while (true) {
            try {
                System.out.print("Enter mark " + markNumber + ": ");
                double mark = Double.parseDouble(scanner.nextLine().trim());
                if (mark < 0 || mark > 100) {
                    System.out.println("ERROR: Mark must be between 0 and 100.");
                    continue;
                }
                return mark;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid number.");
            }
        }
    }

    // ---------- Menu actions ----------

    private static void addStudent() throws InvalidJsonException {
        String studentNumber = readNonEmptyStudentNumber();
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter surname: ");
        String surname = scanner.nextLine().trim();
        System.out.print("Enter course: ");
        String course = scanner.nextLine().trim();
        ArrayList<Double> marks = readMarks();

        Student student = new Student(studentNumber, name, surname, course, marks);
        fileManager.addStudent(student);
    }

    private static void displayAllStudents() throws InvalidJsonException {
        ArrayList<Student> students = fileManager.readStudents();

        if (students.isEmpty()) {
            System.out.println("No students on record.");
            return;
        }

        for (Student s : students) {
            printStudent(s);
            System.out.println();
        }
    }

    private static void searchStudent() throws InvalidJsonException {
        String studentNumber = readNonEmptyStudentNumber();
        ArrayList<Student> students = fileManager.readStudents();

        for (Student s : students) {
            if (s.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                printStudent(s);
                return;
            }
        }

        System.out.println("ERROR: Student not found.");
    }

    private static void printStudent(Student s) {
        System.out.println("Student Number : " + s.getStudentNumber());
        System.out.println("Name           : " + s.getName() + " " + s.getSurname());
        System.out.println("Course         : " + s.getCourse());
        System.out.println("Marks          : " + formatMarks(s.getMarks()));
        System.out.println("Average        : " + String.format("%.2f", s.calculateAverage()));
        System.out.println("Highest Mark   : " + trimTrailingZero(s.getHighestMark()));
        System.out.println("Lowest Mark    : " + trimTrailingZero(s.getLowestMark()));
    }

    private static String formatMarks(ArrayList<Double> marks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < marks.size(); i++) {
            sb.append(trimTrailingZero(marks.get(i)));
            if (i < marks.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static String trimTrailingZero(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private static void updateStudent() throws InvalidJsonException {
        String studentNumber = readNonEmptyStudentNumber();

        if (!fileManager.updateStudent(studentNumber)) {
            System.out.println("ERROR: Student not found.");
            return;
        }

        ArrayList<Student> students = fileManager.readStudents();
        Student target = null;
        for (Student s : students) {
            if (s.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                target = s;
                break;
            }
        }

        System.out.println("1. Update Name");
        System.out.println("2. Update Surname");
        System.out.println("3. Update Course");
        System.out.println("4. Update Marks");
        System.out.println("5. Cancel");
        int option = readMenuOption();

        switch (option) {
            case 1:
                System.out.print("Enter new name: ");
                target.setName(scanner.nextLine().trim());
                break;
            case 2:
                System.out.print("Enter new surname: ");
                target.setSurname(scanner.nextLine().trim());
                break;
            case 3:
                System.out.print("Enter new course: ");
                target.setCourse(scanner.nextLine().trim());
                break;
            case 4:
                target.setMarks(readMarks());
                break;
            case 5:
                System.out.println("Update cancelled.");
                return;
            default:
                System.out.println("ERROR: Please choose a valid option.");
                return;
        }

        fileManager.saveStudents(students);
        System.out.println("Student updated successfully.");
    }

    private static void deleteStudent() throws InvalidJsonException {
        String studentNumber = readNonEmptyStudentNumber();

        if (fileManager.deleteStudent(studentNumber)) {
            System.out.println("Student deleted successfully.");
        } else {
            System.out.println("ERROR: Student not found.");
        }
    }

    private static void studentStatistics() throws InvalidJsonException {
        ArrayList<Student> students = fileManager.readStudents();

        if (students.isEmpty()) {
            System.out.println("No students on record.");
            return;
        }

        double totalMarksSum = 0;
        int totalMarksCount = 0;
        double highestOverallMark = Double.MIN_VALUE;

        Student highestAvgStudent = students.get(0);
        Student lowestAvgStudent = students.get(0);
        int passingCount = 0;

        for (Student s : students) {
            for (double mark : s.getMarks()) {
                totalMarksSum += mark;
                totalMarksCount++;
                if (mark > highestOverallMark) {
                    highestOverallMark = mark;
                }
            }

            double avg = s.calculateAverage();
            if (avg > highestAvgStudent.calculateAverage()) {
                highestAvgStudent = s;
            }
            if (avg < lowestAvgStudent.calculateAverage()) {
                lowestAvgStudent = s;
            }
            if (avg >= 50) {
                passingCount++;
            }
        }

        double overallAverage = totalMarksCount == 0 ? 0 : totalMarksSum / totalMarksCount;
        double passRate = (double) passingCount / students.size() * 100;

        System.out.println("Overall Average         : " + String.format("%.2f", overallAverage));
        System.out.println("Highest Performing Student : " + highestAvgStudent.getName() + " "
                + highestAvgStudent.getSurname() + " (Avg: "
                + String.format("%.2f", highestAvgStudent.calculateAverage()) + ")");
        System.out.println("Lowest Performing Student   : " + lowestAvgStudent.getName() + " "
                + lowestAvgStudent.getSurname() + " (Avg: "
                + String.format("%.2f", lowestAvgStudent.calculateAverage()) + ")");
        System.out.println("Class Pass Rate         : " + String.format("%.2f", passRate) + "%");
        System.out.println("Highest Individual Mark : " + trimTrailingZero(highestOverallMark));
    }

    private static void sortStudents() throws InvalidJsonException {
        ArrayList<Student> students = fileManager.readStudents();

        if (students.isEmpty()) {
            System.out.println("No students on record.");
            return;
        }

        System.out.println("1. Sort by Student Number");
        System.out.println("2. Sort by Surname");
        System.out.println("3. Sort by Average");
        System.out.println("4. Sort by Highest Mark");
        int option = readMenuOption();

        // Sort a copy for display only - the JSON file/order on disk is untouched.
        ArrayList<Student> sorted = new ArrayList<>(students);

        switch (option) {
            case 1:
                sorted.sort(Comparator.comparing(Student::getStudentNumber));
                break;
            case 2:
                sorted.sort(Comparator.comparing(Student::getSurname));
                break;
            case 3:
                sorted.sort(Comparator.comparingDouble(Student::calculateAverage).reversed());
                break;
            case 4:
                sorted.sort(Comparator.comparingDouble(Student::getHighestMark).reversed());
                break;
            default:
                System.out.println("ERROR: Please choose a valid option.");
                return;
        }

        for (Student s : sorted) {
            printStudent(s);
            System.out.println();
        }
    }

    private static void createBackup() throws InvalidJsonException {
        // Reading and immediately saving triggers the file manager's
        // backup-then-write logic (Part M).
        ArrayList<Student> students = fileManager.readStudents();
        fileManager.saveStudents(students);
        System.out.println("Backup created as students_backup.json.");
    }
}