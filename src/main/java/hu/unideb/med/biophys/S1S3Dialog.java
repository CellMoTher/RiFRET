/*
 * Copyright (C) 2009 - 2018 RiFRET Developers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hu.unideb.med.biophys;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 *
 */
public class S1S3Dialog extends JDialog implements ActionListener {

    private final RiFRET_Plugin mainWindow;
    private ImagePlus donorImg, transferImg, acceptorImg;
    private JPanel panel;
    private JButton setDonorButton, setTransferButton, setAcceptorButton;
    private JButton setDonorThresholdButton, setTransferThresholdButton, setAcceptorThresholdButton, calculateButton, setButton;
    private JButton subtractDonorButton, subtractTransferButton, subtractAcceptorButton, copyRoiButton;
    private JButton resetButton;
    private JCheckBox showSImagesCB;
    private JLabel s1ResultLabel, s3ResultLabel;

    public S1S3Dialog(RiFRET_Plugin mainWindow) {
        setTitle("S1/S3 Factor Calculation");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 450);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2, 2, 6, 2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>S1 and S3 are calculated based on images of the donor, transfer and acceptor channels of a donor only labeled sample.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorButton = new JButton("Set donor channel image");
        setDonorButton.addActionListener(this);
        setDonorButton.setActionCommand("setS1S3Donor");
        panel.add(setDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setTransferButton = new JButton("Set transfer channel image");
        setTransferButton.addActionListener(this);
        setTransferButton.setActionCommand("setS1S3Transfer");
        panel.add(setTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setAcceptorButton = new JButton("Set acceptor channel image");
        setAcceptorButton.addActionListener(this);
        setAcceptorButton.setActionCommand("setS1S3Acceptor");
        panel.add(setAcceptorButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        copyRoiButton = new JButton("(Optional): Copy ROI");
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyS1S3Roi");
        panel.add(copyRoiButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractDonorButton = new JButton("Subtract background of donor channel");
        subtractDonorButton.addActionListener(this);
        subtractDonorButton.setActionCommand("subtractS1S3Donor");
        panel.add(subtractDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        subtractTransferButton = new JButton("Subtract background of transfer channel");
        subtractTransferButton.addActionListener(this);
        subtractTransferButton.setActionCommand("subtractS1S3Transfer");
        panel.add(subtractTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        subtractAcceptorButton = new JButton("Subtract background of acceptor channel");
        subtractAcceptorButton.addActionListener(this);
        subtractAcceptorButton.setActionCommand("subtractS1S3Acceptor");
        panel.add(subtractAcceptorButton, gc);
        gc.gridx = 0;
        gc.gridy = 8;
        setDonorThresholdButton = new JButton("Set threshold for donor channel");
        setDonorThresholdButton.addActionListener(this);
        setDonorThresholdButton.setActionCommand("setS1S3DonorThreshold");
        panel.add(setDonorThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        setTransferThresholdButton = new JButton("Set threshold for transfer channel");
        setTransferThresholdButton.addActionListener(this);
        setTransferThresholdButton.setActionCommand("setS1S3TransferThreshold");
        panel.add(setTransferThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 10;
        setAcceptorThresholdButton = new JButton("Set threshold for acceptor channel");
        setAcceptorThresholdButton.addActionListener(this);
        setAcceptorThresholdButton.setActionCommand("setS1S3AcceptorThreshold");
        panel.add(setAcceptorThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 11;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0, 4, 4, 4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel resultLabel = new JLabel("Results (S1 S3):");
        s1ResultLabel = new JLabel("", JLabel.CENTER);
        s3ResultLabel = new JLabel("", JLabel.CENTER);
        gcr.gridx = 0;
        gcr.gridy = 0;
        radioPanel.add(resultLabel, gcr);
        gcr.weightx = 10;
        gcr.gridx = 1;
        gcr.gridy = 0;
        radioPanel.add(s1ResultLabel, gcr);
        gcr.gridx = 2;
        gcr.gridy = 0;
        radioPanel.add(s3ResultLabel, gcr);
        panel.add(radioPanel, gc);
        gc.gridx = 0;
        gc.gridy = 13;
        showSImagesCB = new JCheckBox("Show S1 and S3 images (for manual calc.)");
        panel.add(showSImagesCB, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 14;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);
        gc.gridx = 1;
        gc.gridy = 14;
        setButton = new JButton("Set S1 and S3");
        setButton.addActionListener(this);
        setButton.setActionCommand("setfactor");
        panel.add(setButton, gc);
        gc.gridx = 2;
        gc.gridy = 14;
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        resetButton.setActionCommand("reset");
        panel.add(resetButton, gc);

        getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "reset":
                    donorImg = null;
                    transferImg = null;
                    acceptorImg = null;
                    setDonorButton.setBackground(mainWindow.originalButtonColor);
                    setDonorButton.setOpaque(false);
                    setDonorButton.setBorderPainted(true);
                    setTransferButton.setBackground(mainWindow.originalButtonColor);
                    setTransferButton.setOpaque(false);
                    setTransferButton.setBorderPainted(true);
                    setAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorButton.setOpaque(false);
                    setAcceptorButton.setBorderPainted(true);
                    copyRoiButton.setBackground(mainWindow.originalButtonColor);
                    copyRoiButton.setOpaque(false);
                    copyRoiButton.setBorderPainted(true);
                    subtractDonorButton.setBackground(mainWindow.originalButtonColor);
                    subtractDonorButton.setOpaque(false);
                    subtractDonorButton.setBorderPainted(true);
                    subtractTransferButton.setBackground(mainWindow.originalButtonColor);
                    subtractTransferButton.setOpaque(false);
                    subtractTransferButton.setBorderPainted(true);
                    subtractAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    subtractAcceptorButton.setOpaque(false);
                    subtractAcceptorButton.setBorderPainted(true);
                    setDonorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setDonorThresholdButton.setOpaque(false);
                    setDonorThresholdButton.setBorderPainted(true);
                    setTransferThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setTransferThresholdButton.setOpaque(false);
                    setTransferThresholdButton.setBorderPainted(true);
                    setAcceptorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorThresholdButton.setOpaque(false);
                    setAcceptorThresholdButton.setBorderPainted(true);
                    calculateButton.setBackground(mainWindow.originalButtonColor);
                    calculateButton.setOpaque(false);
                    calculateButton.setBorderPainted(true);
                    setButton.setBackground(mainWindow.originalButtonColor);
                    setButton.setOpaque(false);
                    setButton.setBorderPainted(true);
                    s1ResultLabel.setText("");
                    s3ResultLabel.setText("");
                    break;
                case "setS1S3Donor":
                    donorImg = WindowManager.getCurrentImage();
                    if (donorImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3 calc.)");
                        return;
                    }
                    if (donorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + donorImg.getImageStackSize() + "). Please split it into parts. (S1/S3 calc.)");
                        donorImg = null;
                        return;
                    } else if (donorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + donorImg.getNSlices() + "). Please split it into parts. (S1/S3 calc.)");
                        donorImg = null;
                        return;
                    }
                    donorImg.setTitle("Donor channel (S1/S3 calc.) - " + new Date().toString());
                    new ImageConverter(donorImg).convertToGray32();
                    setDonorButton.setBackground(mainWindow.greenColor);
                    setDonorButton.setOpaque(true);
                    setDonorButton.setBorderPainted(false);
                    break;
                case "setS1S3Transfer":
                    transferImg = WindowManager.getCurrentImage();
                    if (transferImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3 calc.)");
                        return;
                    }
                    if (transferImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + transferImg.getImageStackSize() + "). Please split it into parts. (S1/S3 calc.)");
                        transferImg = null;
                        return;
                    } else if (transferImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + transferImg.getNSlices() + "). Please split it into parts. (S1/S3 calc.)");
                        transferImg = null;
                        return;
                    }
                    transferImg.setTitle("Transfer channel (S1/S3 calc.) - " + new Date().toString());
                    new ImageConverter(transferImg).convertToGray32();
                    setTransferButton.setBackground(mainWindow.greenColor);
                    setTransferButton.setOpaque(true);
                    setTransferButton.setBorderPainted(false);
                    break;
                case "setS1S3Acceptor":
                    acceptorImg = WindowManager.getCurrentImage();
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is selected. (S1/S3 calc.)");
                        return;
                    }
                    if (acceptorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + acceptorImg.getImageStackSize() + "). Please split it into parts. (S1/S3 calc.)");
                        acceptorImg = null;
                        return;
                    } else if (acceptorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + acceptorImg.getNSlices() + "). Please split it into parts. (S1/S3 calc.)");
                        acceptorImg = null;
                        return;
                    }
                    acceptorImg.setTitle("Acceptor channel (S1/S3 calc.) - " + new Date().toString());
                    new ImageConverter(acceptorImg).convertToGray32();
                    setAcceptorButton.setBackground(mainWindow.greenColor);
                    setAcceptorButton.setOpaque(true);
                    setAcceptorButton.setBorderPainted(false);
                    break;
                case "copyS1S3Roi":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3 calc.)");
                        return;
                    }
                    if (donorImg.getRoi() != null) {
                        if (transferImg != null) {
                            transferImg.setRoi(donorImg.getRoi());
                        }
                        if (acceptorImg != null) {
                            acceptorImg.setRoi(donorImg.getRoi());
                        }
                        copyRoiButton.setBackground(mainWindow.greenColor);
                        copyRoiButton.setOpaque(true);
                        copyRoiButton.setBorderPainted(false);
                    } else {
                        if (transferImg != null) {
                            transferImg.killRoi();
                        }
                        if (acceptorImg != null) {
                            acceptorImg.killRoi();
                        }
                    }
                    break;
                case "subtractS1S3Donor": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3 calc.)");
                        return;
                    } else if (donorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for donor channel. (S1/S3 calc.)");
                        return;
                    }
                    ImageProcessor ipD = donorImg.getProcessor();
                    int width = donorImg.getWidth();
                    int height = donorImg.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (donorImg.getRoi().contains(i, j)) {
                                sum += ipD.getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float backgroundAvgD = (float) (sum / count);
                    float value = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            value = ipD.getPixelValue(x, y);
                            value = value - backgroundAvgD;
                            ipD.putPixelValue(x, y, value);
                        }
                    }
                    donorImg.updateAndDraw();
                    donorImg.killRoi();
                    mainWindow.log("Subtracted background (" + backgroundAvgD + ") of donor channel. (S1/S3 calc.)");
                    subtractDonorButton.setBackground(mainWindow.greenColor);
                    subtractDonorButton.setOpaque(true);
                    subtractDonorButton.setBorderPainted(false);
                    break;
                }
                case "subtractS1S3Transfer": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3 calc.)");
                        return;
                    } else if (transferImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for transfer channel. (S1/S3 calc.)");
                        return;
                    }
                    ImageProcessor ipT = transferImg.getProcessor();
                    int width = transferImg.getWidth();
                    int height = transferImg.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (transferImg.getRoi().contains(i, j)) {
                                sum += ipT.getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float backgroundAvgT = (float) (sum / count);
                    float value = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            value = ipT.getPixelValue(x, y);
                            value = value - backgroundAvgT;
                            ipT.putPixelValue(x, y, value);
                        }
                    }
                    transferImg.updateAndDraw();
                    transferImg.killRoi();
                    mainWindow.log("Subtracted background (" + backgroundAvgT + ") of transfer channel. (S1/S3 calc.)");
                    subtractTransferButton.setBackground(mainWindow.greenColor);
                    subtractTransferButton.setOpaque(true);
                    subtractTransferButton.setBorderPainted(false);
                    break;
                }
                case "subtractS1S3Acceptor": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3 calc.)");
                        return;
                    } else if (acceptorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for acceptor channel. (S1/S3 calc.)");
                        return;
                    }
                    ImageProcessor ipA = acceptorImg.getProcessor();
                    int width = acceptorImg.getWidth();
                    int height = acceptorImg.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (acceptorImg.getRoi().contains(i, j)) {
                                sum += ipA.getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float backgroundAvgA = (float) (sum / count);
                    float value = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            value = ipA.getPixelValue(x, y);
                            value = value - backgroundAvgA;
                            ipA.putPixelValue(x, y, value);
                        }
                    }
                    acceptorImg.updateAndDraw();
                    acceptorImg.killRoi();
                    mainWindow.log("Subtracted background (" + backgroundAvgA + ") of acceptor channel. (S1/S3 calc.)");
                    subtractAcceptorButton.setBackground(mainWindow.greenColor);
                    subtractAcceptorButton.setOpaque(true);
                    subtractAcceptorButton.setBorderPainted(false);
                    break;
                }
                case "setS1S3DonorThreshold":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3 calc.)");
                        return;
                    }
                    IJ.selectWindow(donorImg.getTitle());
                    IJ.run("Threshold...");
                    setDonorThresholdButton.setBackground(mainWindow.greenColor);
                    setDonorThresholdButton.setOpaque(true);
                    setDonorThresholdButton.setBorderPainted(false);
                    break;
                case "setS1S3TransferThreshold":
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3 calc.)");
                        return;
                    }
                    IJ.selectWindow(transferImg.getTitle());
                    IJ.run("Threshold...");
                    setTransferThresholdButton.setBackground(mainWindow.greenColor);
                    setTransferThresholdButton.setOpaque(true);
                    setTransferThresholdButton.setBorderPainted(false);
                    break;
                case "setS1S3AcceptorThreshold":
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3 calc.)");
                        return;
                    }
                    IJ.selectWindow(acceptorImg.getTitle());
                    IJ.run("Threshold...");
                    setAcceptorThresholdButton.setBackground(mainWindow.greenColor);
                    setAcceptorThresholdButton.setOpaque(true);
                    setAcceptorThresholdButton.setBorderPainted(false);
                    break;
                case "calculate":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S1/S3 calc.)");
                    } else if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S1/S3 calc.)");
                    } else if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S1/S3 calc.)");
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        ImageProcessor ipDP = donorImg.getProcessor();
                        ImageProcessor ipTP = transferImg.getProcessor();
                        ImageProcessor ipAP = acceptorImg.getProcessor();
                        double s1c = 0;
                        double s3c = 0;
                        double countc = 0;
                        float[][] imgS1Points = null;
                        float[][] imgS3Points = null;
                        int width = ipDP.getWidth();
                        int height = ipDP.getHeight();
                        if (showSImagesCB.isSelected()) {
                            imgS1Points = new float[width][height];
                            imgS3Points = new float[width][height];
                        }
                        float currentS1 = 0;
                        float currentS3 = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ipDP.getPixelValue(i, j) > 0 && ipTP.getPixelValue(i, j) >= 0 && ipAP.getPixelValue(i, j) >= 0) {
                                    currentS1 = ipTP.getPixelValue(i, j) / ipDP.getPixelValue(i, j);
                                    currentS3 = ipAP.getPixelValue(i, j) / ipDP.getPixelValue(i, j);
                                    s1c += currentS1;
                                    s3c += currentS3;
                                    countc++;
                                } else {
                                    currentS1 = Float.NaN;
                                    currentS3 = Float.NaN;
                                }
                                if (showSImagesCB.isSelected()) {
                                    imgS1Points[i][j] = currentS1;
                                    imgS3Points[i][j] = currentS3;
                                }
                            }
                        }
                        if (showSImagesCB.isSelected()) {
                            ImagePlus s1Img = new ImagePlus("S1 image", new FloatProcessor(imgS1Points));
                            s1Img.show();
                            ImagePlus s3Img = new ImagePlus("S3 image", new FloatProcessor(imgS3Points));
                            s3Img.show();
                        }
                        float avgS1 = (float) (s1c / countc);
                        float avgS3 = (float) (s3c / countc);
                        s1ResultLabel.setText(df.format(avgS1));
                        s3ResultLabel.setText(df.format(avgS3));
                        calculateButton.setBackground(mainWindow.greenColor);
                        calculateButton.setOpaque(true);
                        calculateButton.setBorderPainted(false);
                        donorImg.changes = false;
                        transferImg.changes = false;
                        acceptorImg.changes = false;
                    }
                    break;
                case "setfactor":
                    if (s1ResultLabel.getText().isEmpty() || s3ResultLabel.getText().isEmpty()) {
                        mainWindow.logError("S1 and S3 have to be calculated before setting them. (S1/S3 calc.)");
                        return;
                    }
                    mainWindow.setS1Factor(s1ResultLabel.getText());
                    mainWindow.setS3Factor(s3ResultLabel.getText());
                    setButton.setBackground(mainWindow.greenColor);
                    setButton.setOpaque(true);
                    setButton.setBorderPainted(false);
                    mainWindow.calculateS1S3Button.setBackground(mainWindow.greenColor);
                    mainWindow.calculateS1S3Button.setOpaque(true);
                    mainWindow.calculateS1S3Button.setBorderPainted(false);
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
