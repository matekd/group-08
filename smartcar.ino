#include <Smartcar.h>

// TODO: Find out which pins to place in which variables.

// Left motor
int leftMotorForwardPin = 8;
int leftMotorBackwardPin = 10;
int leftMotorSpeedPin = 9;

// Right motor
int rightMotorForwardPin = 12;
int rightMotorBackwardPin = 13;
int rightMotorSpeedPin = 11;

BrushedMotor leftMotor(leftMotorForwardPin, leftMotorBackwardPin, leftMotorSpeedPin);
BrushedMotor rightMotor(rightMotorForwardPin, rightMotorBackwardPin, rightMotorSpeedPin);

DifferentialControl(leftMotor, rightMotor);

SmartCar car(control);

void setup() {
    car.setSpeed(100);
    delay(1000);
    car.setSpeed(-50);
    delay(2000);
    car.setSpeed(0);
}

void loop() {

}