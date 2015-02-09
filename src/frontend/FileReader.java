package frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {

    private char sym;
    private int lineNum;

    private LineNumberReader reader;

    public FileReader(String path) throws IOException {
        this.reader = new LineNumberReader(Files.newBufferedReader(Paths.get(path), Charset.forName("UTF-8")));
        lineNum = 1;
    }

    //return current and advance to the next character on the input
    public char getSym() throws Exception {
        try {
            this.sym = (char)reader.read();
            if(this.sym == '\r' || this.sym == '\n') { lineNum++; }
        }
        catch (Exception e) {
            throw new Exception("IOException: frontend.FileReader encountered an I/O " +
                    "exception when advancing to the next symbol on line "+lineNum);
        }
        return this.sym;
    }

    public void nextLine() throws IOException {
        String s = reader.readLine();
    }

    public int getLineNumber() {
        return reader.getLineNumber() + 1;
    }

}