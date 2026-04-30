How To Start:

ON ONYX:

-cd into project folder
-set path if not set for connector:
export CLASSPATH=/opt/mysql/mysql-connector-j-9.4.0.jar:.:$CLASSPATH

-compile java
-run shell

Tests:

new-class CS410 Sp20 1 "Databases"
list-classes
select-class CS410 Sp20 1
add-category Homework 40
add-category Exams 60
show-categories
add-assignment HW1 Homework "Intro SQL" 100
show-assignment
add-student jdoe 123456 Doe John
show-students
grade HW1 jdoe 95
student-grades jdoe
gradebook

These are already ran so they need to be changed for new examples.

Implementation:
The Java shell opens one JDBC connection, reads commands in a loop, and runs SQL for each command.
It tracks the currently selected class in memory, so class-scoped commands use that class_id.
Grades and totals are computed from the database rows and printed to the console.

Reflection:

This project was a lot of fun and taught us some cool uses and features of SQL as well as using a coding language to intertwine with it to make it functional outside of its normal scope. This helped strengthen our skills of turning word problems into ER Models, turning that into SQL DDL and then using those tables for real storage. As for use of AI, we used Github Copilot to help us configure the shell and work through bugs. Implementing this was easy for the most part besides for the amount of lines of code it took but the idea is quite simple. Overall this was a great learning experience for us and we enjoyed working on it.
