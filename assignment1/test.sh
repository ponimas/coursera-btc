#!/usr/bin/env sh
java -cp /Users/ponimas/.m2/repository/junit/junit/4.12/junit-4.12.jar:/Users/ponimas/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:$PWD/test/:$PWD org.junit.runner.JUnitCore IsValidTest
