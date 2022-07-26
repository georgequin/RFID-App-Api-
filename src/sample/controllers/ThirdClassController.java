package sample.controllers;

import com.goebl.david.Webb;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ThirdClassController implements Initializable {
    public ImageView rfid_imageView;
    public HBox rfid_image_view;
    public VBox mother_paneV;
    @FXML
    private VBox new_flag_view;
    @FXML
    private Label new_flag_reason;
    @FXML
    private ComboBox<String> select_course;

    @FXML
    private Button start_Exams;
    public HBox center_pane;
    public Button scan_button;
    public Circle logo1 , army_logo,Circle_img;
    public TextField input;
    public Label level_field, full_name, full_name1, mat_Onj, dept_obj, return_msg, sign_in_time, sign_out_time,
            sign_out_label, sign_in_label, designation_field;
    public SerialPort arduinoPort;
    LoginController loginController = new LoginController();
    String ip_address = loginController.IPAddress;

    public ThirdClassController() throws IOException {
    }

    public void StopScan() {
        loginController.disconnectArduino(arduinoPort);
        scan_button.setDisable(false);
        mother_paneV.getChildren().set(3, center_pane);
        rfid_image_view.setVisible(false);
    }

    public void RaiseAlarm() {
    }

    public void logout() {
        loginController.logOutdata(mother_paneV);
    }

    public void AboutPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/sample/about_page.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            Stage stage = new Stage();
            stage.setTitle("NIGERIAN ARMY UNIVERSITY BIU");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMaximized(false);
            //stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    public void scan() {
        connectArduino();
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
            JSONObject response99 = webb.post("https://" +ip_address + "/search-profile/")
                    .param("id_number",matric_number)
                    .ensureSuccess()
                    .connectTimeout(10000)
                    .retry(0,false)
                    .asJsonObject()
                    .getBody();

            try {
                int status_code = (int) response99.get("status_code");
                if (status_code == 0){
                    System.out.println("id doesnt exist");
                    System.out.println("Display Error Page");
                    try {
                        Parent content = FXMLLoader.load(ThirdClassController.class.getResource("/sample/no_student_error.fxml"));
                        mother_paneV.getChildren().set(3, content);

                    } catch (IOException E) {
                        E.printStackTrace();
                    }
                }else {
                    boolean is_staff = response99.getBoolean("is_staff");
                    if (is_staff){
                        System.out.println("this is staff");
                        JSONObject jsonArray1 = (JSONObject) response99.getJSONArray("staff").get(0);
                        System.out.println(jsonArray1);
                        String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                        String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                        String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                        String designation = jsonArray1.getJSONObject("fields").get("designation").toString();
                        String id_number = jsonArray1.getJSONObject("fields").get("staff_id_number").toString();
                        String department = jsonArray1.getJSONObject("fields").get("department").toString();
                        String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                        Image image1 = new Image("https://" + ip_address +"/media/" +image_url, 250, 250, true, true);

                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(ThirdClassController.class.getResource("/sample/staff_profile.fxml"));
                        loader.setController(this);
                        System.out.println("collected ui successfully");
                        mother_paneV.getChildren().set(3, loader.load());

                        full_name1.setText(first_name + " " + surname + " " + other_name );
                        designation_field.setText(designation);
                        mat_Onj.setText(id_number);
                        dept_obj.setText(department);
                        Circle_img.setFill(new ImagePattern(image1));
                        return_msg.setText("STAFF");

                    }else {
                        System.out.println("this is student");
                        System.out.println(response99.getJSONArray("student").get(0));
                        JSONObject jsonArray1 = (JSONObject) response99.getJSONArray("student").get(0);

                        String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
                        String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
                        String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
                        String department = jsonArray1.getJSONObject("fields").get("department").toString();
                        String matric_no = jsonArray1.getJSONObject("fields").get("matric_number").toString();
                        String level = jsonArray1.getJSONObject("fields").get("level").toString();
                        String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
                        Image image1 = new Image("https://" + ip_address +"/media/" +image_url, 250, 250, true, true);

                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(ThirdClassController.class.getResource("/sample/profile_page.fxml"));
                        loader.setController(this);
                        System.out.println("collected ui successfully");
                        //mother_pane.setCenter(loader.load());
                        mother_paneV.getChildren().set(3, loader.load());

                        full_name1.setText(first_name + " " + surname + " " + other_name );
                        level_field.setText(level);
                        mat_Onj.setText(matric_no);
                        dept_obj.setText(department);
                        Circle_img.setFill(new ImagePattern(image1));
                        return_msg.setVisible(false);
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                showAlert("SERVER NOT REACHABLE");
            }
        });

    }

    public void Exams() throws IOException, JSONException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FourthClassController.class.getResource("/sample/start_exams.fxml"));
        loader.setController(this);
        System.out.println("collected ui successfully");
        mother_paneV.getChildren().set(3, loader.load());

        Webb webb = Webb.create();
        String decodedPath = System.getProperty("user.dir");
        System.out.println(decodedPath);

        String new_path1 = decodedPath + "\\token.txt";
        String new_path2 = new_path1;



        String token = null;
        try {
            Stream<String> lines = Files.lines(Paths.get(new_path2));
            token = lines.collect(Collectors.joining(System.lineSeparator()));
            System.out.println(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(token);
        JSONObject response4 = webb.get("https://" +ip_address + "/graphql?query={courses{courseCode}}")
                .header("Authorization", "JWT " + token)
                .ensureSuccess()
                .asJsonObject()
                .getBody();
        JSONArray jsonArray = null;
        try {
           jsonArray =  response4.getJSONObject("data").getJSONArray("courses");
        }catch (JSONException e){
            e.printStackTrace();
        }
        List<String> list = new ArrayList<String>();
        if (jsonArray != null){
            for (int i = 0; i <= (jsonArray).length() -1; i++ ){
                String my_courses = jsonArray.getJSONObject(i).get("courseCode").toString();
                list.add(my_courses);
                System.out.println(my_courses);
            }
            ObservableList<String> observableList = FXCollections.observableList(list);
//            System.getProperties();
            select_course.getItems().addAll(list);
            select_course.setPromptText("Select course");
            new ComboBoxAutoComplete<>(select_course);

            start_Exams.setOnAction(value->{
                String selectedItem = select_course.getSelectionModel().getSelectedItem();
                JSONObject response5 = webb.post("https://" +ip_address + "/exams/check-course/")
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
                    loginController.examsScan(selectedItem);
                    scan_button.setDisable(true);
                    mother_paneV.getChildren().set(3, center_pane);
                    rfid_image_view.setVisible(true);
                    System.out.println(result);
                }else {
                    showAlert(result);
                }
            });
        }
    }

    public void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image imag = new Image("sample/image/army.jpg", 250, 250, true, true);
        army_logo.setFill(new ImagePattern(imag));

        Image image = new Image("sample/image/naub.png", 250, 250, true, true);
        logo1.setFill(new ImagePattern(image));

