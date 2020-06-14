package utils;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WavPlayer {
	
	private final Clip clip;
	private final File alertSoundFile;
	
	public WavPlayer(File alertSoundFile) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		this.clip = AudioSystem.getClip();
		this.alertSoundFile = alertSoundFile;
		clip.addLineListener(new LineListener() {
			@Override
			public void update(LineEvent event) {
				if (event.getType() == LineEvent.Type.STOP)
					clip.close();
			}
		});
	}
	
	public void play() {
		try {
			clip.open(AudioSystem.getAudioInputStream(alertSoundFile));
			clip.start();
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {}
	}
	
}
