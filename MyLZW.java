public class MyLZW {

	private static final int R = 256; // number of input chars
	private static int L = 512; // number of codewords = 2^W
	private static int W = 9; // codeword width
	private static char mode;

	public static void compress() {
		double size_uncompData = 0;
		double size_compData = 0;
		double prevRatio = 0;
		double currRatio = 0;

		String input = BinaryStdIn.readString();
		TST<Integer> st = new TST<Integer>();
		for (int i = 0; i < R; i++)
			st.put("" + (char) i, i);
		int code = R + 1; // R is codeword for EOF

		BinaryStdOut.write(mode);

		while (input.length() > 0) {
			String s = st.longestPrefixOf(input); // Find max prefix match s.
			BinaryStdOut.write(st.get(s), W); // Print s's encoding.
			int t = s.length();

			size_uncompData += t * 8;
			size_compData += W;

			if (t < input.length() && code < L) {
				// Add s to symbol table.
				st.put(input.substring(0, t + 1), code++);
				prevRatio = size_uncompData / size_compData;
			} else if (t < input.length() && code >= L && W < 16) {
				W++;
				L = (int) Math.pow(2, W);
				st.put(input.substring(0, t + 1), code++);
			} else if (t < input.length() && code == L && W == 16) {
				if (mode == 'r') {
					// Reset mode
					st = new TST<Integer>();
					for (int i = 0; i < R; i++)
						st.put("" + (char) i, i);
					L = 512;
					W = 9;
					code = R + 1;
					st.put(input.substring(0, t + 1), code++);
				} else if (mode == 'm') {
					// Monitor mode
					currRatio = size_uncompData / size_compData;
					double compRatio = prevRatio / currRatio; // Threshold for
																// resetting =
															// 1.1
					if (compRatio > 1.1) {
						st = new TST<Integer>();
						for (int i = 0; i < R; i++)
							st.put("" + (char) i, i);
						code = R + 1; // R is codeword for EOF
						L = 512;
						W = 9;
						st.put(input.substring(0, t + 1), code++);
					}

				} else if (mode == 'n') {
					// Do nothing mode
				}
			}
			input = input.substring(t); // Scan past s in input.
		}
		BinaryStdOut.write(R, W);
		BinaryStdOut.close();
	}

	public static void expand() {
		double size_uncompData = 0;
		double size_compData = 0;
		double prevRatio = 0;
		double currRatio = 0;
		char mode;

		String[] st = new String[65536];
		int j;
		// initialize symbol table with all 1-character strings
		for (j = 0; j < R; j++)
			st[j] = "" + (char) j;
		st[j++] = ""; // (unused) lookahead for EOF

		int i = R + 1; // next available codeword value

		mode = BinaryStdIn.readChar();

		int codeword = BinaryStdIn.readInt(W);

		if (codeword == R)
			return; // expanded message is empty string
		String val = st[codeword];

		if (mode != 'n' && mode != 'r' && mode != 'm') {
			System.err.println("This file wasn't compressed using the same algorithm, hence no mode found to expand.");
			throw new IllegalArgumentException("Cannot Expand the file");
		} else {
			while (true) {
				size_uncompData += (val.length() * 8);
				size_compData += W;
				BinaryStdOut.write(val);
				if (i >= L && W < 16) {
					W++;
					L = (int) Math.pow(2, W);
					prevRatio = size_uncompData / size_compData;
				} else if (i >= L && W == 16) {
					if (mode == 'r') {

						st = new String[65536];
						int x; // next available codeword value
						// initialize symbol table with all 1-character strings

						for (x = 0; x < R; x++)
							st[x] = "" + (char) x;
						st[x++] = ""; // (unused) lookahead for EOF

						W = 9;
						L = 512;
						i = R + 1;
					} else if (mode == 'm') {

						currRatio = size_uncompData / size_compData;

						double compRatio = prevRatio / currRatio; // Threshold
																	// for
																	// resetting
																	// = 1.1

						if (compRatio > 1.1) {
							st = new String[65536];
							int x; // next available codeword value

							// initialize symbol table with all 1-character
							// strings
							for (x = 0; x < R; x++)
								st[x] = "" + (char) x;
							st[x++] = ""; // (unused) lookahead for EOF
							W = 9;
							L = 512;
							i = R + 1;

							prevRatio = 0;
						}
					} else if (mode == 'n') {
						// Do nothing mode
					}
				}

				codeword = BinaryStdIn.readInt(W);

				if (codeword == R)
					break;

				String s = st[codeword];

				if (i == codeword)
					s = val + val.charAt(0); // special case hack

				if (i < L) {
					st[i++] = val + s.charAt(0);
					prevRatio = size_uncompData / size_compData;
				}
				val = s;
			}
		}
		BinaryStdOut.close();
	}

	public static void main(String[] args) {
		if (args[0].equals("-")) {
			if (args[1].equals("n") || args[1].equals("m") || args[1].equals("r")) {
				mode = args[1].charAt(0);
			} else {
				System.err.println("Incorrect mode entered. Defaulting to [n]ormal mode.");
				mode = 'n';
			}
			compress();

		} else if (args[0].equals("+")) {
			expand();
		} else {
			throw new IllegalArgumentException("Illegal command line argument");
		}
	}

}
