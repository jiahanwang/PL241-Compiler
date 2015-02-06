package frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {

    private char sym;

    private BufferedReader reader;

    public FileReader(String path) throws IOException {
        this.reader = Files.newBufferedReader(Paths.get(path), Charset.forName("UTF-8"));
    }

    //return current and advance to the next character on the input
    public char getSym() throws IOException{
        try {
            this.sym = (char)reader.read();
        }
        catch (IOException e) {
            throw new IOException("IOException: frontend.FileReader encountered an I/O " +
                    "exception when advancing to the next symbol.");
        }
        return this.sym;
    }

}