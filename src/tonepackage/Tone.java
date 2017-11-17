package tonepackage;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Tone {

	/**
	 * The method that creates the list for the bell notes, and the method that reads the file
	 */
	private static List<BellNote> loadNotes(String song) {
		final List<BellNote> notes = new ArrayList<>(); 
		final File file = new File(song); 
		if (file.exists()) { //checks to see if there is a file
			try (FileReader fileReader = new FileReader(file); //try/catch to surround the fileReader
					BufferedReader br = new BufferedReader(fileReader)) {
				String line = null; //declares and initializes the string 'line'
				while ((line = br.readLine()) != null) { //while loop starts to read file as long as the line is not null/empty
					BellNote n = parseBellNote(line); //Calls the method that parses the BellNotes so that they are readable
					if (n != null) { //as long as the BellNote isn't null, it adds the notes to the song
						notes.add(n);
					} else {
						System.err.println("Error: Invalid note '" + line + "'"); //if the bell note is unreadable 
					}
				}
			} catch (IOException ignored) {} //catch for the fileReader
		} else {
			System.err.println("File '" + song + "' not found"); //if there is no file
		}
		return notes; //returns notes so that song can be played
	} 
	
	private static BellNote parseBellNote(String line) { //method to parse the notes/split the components up to be readable
		String[] fields = line.split("\\s+"); //splits the line up when there is one or many white spaces
		if (fields.length == 2) { //if the field has 2 items/1 split in it 
			return new BellNote(parseNote(fields[0]), parseNoteLength(fields[1])); //returns the BellNotes after parsing the two fields
		}
		return null;
	}
	
	/**
	 * method that parses the Notes
	 * @param bunny
	 * @return
	 */
	private static Note parseNote(String bunny) { 
		switch (bunny.toUpperCase()) { 
		case "REST": 
			return Note.REST;
		case "A4": 
			return Note.A4; 
		case "A4S": 
			return Note.A4S; 
		case "A5": 
			return Note.A5; 
		case "B4": 
			return Note.B4; 
		case "C4": 
			return Note.C4; 
		case "C4S": 
			return Note.C4S; 
		case "D4": 
			return Note.D4; 
		case "D4S": 
			return Note.D4S; 
		case "E4": 
			return Note.E4;
		case "F4": 
			return Note.F4; 
		case "F4S": 
			return Note.F4S; 
		case "G4": 
			return Note.G4; 
		case "G4S": 
			return Note.G4S;  
		case "G3": 
			return Note.G3; 
		case "F3S": 
			return Note.F3S; 
		case "F3": 
			return Note.F3; 
		case "A3": 
			return Note.A3;
		default: 
			return Note.INVALID;  //if the note does not match any of the above it returns and invalid note
		}
	}
	
	/**
	 * parses the notelength handed to it
	 * @param rabbit
	 * @return
	 */
	private static NoteLength parseNoteLength(String rabbit) {
		switch (rabbit.toLowerCase()) {
		case "4": 
			return NoteLength.QUARTER; 
		case "2": 
			return NoteLength.HALF; 
		case "8": 
			return NoteLength.EIGTH; 
		case "1": 
			return NoteLength.WHOLE;
		default: 
			return NoteLength.INVALID; 			
		}
	}

	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception { 
		NoteLength ln = null; 
		Note no = null; 
		final AudioFormat af =
				new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false); 
		Tone t = new Tone(af); 
		final List<BellNote> sun = loadNotes(args[0]); 
		if (!validateData(sun, ln, no)) { 
			System.err.println("Failed");
			System.exit(-1);
		}t.playSong(sun); 
	}		

	/**
	 * validate data method
	 * @param sun
	 * @param ln
	 * @param no
	 * @return
	 */
	private static boolean validateData(List<BellNote> sun, NoteLength ln, Note no) { //validation method to check data
		boolean success = true;  
		for(BellNote s : sun ) { 
			if (s.note == Note.INVALID) { 
				System.err.println("Invalid Note"); 
				success = false; 
			}
			if (s.length == NoteLength.INVALID) { 
				System.err.println("Note length invalid"); 
				success = false;
			}
		}
		return success;
	}

	private final AudioFormat af;

	Tone(AudioFormat af) {
		this.af = af;
	}

	/**
	 * 
	 * @param song
	 * @throws LineUnavailableException
	 */
	void playSong(List<BellNote> song) throws LineUnavailableException { //method to play song
		try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) { 
			line.open(); 
			line.start();
			for (BellNote bn: song) { //as long as there are lines being handed, play song
				playNote(line, bn); //calls method that plays the note
			}
			line.drain();
		}
	}

	/**
	 * 
	 * @param line
	 * @param bn
	 */
	void playNote(SourceDataLine line, BellNote bn) { //method to go through and play each note
		final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000); 
		final int length = Note.SAMPLE_RATE * ms / 1000;
		line.write(bn.note.sample(), 0, length);
		line.write(Note.REST.sample(), 0, 50);
	}
}


class BellNote { 
	final Note note; //declares note
	final NoteLength length; //declares length

	BellNote(Note note, NoteLength length) { //method BellNote
		this.note = note; //in this instance, note = note
		this.length = length; //in this instance, length = length
	}
}

enum NoteLength { //declares each of the lengths for the NoteLength
	INVALID(0),
	WHOLE(1.0f),
	HALF(0.5f),
	QUARTER(0.25f),
	EIGTH(0.125f);

	private final int timeMs; //declares timeMs

	private NoteLength(float length) { //creates each of the NoteLengths
		timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
	}

	public int timeMs() { 
		return timeMs; //returns the timeMs that has been calculated for the noteLengths
	}
}

enum Note { 
	REST,
	A3,
	F3,
	F3S,
	G3,
	A4,
	A4S,
	B4,
	C4,
	C4S,
	D4,
	D4S,
	E4,
	F4,
	F4S,
	G4,
	G4S,
	A5,
	INVALID;

	public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
	public static final int MEASURE_LENGTH_SEC = 1;

	// Circumference of a circle divided by # of samples
	private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

	private final double FREQUENCY_A_HZ = 440.0d;
	private final double MAX_VOLUME = 127.0d;

	private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

	private Note() {
		int n = this.ordinal();
		if (n > 0) {
			// Calculate the frequency!
			final double halfStepUpFromA = n - 1;
			final double exp = halfStepUpFromA / 12.0d;
			final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

			// Create sinusoidal data sample for the desired frequency
			final double sinStep = freq * step_alpha;
			for (int i = 0; i < sinSample.length; i++) {
				sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
			}
		}
	}

	public byte[] sample() {
		return sinSample;
	}
}