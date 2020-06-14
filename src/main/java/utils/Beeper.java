package utils;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Beeper {
	
	private static final byte[] BYTE_BUFFER = new byte[1];
	
	public static void beep(int msecs) throws LineUnavailableException {
		beep(8000, 1100, msecs, 1.0);
	}
	
	public static void beep(int hz, int msecs) throws LineUnavailableException {
		beep(8000, hz, msecs, 1.0);
	}
	
	public static void beep(int hz, int msecs, double vol) throws LineUnavailableException {
		beep(8000, hz, msecs, vol);
	}
	
	public static void beep(float sampleRate, int hz, int msecs) throws LineUnavailableException {
		beep(sampleRate, hz, msecs, 1.0);
	}
	
	public static void beep(float sampleRate, int hz, int msecs, double vol) throws LineUnavailableException {
		AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);	  
		SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
		sdl.open(af);
		sdl.start();
		for (int i=0; i < msecs*8; i++) {
			double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
			BYTE_BUFFER[0] = (byte)(Math.sin(angle) * 127.0 * vol);
			sdl.write(BYTE_BUFFER,0,1);
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}
}
