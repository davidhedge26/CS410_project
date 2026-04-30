
import java.sql.*;
import java.util.*;

public class CS410Shell {

    private static final String DB_URL = "jdbc:mysql://localhost:56438/school?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "msandbox";
    private static final String DB_PASS = "CS410QD";

    private Integer currentClassId = null;
    private String currentClassLabel = null;

    // Program entry point.
    public static void main(String[] args) {
        new CS410Shell().run();
    }

    // Main REPL loop: connect, read commands, dispatch.
    private void run() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); Scanner scanner = new Scanner(System.in)) {
            System.out.println("CS410 gradebook shell. Type 'help' for commands.");
            while (true) {
                System.out.print(prompt());
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> tokens = tokenize(line);
                if (tokens.isEmpty()) {
                    continue;
                }
                String cmd = tokens.get(0).toLowerCase(Locale.ROOT);
                try {
                    switch (cmd) {
                        case "help":
                            printHelp();
                            break;
                        case "exit":
                        case "quit":
                            return;
                        case "init-schema":
                            initSchema(conn);
                            break;
                        case "new-class":
                            cmdNewClass(conn, tokens);
                            break;
                        case "list-classes":
                            cmdListClasses(conn);
                            break;
                        case "select-class":
                            cmdSelectClass(conn, tokens);
                            break;
                        case "show-class":
                            cmdShowClass();
                            break;
                        case "show-categories":
                            requireClass();
                            cmdShowCategories(conn);
                            break;
                        case "add-category":
                            requireClass();
                            cmdAddCategory(conn, tokens);
                            break;
                        case "show-assignment":
                            requireClass();
                            cmdShowAssignment(conn);
                            break;
                        case "add-assignment":
                            requireClass();
                            cmdAddAssignment(conn, tokens);
                            break;
                        case "add-student":
                            requireClass();
                            cmdAddStudent(conn, tokens);
                            break;
                        case "show-students":
                            requireClass();
                            cmdShowStudents(conn, tokens);
                            break;
                        case "grade":
                            requireClass();
                            cmdGrade(conn, tokens);
                            break;
                        case "student-grades":
                            requireClass();
                            cmdStudentGrades(conn, tokens);
                            break;
                        case "gradebook":
                            requireClass();
                            cmdGradebook(conn);
                            break;
                        default:
                            System.out.println("Unknown command. Type 'help' for commands.");
                            break;
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Error: " + ex.getMessage());
                } catch (SQLException ex) {
                    System.out.println("SQL error: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.out.println("Failed to connect: " + ex.getMessage());
        }
    }

    // Prompt reflects the selected class when available.
    private String prompt() {
        if (currentClassLabel == null) {
            return "cs410> ";
        }
        return "cs410[" + currentClassLabel + "]> ";
    }

    // Print supported commands.
    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  new-class <ClassNum> <Term> <Section> \"Description\"");
        System.out.println("  list-classes");
        System.out.println("  select-class <ClassNum> [Term] [Section]");
        System.out.println("  show-class");
        System.out.println("  show-categories");
        System.out.println("  add-category <Name> <Weight>");
        System.out.println("  show-assignment");
        System.out.println("  add-assignment <Name> <Category> \"Description\" <Points>");
        System.out.println("  add-student <Username> [StudentId Last First]");
        System.out.println("  show-students [Filter]");
        System.out.println("  grade <AssignmentName> <Username> <Points>");
        System.out.println("  student-grades <Username>");
        System.out.println("  gradebook");
        System.out.println("  init-schema");
        System.out.println("  quit | exit");
    }

    // Split a command line into tokens while honoring quotes.
    private List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    // Ensure a class is selected before running class-scoped commands.
    private void requireClass() {
        if (currentClassId == null) {
            throw new IllegalArgumentException("No class selected. Use select-class.");
        }
    }

    // Create schema if it does not exist.
    private void initSchema(Connection conn) throws SQLException {
        String[] ddl = new String[]{
            "CREATE TABLE IF NOT EXISTS classes ("
            + "class_id INT PRIMARY KEY AUTO_INCREMENT,"
            + "class_num VARCHAR(32) NOT NULL,"
            + "term VARCHAR(32) NOT NULL,"
            + "section_num INT NOT NULL,"
            + "description TEXT NOT NULL,"
            + "professor VARCHAR(255) DEFAULT ''"
            + ")",
            "CREATE TABLE IF NOT EXISTS categories ("
            + "category_id INT PRIMARY KEY AUTO_INCREMENT,"
            + "class_id INT NOT NULL,"
            + "name VARCHAR(255) NOT NULL,"
            + "weight DECIMAL(7,2) NOT NULL,"
            + "UNIQUE(class_id, name),"
            + "FOREIGN KEY (class_id) REFERENCES classes(class_id)"
            + ")",
            "CREATE TABLE IF NOT EXISTS assignments ("
            + "assignment_id INT PRIMARY KEY AUTO_INCREMENT,"
            + "class_id INT NOT NULL,"
            + "category_id INT NOT NULL,"
            + "name VARCHAR(255) NOT NULL,"
            + "description TEXT NOT NULL,"
            + "points INT NOT NULL,"
            + "UNIQUE(class_id, name),"
            + "FOREIGN KEY (class_id) REFERENCES classes(class_id),"
            + "FOREIGN KEY (category_id) REFERENCES categories(category_id)"
            + ")",
            "CREATE TABLE IF NOT EXISTS students ("
            + "student_id INT PRIMARY KEY AUTO_INCREMENT,"
            + "username VARCHAR(255) NOT NULL UNIQUE,"
            + "studentid VARCHAR(64),"
            + "last_name VARCHAR(255) NOT NULL,"
            + "first_name VARCHAR(255) NOT NULL"
            + ")",
            "CREATE TABLE IF NOT EXISTS enrollments ("
            + "class_id INT NOT NULL,"
            + "student_id INT NOT NULL,"
            + "PRIMARY KEY (class_id, student_id),"
            + "FOREIGN KEY (class_id) REFERENCES classes(class_id),"
            + "FOREIGN KEY (student_id) REFERENCES students(student_id)"
            + ")",
            "CREATE TABLE IF NOT EXISTS grades ("
            + "assignment_id INT NOT NULL,"
            + "student_id INT NOT NULL,"
            + "points DECIMAL(7,2) NOT NULL,"
            + "PRIMARY KEY (assignment_id, student_id),"
            + "FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id),"
            + "FOREIGN KEY (student_id) REFERENCES students(student_id)"
            + ")"
        };
        for (String stmt : ddl) {
            try (Statement s = conn.createStatement()) {
                s.execute(stmt);
            }
        }
        System.out.println("Schema initialized (if missing).");
    }

    // Create a new class record.
    private void cmdNewClass(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 5) {
            throw new IllegalArgumentException("Usage: new-class <ClassNum> <Term> <Section> \"Description\"");
        }
        String classNum = tokens.get(1);
        String term = tokens.get(2);
        int section = Integer.parseInt(tokens.get(3));
        String description = tokens.get(4);
        String sql = "INSERT INTO classes (class_num, term, section_num, description, professor) VALUES (?, ?, ?, ?, '')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classNum);
            ps.setString(2, term);
            ps.setInt(3, section);
            ps.setString(4, description);
            ps.executeUpdate();
        }
        System.out.println("Class created.");
    }

    // List classes with enrollment counts.
    private void cmdListClasses(Connection conn) throws SQLException {
        String sql = "SELECT c.class_id, c.class_num, c.term, c.section_num, c.description, "
                + "COUNT(e.student_id) AS students "
                + "FROM classes c LEFT JOIN enrollments e ON e.class_id = c.class_id "
                + "GROUP BY c.class_id, c.class_num, c.term, c.section_num, c.description "
                + "ORDER BY c.class_num, c.term, c.section_num";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("%s %s %d - %s (students: %d)%n",
                        rs.getString("class_num"),
                        rs.getString("term"),
                        rs.getInt("section_num"),
                        rs.getString("description"),
                        rs.getInt("students"));
            }
        }
    }

    // Select class based on supplied arguments.
    private void cmdSelectClass(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 2) {
            throw new IllegalArgumentException("Usage: select-class <ClassNum> [Term] [Section]");
        }
        String classNum = tokens.get(1);
        if (tokens.size() == 2) {
            selectMostRecent(conn, classNum);
            return;
        }
        String term = tokens.get(2);
        if (tokens.size() == 3) {
            selectSingleInTerm(conn, classNum, term);
            return;
        }
        int section = Integer.parseInt(tokens.get(3));
        selectSpecific(conn, classNum, term, section);
    }

    // Show the active class label.
    private void cmdShowClass() {
        if (currentClassId == null) {
            System.out.println("No class selected.");
        } else {
            System.out.println("Current class: " + currentClassLabel);
        }
    }

    // List categories and weights for the active class.
    private void cmdShowCategories(Connection conn) throws SQLException {
        String sql = "SELECT name, weight FROM categories WHERE class_id = ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("%s (weight: %s)%n", rs.getString("name"), rs.getString("weight"));
                }
            }
        }
    }

    // Add a category to the active class.
    private void cmdAddCategory(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 3) {
            throw new IllegalArgumentException("Usage: add-category <Name> <Weight>");
        }
        String name = tokens.get(1);
        double weight = Double.parseDouble(tokens.get(2));
        String sql = "INSERT INTO categories (class_id, name, weight) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            ps.setString(2, name);
            ps.setDouble(3, weight);
            ps.executeUpdate();
        }
        System.out.println("Category added.");
    }

    // List assignments grouped by category.
    private void cmdShowAssignment(Connection conn) throws SQLException {
        String sql = "SELECT c.name AS category, a.name AS assignment, a.description, a.points "
                + "FROM assignments a JOIN categories c ON a.category_id = c.category_id "
                + "WHERE a.class_id = ? ORDER BY c.name, a.name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                String currentCategory = null;
                while (rs.next()) {
                    String category = rs.getString("category");
                    if (!category.equals(currentCategory)) {
                        currentCategory = category;
                        System.out.println("Category: " + category);
                    }
                    System.out.printf("  %s (%d pts) - %s%n",
                            rs.getString("assignment"),
                            rs.getInt("points"),
                            rs.getString("description"));
                }
            }
        }
    }

    // Add an assignment under a category.
    private void cmdAddAssignment(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 5) {
            throw new IllegalArgumentException("Usage: add-assignment <Name> <Category> \"Description\" <Points>");
        }
        String name = tokens.get(1);
        String category = tokens.get(2);
        String description = tokens.get(3);
        int points = Integer.parseInt(tokens.get(4));
        Integer categoryId = findCategoryId(conn, category);
        if (categoryId == null) {
            throw new IllegalArgumentException("Category not found: " + category);
        }
        String sql = "INSERT INTO assignments (class_id, category_id, name, description, points) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            ps.setInt(2, categoryId);
            ps.setString(3, name);
            ps.setString(4, description);
            ps.setInt(5, points);
            ps.executeUpdate();
        }
        System.out.println("Assignment added.");
    }

    // Add a student or enroll an existing student.
    private void cmdAddStudent(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 2) {
            throw new IllegalArgumentException("Usage: add-student <Username> [StudentId Last First]");
        }
        String username = tokens.get(1);
        if (tokens.size() == 2) {
            Integer studentId = findStudentId(conn, username);
            if (studentId == null) {
                throw new IllegalArgumentException("Student not found: " + username);
            }
            enrollStudent(conn, studentId);
            System.out.println("Student enrolled.");
            return;
        }
        if (tokens.size() < 5) {
            throw new IllegalArgumentException("Usage: add-student <Username> <StudentId> <Last> <First>");
        }
        String studentIdText = tokens.get(2);
        String last = tokens.get(3);
        String first = tokens.get(4);
        Integer studentId = findStudentId(conn, username);
        if (studentId == null) {
            String insert = "INSERT INTO students (username, studentid, last_name, first_name) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, studentIdText);
                ps.setString(3, last);
                ps.setString(4, first);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        studentId = rs.getInt(1);
                    }
                }
            }
            enrollStudent(conn, studentId);
            System.out.println("Student added and enrolled.");
            return;
        }
        String currentNameSql = "SELECT last_name, first_name, studentid FROM students WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(currentNameSql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String curLast = rs.getString("last_name");
                    String curFirst = rs.getString("first_name");
                    String curStudentId = rs.getString("studentid");
                    if (!last.equals(curLast) || !first.equals(curFirst) || !studentIdText.equals(curStudentId)) {
                        System.out.println("Warning: student info changed for " + username + ".");
                        String update = "UPDATE students SET last_name = ?, first_name = ?, studentid = ? WHERE student_id = ?";
                        try (PreparedStatement upd = conn.prepareStatement(update)) {
                            upd.setString(1, last);
                            upd.setString(2, first);
                            upd.setString(3, studentIdText);
                            upd.setInt(4, studentId);
                            upd.executeUpdate();
                        }
                    }
                }
            }
        }
        enrollStudent(conn, studentId);
        System.out.println("Student enrolled.");
    }

    // Show students in the active class, optionally filtered.
    private void cmdShowStudents(Connection conn, List<String> tokens) throws SQLException {
        String filter = tokens.size() > 1 ? tokens.get(1).toLowerCase(Locale.ROOT) : null;
        String sql = "SELECT s.username, s.studentid, s.last_name, s.first_name "
                + "FROM students s JOIN enrollments e ON e.student_id = s.student_id "
                + "WHERE e.class_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String studentId = rs.getString("studentid");
                    String last = rs.getString("last_name");
                    String first = rs.getString("first_name");
                    if (filter != null) {
                        String hay = (username + " " + last + " " + first).toLowerCase(Locale.ROOT);
                        if (!hay.contains(filter)) {
                            continue;
                        }
                    }
                    System.out.printf("%s (%s) %s, %s%n", username, studentId, last, first);
                }
            }
        }
    }

    // Record or update a grade for a student.
    private void cmdGrade(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 4) {
            throw new IllegalArgumentException("Usage: grade <AssignmentName> <Username> <Points>");
        }
        String assignmentName = tokens.get(1);
        String username = tokens.get(2);
        double points = Double.parseDouble(tokens.get(3));
        Integer assignmentId = findAssignmentId(conn, assignmentName);
        if (assignmentId == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentName);
        }
        Integer studentId = findStudentIdInClass(conn, username);
        if (studentId == null) {
            throw new IllegalArgumentException("Student not enrolled: " + username);
        }
        int maxPoints = getAssignmentPoints(conn, assignmentId);
        if (points > maxPoints) {
            System.out.println("Warning: points exceed configured assignment points (" + maxPoints + ").");
        }
        String sql = "INSERT INTO grades (assignment_id, student_id, points) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE points = VALUES(points)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);
            ps.setDouble(3, points);
            ps.executeUpdate();
        }
        System.out.println("Grade recorded.");
    }

    // Show a student's grades with category subtotals.
    private void cmdStudentGrades(Connection conn, List<String> tokens) throws SQLException {
        if (tokens.size() < 2) {
            throw new IllegalArgumentException("Usage: student-grades <Username>");
        }
        String username = tokens.get(1);
        Integer studentId = findStudentIdInClass(conn, username);
        if (studentId == null) {
            throw new IllegalArgumentException("Student not enrolled: " + username);
        }

        Map<Integer, CategoryInfo> categories = loadCategories(conn);
        List<AssignmentInfo> assignments = loadAssignments(conn);
        Map<Integer, Double> grades = loadGradesForStudent(conn, studentId);

        double overall = computeOverall(categories, assignments, grades);
        String currentCategory = null;
        double catEarned = 0;
        double catPossible = 0;

        for (AssignmentInfo a : assignments) {
            CategoryInfo c = categories.get(a.categoryId);
            if (c == null) {
                continue;
            }
            if (!c.name.equals(currentCategory)) {
                if (currentCategory != null) {
                    System.out.printf("  Subtotal: %.2f / %.2f%n", catEarned, catPossible);
                }
                currentCategory = c.name;
                catEarned = 0;
                catPossible = 0;
                System.out.println("Category: " + currentCategory + " (weight: " + c.weight + ")");
            }
            Double grade = grades.get(a.assignmentId);
            catPossible += a.points;
            if (grade != null) {
                catEarned += grade;
            }
            System.out.printf("  %s: %s / %d%n", a.name, grade == null ? "-" : String.format("%.2f", grade), a.points);
        }
        if (currentCategory != null) {
            System.out.printf("  Subtotal: %.2f / %.2f%n", catEarned, catPossible);
        }
        System.out.printf("Overall grade: %.2f%%%n", overall);
    }

    // Print gradebook totals for all students in the active class.
    private void cmdGradebook(Connection conn) throws SQLException {
        Map<Integer, CategoryInfo> categories = loadCategories(conn);
        List<AssignmentInfo> assignments = loadAssignments(conn);

        String sql = "SELECT s.student_id, s.username, s.studentid, s.last_name, s.first_name "
                + "FROM students s JOIN enrollments e ON e.student_id = s.student_id "
                + "WHERE e.class_id = ? ORDER BY s.username";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    String username = rs.getString("username");
                    String studentid = rs.getString("studentid");
                    String last = rs.getString("last_name");
                    String first = rs.getString("first_name");
                    Map<Integer, Double> grades = loadGradesForStudent(conn, studentId);
                    double overall = computeOverall(categories, assignments, grades);
                    System.out.printf("%s (%s) %s, %s - %.2f%%%n", username, studentid, last, first, overall);
                }
            }
        }
    }

    // Look up a category id by name for the active class.
    private Integer findCategoryId(Connection conn, String categoryName) throws SQLException {
        String sql = "SELECT category_id FROM categories WHERE class_id = ? AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            ps.setString(2, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }
        return null;
    }

    // Look up an assignment id by name for the active class.
    private Integer findAssignmentId(Connection conn, String assignmentName) throws SQLException {
        String sql = "SELECT assignment_id FROM assignments WHERE class_id = ? AND name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            ps.setString(2, assignmentName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("assignment_id");
                }
            }
        }
        return null;
    }

    // Fetch max points for an assignment.
    private int getAssignmentPoints(Connection conn, int assignmentId) throws SQLException {
        String sql = "SELECT points FROM assignments WHERE assignment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("points");
                }
            }
        }
        return 0;
    }

    // Look up a student id by username.
    private Integer findStudentId(Connection conn, String username) throws SQLException {
        String sql = "SELECT student_id FROM students WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        }
        return null;
    }

    // Look up a student id by username within the active class.
    private Integer findStudentIdInClass(Connection conn, String username) throws SQLException {
        String sql = "SELECT s.student_id FROM students s JOIN enrollments e ON e.student_id = s.student_id "
                + "WHERE e.class_id = ? AND s.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        }
        return null;
    }

    // Enroll a student in the active class.
    private void enrollStudent(Connection conn, int studentId) throws SQLException {
        String sql = "INSERT IGNORE INTO enrollments (class_id, student_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    // Select the most recent term for a class number.
    private void selectMostRecent(Connection conn, String classNum) throws SQLException {
        List<ClassRow> rows = loadClasses(conn, classNum);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("No classes found for " + classNum);
        }
        int maxKey = Integer.MIN_VALUE;
        for (ClassRow row : rows) {
            int key = termKey(row.term);
            if (key > maxKey) {
                maxKey = key;
            }
        }
        List<ClassRow> candidates = new ArrayList<>();
        for (ClassRow row : rows) {
            if (termKey(row.term) == maxKey) {
                candidates.add(row);
            }
        }
        if (candidates.size() != 1) {
            throw new IllegalArgumentException("Multiple sections found for most recent term; specify section.");
        }
        setCurrentClass(candidates.get(0));
    }

    // Select a class in a specific term when only one section exists.
    private void selectSingleInTerm(Connection conn, String classNum, String term) throws SQLException {
        List<ClassRow> rows = loadClasses(conn, classNum);
        List<ClassRow> candidates = new ArrayList<>();
        for (ClassRow row : rows) {
            if (row.term.equalsIgnoreCase(term)) {
                candidates.add(row);
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No class found for " + classNum + " " + term);
        }
        if (candidates.size() != 1) {
            throw new IllegalArgumentException("Multiple sections found; specify section.");
        }
        setCurrentClass(candidates.get(0));
    }

    // Select an explicit class number, term, and section.
    private void selectSpecific(Connection conn, String classNum, String term, int section) throws SQLException {
        String sql = "SELECT class_id, class_num, term, section_num, description FROM classes "
                + "WHERE class_num = ? AND term = ? AND section_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classNum);
            ps.setString(2, term);
            ps.setInt(3, section);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Class not found.");
                }
                ClassRow row = new ClassRow(rs.getInt("class_id"), rs.getString("class_num"),
                        rs.getString("term"), rs.getInt("section_num"), rs.getString("description"));
                setCurrentClass(row);
            }
        }
    }

    // Load all classes for a class number.
    private List<ClassRow> loadClasses(Connection conn, String classNum) throws SQLException {
        String sql = "SELECT class_id, class_num, term, section_num, description FROM classes WHERE class_num = ?";
        List<ClassRow> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classNum);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ClassRow(rs.getInt("class_id"), rs.getString("class_num"),
                            rs.getString("term"), rs.getInt("section_num"), rs.getString("description")));
                }
            }
        }
        return rows;
    }

    // Update the active class context.
    private void setCurrentClass(ClassRow row) {
        currentClassId = row.classId;
        currentClassLabel = row.classNum + " " + row.term + " " + row.sectionNum;
        System.out.println("Selected class: " + currentClassLabel + " - " + row.description);
    }

    // Normalize term strings for ordering by year and season.
    private int termKey(String term) {
        String lower = term.toLowerCase(Locale.ROOT);
        int season = 0;
        if (lower.startsWith("wi")) {
            season = 0;
        } else if (lower.startsWith("sp")) {
            season = 1;
        } else if (lower.startsWith("su")) {
            season = 2;
        } else if (lower.startsWith("fa")) {
            season = 3;
        }
        int year = 0;
        String digits = lower.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            year = Integer.parseInt(digits);
            if (digits.length() == 2) {
                year += 2000;
            }
        }
        return year * 10 + season;
    }

    // Load categories for the active class.
    private Map<Integer, CategoryInfo> loadCategories(Connection conn) throws SQLException {
        Map<Integer, CategoryInfo> map = new HashMap<>();
        String sql = "SELECT category_id, name, weight FROM categories WHERE class_id = ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("category_id"),
                            new CategoryInfo(rs.getInt("category_id"), rs.getString("name"), rs.getDouble("weight")));
                }
            }
        }
        return map;
    }

    // Load assignments for the active class.
    private List<AssignmentInfo> loadAssignments(Connection conn) throws SQLException {
        List<AssignmentInfo> list = new ArrayList<>();
        String sql = "SELECT assignment_id, category_id, name, points FROM assignments WHERE class_id = ? "
                + "ORDER BY category_id, name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AssignmentInfo(rs.getInt("assignment_id"), rs.getInt("category_id"),
                            rs.getString("name"), rs.getInt("points")));
                }
            }
        }
        return list;
    }

    // Load grades for a single student in the active class.
    private Map<Integer, Double> loadGradesForStudent(Connection conn, int studentId) throws SQLException {
        Map<Integer, Double> map = new HashMap<>();
        String sql = "SELECT g.assignment_id, g.points FROM grades g "
                + "JOIN assignments a ON a.assignment_id = g.assignment_id "
                + "WHERE g.student_id = ? AND a.class_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, currentClassId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("assignment_id"), rs.getDouble("points"));
                }
            }
        }
        return map;
    }

    // Compute overall percentage using category weights.
    private double computeOverall(Map<Integer, CategoryInfo> categories,
            List<AssignmentInfo> assignments,
            Map<Integer, Double> grades) {
        Map<Integer, Double> earnedByCat = new HashMap<>();
        Map<Integer, Double> possibleByCat = new HashMap<>();

        for (AssignmentInfo a : assignments) {
            possibleByCat.put(a.categoryId, possibleByCat.getOrDefault(a.categoryId, 0.0) + a.points);
            Double grade = grades.get(a.assignmentId);
            if (grade != null) {
                earnedByCat.put(a.categoryId, earnedByCat.getOrDefault(a.categoryId, 0.0) + grade);
            }
        }

        double totalWeight = 0.0;
        for (CategoryInfo c : categories.values()) {
            totalWeight += c.weight;
        }
        if (totalWeight <= 0.0) {
            double totalEarned = 0.0;
            double totalPossible = 0.0;
            for (AssignmentInfo a : assignments) {
                totalPossible += a.points;
                Double grade = grades.get(a.assignmentId);
                if (grade != null) {
                    totalEarned += grade;
                }
            }
            return totalPossible == 0.0 ? 0.0 : (totalEarned / totalPossible) * 100.0;
        }

        double scale = totalWeight > 1.5 ? 100.0 : 1.0;
        double overall = 0.0;
        for (CategoryInfo c : categories.values()) {
            double possible = possibleByCat.getOrDefault(c.id, 0.0);
            if (possible == 0.0) {
                continue;
            }
            double earned = earnedByCat.getOrDefault(c.id, 0.0);
            double percent = earned / possible;
            overall += percent * (c.weight / scale);
        }
        return overall * 100.0;
    }

    private static class ClassRow {

        final int classId;
        final String classNum;
        final String term;
        final int sectionNum;
        final String description;

        // Store a lightweight class record.
        ClassRow(int classId, String classNum, String term, int sectionNum, String description) {
            this.classId = classId;
            this.classNum = classNum;
            this.term = term;
            this.sectionNum = sectionNum;
            this.description = description;
        }
    }

    private static class CategoryInfo {

        final int id;
        final String name;
        final double weight;

        // Store a lightweight category record.
        CategoryInfo(int id, String name, double weight) {
            this.id = id;
            this.name = name;
            this.weight = weight;
        }
    }

    private static class AssignmentInfo {

        final int assignmentId;
        final int categoryId;
        final String name;
        final int points;

        // Store a lightweight assignment record.
        AssignmentInfo(int assignmentId, int categoryId, String name, int points) {
            this.assignmentId = assignmentId;
            this.categoryId = categoryId;
            this.name = name;
            this.points = points;
        }
    }
}
