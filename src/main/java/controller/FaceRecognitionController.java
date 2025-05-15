package controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.json.JSONObject;


import javax.imageio.ImageIO;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FaceRecognitionController {

    @FXML private ImageView cameraView;

    private VideoCapture capture;
    private volatile boolean stopCamera = false;

    static {
        OpenCV.loadLocally();
    }

    public void initialize() {
        capture = new VideoCapture(0);
        startCamera();
    }

    private void startCamera() {
        new Thread(() -> {
            Mat frame = new Mat();
            while (!stopCamera) {
                if (capture.read(frame)) {
                    Image image = matToImage(frame);
                    Platform.runLater(() -> cameraView.setImage(image));
                }
            }
            frame.release();
        }).start();
    }

    private Image matToImage(Mat frame) {
        Mat matBGR = new Mat();
        Imgproc.cvtColor(frame, matBGR, Imgproc.COLOR_BGR2RGB);
        return SwingFXUtils.toFXImage(matToBufferedImage(matBGR), null);
    }

    private java.awt.image.BufferedImage matToBufferedImage(Mat original) {
        try {
            MatOfByte byteMat = new MatOfByte();
            Imgcodecs.imencode(".jpg", original, byteMat);
            byte[] bytes = byteMat.toArray();
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleCaptureAndSend() {
        Mat frame = new Mat();
        if (capture.read(frame)) {
            try {
                File tempFile = File.createTempFile("capture", ".jpg");
                Imgcodecs.imwrite(tempFile.getAbsolutePath(), frame);
                boolean recognized = sendToFlask(tempFile);
                if (recognized) {

                    redirectToLogin();
                } else {
                    showAlert("Visage non reconnu.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean sendToFlask(File imageFile) {
        try {
            URL url = new URL("http://127.0.0.1:5000/recognize");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String boundary = "*****";
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());

            request.writeBytes("--" + boundary + "\r\n");
            request.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"\r\n");
            request.writeBytes("Content-Type: image/jpeg\r\n\r\n");

            FileInputStream inputStream = new FileInputStream(imageFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                request.write(buffer, 0, bytesRead);
            }
            request.writeBytes("\r\n--" + boundary + "--\r\n");
            inputStream.close();
            request.flush();
            request.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("🧪 Réponse Flask : " + response.toString());

                // Parse JSON proprement
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean success = jsonResponse.getBoolean("success");

                if (success) {
                    String user = jsonResponse.getString("user");
                    System.out.println("👤 Utilisateur reconnu : " + user);
                    // Passer le nom de l'utilisateur à l'alerte
                    showAlert("Visage reconnu. Utilisateur : " + user);
                }

                return success;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }





    private void redirectToLogin() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginForm.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) cameraView.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reconnaissance faciale");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public void stopCamera() {
        stopCamera = true;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }
}
