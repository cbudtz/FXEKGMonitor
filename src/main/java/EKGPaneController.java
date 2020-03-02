import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polyline;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class EKGPaneController {
    @FXML
    public Polyline polyLine;
    @FXML
    public TextField textField;
    double position = 0;

    @FXML
    public void recordEKG(MouseEvent mouseEvent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] portNames = SerialPortList.getPortNames();
                for (String name : portNames){
                    System.out.println("Port found: " + name);
                    textField.setText(textField.getText() + "Port found: " +name + "\n");
                }
                SerialPort serialPort = new SerialPort(portNames[0]);
                System.out.println("Opening Port");
                try {
                    serialPort.openPort();
                   // serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
                System.out.println("Reading from port...");

                while (true){
                    try {
                        String serialString = serialPort.readString().replace("\"", "");
                        String[] split = serialString.split("\\r?\\n");
                        for (String s : split){
                            System.out.println("String: " + s);
                            if (s==null || "" == s)continue;
                            try  {
                                double mv = Double.parseDouble(s);
                                if (mv>100){ //Discard half-readings
                                mv = (640 - mv)/4 + 300;
                                    polyLine.getPoints().addAll(position++, mv); //
                                }
                            } catch (NumberFormatException e){
                                System.out.println("Error on " + s);
                            }

                            if (position >640){
                                position=0;
                                polyLine.getPoints().clear();
                            }
                        }

                        //textArea.setText(textArea.getText() + serialString);


                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            }
        }
        ).start();

    }
}
