#include <Smartcar.h>
#include <BluetoothSerial.h>

int input;
int currentSpeed; // The speed the car is set to move at, used for manual drive only
const int TRIGGER_PIN = 5; //D5 red cable
const int ECHO_PIN = 18; //D18 green cable
const int TRIGGER_PIN2 = 17; //D5 red cable
const int ECHO_PIN2 = 16; //D18 green cable
const unsigned int MAX_DISTANCE = 100;
SR04 sensor(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);
SR04 backsensor(TRIGGER_PIN2, ECHO_PIN2, MAX_DISTANCE);
const int GYROSCOPE_OFFSET = 37;
const unsigned long PRINT_INTERVAL = 100;
unsigned long previousPrintout     = 0;
const auto pulsesPerMeter = 600;
const int fSpeed    = 50;  // 50% of the full speed forward
const int bSpeed    = -50; // 50% of the full speed backward
const int tankSpeed = 55;  // Speed at which the wheels turn on the spot
const int flDegrees = -152; // degrees to turn forward left
const int frDegrees = 152;  // degrees to turn forward right
const int blDegrees = -152; // degrees to turn backward left
const int brDegrees = 152; // degrees to turn backward right

BluetoothSerial bluetooth;
BrushedMotor leftMotor(smartcarlib::pins::v2::leftMotorPins);
BrushedMotor rightMotor(smartcarlib::pins::v2::rightMotorPins);
DifferentialControl control(leftMotor, rightMotor);
GY50 gyroscope(GYROSCOPE_OFFSET);
DirectionlessOdometer leftOdometer(
    smartcarlib::pins::v2::leftOdometerPin, []() { leftOdometer.update(); }, pulsesPerMeter);
DirectionlessOdometer rightOdometer(
    smartcarlib::pins::v2::rightOdometerPin, []() { rightOdometer.update(); }, pulsesPerMeter);
SmartCar car(control, gyroscope, leftOdometer, rightOdometer);

void setup()
{
    Serial.begin(9600);
    bluetooth.begin("Car"); // Device name
    currentSpeed = 0;
}

void loop()
{
    handleInput();
 Serial.println(input);

    Serial.println(sensor.getDistance());
    Serial.println(backsensor.getDistance());
}

void rotateOnSpot(int targetDegrees, int speed) // taken from smartcar library
{
    speed = smartcarlib::utils::getAbsolute(speed);
    targetDegrees %= 360; // put it on a (-360,360) scale
    if (!targetDegrees)
        return; // if the target degrees is 0, don't bother doing anything
    /* Let's set opposite speed on each side of the car, so it rotates on spot */
    if (targetDegrees > 0)
    { // positive value means we should rotate clockwise
        car.overrideMotorSpeed(speed,
                               -speed); // left motors spin forward, right motors spin backward
    }
    else
    { // rotate counter clockwise
        car.overrideMotorSpeed(-speed,
                               speed); // left motors spin backward, right motors spin forward
    }
    const auto initialHeading = car.getHeading(); // the initial heading we'll use as offset to
                                                  // calculate the absolute displacement
    int degreesTurnedSoFar
        = 0; // this variable will hold the absolute displacement from the beginning of the rotation
    while (abs(degreesTurnedSoFar) < abs(targetDegrees))
    { // while absolute displacement hasn't reached the (absolute) target, keep turning
        car.update(); // update to integrate the latest heading sensor readings
        auto currentHeading = car.getHeading(); // in the scale of 0 to 360
        if ((targetDegrees < 0) && (currentHeading > initialHeading))
        { // if we are turning left and the current heading is larger than the
            // initial one (e.g. started at 10 degrees and now we are at 350), we need to substract
            // 360, so to eventually get a signed
            currentHeading -= 360; // displacement from the initial heading (-20)
        }
        else if ((targetDegrees > 0) && (currentHeading < initialHeading))
        { // if we are turning right and the heading is smaller than the
            // initial one (e.g. started at 350 degrees and now we are at 20), so to get a signed
            // displacement (+30)
            currentHeading += 360;
        }
        degreesTurnedSoFar
            = initialHeading - currentHeading; // degrees turned so far is initial heading minus
                                               // current (initial heading
        // is at least 0 and at most 360. To handle the "edge" cases we substracted or added 360 to
        // currentHeading)
    }
    changeSpeed(0); // we have reached the target, so stop the car
}

void handleInput()
{ 
    
    while (bluetooth.available()){ input = bluetooth.read(); }        
    car.update();
    int front = sensor.getDistance(); 
    int back = backsensor.getDistance();
    
    
    while(front != 0 && front < 20) { 
        car.getHeading();
        changeSpeed(0);
        car.setAngle(0);
        delay(100);
        rotateOnSpot(-90, 80);
        front = sensor.getDistance();
 
    }

    while(back != 0 && back < 20){
        car.getHeading();
        changeSpeed(0);
        car.setAngle(0);
        delay(100);
        rotateOnSpot(-90, 80);
        back = backsensor.getDistance()
    }
    

    // Converts inputs which were neagtive back to negative
    if (input > 127){
        input = (256 - input);
        input = (input * -1);   
    }

    switch (input) {
        //  manual  |   mindcontrol plus phone gyroscope
        //  8 1 2   |   9
        //  7   3   |   10
        //  6 5 4   |   -110 to -11 and 11 to 110

        case 1: // Move straight forward
            car.setAngle(0);
            changeSpeed(fSpeed);
            break;
        
        case 2: // Turn right forward
            car.setAngle(frDegrees);
            changeSpeed(fSpeed);
            break;

        case 3: // turn clockwise 
            car.setAngle(0);
            rotateOnSpot(90, 80);
            break;

        case 4: // Turn right backward
            car.setAngle(brDegrees);
            changeSpeed(bSpeed);
            break;

        case 5: // Move straight backward
            car.setAngle(0);
            changeSpeed(bSpeed);
            break;

        case 6: // Turn left backward
            car.setAngle(blDegrees);
            changeSpeed(bSpeed);
            break;

        case 7: // turn counterclockwise
            car.setAngle(0);
            rotateOnSpot(-90, 80);
            break;

        case 8: // turn left forward
            car.setAngle(flDegrees);
            changeSpeed(fSpeed);
            break;
        case 13: //stop
            car.setAngle(0);
            changeSpeed(0);
            break;

        case 9: // Mindcontrol: move forwards
            changeSpeed(fSpeed);
            break;

        case 10: // Mindcontrol: stop
            changeSpeed(0);
            break;

        default: // Inputs are angles for mindcontrol steering
            car.setAngle(-input);
    }
}

// Motors can't switch direction without delay
// When speed is set to something other than 0;
void changeSpeed(int targetSpeed)
{
    if (currentSpeed == 0){
        car.setSpeed(targetSpeed);
        currentSpeed = targetSpeed;
    } 
    else if (currentSpeed < targetSpeed){ // backward to forward
        car.setSpeed(0);
        delay(100);
        car.setSpeed(targetSpeed);
        currentSpeed = targetSpeed;
    }
    else if (currentSpeed > targetSpeed){ // forward to backward
        car.setSpeed(0);
        delay(100);
        car.setSpeed(targetSpeed);
        currentSpeed = targetSpeed;
    } else {} // targetSpeed == currentSpeed. Do nothing
}
