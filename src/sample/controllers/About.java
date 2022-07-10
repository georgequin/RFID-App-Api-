package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class About implements Initializable {

        @FXML
        private Circle naub_logo, proj_sup_img, dev_img1, dev_img2, dev_img3;

        @FXML
        private Label supName, supTitle, supDetails,devName1, devTitle1, devDetails1, devName2, devTitle2, devDetails2, devName3, devTitle3, devDetails3;

        @Override
        public void initialize(URL location, ResourceBundle resources) {

            Image imgg = new Image("sample/image/naub.png", 250, 250, true, true);
            naub_logo.setFill(new ImagePattern(imgg));

//            Image imgg2 = new Image("file:naub.png", 250, 250, true, true);
//            proj_sup_img.setFill(new ImagePattern(imgg2));


            Image imgg4 = new Image("sample/image/ken.jpeg", 250, 250, true, true);
            dev_img1.setFill(new ImagePattern(imgg4));

            Image imgg5 = new Image("sample/image/oche.png", 250, 250, true, true);
            dev_img2.setFill(new ImagePattern(imgg5));

            Image imgg6 = new Image("sample/image/salim.jpg", 250, 250, true, true);
            dev_img3.setFill(new ImagePattern(imgg6));

//            Image imgg6 = new Image("file:naub.png", 250, 250, true, true);
//            dev_img3.setFill(new ImagePattern(imgg6));

//        supName.setText("");
//        supTitle.setText("");
//        supDetails.setText("");
//
            devName1.setText("KENNEDY. E (MCITP|CCNA|MSCA)");
            devTitle1.setText("ELECTRONICS/ MACHINE LEARNING ENGINEER");
            devDetails1.setText("Senior Python Developer at NTECH.\n" +
                    "Server / Network Administrator\n" +
                    "Django Backend Developer\n");

            devName2.setText("GEORGE DAVID");
            devTitle2.setText("SOFTWARE ENGINEER");
            devDetails2.setText("Senior Software Developer at NTECH.\n" +
                    "Developer in java and Android Technologies\n" +
                    "javafx GUI developer");

            devName3.setText("M.S SHUAIBU");
            devTitle3.setText("UI/UX DESIGNER");
            devDetails3.setText("UI designer at NTECH\n" +
                    "Front-End Web developer\n" +
                    "Project Manager");

        }



}
