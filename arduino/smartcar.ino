#include <Smartcar.h>
#include <BluetoothSerial.h>
#include <VL53L0X.h>
#include <Wire.h>

char input; 
const int fSpeed   = 50;  // 50% of the full speed forward
const int bSpeed   = -50; // 50% of the full speed backward
const int lDegrees = -55; // degrees to turn left
const int rDegrees = 55;  // degrees to turn right
const int flDegrees = -28; // degrees to turn forward left
const int frDegrees = 28;  // degrees to turn forward right
const int blDegrees = -152; // degrees to turn backward left
const int brDegrees = 152; // degrees to turn backward right
BluetoothSerial bluetooth;
BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
VL53L0X sensor;
SimpleCar car(control);

void setup()
{
    Serial.begin(9600);
    bluetooth.begin("Car"); // Device name
    Wire.begin();
     sensor.setTimeout(500);
  if (!sensor.init())
  {
    Serial.println("Failed to detect and initialize sensor!");
    while (1) {}
  }
  sensor.startContinuous();
}

void loop()
{
    handleInput();
     Serial.print(sensor.readRangeContinuousMillimeters());
  if (sensor.timeoutOccurred()) { Serial.print(" TIMEOUT"); }
  Serial.println();
 
}

void handleInput()
{ 
        while (bluetooth.available()){input = bluetooth.read();
        
        } 
                 int distance = sensor.readRangeContinuousMillimeters();
  while(distance !=0 && distance<200)
  { 
    while (bluetooth.available()){input = bluetooth.read();}
    if(input=='b'){
      car.setSpeed(bSpeed);
      car.setAngle(0);
      
    } else {
      car.setSpeed(0);
    }
    distance = sensor.readRangeContinuousMillimeters();
  }
  
                       
        switch (input){
        
        case 'l': // rotate counter-clockwise going forward
            car.setSpeed(fSpeed);
            car.setAngle(lDegrees);
            break;
        case 'r': // turn clock-wise
            car.setSpeed(fSpeed);
            car.setAngle(rDegrees);
            break;
        case 'f': // go ahead
            car.setSpeed(fSpeed);
            car.setAngle(0);
            break;
        case 'b': // go back
            car.setSpeed(bSpeed);
            car.setAngle(0);
            break;
        case 'fr': // go forward right
            car.setSpeed(fSpeed);
            car.setAngle(frDegrees);
            break;
         case 'fl': // go forward left
            car.setSpeed(fSpeed);
            car.setAngle(flDegrees);
            break;
         case 'br': // go backward right
            car.setSpeed(bSpeed);
            car.setAngle(brDegrees);
            break;
         case 'bl': // go backward left
            car.setSpeed(bSpeed);
            car.setAngle(blDegrees);
            break;
        default: // if you receive something that you don't know, just stop
            car.setSpeed(0);
            car.setAngle(0);
        }
  
}
