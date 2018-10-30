/*
 * Copyright (C) 2009 - 2018 RiFRET developers.
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

import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Color;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 *
 */
public class AutoflDialog extends JDialog implements ActionListener {

    private final RiFRET_Plugin mainWindow;
    private ImagePlus donorImg, transferImg, acceptorImg;
    private JPanel panel;
    private JButton setDonorButton, setTransferButton, setAcceptorButton;
    private JButton subtractDonorButton, subtractTransferButton, subtractAcceptorButton;
    private JButton calculateDonorAfButton, calculateTransferAfButton, calculateAcceptorAfButton;

    public AutoflDialog(RiFRET_Plugin mainWindow) {
        setTitle("Autofluorescence Calculation");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 415);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2, 2, 6, 2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>For the calculation of autofluorescence, donor, transfer and acceptor channel images of an unlabeled sample can be set and background subtracted. Then, the averages of given ROIs are calculated and set in the main window.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorButton = new JButton("Set donor channel image");
        setDonorButton.addActionListener(this);
        setDonorButton.setActionCommand("setAutoFlDonor");
        panel.add(setDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setTransferButton = new JButton("Set transfer channel image");
        setTransferButton.addActionListener(this);
        setTransferButton.setActionCommand("setAutoFlTransfer");
        panel.add(setTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setAcceptorButton = new JButton("Set acceptor channel image");
        setAcceptorButton.addActionListener(this);
        setAcceptorButton.setActionCommand("setAutoFlAcceptor");
        panel.add(setAcceptorButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractDonorButton = new JButton("Subtract background of donor channel");
        subtractDonorButton.addActionListener(this);
        subtractDonorButton.setActionCommand("subtractAutoFlDonor");
        panel.add(subtractDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractTransferButton = new JButton("Subtract background of transfer channel");
        subtractTransferButton.addActionListener(this);
        subtractTransferButton.setActionCommand("subtractAutoFlTransfer");
        panel.add(subtractTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        subtractAcceptorButton = new JButton("Subtract background of acceptor channel");
        subtractAcceptorButton.addActionListener(this);
        subtractAcceptorButton.setActionCommand("subtractAutoFlAcceptor");
        panel.add(subtractAcceptorButton, gc);

        gc.gridx = 0;
        gc.gridy = 7;
        gc.fill = GridBagConstraints.HORIZONTAL;
        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(getWidth() - 35, 1));
        line.setBackground(Color.lightGray);
        panel.add(line, gc);

        gc.gridx = 0;
        gc.gridy = 8;
        gc.fill = GridBagConstraints.BOTH;
        calculateDonorAfButton = new JButton("Calculate & set donor autofluorescence");
        calculateDonorAfButton.addActionListener(this);
        calculateDonorAfButton.setActionCommand("calculateDonorAF");
        panel.add(calculateDonorAfButton, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        calculateTransferAfButton = new JButton("Calculate & set transfer autofluorescence");
        calculateTransferAfButton.addActionListener(this);
        calculateTransferAfButton.setActionCommand("calculateTransferAF");
        panel.add(calculateTransferAfButton, gc);
        gc.gridx = 0;
        gc.gridy = 10;
        calculateAcceptorAfButton = new JButton("Calculate & set acceptor autofluorescence");
        calculateAcceptorAfButton.addActionListener(this);
        calculateAcceptorAfButton.setActionCommand("calculateAcceptorAF");
        panel.add(calculateAcceptorAfButton, gc);

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
                    subtractDonorButton.setBackground(mainWindow.originalButtonColor);
                    subtractDonorButton.setOpaque(false);
                    subtractDonorButton.setBorderPainted(true);
                    subtractTransferButton.setBackground(mainWindow.originalButtonColor);
                    subtractTransferButton.setOpaque(false);
                    subtractTransferButton.setBorderPainted(true);
                    subtractAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    subtractAcceptorButton.setOpaque(false);
                    subtractAcceptorButton.setBorderPainted(true);
                    calculateDonorAfButton.setBackground(mainWindow.originalButtonColor);
                    calculateDonorAfButton.setOpaque(false);
                    calculateDonorAfButton.setBorderPainted(true);
                    calculateTransferAfButton.setBackground(mainWindow.originalButtonColor);
                    calculateTransferAfButton.setOpaque(false);
                    calculateTransferAfButton.setBorderPainted(true);
                    calculateAcceptorAfButton.setBackground(mainWindow.originalButtonColor);
                    calculateAcceptorAfButton.setOpaque(false);
                    calculateAcceptorAfButton.setBorderPainted(true);
                    break;
                case "setAutoFlDonor":
                    donorImg = WindowManager.getCurrentImage();
                    if (donorImg == null) {
                        mainWindow.logError("No image is selected. (Autofl.)");
                        return;
                    }
                    if (donorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + donorImg.getImageStackSize() + "). Please split it into parts. (Autofl.)");
                        donorImg = null;
                        return;
                    } else if (donorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + donorImg.getNSlices() + "). Please split it into parts. (Autofl.)");
                        donorImg = null;
                        return;
                    }
                    donorImg.setTitle("Donor channel (Autofl.) - " + new Date().toString());
                    new ImageConverter(donorImg).convertToGray32();
                    setDonorButton.setBackground(mainWindow.greenColor);
                    setDonorButton.setOpaque(true);
                    setDonorButton.setBorderPainted(false);
                    break;
                case "setAutoFlTransfer":
                    transferImg = WindowManager.getCurrentImage();
                    if (transferImg == null) {
                        mainWindow.logError("No image is selected. (Autofl.)");
                        return;
                    }
                    if (transferImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + transferImg.getImageStackSize() + "). Please split it into parts. (Autofl.)");
                        transferImg = null;
                        return;
                    } else if (transferImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + transferImg.getNSlices() + "). Please split it into parts. (Autofl.)");
                        transferImg = null;
                        return;
                    }
                    transferImg.setTitle("Transfer channel (Autofl.) - " + new Date().toString());
                    new ImageConverter(transferImg).convertToGray32();
                    setTransferButton.setBackground(mainWindow.greenColor);
                    setTransferButton.setOpaque(true);
                    setTransferButton.setBorderPainted(false);
                    break;
                case "setAutoFlAcceptor":
                    acceptorImg = WindowManager.getCurrentImage();
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is selected. (Autofl.)");
                        return;
                    }
                    if (acceptorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + acceptorImg.getImageStackSize() + "). Please split it into parts. (Autofl.)");
                        acceptorImg = null;
                        return;
                    } else if (acceptorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + acceptorImg.getNSlices() + "). Please split it into parts. (Autofl.)");
                        acceptorImg = null;
                        return;
                    }
                    acceptorImg.setTitle("Acceptor channel (Autofl.) - " + new Date().toString());
                    new ImageConverter(acceptorImg).convertToGray32();
                    setAcceptorButton.setBackground(mainWindow.greenColor);
                    setAcceptorButton.setOpaque(true);
                    setAcceptorButton.setBorderPainted(false);
                    break;
                case "subtractAutoFlDonor": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (Autofl.)");
                        return;
                    } else if (donorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for donor channel. (Autofl.)");
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
                    mainWindow.log("Subtracted background (" + backgroundAvgD + ") of donor channel. (Autofl.)");
                    subtractDonorButton.setBackground(mainWindow.greenColor);
                    subtractDonorButton.setOpaque(true);
                    subtractDonorButton.setBorderPainted(false);
                    break;
                }
                case "subtractAutoFlTransfer": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (Autofl.)");
                        return;
                    } else if (transferImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for transfer channel. (Autofl.)");
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
                    mainWindow.log("Subtracted background (" + backgroundAvgT + ") of transfer channel. (Autofl.)");
                    subtractTransferButton.setBackground(mainWindow.greenColor);
                    subtractTransferButton.setOpaque(true);
                    subtractTransferButton.setBorderPainted(false);
                    break;
                }
                case "subtractAutoFlAcceptor": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (Autofl.)");
                        return;
                    } else if (acceptorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for acceptor channel. (Autofl.)");
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
                    mainWindow.log("Subtracted background (" + backgroundAvgA + ") of acceptor channel. (Autofl.)");
                    subtractAcceptorButton.setBackground(mainWindow.greenColor);
                    subtractAcceptorButton.setOpaque(true);
                    subtractAcceptorButton.setBorderPainted(false);
                    break;
                }
                case "calculateDonorAF": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (Autofl.)");
                        return;
                    } else if (donorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for donor channel. (Autofl.)");
                        return;
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = donorImg.getWidth();
                    int height = donorImg.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (donorImg.getRoi().contains(i, j)) {
                                sum += donorImg.getProcessor().getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float autoflAvgD = (float) (sum / count);
                    mainWindow.autoflDInDField.setText(df.format(autoflAvgD));
                    calculateDonorAfButton.setBackground(mainWindow.greenColor);
                    calculateDonorAfButton.setOpaque(true);
                    calculateDonorAfButton.setBorderPainted(false);
                    break;
                }
                case "calculateTransferAF": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (Autofl.)");
                        return;
                    } else if (transferImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for transfer channel. (Autofl.)");
                        return;
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = transferImg.getWidth();
                    int height = transferImg.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (transferImg.getRoi().contains(i, j)) {
                                sum += transferImg.getProcessor().getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float autoflAvgT = (float) (sum / count);
                    mainWindow.autoflAInDField.setText(df.format(autoflAvgT));
                    calculateTransferAfButton.setBackground(mainWindow.greenColor);
                    calculateTransferAfButton.setOpaque(true);
                    calculateTransferAfButton.setBorderPainted(false);
                    break;
                }
                case "calculateAcceptorAF": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (Autofl.)");
                        return;
                    } else if (acceptorImg.getRoi() == null) {
                        mainWindow.logError("No ROI is defined for acceptor channel. (Autofl.)");
                        return;
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = acceptorImg.getWidth();
                    int height = acceptorImg.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (acceptorImg.getRoi().contains(i, j)) {
                                sum += acceptorImg.getProcessor().getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float autoflAvgA = (float) (sum / count);
                    mainWindow.autoflAInAField.setText(df.format(autoflAvgA));
                    calculateAcceptorAfButton.setBackground(mainWindow.greenColor);
                    calculateAcceptorAfButton.setOpaque(true);
                    calculateAcceptorAfButton.setBorderPainted(false);
                    break;
                }
                case "close":
                    setVisible(false);
                    dispose();
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
