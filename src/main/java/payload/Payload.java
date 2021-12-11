package payload;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

public class Payload implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6447999399589134690L;

	static {
		System.out.println("payload static");
	}

	public Payload() {
		
	}

	@Override
	public String toString() {
		long pid = ProcessHandle.current().pid();
		
		System.out.println("Payload toString sysout PID=" + pid);
		
		try (InputStreamReader in = new InputStreamReader(Runtime.getRuntime().exec(new String[]{"ps"}).getInputStream());
				BufferedReader br = new BufferedReader(in);) {
			String s = null;
			while ((s = br.readLine()) != null) {
				if (s.contains("" + pid)) {
					System.out.println(s);					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Payload toString";
	}
}
