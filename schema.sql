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
    studentid VARCHAR(64) NOT NULL,
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