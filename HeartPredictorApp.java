import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class HeartPredictorApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Heart Attack Risk Predictor");

        // Load background image
        final BufferedImage bgImage;
        BufferedImage tempImage = null;
        File imgFile = new File("hospital.jpg");  // Make sure this path is correct
        System.out.println("Trying to load image from: " + imgFile.getAbsolutePath());
        try {
            tempImage = ImageIO.read(imgFile);
        } catch (Exception ex) {
            System.out.println("Could not load background image.");
        }
        bgImage = tempImage;

        // Create inputs
        JTextField ageField = new JTextField(10);
        String[] sexOptions = { "Female", "Male" };
        JComboBox<String> sexBox = new JComboBox<>(sexOptions);
        String[] cpOptions = { "Typical", "Atypical", "Non-anginal", "Asymptomatic" };
        JComboBox<String> cpBox = new JComboBox<>(cpOptions);
        JTextField bpField = new JTextField(10);
        JTextField cholField = new JTextField(10);
        String[] fbsOptions = { "No", "Yes" };
        JComboBox<String> fbsBox = new JComboBox<>(fbsOptions);
        String[] ecgOptions = { "Normal", "ST", "LVH" };
        JComboBox<String> ecgBox = new JComboBox<>(ecgOptions);
        JTextField maxHrField = new JTextField(10);
        String[] anginaOptions = { "No", "Yes" };
        JComboBox<String> anginaBox = new JComboBox<>(anginaOptions);
        JTextField oldpeakField = new JTextField(10);
        String[] slopeOptions = { "Up", "Flat", "Down" };
        JComboBox<String> slopeBox = new JComboBox<>(slopeOptions);

        // Semi-transparent white background on text fields
        Color transparentWhite = new Color(255, 255, 255, 200);
        ageField.setOpaque(true); ageField.setBackground(transparentWhite);
        bpField.setOpaque(true); bpField.setBackground(transparentWhite);
        cholField.setOpaque(true); cholField.setBackground(transparentWhite);
        maxHrField.setOpaque(true); maxHrField.setBackground(transparentWhite);
        oldpeakField.setOpaque(true); oldpeakField.setBackground(transparentWhite);

        JButton predictButton = new JButton("Predict");
        JTextArea resultArea = new JTextArea(3, 25);
        resultArea.setEditable(false);

        // Create panel with GridBagLayout and background image
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);  // padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("SansSerif", Font.BOLD, 16);
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 16);
        Font buttonFont = new Font("SansSerif", Font.BOLD, 18);
        Font resultFont = new Font("Monospaced", Font.BOLD, 16);

        // Helper method to add label and component
        class AddHelper {
            void addLabelAndComp(String labelText, Component comp, int y) {
                gbc.gridx = 0;
                gbc.gridy = y;
                JLabel label = new JLabel(labelText);
                label.setForeground(Color.WHITE);
                label.setFont(labelFont);
                label.setOpaque(false);
                panel.add(label, gbc);

                gbc.gridx = 1;
                comp.setFont(fieldFont);
                panel.add(comp, gbc);
            }
        }
        AddHelper addHelper = new AddHelper();

        int row = 0;
        addHelper.addLabelAndComp("Age:", ageField, row++);
        addHelper.addLabelAndComp("Sex:", sexBox, row++);
        addHelper.addLabelAndComp("Chest Pain Type:", cpBox, row++);
        addHelper.addLabelAndComp("Resting BP:", bpField, row++);
        addHelper.addLabelAndComp("Cholesterol:", cholField, row++);
        addHelper.addLabelAndComp("FastingBS:", fbsBox, row++);
        addHelper.addLabelAndComp("Resting ECG:", ecgBox, row++);
        addHelper.addLabelAndComp("Max HR:", maxHrField, row++);
        addHelper.addLabelAndComp("Exercise Angina:", anginaBox, row++);
        addHelper.addLabelAndComp("Oldpeak:", oldpeakField, row++);
        addHelper.addLabelAndComp("ST Slope:", slopeBox, row++);

        // Add button
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        predictButton.setFont(buttonFont);
        panel.add(predictButton, gbc);

        // Add resultArea
        gbc.gridy = row;
        resultArea.setFont(resultFont);
        resultArea.setOpaque(false);
        resultArea.setForeground(Color.WHITE);
        resultArea.setBackground(new Color(0, 0, 0, 0)); // fully transparent
        panel.add(resultArea, gbc);

        frame.add(panel);
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Button action with input validation and request
        predictButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!validateInputs(ageField, bpField, cholField, maxHrField, oldpeakField)) {
                    return;
                }

                try {
                    String jsonInput = String.format(
                        "{"
                        + "\"Age\": %s,"
                        + "\"Sex\": %s,"
                        + "\"ChestPainType\": %s,"
                        + "\"RestingBP\": %s,"
                        + "\"Cholesterol\": %s,"
                        + "\"FastingBS\": %s,"
                        + "\"RestingECG\": %s,"
                        + "\"MaxHR\": %s,"
                        + "\"ExerciseAngina\": %s,"
                        + "\"Oldpeak\": %s,"
                        + "\"ST_Slope\": %s"
                        + "}",
                        ageField.getText(),
                        sexBox.getSelectedIndex(),
                        cpBox.getSelectedIndex(),
                        bpField.getText(),
                        cholField.getText(),
                        fbsBox.getSelectedIndex(),
                        ecgBox.getSelectedIndex(),
                        maxHrField.getText(),
                        anginaBox.getSelectedIndex(),
                        oldpeakField.getText(),
                        slopeBox.getSelectedIndex()
                    );

                    URL url = new URL("http://127.0.0.1:5000/predict");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonInput.getBytes());
                    os.flush();
                    os.close();

                    Scanner sc = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (sc.hasNext()) {
                        response.append(sc.nextLine());
                    }
                    sc.close();

                    String prediction = response.toString();

                    String advice = "";
                    if (prediction.contains("High Risk")) {
                        advice = "\n⚠️ Doctor Tip: Immediate lifestyle changes and medical check-up recommended.";
                    } else if (prediction.contains("Low Risk")) {
                        advice = "\n✅ Doctor Tip: Keep up your healthy habits!";
                    }

                    resultArea.setText("Result: " + prediction + advice);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    resultArea.setText("Error: " + ex.getMessage());
                }
            }
        });
    }

    private static boolean validateInputs(
        JTextField ageField,
        JTextField bpField,
        JTextField cholField,
        JTextField maxHrField,
        JTextField oldpeakField
    ) {
        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 1 || age > 120) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Age between 1 and 120.");
                return false;
            }

            int bp = Integer.parseInt(bpField.getText());
            if (bp < 50 || bp > 250) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Resting BP between 50 and 250.");
                return false;
            }

            int chol = Integer.parseInt(cholField.getText());
            if (chol < 100 || chol > 600) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Cholesterol value between 100 and 600.");
                return false;
            }

            int maxHr = Integer.parseInt(maxHrField.getText());
            if (maxHr < 60 || maxHr > 220) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Max HR between 60 and 220.");
                return false;
            }

            double oldpeak = Double.parseDouble(oldpeakField.getText());
            if (oldpeak < 0 || oldpeak > 10) {
                JOptionPane.showMessageDialog(null, "Please enter a valid Oldpeak value between 0 and 10.");
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid numeric values for all fields.");
            return false;
        }
        return true;
    }
}
