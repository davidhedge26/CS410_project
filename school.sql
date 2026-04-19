CREATE DATABASE IF NOT EXISTS school;
USE school;

CREATE TABLE class (
	class_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    class_num INTEGER NOT NULL,
    term VARCHAR(255) NOT NULL,
    section_num INTEGER NOT NULL,
    description TEXT(65535) NOT NULL,
    professor VARCHAR(255) NOT NULL
);

CREATE TABLE assignments(
	assignments_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    class_id INTEGER NOT NULL REFERENCES class,
    name VARCHAR(255) NOT NULL,
    description TEXT(65535) NOT NULL,
    val INTEGER NOT NULL,
    category VARCHAR(255),
    
    FOREIGN KEY (class_id) REFERENCES class (class_id),
    INDEX (class_id)
);

CREATE TABLE student(
	student_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    class_id INTEGER NOT NULL REFERENCES class,
    assignments_id INTEGER NOT NULL REFERENCES assignments,
    username VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    
    FOREIGN KEY (assignments_id) REFERENCES assignments (assignments_id),
    FOREIGN KEY (class_id) REFERENCES class (class_id),
    INDEX(class_id, assignments_id)
);

CREATE TABLE grade(
	grade INTEGER NOT NULL,
    assignments_id INTEGER NOT NULL REFERENCES assignments,
    student_id INTEGER NOT NULL REFERENCES students,
    
    FOREIGN KEY (assignments_id) REFERENCES assignments (assignments_id),
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    INDEX(assignments_id, student_id)
);
