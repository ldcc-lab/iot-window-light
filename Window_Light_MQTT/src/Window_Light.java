import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import comus.wp.onem2m.common.enums.M2MCmdType;
import comus.wp.onem2m.common.enums.M2MExecModeType;
import comus.wp.onem2m.common.vo.ln.AnyArgType;
import comus.wp.onem2m.iwf.common.M2MException;
import comus.wp.onem2m.iwf.nch.NotifyResponse;
import comus.wp.onem2m.iwf.run.CmdListener;
import comus.wp.onem2m.iwf.run.IWF;

public class Window_Light {

	public static void main(String[] args) throws Exception {

		System.out.println("***** SYSTEM started *****");

		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalInput window_pinIn1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);

		//final GpioPinDigitalOutput window_pinOut1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "LED", PinState.LOW); pinstate체크를 위한 발광다이오
		final GpioPinDigitalOutput light_pinOut1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "POWER", PinState.LOW);


		//pinIn1.setShutdownOptions(true);

		final IWF device = new IWF("44444.4444.RP09");

		device.register();
		
		
		
		System.out.println("initial : " + "window1" + " = " + window_pinIn1.getState() + ": 0 = Closed , 1 = Opened");
/*
		if (window_pinIn1.getState().isHigh())
			window_pinOut1.low();
		else if (window_pinIn1.getState().isLow())
			window_pinOut1.high();
*/
		window_pinIn1.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {

				System.out.print("window1 = " + arg0.getState() + " ");

				if (arg0.getState() == PinState.LOW) {
					//window_pinOut1.low();
					System.out.println("***** Window1 Close *****");
					device.putContent("window1", "0");

				} else if (arg0.getState() == PinState.HIGH) {
					//window_pinOut1.high();
					System.out.println("***** Window1 Open *****");
					device.putContent("window1", "1");
				} else {
					System.out.println("!!!!! Error :  Floating State !!!!!");
				}
			}
		});
 
    //전등제어 부분
		AnyArgType command = new AnyArgType();   //명령어 속성
		command.setName("switch");
		command.setValue("initial_value");
		try {
			device.putControl("control", M2MCmdType.DOWNLOAD, M2MExecModeType.IMMEDIATEONCE, command);
		} catch (M2MException e) {
			e.printStackTrace();
		}
		
		device.addCmdListener(new CmdListener() {
			String power;

			@Override
			public void excute(Map<String, String> cmd, NotifyResponse response) {
				try {
					power = cmd.get("switch");
					System.out.println("switch : " + power);

				} catch (Exception e) {
					e.printStackTrace();
				}

				if ("OFF".equals(power)) {
					light_pinOut1.high();
				} else if ("ON".equals(power)) {
					light_pinOut1.low();
				}
			}
		});

		
		
	}
	
	

}