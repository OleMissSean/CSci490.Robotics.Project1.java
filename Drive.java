package edu.olemiss.robotics;

import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Drive {
	public static void main(String[] args) {
		final double INFINITY = Double.POSITIVE_INFINITY;

		Port port1 = LocalEV3.get().getPort("S1");
		Port port2 = LocalEV3.get().getPort("S2");
		Port port3 = LocalEV3.get().getPort("S3");

		EV3UltrasonicSensor Ultrasonic = new EV3UltrasonicSensor(port1);
		SensorMode distanceProvider = (SensorMode) Ultrasonic.getDistanceMode();
		EV3TouchSensor touch1 = new EV3TouchSensor(port2);
		TouchSensor touch11 = new TouchSensor(touch1);
		EV3TouchSensor touch2 = new EV3TouchSensor(port3);
		TouchSensor touch22 = new TouchSensor(touch2);

		DifferentialPilot pilot = new DifferentialPilot(5.6f, 15.85f, Motor.C,
				Motor.D, true);

		boolean press1;
		boolean press2;

		do {
			press1 = false;
			press2 = false;

			float[] distanceSample = new float[distanceProvider.sampleSize()];

			distanceProvider.fetchSample(distanceSample, 0);

			Ultrasonic.enable();
			if (distanceSample[0] * 100 > 40 && !touch11.isPressed()
					&& !touch22.isPressed()) {
				pilot.backward();
			} else {
				pilot.stop();

				press1 = touch11.isPressed();
				press2 = touch22.isPressed();

				Sound.beep();

				LCD.drawString("TS: " + press1 + "||" + press2, 0, 0);

				pilot.rotate(getRange(distanceProvider, distanceSample,
						INFINITY));
			}
		} while (Button.readButtons() != Button.ID_ESCAPE);
	}

	public static int getRange(SensorMode distanceProv, float[] distanceSample,
			final double INFINITY) {
		LCD.clear();
		float[] Distance = new float[30];
		int[] Angle = { -85, -80, -75, -70, -65, -60, -55, -50, -45, -40, -35,
				-30, -25, -20, -15, -10, -5, 0, 5, 10, 15, 20, 25, 30, 35, 40,
				45, 50, 55, 60, 65, 70, 75, 80, 85 };
		Motor.A.rotate(-75);
		LCD.drawString("       Array:", 0, 0);

		for (int r = 0; r < 30; r++) {
			distanceProv.fetchSample(Distance, r);
			LCD.drawString("" + Distance[r], 0, (r + 1));
			Motor.A.rotate(5);
		}

		Motor.A.rotate(-75);

		int index = max(Distance, INFINITY); // 5 degrees

		LCD.clear();
		LCD.drawString("midMaxIndex" + index + " degrees.", 0, 0);
		LCD.drawString("Rotating: " + Angle[index] + " degrees.", 0, 1);
		return Angle[index]; // choosePath(copyDist);
	}

	public static int max(float[] distanceArray, final double INFINITY) {

		int[] indexArray = new int[distanceArray.length];
		int middle = 0;
		int i = 0;

		LCD.clear();
		LCD.drawString("indexArray:", 0, 0);

		for (int r = 0; r < 30; r++) {
			if (distanceArray[r] == INFINITY) {

				indexArray[i] = r;
				i++;
				LCD.drawString("" + indexArray[i], 0, (i + 1));
			}

			middle = i / 2;

		}
		return indexArray[middle];
	}
}
