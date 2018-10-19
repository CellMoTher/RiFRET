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
public class S2S4Dialog extends JDialog implements ActionListener {

    private RiFRET_Plugin mainWindow;
    private ImagePlus donorImg, transferImg, acceptorImg;
    private JPanel panel;
    private JButton setDonorButton, setTransferButton, setAcceptorButton;
    private JButton setDonorThresholdButton, setTransferThresholdButton, setAcceptorThresholdButton, calculateButton, setButton;
    private JButton subtractDonorButton, subtractTransferButton, subtractAcceptorButton;
    private JButton resetButton;
    private JCheckBox showSImagesCB;
    private JLabel s2ResultLabel, s4ResultLabel;

    public S2S4Dialog(RiFRET_Plugin mainWindow) {
        setTitle("S2/S4 factor calculation");
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
        JLabel infoLabel = new JLabel("<html><center>S2 and S4 are calculated based on images of the donor, transfer and acceptor channels of an acceptor only labeled sample.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorButton = new JButton("Set donor channel image");
        setDonorButton.addActionListener(this);
        setDonorButton.setActionCommand("setS2S4Donor");
        panel.add(setDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setTransferButton = new JButton("Set transfer channel image");
        setTransferButton.addActionListener(this);
        setTransferButton.setActionCommand("setS2S4Transfer");
        panel.add(setTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setAcceptorButton = new JButton("Set acceptor channel image");
        setAcceptorButton.addActionListener(this);
        setAcceptorButton.setActionCommand("setS2S4Acceptor");
        panel.add(setAcceptorButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractDonorButton = new JButton("Subtract background of donor channel");
        subtractDonorButton.addActionListener(this);
        subtractDonorButton.setActionCommand("subtractS2S4Donor");
        panel.add(subtractDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractTransferButton = new JButton("Subtract background of transfer channel");
        subtractTransferButton.addActionListener(this);
        subtractTransferButton.setActionCommand("subtractS2S4Transfer");
        panel.add(subtractTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        subtractAcceptorButton = new JButton("Subtract background of acceptor channel");
        subtractAcceptorButton.addActionListener(this);
        subtractAcceptorButton.setActionCommand("subtractS2S4Acceptor");
        panel.add(subtractAcceptorButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        setDonorThresholdButton = new JButton("Set threshold for donor channel");
        setDonorThresholdButton.addActionListener(this);
        setDonorThresholdButton.setActionCommand("setS2S4DonorThreshold");
        panel.add(setDonorThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 8;
        setTransferThresholdButton = new JButton("Set threshold for transfer channel");
        setTransferThresholdButton.addActionListener(this);
        setTransferThresholdButton.setActionCommand("setS2S4TransferThreshold");
        panel.add(setTransferThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        setAcceptorThresholdButton = new JButton("Set threshold for acceptor channel");
        setAcceptorThresholdButton.addActionListener(this);
        setAcceptorThresholdButton.setActionCommand("setS2S4AcceptorThreshold");
        panel.add(setAcceptorThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 10;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0, 4, 4, 4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel resultLabel = new JLabel("Results (S2 S4):");
        s2ResultLabel = new JLabel("", JLabel.CENTER);
        s4ResultLabel = new JLabel("", JLabel.CENTER);
        gcr.gridx = 0;
        gcr.gridy = 0;
        radioPanel.add(resultLabel, gcr);
        gcr.weightx = 10;
        gcr.gridx = 1;
        gcr.gridy = 0;
        radioPanel.add(s2ResultLabel, gcr);
        gcr.gridx = 2;
        gcr.gridy = 0;
        radioPanel.add(s4ResultLabel, gcr);
        panel.add(radioPanel, gc);
        gc.gridx = 0;
        gc.gridy = 12;
        showSImagesCB = new JCheckBox("show S2 and S4 images (for manual calc.)");
        panel.add(showSImagesCB, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 13;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);
        gc.gridx = 1;
        gc.gridy = 13;
        setButton = new JButton("Set S2 and S4");
        setButton.addActionListener(this);
        setButton.setActionCommand("setfactor");
        panel.add(setButton, gc);
        gc.gridx = 2;
        gc.gridy = 13;
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
                    setTransferButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    subtractDonorButton.setBackground(mainWindow.originalButtonColor);
                    subtractTransferButton.setBackground(mainWindow.originalButtonColor);
                    subtractAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    setDonorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setTransferThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    calculateButton.setBackground(mainWindow.originalButtonColor);
                    setButton.setBackground(mainWindow.originalButtonColor);
                    s2ResultLabel.setText("");
                    s4ResultLabel.setText("");
                    break;
                case "setS2S4Donor":
                    donorImg = WindowManager.getCurrentImage();
                    if (donorImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4 calc.)");
                        return;
                    }
                    if (donorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + donorImg.getImageStackSize() + "). Please split it into parts. (S2/S4 calc.)");
                        donorImg = null;
                        return;
                    } else if (donorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + donorImg.getNSlices() + "). Please split it into parts. (S2/S4 calc.)");
                        donorImg = null;
                        return;
                    }
                    donorImg.setTitle("Donor channel (S2/S4 calc.) - " + new Date().toString());
                    new ImageConverter(donorImg).convertToGray32();
                    setDonorButton.setBackground(mainWindow.greenColor);
                    break;
                case "setS2S4Transfer":
                    transferImg = WindowManager.getCurrentImage();
                    if (transferImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4 calc.)");
                        return;
                    }
                    if (transferImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + transferImg.getImageStackSize() + "). Please split it into parts. (S2/S4 calc.)");
                        transferImg = null;
                        return;
                    } else if (transferImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + transferImg.getNSlices() + "). Please split it into parts. (S2/S4 calc.)");
                        transferImg = null;
                        return;
                    }
                    transferImg.setTitle("Transfer channel (S2/S4 calc.) - " + new Date().toString());
                    new ImageConverter(transferImg).convertToGray32();
                    setTransferButton.setBackground(mainWindow.greenColor);
                    break;
                case "setS2S4Acceptor":
                    acceptorImg = WindowManager.getCurrentImage();
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4 calc.)");
                        return;
                    }
                    if (acceptorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + acceptorImg.getImageStackSize() + "). Please split it into parts. (S2/S4 calc.)");
                        acceptorImg = null;
                        return;
                    } else if (acceptorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + acceptorImg.getNSlices() + "). Please split it into parts. (S2/S4 calc.)");
                        acceptorImg = null;
                        return;
                    }
                    acceptorImg.setTitle("Acceptor channel (S2/S4 calc.) - " + new Date().toString());
                    new ImageConverter(acceptorImg).convertToGray32();
                    setAcceptorButton.setBackground(mainWindow.greenColor);
                    break;
                case "subtractS2S4Donor": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4 calc.)");
                        return;
                    } else if (donorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for donor channel. (S2/S4 calc.)");
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
                    mainWindow.log("Subtracted background (" + backgroundAvgD + ") of donor channel. (S2/S4 calc.)");
                    subtractDonorButton.setBackground(mainWindow.greenColor);
                    break;
                }
                case "subtractS2S4Transfer": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4 calc.)");
                        return;
                    } else if (transferImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for transfer channel. (S2/S4 calc.)");
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
                    mainWindow.log("Subtracted background (" + backgroundAvgT + ") of transfer channel. (S2/S4 calc.)");
                    subtractTransferButton.setBackground(mainWindow.greenColor);
                    break;
                }
                case "subtractS2S4Acceptor": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4 calc.)");
                        return;
                    } else if (acceptorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for acceptor channel. (S2/S4 calc.)");
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
                    mainWindow.log("Subtracted background (" + backgroundAvgA + ") of acceptor channel. (S2/S4 calc.)");
                    subtractAcceptorButton.setBackground(mainWindow.greenColor);
                    break;
                }
                case "setS2S4DonorThreshold":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4 calc.)");
                        return;
                    }
                    IJ.selectWindow(donorImg.getTitle());
                    IJ.run("Threshold...");
                    setDonorThresholdButton.setBackground(mainWindow.greenColor);
                    break;
                case "setS2S4TransferThreshold":
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4 calc.)");
                        return;
                    }
                    IJ.selectWindow(transferImg.getTitle());
                    IJ.run("Threshold...");
                    setTransferThresholdButton.setBackground(mainWindow.greenColor);
                    break;
                case "setS2S4AcceptorThreshold":
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4 calc.)");
                        return;
                    }
                    IJ.selectWindow(acceptorImg.getTitle());
                    IJ.run("Threshold...");
                    setAcceptorThresholdButton.setBackground(mainWindow.greenColor);
                    break;
                case "calculate":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4 calc.)");
                    } else if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4 calc.)");
                    } else if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4 calc.)");
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        ImageProcessor ipDP = donorImg.getProcessor();
                        ImageProcessor ipTP = transferImg.getProcessor();
                        ImageProcessor ipAP = acceptorImg.getProcessor();
                        double s2c = 0;
                        double s4c = 0;
                        double countc = 0;
                        float[][] imgS2Points = null;
                        float[][] imgS4Points = null;
                        int width = ipDP.getWidth();
                        int height = ipDP.getHeight();
                        if (showSImagesCB.isSelected()) {
                            imgS2Points = new float[width][height];
                            imgS4Points = new float[width][height];
                        }
                        float currentS2 = 0;
                        float currentS4 = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ipDP.getPixelValue(i, j) > 0 && ipTP.getPixelValue(i, j) >= 0 && ipAP.getPixelValue(i, j) >= 0) {
                                    currentS2 = ipTP.getPixelValue(i, j) / ipAP.getPixelValue(i, j);
                                    currentS4 = ipDP.getPixelValue(i, j) / ipAP.getPixelValue(i, j);
                                    s2c += currentS2;
                                    s4c += currentS4;
                                    countc++;
                                } else {
                                    currentS2 = Float.NaN;
                                    currentS4 = Float.NaN;
                                }
                                if (showSImagesCB.isSelected()) {
                                    imgS2Points[i][j] = currentS2;
                                    imgS4Points[i][j] = currentS4;
                                }
                            }
                        }
                        if (showSImagesCB.isSelected()) {
                            ImagePlus s2Img = new ImagePlus("S2 image", new FloatProcessor(imgS2Points));
                            s2Img.show();
                            ImagePlus s4Img = new ImagePlus("S4 image", new FloatProcessor(imgS4Points));
                            s4Img.show();
                        }
                        float avgS2 = (float) (s2c / countc);
                        float avgS4 = (float) (s4c / countc);
                        s2ResultLabel.setText(df.format(avgS2));
                        s4ResultLabel.setText(df.format(avgS4));
                        calculateButton.setBackground(mainWindow.greenColor);
                        donorImg.changes = false;
                        transferImg.changes = false;
                        acceptorImg.changes = false;
                    }
                    break;
                case "setfactor":
                    if (s2ResultLabel.getText().isEmpty() || s4ResultLabel.getText().isEmpty()) {
                        mainWindow.logError("S2 and S4 have to be calculated before setting them. (S2/S4 calc.)");
                        return;
                    }
                    mainWindow.setS2Factor(s2ResultLabel.getText());
                    mainWindow.setS4Factor(s4ResultLabel.getText());
                    setButton.setBackground(mainWindow.greenColor);
                    mainWindow.calculateS2S4Button.setBackground(mainWindow.greenColor);
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
