import java.io.File;
import javax.sound.sampled.*;

public class Playback {

	public static void main(String[] args) {
		try {
			// Open stream
			final String soundfile = "staticx.wav";
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
					soundfile));
			AudioFormat format = ais.getFormat();
			// Open line
			SourceDataLine line = AudioSystem.getSourceDataLine(format);
			line.open();			
			line.start();
			// Push data from stream into line
			final int buffLen = 1024;
			byte[] data = new byte[buffLen];
			while(ais.read(data) != -1)
				line.write(data , 0, data.length);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}