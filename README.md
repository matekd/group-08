# Mini project: NeuroDrive
## Table of contents
1. [What does this repository contain?](#what)
2. [Why did we develop this?](#why)
3. [How did we reach these results?](#how)
4. [Resources we used](#resources)
5. [Application user manual](#application-user-manual)
6. [Hardware user manual](#hardware-user-manual)
7. [Team members](#team-members)
## What?
A software that allows an end-user to remotely control a smart-car in a less traditional way, with the use of an EEG-headset and a mobile app. Moreover, the smart-car can protect itself to prevent for example hitting an obstacle.
## Why?
The main objective of this software is to utilize and demonstrate the use and application of EEG (Electroencephalography) readings to IoT items. By exploring this domain we hope to demonstrate ways using software engineering to develop for people with different levels of mobility.
## How?
The EEG-chip within the “Force Trainer II Bluetooth Headset” measures brain activity in Hz, and depending on users’ mental state e.g. stressed or relaxed, the headset will pick up frequencies within different Hz intervals. With the data retrieved from the headset, we will be able to control the forward throttle of the car, while being able to steer right and left via an app.  
The smart-car will also be able to prevent itself from colliding with obstacles by using ultrasonic sensors. Once the ultrasonic wave reaches the wall or obstacle it bounces back and the displacement between the car and the wall is calculated, and once it is too short the car will be locked in that direction.
## Resources
| Hardware | Software |
| --- | --- |
| 1x Smartcar | Smart Car shield library |
| 8x AA rechargeable batteries | Java |
| 2x SR04 Sensors | C++ |
| 1x MicroUSB cable | Travis CI |
| [Bluetooth EEG headset](https://estore.nu/sv/star-wars/5028-star-wars-force-trainer-ii-8001444158953.html?SubmitCurrency=1&id_currency=1&gclid=EAIaIQobChMIoN7K4YrC6AIV2OeaCh3drQbnEAQYASABEgJZZfD_BwE) | [Neurosky android developer tools](https://store.neurosky.com/products/android-developer-tools-4) |
| Android OS with version Lollipop(5.0) or higher | |
## Application user manual
![eeg-view](https://github.com/DIT112-V20/group-08/blob/documentation/readme-images/eeg-view.png)
![joystick-view](https://github.com/DIT112-V20/group-08/blob/documentation/readme-images/joystick-view.png)
### Application header
1. The application UI indicates to the user that external hardware can be connected to via Bluetooth.
2. If tapped, the ‘Switch’ button will switch the contents of the UI screen from EEG reading smart car control, to manual smart car control.
3. The github button takes any curious app users to the project repository.
### Mind control view
4. The application user can enable and disable EEG readings from the headset.
### Manual control view
5. In order to return to the previous UI screen content, the app end-user can tap the ‘Switch’ button again.
6. The app end-user can control the smart car manually with the help of a joystick. This is done by pressing down on the screen within the borders of the joystick trackpad, while moving their finger in the displayed directions.
### Visual EEG readings
![eeg-visual](https://github.com/DIT112-V20/group-08/blob/documentation/readme-images/eeganimation.gif)
The application will start to display the levels of EEG received by the end-user wearing the headset while EEG readings are enabled. This is visualized both with a text displaying the exact level of the reading, as well as an animation that changes when the reading goes up or down.
## Hardware user manual
## Team members
- Liv Alterskjaer, gusalteli
- Annan Lao, guslaoan
- Zhijie Wei, gusweizh
- Douglas Johansson, gusjohdo
- Mattias Ekdahl, gusekdmad
- Simon Engström, gussimen
