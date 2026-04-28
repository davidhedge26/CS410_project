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
