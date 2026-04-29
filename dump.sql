CREATE DATABASE IF NOT EXISTS school;

USE school;

CREATE TABLE classes (
    class_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    class_num VARCHAR(32) NOT NULL,
    term VARCHAR(32) NOT NULL,
    section_num INTEGER NOT NULL,
    description TEXT(65535) NOT NULL,
    professor VARCHAR(255) NOT NULL
);

CREATE TABLE categories (
    category_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    class_id INTEGER REFERENCES classes,
    name VARCHAR(255) NOT NULL,
    weight DECIMAL(7, 2) NOT NULL,
    UNIQUE (class_id, name),
    FOREIGN KEY (class_id) REFERENCES classes (class_id)
);

CREATE TABLE assignments (
    assignment_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    class_id INTEGER REFERENCES classes,
    category_id INTEGER REFERENCES categories,
    name VARCHAR(255) NOT NULL,
    description TEXT(65535) NOT NULL,
    points INTEGER NOT NULL,
    UNIQUE (class_id, name),
    FOREIGN KEY (class_id) REFERENCES classes (class_id),
    FOREIGN KEY (category_id) REFERENCES categories (category_id),
    INDEX (class_id)
);

CREATE TABLE students (
    student_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    studentid VARCHAR(64) NOT NULL UNIQUE,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL
);

CREATE TABLE enrollments (
    class_id INTEGER NOT NULL REFERENCES classes,
    student_id INTEGER NOT NULL REFERENCES students,
    PRIMARY KEY (class_id, student_id),
    FOREIGN KEY (class_id) REFERENCES classes (class_id),
    FOREIGN KEY (student_id) REFERENCES students (student_id)
);

CREATE TABLE grades (
    points DECIMAL(7, 2) NOT NULL,
    assignment_id INTEGER REFERENCES assignments,
    student_id INTEGER REFERENCES students,
    PRIMARY KEY (assignment_id, student_id),
    FOREIGN KEY (assignment_id) REFERENCES assignments (assignment_id),
    FOREIGN KEY (student_id) REFERENCES students (student_id),
    INDEX (assignment_id, student_id)
);

INSERT INTO classes (class_num, term, section_num, description, professor) VALUES
('CS101', 'Fall 2025', 1, 'Introduction to computer science concepts including algorithms, data structures, and basic programming.', 'Dr. Emily Carter'),
('CS101', 'Fall 2025', 2, 'Introduction to computer science concepts including algorithms, data structures, and basic programming.', 'Dr. Michael Chen'),
('CS410', 'Spring 2026', 1, 'Introduction to concepts in database architecture and design.', 'Dr. Francesca Spezzano'),
('MATH201', 'Spring 2026', 1, 'Calculus I covering limits, derivatives, and applications.', 'Dr. Sarah Johnson'),
('MATH202', 'Spring 2026', 1, 'Calculus II covering integrals, sequences, and series.', 'Dr. David Lee'),
('PHYS101', 'Fall 2025', 1, 'General physics focusing on mechanics, motion, and energy.', 'Dr. Robert Brown'),
('PHYS102', 'Spring 2026', 1, 'General physics focusing on electricity, magnetism, and waves.', 'Dr. Linda Martinez'),
('ENG101', 'Fall 2025', 1, 'English composition with emphasis on writing and critical thinking.', 'Dr. Karen White'),
('ENG201', 'Spring 2026', 1, 'Advanced composition and rhetoric with research methods.', 'Dr. James Anderson'),
('HIST101', 'Fall 2025', 1, 'World history from ancient civilizations to the modern era.', 'Dr. Patricia Taylor'),
('HIST202', 'Spring 2026', 1, 'Modern world history focusing on global conflicts and developments.', 'Dr. Christopher Moore'),
('BIO101', 'Fall 2025', 1, 'Introduction to biology including cell structure, genetics, and evolution.', 'Dr. Jennifer Thomas'),
('BIO102', 'Spring 2026', 1, 'Introduction to ecology, biodiversity, and environmental science.', 'Dr. William Jackson'),
('CHEM101', 'Fall 2025', 1, 'General chemistry covering atomic structure, bonding, and reactions.', 'Dr. Susan Harris'),
('CHEM102', 'Spring 2026', 1, 'General chemistry covering thermodynamics and kinetics.', 'Dr. Charles Martin'),
('PSY101', 'Fall 2025', 1, 'Introduction to psychology including behavior, cognition, and mental processes.', 'Dr. Nancy Thompson'),
('SOC101', 'Spring 2026', 1, 'Introduction to sociology focusing on social structures and institutions.', 'Dr. Steven Garcia'),
('ECON101', 'Fall 2025', 1, 'Principles of microeconomics including supply, demand, and market behavior.', 'Dr. Lisa Rodriguez'),
('ECON102', 'Spring 2026', 1, 'Principles of macroeconomics including inflation, unemployment, and fiscal policy.', 'Dr. Daniel Lewis'),
('ART101', 'Fall 2025', 1, 'Introduction to visual arts including drawing, painting, and design fundamentals.', 'Prof. Angela Walker'),
('MUSIC101', 'Spring 2026', 1, 'Introduction to music theory and appreciation.', 'Prof. Brian Hall');