//        File file = new File("sample/rfid_image.png");
//        Image rfid_image = new Image(file.toURI().toString());
//        rfid_imageView.setImage(rfid_image);

    }

    public void goTODashboard() {
        loginController.gotoDB();
    }

    public void connectArduino(){
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
                scan_button.setDisable(true);
                mother_paneV.getChildren().set(3, center_pane);
                rfid_image_view.setVisible(true);
                System.out.println("Connected to port: " + portName);

                try{
                    AtomicReference<String> rfid_code = new AtomicReference<>("");
                    System.out.println("trying the listener");
                    serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                        if (serialPortEvent.isRXCHAR()) {
                            System.out.println("Abeg this is before try");

                            try {
                                String st = serialPort.readString(serialPortEvent.getEventValue());
                                String st1 = serialPort.readHexString(serialPortEvent.getEventValue());
                                 if (st.equals(" ")){

                                     rfid_code.set(st.replace(" ", ""));
                                     System.out.println("Card number: " + rfid_code);
                                 }else {
                                     if (st.equals("\n")){
                                         rfid_code.set(st.replace("\n", ""));
                                     }
//                                     System.out.println("has something");
                                     rfid_code.set(rfid_code + st);
                                     System.out.println(rfid_code.get());
                                 }

                                //make database request, validate result and return either error page or profile page
//                                Platform.runLater(() ->{
//                                    //get student data with token
//                                    Webb webb = Webb.create();
//                                    JSONObject response00 = webb.post("https://" +ip_address + "/scan-profile/")
//                                            .param("rfid_code",rfid_code)
//                                            .param("api_request", true)
//                                            .ensureSuccess()
//                                            .connectTimeout(10000)
//                                            .retry(0,false)
//                                            .asJsonObject()
//                                            .getBody();
//
//                                    try {
//                                        boolean status_code = response00.getBoolean("success");
//                                        if (!status_code){
//                                            System.out.println("card doesnt exist");
//                                            try {
//                                                Parent content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/sample/card_not_registered_error.fxml")));
//                                                mother_paneV.getChildren().set(3, content);
//                                            } catch (IOException E) {
//                                                E.printStackTrace();
//                                            }
//                                        }else{
//                                            boolean is_staff = response00.getBoolean("is_staff");
//                                            if (is_staff){
//                                                System.out.println("this is staff");
//                                                System.out.println(response00.getJSONArray("student").get(0));
//                                                JSONObject jsonArray1 = (JSONObject) response00.getJSONArray("student").get(0);
//                                                //COLLECT DIRECTLY FROM JSON
//                                                String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
//                                                String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
//                                                String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
//                                                String department = jsonArray1.getJSONObject("fields").get("department").toString();
//                                                String staff_id = jsonArray1.getJSONObject("fields").get("staff_id_number").toString();
//                                                String designation = jsonArray1.getJSONObject("fields").get("designation").toString();
//                                                String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
//                                                Image image1 = new Image("https://" + ip_address +"/media/" +image_url, 250, 250, true, true);
//
//                                                FXMLLoader loader = new FXMLLoader();
//                                                loader.setLocation(FourthClassController.class.getResource("/sample/staff_profile.fxml"));
//                                                loader.setController(this);
//                                                System.out.println("collected ui successfully");
//                                                mother_paneV.getChildren().set(3, loader.load());
//
//                                                full_name1.setText(surname + " " + first_name + " " + other_name );
//                                                designation_field.setText(designation);
//                                                mat_Onj.setText(staff_id);
//                                                dept_obj.setText(department);
//                                                Circle_img.setFill(new ImagePattern(image1));
//                                                return_msg.setText("STAFF");
//
//                                            }else {
//                                                System.out.println("this is student");
//                                                System.out.println(response00.getJSONArray("student").get(0));
//                                                JSONObject jsonArray1 = (JSONObject) response00.getJSONArray("student").get(0);
//                                                String first_name = jsonArray1.getJSONObject("fields").get("first_name").toString();
//                                                String surname = jsonArray1.getJSONObject("fields").get("surname").toString();
//                                                String other_name = jsonArray1.getJSONObject("fields").get("other_name").toString();
//                                                String department = jsonArray1.getJSONObject("fields").get("department").toString();
//                                                String matric_no = jsonArray1.getJSONObject("fields").get("matric_number").toString();
//                                                String level = jsonArray1.getJSONObject("fields").get("level").toString();
//                                                String flag = jsonArray1.getJSONObject("fields").get("is_flaged").toString();
//                                                System.out.println(flag);
//                                                String image_url = jsonArray1.getJSONObject("fields").get("photo").toString();
//                                                Image image1 = new Image("https://" + ip_address +"/media/" +image_url, 250, 250, true, true);
//
//                                                FXMLLoader loader = new FXMLLoader();
//                                                loader.setLocation(FourthClassController.class.getResource("/sample/profile_page.fxml"));
//                                                loader.setController(this);
//                                                System.out.println("collected ui successfully");
//                                                mother_paneV.getChildren().set(3, loader.load());
//
//                                                if (flag.equals("null")) {
//                                                    new_flag_view.setVisible(false);
//                                                    System.out.println("flag is null");
//                                                } else {
//                                                    new_flag_view.setVisible(true);
//                                                    new_flag_reason.setText(flag);
//                                                }
//                                                full_name1.setText(first_name + " " + surname + " " + other_name );
//                                                level_field.setText(level);
//                                                mat_Onj.setText(matric_no);
//                                                dept_obj.setText(department);
//                                                Circle_img.setFill(new ImagePattern(image1));
//                                                return_msg.setVisible(false);
//                                            }
//                                        }
//                                    } catch (JSONException | IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                });
                                System.out.println("End of connection.");

                            } catch (SerialPortException ex) {
                                Logger.getLogger(ThirdClassController.class.getName()).log(Level.SEVERE, null, ex);
                                System.out.println("this is where you are");
                            }

                        }
                    });

                    System.out.println("final code: " + rfid_code);
                }catch (SerialPortException e){
                    e.printStackTrace();
                    System.out.println("not listening");
                }
                arduinoPort = serialPort;
                success = true;

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


    public void staffAttendance() {
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
                            String st1 = serialPort.readHexString(serialPortEvent.getEventValue());
                            String rfid_code = st.replace("\n", "");
                            System.out.println("Card number: " + rfid_code);
                            //make database request, validate result and return either error page or profile page
                            Platform.runLater(() ->{
                                //get student data with token
                                Webb webb = Webb.create();
                                JSONObject response00 = webb.post("https://" +ip_address + "/staff/staff-attendance/")
                                        .param("rfid_code",rfid_code)
                                        .param("api_request", true)
                                        .ensureSuccess()
                                        .connectTimeout(10000)
                                        .retry(0,false)
                                        .asJsonObject()
                                        .getBody();

                                //                                    int status_code = response00.getInt("status_code");
                                //JSONObject status = response00.getJSONObject("status");
                                System.out.println(response00);
                            });
                            System.out.println("End of connection.");

                        } catch (SerialPortException ex) {
                            Logger.getLogger(ThirdClassController.class.getName()).log(Level.SEVERE, null, ex);

                        }
                    }
                });

                arduinoPort = serialPort;
                success = true;
                scan_button.setDisable(true);
                mother_paneV.getChildren().set(3, center_pane);
                rfid_image_view.setVisible(true);
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

    public void studentAttendance() {


    }
}
