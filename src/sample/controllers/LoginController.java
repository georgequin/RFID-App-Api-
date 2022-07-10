package sample.controllers;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import sample.controllers.ComboBoxAutoComplete;
import sample.controllers.FourthClassController;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoginController implements Initializable {
    @FXML
    private ComboBox<String> select_course, select_attendance_course;
    @FXML
    private Button start_Exams;
    @FXML
    private VBox new_flag_view;
    @FXML
    private Label new_flag_reason;
    public Circle logo1 , army_logo,Circle_img;
    public Label level_field, full_name1, mat_Onj, dept_obj, return_msg,
            sign_out_label, sign_in_label, designation_field;
    public TextField username_fd, ipAddress;
    public Font x1;
    public SerialPort arduinoPort;
    public VBox MOTHER_PANE, login_center_pane;
    public PasswordField password_fd;
    public MenuBar login_nav;
    public MenuItem close_attendance;
    public HBox rfid_image_view;
    public Button change_ip_address_button, start_student_attendance;
    Webb webb = Webb.create();

    String decodedPath3 = System.getProperty("user.dir");
    String new_path1 = decodedPath3 + "\\ipAddress.txt";

    Stream<String> lines = Files.lines(Paths.get(new_path1));
    String IPAddress = "rfid-v1.vodatrox.com";

    public LoginController() throws IOException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image imag = new Image("sample/image/army.jpg", 250, 250, true, true);
        army_logo.setFill(new ImagePattern(imag));

        Image imgg = new Image("sample/image/naub.png", 250, 250, true, true);
        logo1.setFill(new ImagePattern(imgg));
    }

    public void login2() throws JSONException, IOException {

        if (username_fd.getText().isEmpty() || password_fd.getText().isEmpty()) {
            infoBox();
        }

        String email = username_fd.getText();
        String password = password_fd.getText();
        String decodedPath = System.getProperty("user.dir");
        System.out.println(decodedPath);

        String decodedPath3 = System.getProperty("user.dir");
        String new_path1 = decodedPath3 + "\\ipAddress.txt";

        Stream<String> lines = Files.lines(Paths.get(new_path1));
       // final String IPAddress1 = lines.collect(Collectors.joining(System.lineSeparator()));
        final String IPAddress1 = "rfid-v1.vodatrox.com";

            try {
                JSONObject make_login = webb.post("https://" + "rfid-v1.vodatrox.com" + "/graphql/?query=mutation{tokenAuth(email:"
                        + "\"" + email + "\"" + ",password:" + "\"" +password + "\"" +"){success,token,errors,refreshToken}}")
                        .ensureSuccess()
                        .connectTimeout(10000)
                        .retry(0,false)
                        .asJsonObject()
                        .getBody();
                boolean is_success = (boolean) make_login.getJSONObject("data").getJSONObject("tokenAuth").get("success");
                System.out.println(is_success);
                if (is_success){
                    String token = make_login.getJSONObject("data").getJSONObject("tokenAuth").get("token").toString();
                    System.out.println(token);
                    String refresh_token = make_login.getJSONObject("data").getJSONObject("tokenAuth").get("refreshToken").toString();
                    System.out.println(refresh_token);


                    JSONObject getPrivilege = webb.get("https://"+ "rfid-v1.vodatrox.com"  +"/graphql?query={me{privilege}}")
                            .header("Authorization", "JWT " + token)
                            .ensureSuccess()
                            .asJsonObject()
                            .getBody();

                    System.out.println("passsed here");


                    File newFile = new File(decodedPath, "\\token.txt");
                    newFile.createNewFile();
                    Writer file_writer = new FileWriter(newFile);
                    file_writer.write(token);
                    file_writer.close();

                    File user_email = new File(decodedPath, "\\user-email.txt");
                    user_email.createNewFile();
                    Writer writer = new FileWriter(user_email);
                    writer.write(email);
                    writer.close();

                    File user_password = new File(decodedPath, "\\user-password.txt");
                    user_password.createNewFile();
                    Writer writer1 = new FileWriter(user_password);
                    writer1.write(password + "," + password + ","+ password);
                    writer1.close();

                    System.out.println(getPrivilege.getJSONObject("data"));
                    String privilege = getPrivilege.getJSONObject("data").getJSONObject("me").get("privilege").toString();
                    checkPrivilege(privilege);
                }else {
                    showAlert("Invalid login credentials");
                }

            }catch (WebbException | IOException e){
                System.out.println(e);
                showAlert("Server not running!");
            }
    }

    public void scanOnLogin(){
        System.out.println("connect Reader");
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
                                //get recent IP and make request
                                String new_path1 = System.getProperty("user.dir") + "\\ipAddress.txt";
                                Stream<String> lines = null;
                                try {
                                    lines = Files.lines(Paths.get(new_path1));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                assert lines != null;
                                final String IPAddress2 = lines.collect(Collectors.joining(System.lineSeparator()));
                                System.out.println(IPAddress2);

                                //get staff data with token
                                JSONObject response00 = null;
                                try {
                                    Webb webb = Webb.create();
                                    response00 = webb.post("http://" +IPAddress2 + "/users/scan-login/")
                                            .param("rfid_code",st9)
                                            .param("api_request", true)
                                            .ensureSuccess()
                                            .connectTimeout(10000)
                                            .retry(0,false)
                                            .asJsonObject()
                                            .getBody();
                                } catch (Exception e) {
                                    showAlert("Server not running!!");
                                    disconnectArduino(serialPort);
                                    e.printStackTrace();
                                }

                                try {
                                    assert response00 != null;
                                    int status_code = response00.getInt("status_code");
                                    if (status_code == 0){
                                        System.out.println("ACCESS DENIED");
                                        showAlert("ACCESS DENIED");
                                    }
                                    else{
                                            System.out.println("ACCESS GRANTED");
                                            System.out.println(response00.getJSONArray("staff").get(0));
                                            System.out.println(response00.getJSONArray("user").get(0));
                                            JSONObject jsonArray1 = (JSONObject) response00.getJSONArray("user").get(0);
                                            String privilege = jsonArray1.getJSONObject("fields").get("privilege").toString();
                                            System.out.println(privilege);
                                        checkPrivilege(privilege);
                                        disconnectArduino(serialPort);
                                    }
                                } catch (JSONException e) {
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

    public void examsScan(String selected_item){
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
                    if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 10) {
                        try {
                            String st = serialPort.readString(serialPortEvent.getEventValue());
                            String st4 = serialPort.readString(serialPortEvent.getEventValue());
                            String st7 = st.replace("\n", "");
                            System.out.println("Card number: " + st7);

                            //make database request, validate result and return either error page or profile page
                            Platform.runLater(() ->{
                                //get student data with token
                                Webb webb = Webb.create();

                                JSONObject response4 = webb.post("http://" +IPAddress + "/exams/exam-profile/")
                                        .param("rfid_code",st7)
                                        .param("current_exam", selected_item)
                                        .param("api_request", true)
                                        .ensureSuccess()
                                        .connectTimeout(10000)
                                        .retry(0,false)
                                        .asJsonObject()
                                        .getBody();
                                try {
                                    int status_code = (int) response4.get("status_code");
                                    if (status_code == 0){
                                        System.out.println("Display Error Page");
                                        try {
                                            Parent content = FXMLLoader.load(getClass().getResource("card_not_registered_error.fxml"));
                                            MOTHER_PANE.getChildren().set(2, content);
                                        } catch (IOException E) {
                                            E.printStackTrace();
                                        }
                                    }else {
                                        String message = response4.get("message").toString();
                                        boolean is_staff = response4.getBoolean("is_staff");
                                        if (is_staff){
                                            System.out.println("this is staff");
                                            if (status_code == 10){
                                                System.out.println("Staff is not an invigilator");
                                                System.out.println("invigilator arrived late, cant sign in");
                                                System.out.println(message);
                                                showAlert(message);
                                            }else if (status_code == 8){
                                                System.out.println("display normal staff details");
                                                JSONObject jsonArray1 = (JSONObject) response4.getJSONArray("staff").get(0);
                                                JSONObject staff_details = (JSONObject) response4.getJSONArray("exam_details").get(0);
                                                String staff_sign_in_time = staff_details.getJSONObject("fields").get("sign_in_time").toString();
                                                String staff_sign_out_time = staff_details.getJSONObject("fields").get("sign_out_time").toString();
                                                String newString = staff_sign_in_time.replace("T", " ");
                                                String newString2 = staff_sign_out_time.replace("T", " ");
                                                String sign_in_time = newString.substring(0, newString.length() - 5);
                                                String sign_out_time = newString2.substring(0, newString2.length() - 5);
                                                System.out.println(sign_in_time);

                                                System.out.println(jsonArray1);
                                                String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                                                String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                                                String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                                                String designation = jsonArray1.getJSONObject("fields").get("designation").toString();
                                                String id_number = jsonArray1.getJSONObject("fields").get("staff_id_number").toString();
                                                String department = jsonArray1.getJSONObject("fields").get("department").toString();
                                                String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                                                Image image1 = new Image("http://" + IPAddress +"/media/" +image_url, 250, 250, true, true);

                                                FXMLLoader loader = new FXMLLoader();
                                                loader.setLocation(FourthClassController.class.getResource("staff_profile.fxml"));
                                                loader.setController(this);
                                                System.out.println("collected ui successfully");
                                                MOTHER_PANE.getChildren().set(2, loader.load());

                                                full_name1.setText(first_name + " " + surname + " " + other_name );
                                                designation_field.setText(designation);
                                                mat_Onj.setText(id_number);
                                                dept_obj.setText(department);
                                                Circle_img.setFill(new ImagePattern(image1));
                                                return_msg.setText(message);
                                                sign_in_label.setVisible(true);
                                                sign_out_label.setVisible(true);
                                                sign_in_label.setText(sign_in_time);
                                                sign_out_label.setText(sign_out_time);


                                            }
                                        }else{
                                            System.out.println("this is student");
                                            if (status_code == 1){
                                                System.out.println("full details and exams details");
                                                System.out.println(response4.getJSONArray("student").get(0));
                                                JSONObject jsonArray1 = (JSONObject) response4.getJSONArray("student").get(0);
                                                JSONObject exams_info = (JSONObject) response4.getJSONArray("exam_details").get(0);


                                                String exams_details = exams_info.getJSONObject("fields").get("sign_in_time").toString();
                                                String exams_details2 = exams_info.getJSONObject("fields").get("sign_out_time").toString();
                                                String newString = exams_details.replace("T", " ");
                                                String newString2 = exams_details2.replace("T", " ");
                                                String sign_in_time = newString.substring(0, newString.length() - 5);
                                                String sign_out_time = newString2.substring(0, newString2.length() - 1);
                                                System.out.println(sign_in_time);

                                                String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                                                String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                                                String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                                                String department = jsonArray1.getJSONObject("fields").get("department").toString();
                                                String matric_no = jsonArray1.getJSONObject("fields").get("matric_number").toString();
                                                String level = jsonArray1.getJSONObject("fields").get("level").toString();
                                                String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                                                Image image1 = new Image("http://" + IPAddress +"/media/" +image_url, 250, 250, true, true);

                                                FXMLLoader loader = new FXMLLoader();
                                                loader.setLocation(FourthClassController.class.getResource("exam_page.fxml"));
                                                loader.setController(this);
                                                System.out.println("collected ui successfully");
                                                MOTHER_PANE.getChildren().set(2, loader.load());

                                                full_name1.setText(first_name + " " + surname + " " + other_name );
                                                level_field.setText(level);
                                                mat_Onj.setText(matric_no);
                                                dept_obj.setText(department);
                                                Circle_img.setFill(new ImagePattern(image1));
                                                return_msg.setText(message);

                                                if (message.equals("THIS STUDENT HAS ALREADY WRITTEN THIS COURSE")){
                                                    sign_in_label.setText(sign_in_time);
                                                    sign_in_label.setVisible(true);
                                                    sign_out_label.setText(sign_out_time);
                                                    sign_out_label.setVisible(true);
                                                }
                                                if (message.equals("STUDENT SIGNED IN")){
                                                    sign_in_label.setText(sign_in_time);
                                                    sign_in_label.setVisible(true);
                                                }
                                                if (message.equals("STUDENT DID NOT SUBMIT ON TIME")){
                                                    sign_in_label.setText(sign_in_time);
                                                    sign_in_label.setVisible(true);

                                                }
                                            }else if (status_code == 2){
                                                System.out.println("no exams details");
                                                showAlert(message);
                                            }
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
//                scan_button.setDisable(true);
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

    public void startStudentAttendance1(String attendance_course, FileWriter fileWriter, List<String> list){
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
                    if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 10) {
                        try {
                            String st = serialPort.readString(serialPortEvent.getEventValue());
                            String st4 = serialPort.readString(serialPortEvent.getEventValue());
                            String st7 = st.replace("\n", "");
                            System.out.println("Card number: " + st7);

                            //make database request, validate result and return either error page or profile page
                            Platform.runLater(() ->{
                                //get student data with token
                                Webb webb = Webb.create();

                                JSONObject response4 = webb.post("http://" +IPAddress + "/students/lecture-attendance/")
                                        .param("rfid_code",st7)
                                        .param("course_code", attendance_course)
                                        .param("api_request", true)
                                        .ensureSuccess()
                                        .connectTimeout(10000)
                                        .retry(0,false)
                                        .asJsonObject()
                                        .getBody();
                                try {
                                    int status_code = (int) response4.get("status_code");
                                    if (status_code == 0){
                                        System.out.println("Display Error Page");
                                        try {
                                            Parent content = FXMLLoader.load(getClass().getResource("card_not_registered_error.fxml"));
                                            MOTHER_PANE.getChildren().set(2, content);
                                        } catch (IOException E) {
                                            E.printStackTrace();
                                        }
                                    }else {
                                            System.out.println("this is student");
                                            if (status_code == 1){
                                                System.out.println("full details ");
                                                System.out.println(response4.getJSONArray("student").get(0));
                                                JSONObject jsonArray1 = (JSONObject) response4.getJSONArray("student").get(0);

                                                String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                                                String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                                                String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                                                String department = jsonArray1.getJSONObject("fields").get("department").toString();
                                                String matric_no = jsonArray1.getJSONObject("fields").get("matric_number").toString();
                                                String level = jsonArray1.getJSONObject("fields").get("level").toString();
                                                String flag = jsonArray1.getJSONObject("fields").get("is_flaged").toString();
                                                System.out.println(flag);
                                                String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                                                Image image1 = new Image("http://" + IPAddress +"/media/" +image_url, 250, 250, true, true);

                                                FXMLLoader loader = new FXMLLoader();
                                                loader.setLocation(FourthClassController.class.getResource("profile_page.fxml"));
                                                loader.setController(this);
                                                System.out.println("collected ui successfully");
                                                MOTHER_PANE.getChildren().set(2, loader.load());

                                                if (flag.equals("null")) {
                                                    new_flag_view.setVisible(false);
                                                    System.out.println("flag is null");
                                                } else {
                                                    new_flag_view.setVisible(true);
                                                    new_flag_reason.setText(flag);
                                                }
                                                full_name1.setText(first_name + " " + surname + " " + other_name );
                                                level_field.setText(level);
                                                mat_Onj.setText(matric_no);
                                                dept_obj.setText(department);
                                                Circle_img.setFill(new ImagePattern(image1));
                                                return_msg.setVisible(false);
                                                String full_name = first_name + " " + surname + " " + other_name;

                                                System.out.println(list);
                                                if (list.contains(full_name)){
                                                    System.out.println("pass");
                                                }else {
                                                    list.add(full_name);
                                                    fileWriter.write("\n" + first_name + " " + surname + " " + other_name +
                                                            "," + matric_no);
                                                }
                                                System.out.println(list);
                                            }else if (status_code == 2){
                                                showAlert("STUDENT DID NOT REGISTER FOR THIS COURSE");
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
                close_attendance.setOnAction(value->{
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

                arduinoPort = serialPort;
                success = true;
//                scan_button.setDisable(true);
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

    public void checkPrivilege(String privilege) {
        switch (privilege) {
            case "1":
                System.out.println("this is the ADMIN, open ADMIN app");
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("/sample/new_third_class.fxml"));
                    LoadScene(fxmlLoader);
                } catch (IOException e) {
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.log(Level.SEVERE, "Failed to create gate Window.", e);
                }
                break;
            case "2":
                System.out.println("this is the SECOND CLASS, open SECOND app");
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("sample/new_third_class.fxml"));
                    LoadScene(fxmlLoader);
                } catch (IOException e) {
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.log(Level.SEVERE, "Failed to create gate Window.", e);
                }
                break;
            case "3":
                System.out.println("this is the THIRD CLASS, open THIRD CLASS app");
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("sample/new_third_class.fxml"));
                    LoadScene(fxmlLoader);
                } catch (IOException e) {
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.log(Level.SEVERE, "Failed to create gate Window.", e);
                }
                break;
            case "4":
                System.out.println("this is the FOURTH CLASS, open FOURTH CLASS app");
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("sample/fourth_class.fxml"));
                    LoadScene(fxmlLoader);
                } catch (IOException e) {
                    Logger logger = Logger.getLogger(getClass().getName());
                    logger.log(Level.SEVERE, "Failed to create gate Window.", e);
                }
                break;
        }
    }

    private void LoadScene(FXMLLoader fxmlLoader) throws IOException {
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        scene.getStylesheets().add(getClass().getResource("/sample/mod.css").toExternalForm());
        stage.setTitle("NIGERIAN ARMY UNIVERSITY BIU");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        Stage pstage = (Stage) username_fd.getScene().getWindow();
        pstage.close();
    }

    private static void infoBox() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Please enter correct Username and Password");
        alert.setTitle("Login Failed");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
        System.out.println(alert.getModality());
    }

    public void logOutdata(VBox mother_paneV){
        System.out.println("Logging out...");
        try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/sample/login_page.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 800, 600);
                Stage stage = new Stage();
                stage.setTitle("NIGERIAN ARMY UNIVERSITY BIU");
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.show();
                Stage primary_stage = (Stage) mother_paneV.getScene().getWindow();
                primary_stage.close();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    public void disconnectArduino(SerialPort arduinoPort) {
        System.out.println("Disconnect Arduino...");
        if (arduinoPort != null) {
            try {
                arduinoPort.removeEventListener();

                if (arduinoPort.isOpened()) {
                    arduinoPort.closePort();
                    System.out.println("disconnected");
                }
            } catch (SerialPortException ex) {
                Logger.getLogger(FourthClassController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Arduino not connected");
        }
    }

    public void gotoDB(){

        String decodedPath = System.getProperty("user.dir");
        System.out.println(decodedPath);

        String driver_path = decodedPath + "\\chromedriver.exe";
        //String firefox_path = decodedPath + "\\geckodriver.exe";
        String email_path = decodedPath + "\\user-email.txt";
        String password_path = decodedPath + "\\user-password.txt";

        String user_email = null;
        String user_password = null;
        try {
            Stream<String> lines = Files.lines(Paths.get(email_path));
            Stream<String> lines1 = Files.lines(Paths.get(password_path));
            user_email = lines.collect(Collectors.joining(System.lineSeparator()));
            user_password = lines1.collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty("webdriver.chrome.driver", driver_path);
        //System.setProperty("webdriver.gecko.driver", firefox_path);
        try {
            System.out.println(user_email);
            System.out.println(user_password);

            WebDriver driver = new ChromeDriver();
           // WebDriver firefoxdriver = new FirefoxDriver();

            driver.manage().window().maximize();
            //firefoxdriver.manage().window().maximize();

            driver.get("https://" + IPAddress);
            //firefoxdriver.get("http://" + IPAddress);

            WebElement email = driver.findElement(By.id("Email"));
            WebElement password = driver.findElement(By.id("Password"));
            WebElement submit = driver.findElement(By.id("submit-btn"));

//            WebElement email1 = firefoxdriver.findElement(By.id("Email"));
//            WebElement password2 = firefoxdriver.findElement(By.id("Password"));
//            WebElement submit3 = firefoxdriver.findElement(By.id("submit-btn"));

            email.sendKeys(user_email);
            password.sendKeys(user_password);
            submit.click();

//            email1.sendKeys(user_email);
//            password2.sendKeys(user_password);
//            submit3.click();
        }catch (NoClassDefFoundError e){
            e.printStackTrace();
            showAlert(e.toString());
        }

    }

    public void change_ip_drop_button(){
        ipAddress.setVisible(true);
        change_ip_address_button.setVisible(true);
    }

    public void scan_invigilator_to_start_exams(){
        showAlert("YOU ARE ABOUT TO SCAN FOR EXAMS\n" +
                "ENSURE YOU ARE AN INVIGILATOR\n" +
                "PRESS OKAY TO SCAN");

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
                                //get recent IP and make request
                                String decodedPath = System.getProperty("user.dir");
                                System.out.println(decodedPath);

                                String decodedPath3 = System.getProperty("user.dir");
                                String new_path1 = decodedPath3 + "\\ipAddress.txt";

                                Stream<String> lines = null;
                                try {
                                    lines = Files.lines(Paths.get(new_path1));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                final String IPAddress3 = lines.collect(Collectors.joining(System.lineSeparator()));
                                System.out.println(IPAddress3);

                                //get student data with token
                                JSONObject response00 = null;
                                try {
                                    Webb webb = Webb.create();
                                    System.out.println(IPAddress);
                                    response00 = webb.post("http://" +IPAddress3 + "/exams/invigilation-scan/")
                                            .param("rfid_code",st9)
                                            .param("api_request", true)
                                            .ensureSuccess()
                                            .connectTimeout(10000)
                                            .retry(0,false)
                                            .asJsonObject()
                                            .getBody();
                                } catch (Exception e) {
                                    showAlert("Server not running!!");
                                    disconnectArduino(serialPort);
                                    e.printStackTrace();
                                }

                                try {
                                    assert response00 != null;
                                    int status_code = response00.getInt("status_code");
                                    if (status_code == 0){
                                        System.out.println("ACCESS DENIED");
                                        showAlert("NOT AN INVIGILATOR");
                                    }
                                    else{
                                        System.out.println("ACCESS GRANTED");
                                        System.out.println(response00.getJSONArray("invigilator").get(0));
                                        System.out.println(response00.getJSONObject("courses"));

                                        JSONArray courses =  response00.getJSONObject("courses").getJSONArray("courses");
                                        System.out.println(courses);
                                        List<String> courses_list = new ArrayList<String>();
                                        for (int i = 0; i <= (courses).length() -1; i++ ){
                                            String my_courses = courses.get(i).toString();
                                            courses_list.add(my_courses);
                                            System.out.println(my_courses);
                                        }
                                        System.out.println(courses_list);
                                        ObservableList<String> observableList = FXCollections.observableList(courses_list);
//                                      System.getProperties();

                                        FXMLLoader loader = new FXMLLoader();
                                        loader.setLocation(FourthClassController.class.getResource("start_exams.fxml"));
                                        loader.setController(this);
                                        System.out.println("collected ui successfully");
                                        MOTHER_PANE.getChildren().set(2, loader.load());
                                        login_nav.setVisible(true);
                                        disconnectArduino(serialPort);


                                        select_course.getItems().addAll(courses_list);
                                        select_course.setPromptText("Select course");
                                        new ComboBoxAutoComplete<>(select_course);
                                        start_Exams.setOnAction(value->{
                                        String selectedItem = select_course.getSelectionModel().getSelectedItem();
                                        JSONObject response5 = webb.post("http://" +IPAddress3 + "/exams/check-course/")
                                                .param("current_exam",selectedItem)
                                                .header("current_exam",selectedItem)
                                                .ensureSuccess()
                                                .connectTimeout(10000)
                                                .retry(0,false)
                                                .asJsonObject()
                                                .getBody();

                                        String result = null;
                                        try {
                                            result = response5.get("response").toString();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        assert result != null;
                                        if (result.equals("EXAM APPROVED")){
                                            examsScan(selectedItem);
                                            System.out.println(selectedItem);
                                            //MOTHER_PANE.getChildren().set(1, center_pane);
                                            FXMLLoader loader1 = new FXMLLoader();
                                            loader1.setLocation(FourthClassController.class.getResource("exams_center_pane.fxml"));
                                            loader1.setController(this);
                                            System.out.println("collected ui successfully");
                                            try {
                                                MOTHER_PANE.getChildren().set(2, loader1.load());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            rfid_image_view.setVisible(true);
                                            System.out.println(result);
                                        }else {
                                            showAlert(result);
                                        }
                                    });
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

    public void changeIPAdress() throws IOException {
        String ip_address = ipAddress.getText();
        ipAddress.setVisible(false);
        change_ip_address_button.setVisible(false);
        String decodedPath = System.getProperty("user.dir");

        File newFile = new File(decodedPath, "\\ipAddress.txt");
        newFile.createNewFile();
        Writer file_writer = new FileWriter(newFile);
        file_writer.write(ip_address);
        file_writer.close();
    }

    public void studentAttendance() {
        showAlert("WELCOME TO STUDENT ATTENDANCE\n" +
                "ENSURE YOU ARE A LECTURER\n" +
                "PRESS OKAY TO SCAN");

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
                                //get recent IP and make request
                                String decodedPath = System.getProperty("user.dir");
                                System.out.println(decodedPath);

                                String decodedPath3 = System.getProperty("user.dir");
                                String new_path1 = decodedPath3 + "\\ipAddress.txt";

                                Stream<String> lines = null;
                                try {
                                    lines = Files.lines(Paths.get(new_path1));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                final String IPAddress3 = lines.collect(Collectors.joining(System.lineSeparator()));
                                System.out.println(IPAddress3);

                                //get student data with token
                                JSONObject response00 = null;
                                try {
                                    Webb webb = Webb.create();
                                    System.out.println(IPAddress);
                                    response00 = webb.post("http://" +IPAddress3 + "/staff/confirm-staff/")
                                            .param("rfid_code",st9)
                                            .param("api_request", true)
                                            .ensureSuccess()
                                            .connectTimeout(10000)
                                            .retry(0,false)
                                            .asJsonObject()
                                            .getBody();
                                } catch (Exception e) {
                                    showAlert("Server not running!!");
                                    disconnectArduino(serialPort);
                                    e.printStackTrace();
                                }

                                try {
                                    assert response00 != null;
                                    int status_code = response00.getInt("status_code");
                                    if (status_code == 0){
                                        System.out.println("ACCESS DENIED");
                                        showAlert("NOT A LECTURER");
                                    }
                                    else{
                                        System.out.println("ACCESS GRANTED");
                                        String lecturer_name = (String) response00.get("staff_name");
                                        String lecturer_id = (String) response00.get("staff_id_number");
                                        String lecturer_designation = response00.getString("staff_designation");

                                        JSONObject response4 = webb.get("http://" +IPAddress3 + "/graphql?query={courses{courseCode}}")
                                                .ensureSuccess()
                                                .asJsonObject()
                                                .getBody();
                                        JSONArray jsonArray = null;
                                        try {
                                            jsonArray =  response4.getJSONObject("data").getJSONArray("courses");
                                        }catch (JSONException e){
                                            e.printStackTrace();
                                        }

                                        FXMLLoader loader = new FXMLLoader();
                                        loader.setLocation(FourthClassController.class.getResource("start_student_attendance.fxml"));
                                        loader.setController(this);
                                        //exams_page_menu.setVisible(true);
                                        System.out.println("collected ui successfully");
                                        MOTHER_PANE.getChildren().set(2, loader.load());
                                        login_nav.setVisible(true);
                                        disconnectArduino(serialPort);

                                        List<String> list = new ArrayList<String>();
                                        if (jsonArray != null){
                                            for (int i = 0; i <= (jsonArray).length() -1; i++ ){
                                                String my_courses = jsonArray.getJSONObject(i).get("courseCode").toString();
                                                list.add(my_courses);
                                                System.out.println(my_courses);
                                            }
                                            ObservableList<String> observableList = FXCollections.observableList(list);
                                            select_attendance_course.getItems().addAll(list);
                                            select_attendance_course.setPromptText("Select course");
                                            new ComboBoxAutoComplete<>(select_attendance_course);

                                            start_student_attendance.setOnAction(value->{
                                                FXMLLoader loader1 = new FXMLLoader();
                                                loader1.setLocation(FourthClassController.class.getResource("exams_center_pane.fxml"));
                                                loader1.setController(this);
                                                System.out.println("collected ui successfully");
                                                try {
                                                    MOTHER_PANE.getChildren().set(2, loader1.load());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                rfid_image_view.setVisible(true);
                                                String selected_course =  select_attendance_course.getSelectionModel().getSelectedItem();
                                                Date date = new Date();
                                                String data = "Attendance for "+ selected_course +" on " + date;
                                                String desktop_path = "C:\\Users\\Public\\Documents";
                                                File newFile = new File(desktop_path, selected_course + "-attendance.csv");
                                                try {
                                                    newFile.createNewFile();
                                                    FileWriter file_writer = new FileWriter(newFile);
                                                    file_writer.write(data + "\n");
                                                    List<String> attendance_list = new ArrayList<String>();
                                                    startStudentAttendance1(selected_course, file_writer, attendance_list);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            });
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

    public void printAttendance(ActionEvent actionEvent) {
    }

    public void logOut(ActionEvent actionEvent) {
        logOutdata(MOTHER_PANE);
        disconnectArduino(arduinoPort);
    }
}