INSERT INTO categories (name, weight) VALUES
('Homework Assignments', 25.00),
('Midterm Exam', 20.00),
('Final Exam', 30.00),
('Quizzes', 10.00),
('Class Participation', 5.00),
('Project', 10.00),
('Lab Work', 15.00),
('Group Presentation', 10.00),
('Attendance', 5.00),
('Extra Credit', 2.50),
('Research Paper', 20.00),
('Weekly Reflections', 10.00),
('Case Study Analysis', 15.00),
('Final Project', 25.00),
('Peer Review', 5.00),
('Discussion Posts', 10.00),
('Oral Exam', 15.00),
('Capstone Project', 30.00),
('Portfolio Submission', 20.00),
('Practical Exam', 25.00);


INSERT INTO assignments (name, description, points) VALUES
('Homework Assignments', 'Weekly problem sets designed to reinforce lecture material and improve problem-solving skills.', 100),
('Midterm Exam', 'A comprehensive exam covering the first half of the course material.', 150),
('Final Exam', 'A cumulative exam assessing understanding of all course topics.', 200),
('Quizzes', 'Short assessments given periodically to test knowledge of recent topics.', 50),
('Class Participation', 'Evaluation based on attendance, engagement, and contribution to discussions.', 25),
('Project', 'A semester-long project applying course concepts to a real-world scenario.', 120),
('Lab Work', 'Hands-on experiments and reports conducted during lab sessions.', 80),
('Group Presentation', 'Collaborative presentation on a selected topic relevant to the course.', 60),
('Attendance', 'Points awarded for consistent class attendance throughout the term.', 20),
('Extra Credit', 'Optional assignments that provide additional points beyond required coursework.', 15),
('Research Paper', 'An in-depth written paper analyzing a specific topic with cited sources.', 130),
('Weekly Reflections', 'Short essays reflecting on weekly lessons and personal insights.', 40),
('Case Study Analysis', 'Detailed examination of real or hypothetical scenarios related to course content.', 90),
('Final Project', 'A comprehensive project demonstrating mastery of course objectives.', 180),
('Peer Review', 'Evaluation of classmates’ work with constructive feedback.', 30),
('Discussion Posts', 'Online forum contributions discussing course topics and readings.', 50),
('Oral Exam', 'Verbal assessment testing understanding and ability to explain concepts.', 70),
('Capstone Project', 'A major culminating project integrating knowledge from the entire program.', 250),
('Portfolio Submission', 'Compilation of completed work showcasing progress and achievements.', 110),
('Practical Exam', 'Hands-on test demonstrating applied skills in a controlled setting.', 140);

INSERT INTO students (username, studentid, last_name, first_name) VALUES
('jdoe01', 'S100001', 'Doe', 'John'),
('asmith02', 'S100002', 'Smith', 'Anna'),
('bwilliams03', 'S100003', 'Williams', 'Brian'),
('cjones04', 'S100004', 'Jones', 'Catherine'),
('dgarcia05', 'S100005', 'Garcia', 'Daniel'),
('mrodriguez06', 'S100006', 'Rodriguez', 'Maria'),
('klee07', 'S100007', 'Lee', 'Kevin'),
('lmartinez08', 'S100008', 'Martinez', 'Laura'),
('jhernandez09', 'S100009', 'Hernandez', 'Jose'),
('pmoore10', 'S100010', 'Moore', 'Patricia'),
('awalker11', 'S100011', 'Walker', 'Andrew'),
('hyoung12', 'S100012', 'Young', 'Hannah'),
('cwhite13', 'S100013', 'White', 'Christopher'),
('sallen14', 'S100014', 'Allen', 'Sophia'),
('tking15', 'S100015', 'King', 'Thomas'),
('wright16', 'S100016', 'Wright', 'Olivia'),
('scott17', 'S100017', 'Scott', 'Samuel'),
('green18', 'S100018', 'Green', 'Grace'),
('baker19', 'S100019', 'Baker', 'Benjamin'),
('adams20', 'S100020', 'Adams', 'Ava');


INSERT INTO grades (assignment_id, student_id, points) VALUES
(1, 1, 10.00),
(2, 2, 20.00),
(3, 3, 20.00),
(4, 4, 20.00),
(5, 5, 50.00),
(6, 6, 50.00),
(7, 7, 70.00),
(8, 8, 80.00),
(9, 9, 90.00),
(10, 10, 100.00),
(11, 11, 100.00),
(12, 12, 100.00),
(13, 13, 100.00),
(14, 14, 100.00),
(15, 15, 150.00),
(16, 16, 150.00),
(17, 17, 150.00),
(18, 18, 200.00),
(19, 19, 200.00),
(20, 20, 200.00);

