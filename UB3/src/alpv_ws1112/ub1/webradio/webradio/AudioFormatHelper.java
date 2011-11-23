package alpv_ws1112.ub1.webradio.webradio;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class AudioFormatHelper {
	private static String format_header = "frmt:";
	private static String byte_header = "byte:";
	private static char header_sign = ':';
	private static char delimiter = ',';
	
	public static AudioFormat stringToAudioFormat(String input) {
		if (!input.startsWith(format_header))
			return null;

		String sub = input.substring(input.indexOf(header_sign) + 1);
		String[] split = sub.split("" + delimiter);
		if(split.length != 5)
			System.err.println("Corrupt AudioFormat string");
		
		float sampleRate = Float.parseFloat(split[0]);
		int sampleSize = Integer.parseInt(split[1]);
		int channels = Integer.parseInt(split[2]);
		boolean signed = Boolean.parseBoolean(split[3]);
		boolean bigEndian = Boolean.parseBoolean(split[4]);
		
		return new AudioFormat(sampleRate, sampleSize, channels, signed, bigEndian);
	}

	public static String audioFormatToString(AudioFormat input) {
		/* AudioFormat data:
		 * SampleRate 		float
		 * sampleSize		int
		 * channels			int
		 * signed			boolean
		 * bigEndian		boolean
		 */
		String str_samplerate = String.valueOf(input.getSampleRate());
		String str_samplesize = String.valueOf(input.getSampleSizeInBits());
		String str_channels = String.valueOf(input.getChannels());
		boolean isSigned = (input.getEncoding() == AudioFormat.Encoding.PCM_SIGNED);
		String str_signed = String.valueOf(isSigned);
		String str_bigendian = String.valueOf(input.isBigEndian());
		String out = format_header + str_samplerate + delimiter + str_samplesize + delimiter +
				str_channels + delimiter + str_signed + delimiter + str_bigendian;
		return out;
	}
	
	public static byte[] stringToBytes(String input) {
		if (!input.startsWith(byte_header))
			return null;
		String sub = input.substring(input.indexOf(header_sign) + 1);
		byte[] out = sub.getBytes();
		
//		for(int i=0; i < out.length; i++)
//			if(out[i] == 0x000A)
//				System.err.println("Warning byte Newline " + i);
	
		return out;
	}

	public static String bytesToString(byte[] input) {
		String out = byte_header;
		String sub = new String(input);
		out = out.concat(sub);
		out = out.replace('\n', (char)0x001E);
		out = out.replace('\r', (char)0x001E);
		return out;
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("real data - statix.wav");
		AudioInputStream ais = null;
		try {
			ais = AudioSystem.getAudioInputStream(new File("staticx.wav"));
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// get Forma
		AudioFormat format = ais.getFormat();
		System.out.println("In:");
		System.out.println(format.toString());
		String fmInn = audioFormatToString(format);
		System.out.println(fmInn);
		System.out.println("Out:");
		AudioFormat fmOutt = stringToAudioFormat(fmInn);
		System.out.println(fmOutt.toString());
		System.out.println(audioFormatToString(fmOutt));
		
		// test audioformat tubing
		System.out.println("fake data");
		AudioFormat fmIn = new AudioFormat(0.1443f, 2, 3, true, false);
		String temp = audioFormatToString(fmIn);
		AudioFormat fmOut = stringToAudioFormat(temp);
		String test = audioFormatToString(fmOut);

		System.out.println("In:");
		System.out.println(fmIn);
		System.out.println(temp);
		System.out.println("Out:");
		System.out.println(fmOut);
		System.out.println(test);
		
		System.out.println();
		// test bytes
		byte[] bIn = new byte[32];
		bIn[14] = 0x000A;
		bIn[17] = 0x0045;
		String temps = bytesToString(bIn);
		byte[] bOut = stringToBytes(temps);
		
		System.out.println("In:");
		for(int i=0; i < bIn.length; i++)
			System.out.print(bIn[i]);
		System.out.println();

		System.out.println("Out:");
		for(int i=0; i < bOut.length; i++)
			System.out.print(bOut[i]);
		System.out.println();
	}

}
