package ekg;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Polyline;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import uk.me.berndporr.iirj.Butterworth;

import java.util.LinkedList;
import java.util.List;

public class EKGPaneController {
    @FXML
    public Polyline polyLine;
    @FXML
    public TextField textField;
    public AnchorPane anchorPane;
    double position = 0;
    double average = 0;
    double lastBeat = System.currentTimeMillis();

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
                                        polyLine.getPoints().addAll(position++, mv);


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

    private void smoothPolyline(Polyline polyLine) {
        ObservableList<Double> points = polyLine.getPoints();
        List<Double> ys = new LinkedList<>();
        for (int i = 1; i < points.size(); i+=2) {
            ys.add(points.get(i));
        }
        Butterworth butterWorth = new Butterworth();
        butterWorth.bandStop(4,150,50,10);
        for (int i = 0; i < ys.size(); i++) {
            Double v = ys.get(i);
            v = butterWorth.filter(v);
            ys.add(v);
        }
        for (int i = 1; i < points.size(); i+=2) {
            points.set(i,ys.get(i-1)/2);
        }


    }
}
