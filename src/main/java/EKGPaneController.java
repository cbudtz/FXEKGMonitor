import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Polyline;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class EKGPaneController {
    @FXML
    public Polyline polyLine;
    @FXML
    public TextField textField;
    public AnchorPane anchorPane;
    double position = 0;

    @FXML
    public void recordEKG(MouseEvent mouseEvent) {
        //noinspection Convert2Lambda
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
                    serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    while (true){
                        double scalefactor = anchorPane.getHeight() * (1.0/2000);
                        System.out.println(scalefactor);
                        try {
                            String serialString = serialPort.readString();
                            if (null==serialString) continue;
                            serialString = serialString.replace("\"", "");
                            String[] split = serialString.split("\\r?\\n");
                            for (String s : split){
                                if (s==null || "".equals(s))continue;
                                try  {
                                    double mv = Double.parseDouble(s);
                                    if (mv>100){ //Discard half-readings
                                        mv = (anchorPane.getHeight() - mv) * scalefactor + anchorPane.getHeight()/2;
                                        polyLine.getPoints().addAll(position++, mv); //
                                    }
                                } catch (NumberFormatException e){
                                    System.out.println("Error on " + s);
                                }

                                if (position >anchorPane.getWidth()){
                                    position=0;
                                    polyLine.getPoints().clear();
                                }
                            }

                            //textArea.setText(textArea.getText() + serialString);


                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (SerialPortException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        serialPort.closePort();
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }


            }
        }
        ).start();


    }
}
