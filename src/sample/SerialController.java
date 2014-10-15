package sample;

import jssc.*;

/**
 * Created by matthew on 10/8/14.
 */
public class SerialController implements SerialPortEventListener {
    private SerialPort serialPort;  // The serial port we'll use

    private GameCharacter[] players;  // For now, this directly dumps input data into the game character

    private String[] portnames = {"/dev/tty.usbmodemfd121", "/dev/tty.usbmodemfa131"};

    private Main parent;

    public int instruction;

    /*
    Set up the port.
     */
    public void initialize(Main parent) {
        this.parent = parent;

        String[] list = SerialPortList.getPortNames();
        for(String name:list) {
            for(String possibleName:portnames) {
                if(name.equals(possibleName)) {
                    serialPort = new SerialPort(name);
                    break;
                }
            }
            System.out.print(name);
        }

        if(serialPort == null) {
            serialPort = new SerialPort("");
        }

        try {
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);
            serialPort.addEventListener(this);
        }
        catch (SerialPortException ex){
            System.out.println(ex);
        }
    }

    /*
    Close the port when we're done with it.
     */
    public void close() {
        try {
            serialPort.closePort();
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    /*
    This will handle incoming data.
     */
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            if(serialPort.getInputBufferBytesCount() >= 5) {
                for(int i = 0; i < 2; ++i) {
                    byte[] input = serialPort.readBytes(2);
                    double leftBend = (input[0] & 0xFF) / 255.0;
                    double rightBend = (input[1] & 0xFF) / 255.0;

                    if((players != null) && (players[i] != null)) {
                        players[i].setBend(leftBend, rightBend);
                    }

                    if(i == 0) {
                        System.out.println("Input: " + (input[0] & 0xFF) + ", " + (input[1] & 0xFF));
                    }
                }

                byte[] instructions = serialPort.readBytes(1);
                this.instruction = instructions[0] & 0xFF;

                System.out.println("Instruction: " + instructions[0]);

                // Purge the incoming buffer, so we aren't accidentally reading old data
                serialPort.purgePort(SerialPort.PURGE_RXCLEAR);

                // Now, request more data from the Arduino
                serialPort.writeByte((byte) 'x');
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    /*
    Set the game character.
     */
    public void setCharacters(GameCharacter[] players) {
        this.players = players;
    }
}