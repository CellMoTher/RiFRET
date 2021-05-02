/*-
 * #%L
 * an ImageJ plugin for intensity-based three-filter set (ratiometric) FRET.
 * %%
 * Copyright (C) 2009 - 2021 RiFRET developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package hu.unideb.med.biophys;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class S2S4S6Dialog extends JDialog implements ActionListener {

    private final RiFRET_Plugin mainWindow;
    private ImagePlus donorImg;
    private ImagePlus transferImg;
    private ImagePlus acceptorImg;
    private ImagePlus autofluorescenceImg;
    private ImageStack donorImgSave = null;
    private ImageStack transferImgSave = null;
    private ImageStack acceptorImgSave = null;
    private ImageStack autofluorescenceImgSave = null;
    private JPanel panel;
    private JButton setDonorButton;
    private JButton setTransferButton;
    private JButton setAcceptorButton;
    private JButton setAutofluorescenceButton;
    private JButton setDonorThresholdButton;
    private JButton setTransferThresholdButton;
    private JButton setAcceptorThresholdButton;
    private JButton setAutofluorescenceThresholdButton;
    private JButton resetDonorThresholdButton;
    private JButton resetTransferThresholdButton;
    private JButton resetAcceptorThresholdButton;
    private JButton resetAutofluorescenceThresholdButton;
    private JButton copyRoiButton;
    private JButton subtractDonorButton;
    private JButton subtractTransferButton;
    private JButton subtractAcceptorButton;
    private JButton subtractAutofluorescenceButton;
    private JButton calculateButton;
    private JButton setButton;
    private JButton resetButton;
    private JCheckBox showSImagesCB;
    private JLabel s2ResultLabel;
    private JLabel s4ResultLabel;
    private JLabel s6ResultLabel;
    private JTextField autoflDInDField;
    private JTextField autoflAInDField;
    private JTextField autoflAInAField;
    private JTextField autoflAFField;
    private final DateTimeFormatter dateTimeFormat;

    public S2S4S6Dialog(RiFRET_Plugin mainWindow) {
        setTitle("S2/S4/S6 Factor Calculation");
        this.mainWindow = mainWindow;
        dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        if (IJ.isMacOSX()) {
            setSize(360, 610);
        } else {
            setSize(330, 590);
        }
        setLocationRelativeTo(null);
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
        JLabel infoLabel = new JLabel("<html><center>S2, S4 and S6 are calculated based on images of the donor, transfer, acceptor and autofluorescence channels of an acceptor only labeled sample.</center></html>");
        panel.add(infoLabel, gc);

        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorButton = new JButton("Set donor channel image");
        setDonorButton.addActionListener(this);
        setDonorButton.setActionCommand("setS2S4S6Donor");
        panel.add(setDonorButton, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        setTransferButton = new JButton("Set transfer channel image");
        setTransferButton.addActionListener(this);
        setTransferButton.setActionCommand("setS2S4S6Transfer");
        panel.add(setTransferButton, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        setAcceptorButton = new JButton("Set acceptor channel image");
        setAcceptorButton.addActionListener(this);
        setAcceptorButton.setActionCommand("setS2S4S6Acceptor");
        panel.add(setAcceptorButton, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        setAutofluorescenceButton = new JButton("Set autofluorescence channel image");
        setAutofluorescenceButton.addActionListener(this);
        setAutofluorescenceButton.setActionCommand("setS2S4S6Autofluorescence");
        panel.add(setAutofluorescenceButton, gc);

        gc.gridx = 0;
        gc.gridy = 5;
        copyRoiButton = new JButton("(Optional:) Copy background ROI");
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyS2S4S6Roi");
        panel.add(copyRoiButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 6;
        JLabel subtractionLabel = new JLabel("Subtract bg. (avg. ROI and constant):");
        panel.add(subtractionLabel, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 6;
        JLabel autofluorescenceLabel = new JLabel("Constant:");
        panel.add(autofluorescenceLabel, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 7;
        subtractDonorButton = new JButton("Subtract from donor channel");
        subtractDonorButton.addActionListener(this);
        subtractDonorButton.setActionCommand("subtractS2S4S6Donor");
        panel.add(subtractDonorButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 7;
        autoflDInDField = new JTextField("0", 5);
        autoflDInDField.setHorizontalAlignment(JTextField.RIGHT);
        autoflDInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        panel.add(autoflDInDField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 8;
        subtractTransferButton = new JButton("Subtract from transfer channel");
        subtractTransferButton.addActionListener(this);
        subtractTransferButton.setActionCommand("subtractS2S4S6Transfer");
        panel.add(subtractTransferButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 8;
        autoflAInDField = new JTextField("0", 5);
        autoflAInDField.setHorizontalAlignment(JTextField.RIGHT);
        autoflAInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        panel.add(autoflAInDField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 9;
        subtractAcceptorButton = new JButton("Subtract from acceptor channel");
        subtractAcceptorButton.addActionListener(this);
        subtractAcceptorButton.setActionCommand("subtractS2S4S6Acceptor");
        panel.add(subtractAcceptorButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 9;
        autoflAInAField = new JTextField("0", 5);
        autoflAInAField.setHorizontalAlignment(JTextField.RIGHT);
        autoflAInAField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        panel.add(autoflAInAField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 10;
        subtractAutofluorescenceButton = new JButton("Subtract from autofluor. channel");
        subtractAutofluorescenceButton.addActionListener(this);
        subtractAutofluorescenceButton.setActionCommand("subtractS2S4S6Autofluorescence");
        panel.add(subtractAutofluorescenceButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 10;
        autoflAFField = new JTextField("0", 5);
        autoflAFField.setHorizontalAlignment(JTextField.RIGHT);
        autoflAFField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        panel.add(autoflAFField, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 11;
        setDonorThresholdButton = new JButton("Set threshold for donor channel");
        setDonorThresholdButton.addActionListener(this);
        setDonorThresholdButton.setActionCommand("setS2S4S6DonorThreshold");
        panel.add(setDonorThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 11;
        resetDonorThresholdButton = new JButton("Reset");
        resetDonorThresholdButton.setToolTipText("Resets threshold settings for donor channel");
        resetDonorThresholdButton.addActionListener(this);
        resetDonorThresholdButton.setActionCommand("resetS2S4S6DonorThreshold");
        panel.add(resetDonorThresholdButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 12;
        setTransferThresholdButton = new JButton("Set threshold for transfer channel");
        setTransferThresholdButton.addActionListener(this);
        setTransferThresholdButton.setActionCommand("setS2S4S6TransferThreshold");
        panel.add(setTransferThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 12;
        resetTransferThresholdButton = new JButton("Reset");
        resetTransferThresholdButton.setToolTipText("Resets threshold settings for transfer channel");
        resetTransferThresholdButton.addActionListener(this);
        resetTransferThresholdButton.setActionCommand("resetS2S4S6TransferThreshold");
        panel.add(resetTransferThresholdButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 13;
        setAcceptorThresholdButton = new JButton("Set threshold for acceptor channel");
        setAcceptorThresholdButton.addActionListener(this);
        setAcceptorThresholdButton.setActionCommand("setS2S4S6AcceptorThreshold");
        panel.add(setAcceptorThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 13;
        resetAcceptorThresholdButton = new JButton("Reset");
        resetAcceptorThresholdButton.setToolTipText("Resets threshold settings for acceptor channel");
        resetAcceptorThresholdButton.addActionListener(this);
        resetAcceptorThresholdButton.setActionCommand("resetS2S4S6AcceptorThreshold");
        panel.add(resetAcceptorThresholdButton, gc);

        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 14;
        setAutofluorescenceThresholdButton = new JButton("Set threshold for autofluor. channel");
        setAutofluorescenceThresholdButton.addActionListener(this);
        setAutofluorescenceThresholdButton.setActionCommand("setS2S4S6AutofluorescenceThreshold");
        panel.add(setAutofluorescenceThresholdButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 2;
        gc.gridy = 14;
        resetAutofluorescenceThresholdButton = new JButton("Reset");
        resetAutofluorescenceThresholdButton.setToolTipText("Resets threshold settings for autofluorescence channel");
        resetAutofluorescenceThresholdButton.addActionListener(this);
        resetAutofluorescenceThresholdButton.setActionCommand("resetS2S4S6AutofluorescenceThreshold");
        panel.add(resetAutofluorescenceThresholdButton, gc);

        gc.gridx = 0;
        gc.gridy = 15;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0, 4, 4, 4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel resultLabel = new JLabel("Results (S2 S4 S6):");
        s2ResultLabel = new JLabel("", JLabel.CENTER);
        s4ResultLabel = new JLabel("", JLabel.CENTER);
        s6ResultLabel = new JLabel("", JLabel.CENTER);
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
        gcr.gridx = 3;
        gcr.gridy = 0;
        radioPanel.add(s6ResultLabel, gcr);
        panel.add(radioPanel, gc);

        gc.gridx = 0;
        gc.gridy = 16;
        showSImagesCB = new JCheckBox("Show S2, S4 and S6 images (for manual calc.)");
        panel.add(showSImagesCB, gc);

        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 17;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);

        gc.gridx = 1;
        gc.gridy = 17;
        setButton = new JButton("Set S2, S4 and S6");
        setButton.addActionListener(this);
        setButton.setActionCommand("setfactor");
        panel.add(setButton, gc);

        gc.gridx = 2;
        gc.gridy = 17;
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
                    autofluorescenceImg = null;
                    setDonorButton.setBackground(mainWindow.originalButtonColor);
                    setDonorButton.setOpaque(false);
                    setDonorButton.setBorderPainted(true);
                    setTransferButton.setBackground(mainWindow.originalButtonColor);
                    setTransferButton.setOpaque(false);
                    setTransferButton.setBorderPainted(true);
                    setAcceptorButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorButton.setOpaque(false);
                    setAcceptorButton.setBorderPainted(true);
                    setAutofluorescenceButton.setBackground(mainWindow.originalButtonColor);
                    setAutofluorescenceButton.setOpaque(false);
                    setAutofluorescenceButton.setBorderPainted(true);
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
                    subtractAutofluorescenceButton.setBackground(mainWindow.originalButtonColor);
                    subtractAutofluorescenceButton.setOpaque(false);
                    subtractAutofluorescenceButton.setBorderPainted(true);
                    setDonorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setDonorThresholdButton.setOpaque(false);
                    setDonorThresholdButton.setBorderPainted(true);
                    setTransferThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setTransferThresholdButton.setOpaque(false);
                    setTransferThresholdButton.setBorderPainted(true);
                    setAcceptorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorThresholdButton.setOpaque(false);
                    setAcceptorThresholdButton.setBorderPainted(true);
                    setAutofluorescenceThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAutofluorescenceThresholdButton.setOpaque(false);
                    setAutofluorescenceThresholdButton.setBorderPainted(true);
                    calculateButton.setBackground(mainWindow.originalButtonColor);
                    calculateButton.setOpaque(false);
                    calculateButton.setBorderPainted(true);
                    setButton.setBackground(mainWindow.originalButtonColor);
                    setButton.setOpaque(false);
                    setButton.setBorderPainted(true);
                    s2ResultLabel.setText("");
                    s4ResultLabel.setText("");
                    s6ResultLabel.setText("");
                    break;
                case "setS2S4S6Donor":
                    donorImg = WindowManager.getCurrentImage();
                    if (donorImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (donorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + donorImg.getImageStackSize() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        donorImg = null;
                        return;
                    } else if (donorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + donorImg.getNSlices() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        donorImg = null;
                        return;
                    }
                    mainWindow.log("Set " + donorImg.getTitle() + " as donor channel. (S2/S4/S6 calc.)");
                    donorImg.setTitle("Donor channel (S2/S4/S6 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(donorImg).convertToGray32();
                    setDonorButton.setBackground(mainWindow.greenColor);
                    setDonorButton.setOpaque(true);
                    setDonorButton.setBorderPainted(false);
                    break;
                case "setS2S4S6Transfer":
                    transferImg = WindowManager.getCurrentImage();
                    if (transferImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (transferImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + transferImg.getImageStackSize() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        transferImg = null;
                        return;
                    } else if (transferImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + transferImg.getNSlices() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        transferImg = null;
                        return;
                    }
                    mainWindow.log("Set " + transferImg.getTitle() + " as transfer channel. (S2/S4/S6 calc.)");
                    transferImg.setTitle("Transfer channel (S2/S4/S6 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(transferImg).convertToGray32();
                    setTransferButton.setBackground(mainWindow.greenColor);
                    setTransferButton.setOpaque(true);
                    setTransferButton.setBorderPainted(false);
                    break;
                case "setS2S4S6Acceptor":
                    acceptorImg = WindowManager.getCurrentImage();
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (acceptorImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + acceptorImg.getImageStackSize() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        acceptorImg = null;
                        return;
                    } else if (acceptorImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + acceptorImg.getNSlices() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        acceptorImg = null;
                        return;
                    }
                    mainWindow.log("Set " + acceptorImg.getTitle() + " as acceptor channel. (S2/S4/S6 calc.)");
                    acceptorImg.setTitle("Acceptor channel (S2/S4/S6 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(acceptorImg).convertToGray32();
                    setAcceptorButton.setBackground(mainWindow.greenColor);
                    setAcceptorButton.setOpaque(true);
                    setAcceptorButton.setBorderPainted(false);
                    break;
                case "setS2S4S6Autofluorescence":
                    autofluorescenceImg = WindowManager.getCurrentImage();
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is selected. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (autofluorescenceImg.getImageStackSize() > 1) {
                        mainWindow.logError("Current image contains more than 1 channel (" + autofluorescenceImg.getImageStackSize() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        autofluorescenceImg = null;
                        return;
                    } else if (autofluorescenceImg.getNSlices() > 1) {
                        mainWindow.logError("Current image contains more than 1 slice (" + autofluorescenceImg.getNSlices() + "). Please split it into parts. (S2/S4/S6 calc.)");
                        autofluorescenceImg = null;
                        return;
                    }
                    mainWindow.log("Set " + autofluorescenceImg.getTitle() + " as autofluorescence channel. (S2/S4/S6 calc.)");
                    autofluorescenceImg.setTitle("Autofluorescence channel (S2/S4/S6 calc.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(autofluorescenceImg).convertToGray32();
                    setAutofluorescenceButton.setBackground(mainWindow.greenColor);
                    setAutofluorescenceButton.setOpaque(true);
                    setAutofluorescenceButton.setBorderPainted(false);
                    break;
                case "subtractS2S4S6Donor": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4/S6 calc.)");
                    } else if (autoflDInDField.getText().trim().isEmpty()) {
                        autoflDInDField.setText("0");
                    } else if (donorImg.getRoi() == null && autoflDInDField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI is defined for donor channel. (S2/S4/S6 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (donorImg.getRoi() == null && !autoflDInDField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflDInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflDInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipD = donorImg.getProcessor();
                        int width = donorImg.getWidth();
                        int height = donorImg.getHeight();
                        donorImgSave = new ImageStack(ipD.getWidth(), ipD.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipD.getPixelValue(x, y);
                                value -= autofl;
                                ipD.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipD.getWidth(), ipD.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipD.duplicate());
                        donorImgSave.addSlice("" + 1, flp);
                        donorImg.updateAndDraw();
                        donorImgSave.setColorModel(ipD.getColorModel());
                        mainWindow.log("Subtracted autofluorescence " + "(" + df.format(autofl) + ") of donor channel. (S2/S4/S6 calc.)");
                        subtractDonorButton.setBackground(mainWindow.greenColor);
                        subtractDonorButton.setOpaque(true);
                        subtractDonorButton.setBorderPainted(false);
                    } else if (donorImg.getRoi() != null) { // if donor channel ROI is selected
                        float autofl = 0;
                        if (!autoflDInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflDInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipD = donorImg.getProcessor();
                        int width = donorImg.getWidth();
                        int height = donorImg.getHeight();
                        donorImgSave = new ImageStack(ipD.getWidth(), ipD.getHeight());
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
                        backgroundAvgD += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipD.getPixelValue(x, y);
                                value -= backgroundAvgD;
                                ipD.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipD.getWidth(), ipD.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipD.duplicate());
                        donorImgSave.addSlice("" + 1, flp);
                        donorImg.updateAndDraw();
                        donorImg.killRoi();
                        donorImgSave.setColorModel(ipD.getColorModel());
                        mainWindow.log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvgD) + ") of donor channel. (S2/S4/S6 calc.)");
                        subtractDonorButton.setBackground(mainWindow.greenColor);
                        subtractDonorButton.setOpaque(true);
                        subtractDonorButton.setBorderPainted(false);
                    }
                    break;
                }
                case "subtractS2S4S6Transfer": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4/S6 calc.)");
                    } else if (autoflAInDField.getText().trim().isEmpty()) {
                        autoflAInDField.setText("0");
                    } else if (transferImg.getRoi() == null && autoflAInDField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI is defined for transfer channel. (S2/S4/S6 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (transferImg.getRoi() == null && !autoflAInDField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflAInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipT = transferImg.getProcessor();
                        int width = transferImg.getWidth();
                        int height = transferImg.getHeight();
                        transferImgSave = new ImageStack(ipT.getWidth(), ipT.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipT.getPixelValue(x, y);
                                value -= autofl;
                                ipT.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipT.getWidth(), ipT.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipT.duplicate());
                        transferImgSave.addSlice("" + 1, flp);
                        transferImg.updateAndDraw();
                        transferImgSave.setColorModel(ipT.getColorModel());
                        mainWindow.log("Subtracted autofluorescence " + "(" + df.format(autofl) + ") of transfer channel. (S2/S4/S6 calc.)");
                        subtractTransferButton.setBackground(mainWindow.greenColor);
                        subtractTransferButton.setOpaque(true);
                        subtractTransferButton.setBorderPainted(false);
                    } else if (transferImg.getRoi() != null) { // if transfer channel ROI is selected
                        float autofl = 0;
                        if (!autoflAInDField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInDField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipT = transferImg.getProcessor();
                        int width = transferImg.getWidth();
                        int height = transferImg.getHeight();
                        transferImgSave = new ImageStack(ipT.getWidth(), ipT.getHeight());
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
                        backgroundAvgT += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipT.getPixelValue(x, y);
                                value -= backgroundAvgT;
                                ipT.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipT.getWidth(), ipT.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipT.duplicate());
                        transferImgSave.addSlice("" + 1, flp);
                        transferImg.updateAndDraw();
                        transferImg.killRoi();
                        transferImgSave.setColorModel(ipT.getColorModel());
                        mainWindow.log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvgT) + ") of transfer channel. (S2/S4/S6 calc.)");
                        subtractTransferButton.setBackground(mainWindow.greenColor);
                        subtractTransferButton.setOpaque(true);
                        subtractTransferButton.setBorderPainted(false);
                    }
                    break;
                }
                case "subtractS2S4S6Acceptor": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4/S6 calc.)");
                    } else if (autoflAInAField.getText().trim().isEmpty()) {
                        autoflAInAField.setText("0");
                    } else if (acceptorImg.getRoi() == null && autoflAInAField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI is defined for acceptor channel. (S2/S4/S6 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (acceptorImg.getRoi() == null && !autoflAInAField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflAInAField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInAField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipA = acceptorImg.getProcessor();
                        int width = acceptorImg.getWidth();
                        int height = acceptorImg.getHeight();
                        acceptorImgSave = new ImageStack(ipA.getWidth(), ipA.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipA.getPixelValue(x, y);
                                value -= autofl;
                                ipA.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipA.getWidth(), ipA.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipA.duplicate());
                        acceptorImgSave.addSlice("" + 1, flp);
                        acceptorImg.updateAndDraw();
                        acceptorImgSave.setColorModel(ipA.getColorModel());
                        mainWindow.log("Subtracted autofluorescence " + "(" + df.format(autofl) + ") of acceptor channel. (S2/S4/S6 calc.)");
                        subtractAcceptorButton.setBackground(mainWindow.greenColor);
                        subtractAcceptorButton.setOpaque(true);
                        subtractAcceptorButton.setBorderPainted(false);
                    } else if (acceptorImg.getRoi() != null) {
                        float autofl = 0;
                        if (!autoflAInAField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAInAField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipA = acceptorImg.getProcessor();
                        int width = acceptorImg.getWidth();
                        int height = acceptorImg.getHeight();
                        acceptorImgSave = new ImageStack(ipA.getWidth(), ipA.getHeight());
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
                        backgroundAvgA += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipA.getPixelValue(x, y);
                                value -= backgroundAvgA;
                                ipA.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipA.getWidth(), ipA.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipA.duplicate());
                        acceptorImgSave.addSlice("" + 1, flp);
                        acceptorImg.updateAndDraw();
                        acceptorImg.killRoi();
                        acceptorImgSave.setColorModel(ipA.getColorModel());
                        mainWindow.log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvgA) + ") of acceptor channel. (S2/S4/S6 calc.)");
                        subtractAcceptorButton.setBackground(mainWindow.greenColor);
                        subtractAcceptorButton.setOpaque(true);
                        subtractAcceptorButton.setBorderPainted(false);
                    }
                    break;
                }
                case "subtractS2S4S6Autofluorescence": {
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S2/S4/S6 calc.)");
                    } else if (autoflAFField.getText().trim().isEmpty()) {
                        autoflAFField.setText("0");
                    } else if (autofluorescenceImg.getRoi() == null && autoflAFField.getText().trim().equals("0")) {
                        mainWindow.logError("No ROI is defined for autofluorescence channel. (S2/S4/S6 calc.)");
                        // if autofluorescence value other than zero is entered, and no ROI is selected
                    } else if (autofluorescenceImg.getRoi() == null && !autoflAFField.getText().trim().equals("0")) {
                        float autofl = 0;
                        if (!autoflAFField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAFField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipAF = autofluorescenceImg.getProcessor();
                        int width = autofluorescenceImg.getWidth();
                        int height = autofluorescenceImg.getHeight();
                        autofluorescenceImgSave = new ImageStack(ipAF.getWidth(), ipAF.getHeight());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipAF.getPixelValue(x, y);
                                value -= autofl;
                                ipAF.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipAF.getWidth(), ipAF.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipAF.duplicate());
                        autofluorescenceImgSave.addSlice("" + 1, flp);
                        autofluorescenceImg.updateAndDraw();
                        autofluorescenceImgSave.setColorModel(ipAF.getColorModel());
                        mainWindow.log("Subtracted autofluorescence " + "(" + df.format(autofl) + ") of autofluorescence channel. (S2/S4/S6 calc.)");
                        subtractAutofluorescenceButton.setBackground(mainWindow.greenColor);
                        subtractAutofluorescenceButton.setOpaque(true);
                        subtractAutofluorescenceButton.setBorderPainted(false);
                    } else if (autofluorescenceImg.getRoi() != null) {
                        float autofl = 0;
                        if (!autoflAFField.getText().trim().isEmpty()) {
                            autofl = Float.parseFloat(autoflAFField.getText().trim());
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        ImageProcessor ipAF = autofluorescenceImg.getProcessor();
                        int width = autofluorescenceImg.getWidth();
                        int height = autofluorescenceImg.getHeight();
                        autofluorescenceImgSave = new ImageStack(ipAF.getWidth(), ipAF.getHeight());
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (autofluorescenceImg.getRoi().contains(i, j)) {
                                    sum += ipAF.getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvgAF = (float) (sum / count);
                        backgroundAvgAF += autofl;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                float value = ipAF.getPixelValue(x, y);
                                value -= backgroundAvgAF;
                                ipAF.putPixelValue(x, y, value);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(ipAF.getWidth(), ipAF.getHeight());
                        flp.setPixels(1, (FloatProcessor) ipAF.duplicate());
                        autofluorescenceImgSave.addSlice("" + 1, flp);
                        autofluorescenceImg.updateAndDraw();
                        autofluorescenceImg.killRoi();
                        autofluorescenceImgSave.setColorModel(ipAF.getColorModel());
                        mainWindow.log("Subtracted background " + (autofl > 0 ? "and AF " : "") + "(" + df.format(backgroundAvgAF) + ") of autofluorescence channel. (S2/S4/S6 calc.)");
                        subtractAutofluorescenceButton.setBackground(mainWindow.greenColor);
                        subtractAutofluorescenceButton.setOpaque(true);
                        subtractAutofluorescenceButton.setBorderPainted(false);
                    }
                    break;
                }
                case "setS2S4S6DonorThreshold":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    IJ.selectWindow(donorImg.getTitle());
                    IJ.run("Threshold...");
                    setDonorThresholdButton.setBackground(mainWindow.greenColor);
                    setDonorThresholdButton.setOpaque(true);
                    setDonorThresholdButton.setBorderPainted(false);
                    break;
                case "setS2S4S6TransferThreshold":
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    IJ.selectWindow(transferImg.getTitle());
                    IJ.run("Threshold...");
                    setTransferThresholdButton.setBackground(mainWindow.greenColor);
                    setTransferThresholdButton.setOpaque(true);
                    setTransferThresholdButton.setBorderPainted(false);
                    break;
                case "setS2S4S6AcceptorThreshold":
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    IJ.selectWindow(acceptorImg.getTitle());
                    IJ.run("Threshold...");
                    setAcceptorThresholdButton.setBackground(mainWindow.greenColor);
                    setAcceptorThresholdButton.setOpaque(true);
                    setAcceptorThresholdButton.setBorderPainted(false);
                    break;
                case "setS2S4S6AutofluorescenceThreshold":
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    IJ.selectWindow(autofluorescenceImg.getTitle());
                    IJ.run("Threshold...");
                    setAutofluorescenceThresholdButton.setBackground(mainWindow.greenColor);
                    setAutofluorescenceThresholdButton.setOpaque(true);
                    setAutofluorescenceThresholdButton.setBorderPainted(false);
                    break;
                case "resetS2S4S6DonorThreshold": {
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (donorImgSave == null) {
                        mainWindow.logError("No saved image. (S2/S4/S6 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(donorImgSave.getWidth(), donorImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(donorImgSave.getProcessor(1).getWidth(), donorImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) donorImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    donorImg.setStack(donorImg.getTitle(), newStack);
                    donorImg.getProcessor().setColorModel(donorImgSave.getColorModel());
                    donorImg.updateAndDraw();
                    setDonorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setDonorThresholdButton.setOpaque(false);
                    setDonorThresholdButton.setBorderPainted(true);
                    break;
                }
                case "resetS2S4S6TransferThreshold": {
                    if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (transferImgSave == null) {
                        mainWindow.logError("No saved image. (S2/S4/S6 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(transferImgSave.getWidth(), transferImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(transferImgSave.getProcessor(1).getWidth(), transferImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) transferImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    transferImg.setStack(transferImg.getTitle(), newStack);
                    transferImg.getProcessor().setColorModel(transferImgSave.getColorModel());
                    transferImg.updateAndDraw();
                    setTransferThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setTransferThresholdButton.setOpaque(false);
                    setTransferThresholdButton.setBorderPainted(true);
                    break;
                }
                case "resetS2S4S6AcceptorThreshold": {
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (acceptorImgSave == null) {
                        mainWindow.logError("No saved image. (S2/S4/S6 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(acceptorImgSave.getWidth(), acceptorImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(acceptorImgSave.getProcessor(1).getWidth(), acceptorImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) acceptorImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    acceptorImg.setStack(acceptorImg.getTitle(), newStack);
                    acceptorImg.getProcessor().setColorModel(acceptorImgSave.getColorModel());
                    acceptorImg.updateAndDraw();
                    setAcceptorThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAcceptorThresholdButton.setOpaque(false);
                    setAcceptorThresholdButton.setBorderPainted(true);
                    break;
                }
                case "resetS2S4S6AutofluorescenceThreshold": {
                    if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (autofluorescenceImgSave == null) {
                        mainWindow.logError("No saved image. (S2/S4/S6 calc.)");
                        return;
                    }
                    ImageStack newStack = new ImageStack(autofluorescenceImgSave.getWidth(), autofluorescenceImgSave.getHeight());
                    FloatProcessor flp = new FloatProcessor(autofluorescenceImgSave.getProcessor(1).getWidth(), autofluorescenceImgSave.getProcessor(1).getHeight());
                    flp.setPixels(1, (FloatProcessor) autofluorescenceImgSave.getProcessor(1).duplicate());
                    newStack.addSlice("" + 1, flp);
                    autofluorescenceImg.setStack(autofluorescenceImg.getTitle(), newStack);
                    autofluorescenceImg.getProcessor().setColorModel(autofluorescenceImgSave.getColorModel());
                    autofluorescenceImg.updateAndDraw();
                    setAutofluorescenceThresholdButton.setBackground(mainWindow.originalButtonColor);
                    setAutofluorescenceThresholdButton.setOpaque(false);
                    setAutofluorescenceThresholdButton.setBorderPainted(true);
                    break;
                }
                case "calculate":
                    if (donorImg == null) {
                        mainWindow.logError("No image is set as donor channel. (S2/S4/S6 calc.)");
                    } else if (transferImg == null) {
                        mainWindow.logError("No image is set as transfer channel. (S2/S4/S6 calc.)");
                    } else if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4/S6 calc.)");
                    } else if (autofluorescenceImg == null) {
                        mainWindow.logError("No image is set as autofluorescence channel. (S2/S4/S6 calc.)");
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        ImageProcessor ipDP = donorImg.getProcessor();
                        ImageProcessor ipTP = transferImg.getProcessor();
                        ImageProcessor ipAP = acceptorImg.getProcessor();
                        ImageProcessor ipAFP = autofluorescenceImg.getProcessor();
                        double s2c = 0;
                        double s4c = 0;
                        double s6c = 0;
                        double countc = 0;
                        float[][] imgS2Points = null;
                        float[][] imgS4Points = null;
                        float[][] imgS6Points = null;
                        int width = ipDP.getWidth();
                        int height = ipDP.getHeight();
                        if (showSImagesCB.isSelected()) {
                            imgS2Points = new float[width][height];
                            imgS4Points = new float[width][height];
                            imgS6Points = new float[width][height];
                        }
                        float currentS2 = 0;
                        float currentS4 = 0;
                        float currentS6 = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                currentS2 = ipTP.getPixelValue(i, j) / ipAP.getPixelValue(i, j);
                                currentS4 = ipDP.getPixelValue(i, j) / ipAP.getPixelValue(i, j);
                                currentS6 = ipAFP.getPixelValue(i, j) / ipAP.getPixelValue(i, j);
                                if (!Float.isNaN(currentS2) && !Float.isNaN(currentS4) && !Float.isNaN(currentS6)) {
                                    s2c += currentS2;
                                    s4c += currentS4;
                                    s6c += currentS6;
                                    countc++;
                                }
                                if (showSImagesCB.isSelected()) {
                                    imgS2Points[i][j] = currentS2;
                                    imgS4Points[i][j] = currentS4;
                                    imgS6Points[i][j] = currentS6;
                                }
                            }
                        }
                        if (showSImagesCB.isSelected()) {
                            ImagePlus s2Img = new ImagePlus("S2 image", new FloatProcessor(imgS2Points));
                            s2Img.show();
                            ImagePlus s4Img = new ImagePlus("S4 image", new FloatProcessor(imgS4Points));
                            s4Img.show();
                            ImagePlus s6Img = new ImagePlus("S6 image", new FloatProcessor(imgS6Points));
                            s6Img.show();
                        }
                        float avgS2 = (float) (s2c / countc);
                        float avgS4 = (float) (s4c / countc);
                        float avgS6 = (float) (s6c / countc);
                        s2ResultLabel.setText(df.format(avgS2));
                        s4ResultLabel.setText(df.format(avgS4));
                        s6ResultLabel.setText(df.format(avgS6));
                        calculateButton.setBackground(mainWindow.greenColor);
                        calculateButton.setOpaque(true);
                        calculateButton.setBorderPainted(false);
                        donorImg.changes = false;
                        transferImg.changes = false;
                        acceptorImg.changes = false;
                        autofluorescenceImg.changes = false;
                    }
                    break;
                case "setfactor":
                    if (s2ResultLabel.getText().isEmpty() || s4ResultLabel.getText().isEmpty() || s6ResultLabel.getText().isEmpty()) {
                        mainWindow.logError("S2, S4 and S6 have to be calculated before setting them. (S2/S4/S6 calc.)");
                        return;
                    }
                    mainWindow.setS2Factor(s2ResultLabel.getText());
                    mainWindow.setS4Factor(s4ResultLabel.getText());
                    mainWindow.setS6Factor(s6ResultLabel.getText());
                    setButton.setBackground(mainWindow.greenColor);
                    setButton.setOpaque(true);
                    setButton.setBorderPainted(false);
                    mainWindow.calculateS2S4S6Button.setBackground(mainWindow.greenColor);
                    mainWindow.calculateS2S4S6Button.setOpaque(true);
                    mainWindow.calculateS2S4S6Button.setBorderPainted(false);
                    break;
                case "copyS2S4S6Roi":
                    if (acceptorImg == null) {
                        mainWindow.logError("No image is set as acceptor channel. (S2/S4/S6 calc.)");
                        return;
                    }
                    if (acceptorImg.getRoi() != null) {
                        if (donorImg != null) {
                            donorImg.setRoi(acceptorImg.getRoi());
                        }
                        if (transferImg != null) {
                            transferImg.setRoi(acceptorImg.getRoi());
                        }
                        if (autofluorescenceImg != null) {
                            autofluorescenceImg.setRoi(acceptorImg.getRoi());
                        }
                        copyRoiButton.setBackground(mainWindow.greenColor);
                        copyRoiButton.setOpaque(true);
                        copyRoiButton.setBorderPainted(false);
                    } else {
                        if (donorImg != null) {
                            donorImg.killRoi();
                        }
                        if (transferImg != null) {
                            transferImg.killRoi();
                        }
                        if (autofluorescenceImg != null) {
                            autofluorescenceImg.killRoi();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}
