package sample.controllers;

import com.goebl.david.Webb;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FourthClassController implements Initializable {
    public HBox logo_pane, center_pane;
    public HBox rfid_image_view;
    public VBox mother_paneV;
    @FXML
    private VBox new_flag_view;
    @FXML
    private Label new_flag_reason;
    public Circle logo1, army_logo;
    public MenuBar menu_bar;
    public Button scan_button;
    public TextField input;
    public Label level_field, full_name, mat_Onj, dept_obj, return_msg, sign_in_time, sign_out_time, full_name1,
            sign_out_label, sign_in_label, designation_field;
    public Circle Circle_img;
    final LoginController loginController = new LoginController();
    final String ip_address = loginController.IPAddress;
    public SerialPort arduinoPort;

    public FourthClassController() throws IOException {
    }

    public void StopScan() {
        System.out.println("Stopping Scan..");
        mother_paneV.getChildren().set(3, center_pane);
        loginController.disconnectArduino(arduinoPort);
        scan_button.setDisable(false);
        rfid_image_view.setVisible(false);
    }

    public void RaiseAlarm() {
    }

    public void logout() {
        loginController.logOutdata(mother_paneV);
    }

    public void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();

    }

    public void AboutPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("about_page.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            Stage stage = new Stage();
            stage.setTitle("NIGERIAN ARMY UNIVERSITY BIU");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    public void scan() {


//        loginController.connectArduino(ip_address, mother_pane, full_name, level_field, mat_Onj, dept_obj, Circle_img,
//                new_flag_view, new_flag_reason, scan_button, arduinoPort);
        System.out.println("connect Arduino");
        boolean success = false;
        int PortNumber = 1;
        while (PortNumber <= 30) {
            String portName = "COM" + PortNumber;
            try{
                SerialPort serialPort = new SerialPort(portName);
                System.out.println("trying" + portName);
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                    if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 10){
                        try {
                            String st = serialPort.readString(serialPortEvent.getEventValue());
                            String st7 = serialPort.readString(serialPortEvent.getEventValue());
                            String st9 = st.replace("\n", "");
                            System.out.println("Card number: " + st9);

                            //make database request, validate result and return either error page or profile page
                            Platform.runLater(() ->{
                                //get student data with token
                                Webb webb = Webb.create();
                                JSONObject response00 = webb.post("http://" +ip_address + "/scan-profile/")
                                        .param("rfid_code",st9)
                                        .param("api_request", true)
                                        .ensureSuccess()
                                        .connectTimeout(10000)
                                        .retry(0,false)
                                        .asJsonObject()
                                        .getBody();

                                try {
                                    int status_code = response00.getInt("status_code");
                                    if (status_code == 0){
                                        System.out.println("card doesnt exist");
                                        try {
                                            Parent content = FXMLLoader.load(getClass().getResource("card_not_registered_error.fxml"));
                                            mother_paneV.getChildren().set(3, content);
                                        } catch (IOException E) {
                                            E.printStackTrace();
                                        }
                                    }else{
                                        boolean is_staff = response00.getBoolean("is_staff");
                                        if (is_staff){
                                            System.out.println("this s staff");
                                            System.out.println(response00.getJSONArray("student").get(0));
                                            JSONObject jsonArray1 = (JSONObject) response00.getJSONArray("student").get(0);
                                            String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                                            String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                                            String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                                            String department = jsonArray1.getJSONObject("fields").get("department").toString();
                                            String staff_id = jsonArray1.getJSONObject("fields").get("staff_id_number").toString();
                                            String designation = jsonArray1.getJSONObject("fields").get("designation").toString();
                                            String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                                            Image image1 = new Image("http://" + ip_address +"/media/" +image_url, 250, 250, true, true);

                                            FXMLLoader loader = new FXMLLoader();
                                            loader.setLocation(FourthClassController.class.getResource("invigilator_profile.fxml"));
                                            loader.setController(this);
                                            System.out.println("collected ui successfully");
                                            mother_paneV.getChildren().set(3, loader.load());

                                            full_name1.setText(first_name + " " + surname + " " + other_name );
                                            designation_field.setText(designation);
                                            mat_Onj.setText(staff_id);
                                            dept_obj.setText(department);
                                            Circle_img.setFill(new ImagePattern(image1));
                                            return_msg.setText("STAFF");


                                        }else {
                                            System.out.println("this is student");
                                            System.out.println(response00.getJSONArray("student").get(0));
                                            JSONObject jsonArray1 = (JSONObject) response00.getJSONArray("student").get(0);
                                            String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                                            String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                                            String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                                            String department = jsonArray1.getJSONObject("fields").get("department").toString();
                                            String matric_no = jsonArray1.getJSONObject("fields").get("matric_number").toString();
                                            String level = jsonArray1.getJSONObject("fields").get("level").toString();
                                            String flag = jsonArray1.getJSONObject("fields").get("is_flaged").toString();
                                            System.out.println(flag);
                                            String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                                            Image image1 = new Image("http://" + ip_address +"/media/" +image_url, 250, 250, true, true);

                                            FXMLLoader loader = new FXMLLoader();
                                            loader.setLocation(FourthClassController.class.getResource("profile_page.fxml"));
                                            loader.setController(this);
                                            System.out.println("collected ui successfully");
                                            mother_paneV.getChildren().set(3, loader.load());

                                            if (flag.equals("null")) {
                                                new_flag_view.setVisible(false);
                                                System.out.println("flag is null");
                                            } else {
                                                new_flag_view.setVisible(true);
                                                new_flag_reason.setText(flag);
                                            }
                                            full_name.setText(first_name + " " + surname + " " + other_name );
                                            level_field.setText(level);
                                            mat_Onj.setText(matric_no);
                                            dept_obj.setText(department);
                                            Circle_img.setFill(new ImagePattern(image1));
                                        }
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            System.out.println("End of connection.");
                        } catch (SerialPortException ex) {
                            Logger.getLogger(FourthClassController.class.getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                });

                arduinoPort = serialPort;
                success = true;
                mother_paneV.getChildren().set(3, center_pane);
                rfid_image_view.setVisible(true);
                scan_button.setDisable(true);
                System.out.println("Connected to port" + portName);

                break;
            } catch (SerialPortException ex) {
                System.out.println("Not connected");
                PortNumber = PortNumber + 1;
                System.out.println(PortNumber);
            }
        }
        if(!success){
            showAlert("please connect Hardware");
        }
    }

    public void Search() {
        String matric_number = input.getText().toLowerCase();
        if (input.getText().isEmpty()) {
            showAlert("Please Enter ID");
        }
        //test if student data exist
        Platform.runLater(() ->{
            //get student data with token
            Webb webb = Webb.create();

            //String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            String path = System.getProperty("user.dir");
            String decodedPath = "";
            try {
                decodedPath = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(decodedPath);

            String new_path2 = decodedPath + "\\token.txt";
            String new_path1 = new_path2.substring(0);
            String token = null;
            try {
                Stream<String> lines = Files.lines(Paths.get(new_path1));
                token = lines.collect(Collectors.joining(System.lineSeparator()));
                System.out.println(token);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(token);

            JSONObject response4 = webb.get("http://" +ip_address + "/graphql?query={student(matricNumber:\"" +matric_number+"\")" +
                    "{firstName,surname,otherName,department,level,photo,isFlaged}}")
                    .header("Authorization", "JWT " + token)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();

            try {
                System.out.println(response4.getJSONObject("data"));
                String data = response4.getJSONObject("data").get("student").toString();

                if (data.equals("null")){
                    System.out.println("Display Error Page");
                    try {
                        Parent content = FXMLLoader.load(FourthClassController.class.getResource("no_student_error.fxml"));
                        mother_paneV.getChildren().set(3, content);
                       // showAlert("loaded error page");
                    } catch (IOException E) {
                        E.printStackTrace();
                    }
                }else {
                    String first_name = response4.getJSONObject("data").getJSONObject("student").get("firstName").toString();
                    String surname = response4.getJSONObject("data").getJSONObject("student").get("surname").toString();
                    String other_name = response4.getJSONObject("data").getJSONObject("student").get("otherName").toString();
                    String department = (String) response4.getJSONObject("data").getJSONObject("student").get("department");
                    String level = response4.getJSONObject("data").getJSONObject("student").get("level").toString();
                    String flag = response4.getJSONObject("data").getJSONObject("student").get("isFlaged").toString();
                    System.out.println(flag);
                    String image_url = response4.getJSONObject("data").getJSONObject("student").getString("photo");
                    System.out.println(image_url);

                    Image image1 = new Image("http://" + ip_address +"/media/" +image_url, 250, 250, true, true);

                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(FourthClassController.class.getResource("profile_page.fxml"));
                    loader.setController(this);
                    System.out.println("collected ui successfully");
                    mother_paneV.getChildren().set(3, loader.load());

                    if (flag.equals("null")) {
                        if (new_flag_view != null){
                            new_flag_view.setVisible(false);
                            System.out.println("flag is null");
                        }
                    } else {
                        if (new_flag_view != null){
                            new_flag_view.setVisible(true);
                        }else {
                            System.out.println("the thing is null");
                        }
                        if (new_flag_reason != null){
                            new_flag_reason.setText(flag);
                        }
                    }
                    try {
                        full_name1.setText( surname + " " + first_name + " " + other_name );
                        level_field.setText(level);
                        mat_Onj.setText(matric_number.toUpperCase());
                        dept_obj.setText(department);
                        if (image_url.equals("")){
                            System.out.println("no image");
                        }else {
                            Circle_img.setFill(new ImagePattern(image1));
                        }
                        return_msg.setVisible(false);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }

                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image imag = new Image("sample/image/army.jpg", 250, 250, true, true);
        army_logo.setFill(new ImagePattern(imag));

        Image image = new Image("sample/image/naub.png", 250, 250, true, true);
        logo1.setFill(new ImagePattern(image));
    }

}
