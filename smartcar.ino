#include <VL53L0X.h>
#include <Smartcar.h>
#include <Wire.h>

BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);

SimpleCar car(control);
VL53L0X sensor;

void setup(){
  Serial.begin(9600);
  Wire.begin();
  car.setSpeed(50);
  sensor.setTimeout(500);
  if(!sensor.init()){
    Serial.println("Failed to detect and initialize sensor!");
    while(1){}
  }
  sensor.startContinuous();
}
void loop(){
  Serial.println(sensor.readRangeContinuousMillimeters());

  if(sensor.timeoutOccurred()) {
  Serial.print("TIMEOUT");
  }

  if(sensor.readRangeContinuousMillimeters() < 200) {
    car.setSpeed(0);
  }
}