public class FileReader {

    public char GetSym(); //return current and advance to the next character on the input

    public void Error(String errorMsg); //signal an error message

    // constructor: open file
    public FileReader(String fileName) {

    }
}