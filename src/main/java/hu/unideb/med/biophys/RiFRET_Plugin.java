/*-
 * #%L
 * an ImageJ plugin for intensity-based three-filter set (ratiometric) FRET.
 * %%
 * Copyright (C) 2009 - 2020 RiFRET developers.
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
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.BrowserLauncher;
import ij.plugin.HyperStackConverter;
import ij.plugin.StackEditor;
import ij.plugin.WindowOrganizer;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.GaussianBlur;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackConverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class RiFRET_Plugin extends JFrame implements ActionListener, WindowListener {

    private final String imagejVersion = "2.0.0-rc-69";
    private final String imagej1Version = "1.52p";
    private final String javaVersion = "1.8.0_202";
    private final int windowWidth = 730;
    private final int windowHeight = 820;
    private ImagePlus donorInDImage;
    private ImagePlus donorInAImage;
    private ImagePlus acceptorInAImage;
    private ImagePlus autofluorescenceImage;
    private ImagePlus transferImage = null;
    private ImageStack donorInDImageSave = null;
    private ImageStack donorInAImageSave = null;
    private ImageStack acceptorInAImageSave = null;
    private ImageStack autofluorescenceImageSave = null;
    private ResultsTable resultsTable;
    private Analyzer analyzer;
    private ApplyMaskRiDialog applyMaskRiDialog;
    private CalculateRatioDialog calculateRatioDialog;
    private AutoflDialog autoflDialog;
    private S1S3Dialog s1S3Dialog;
    private S1S3S5Dialog s1S3S5Dialog;
    private S2S4Dialog s2S4Dialog;
    private S2S4S6Dialog s2S4S6Dialog;
    private B1B2B3Dialog b1B2B3Dialog;
    private AlphaDialog alphaDialog;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu imageMenu;
    private JMenu correctionsMenu;
    private JMenu helpMenu;
    private JMenuItem openMenuItem;
    private JMenuItem saveTiffMenuItem;
    private JMenuItem saveBmpMenuItem;
    private JMenuItem splitMenuItem;
    private JMenuItem tileMenuItem;
    private JMenuItem applyMaskMenuItem;
    private JMenuItem registerMenuItem;
    private JMenuItem calculateRatioMenuItem;
    private JMenuItem calculateAFMenuItem;
    private JMenuItem thresholdMenuItem;
    private JMenuItem lutFireMenuItem;
    private JMenuItem lutSpectrumMenuItem;
    private JMenuItem histogramMenuItem;
    private JMenuItem convertMenuItem;
    private JMenuItem blurMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem helpMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem saveMessagesMenuItem;
    private JMenuItem clearMessagesMenuItem;
    private JMenuItem loadParametersMenuItem;
    private JMenuItem saveParametersMenuItem;
    private JMenuItem semiAutomaticMenuItem;
    private JMenuItem resetImagesMenuItem;
    private JCheckBoxMenuItem debugMenuItem;
    private JButton setDonorInDImageButton;
    private JButton setDonorInAImageButton;
    private JButton setAcceptorInAImageButton;
    private JButton setAutofluorescenceImageButton;
    private JButton subtractDonorInDImageButton;
    private JButton subtractDonorInAImageButton;
    private JButton subtractAcceptorInAImageButton;
    private JButton subtractAutofluorescenceImageButton;
    private JButton thresholdDonorInDImageButton;
    private JButton thresholdDonorInAImageButton;
    private JButton thresholdAcceptorInAImageButton;
    private JButton thresholdAutofluorescenceImageButton;
    private JButton smoothDonorInDImageButton;
    private JButton smoothDonorInAImageButton;
    private JButton smoothAcceptorInAImageButton;
    private JButton smoothAutofluorescenceImageButton;
    private JButton openImageButton;
    private JButton resetDDButton;
    private JButton resetDAButton;
    private JButton resetAAButton;
    private JButton resetAFButton;
    private JButton copyRoiButton;
    private JLabel s5Label;
    private JLabel s6Label;
    private JLabel b1Label;
    private JLabel b2Label;
    private JLabel b3Label;
    private JLabel eRatioLabel;
    private JLabel setAutofluorescenceImageLabel;
    private JLabel subtractAutofluorescenceImageLabel;
    private JLabel smoothAutofluorescenceImageLabel;
    private JLabel thresholdAutofluorescenceImageLabel;
    private JTextField autoflAFField;
    public JTextField autoflDInDField;
    public JTextField autoflAInDField;
    public JTextField autoflAInAField;
    private JTextField sigmaFieldDD;
    private JTextField sigmaFieldDA;
    private JTextField sigmaFieldAA;
    private JTextField sigmaFieldAF;
    public JTextField autoThresholdMin;
    public JTextField autoThresholdMax;
    private JButton createButton;
    private JButton saveButton;
    private JButton measureButton;
    private JButton nextButton;
    private JButton closeImagesButton;
    private JCheckBox useImageStacks;
    private JCheckBox autoThresholdingCB;
    private JCheckBox eRatioCheckbox;
    private JTextField s1Field;
    private JTextField s2Field;
    private JTextField s3Field;
    private JTextField s4Field;
    private JTextField s5Field;
    private JTextField s6Field;
    private JTextField b1Field;
    private JTextField b2Field;
    private JTextField b3Field;
    private JTextField alphaField;
    private JTextField eRatioField;
    public JButton calculateS1S3Button;
    public JButton calculateS1S3S5Button;
    public JButton calculateS2S4Button;
    public JButton calculateS2S4S6Button;
    public JButton calculateB1B2B3Button;
    public JButton calculateAlphaButton;
    private JTextPane log;
    private JScrollPane logScrollPane;
    private final DateTimeFormatter dateTimeFormat;
    private final DateTimeFormatter timeFormat;
    private File[] automaticallyProcessedFiles = null;
    private int currentlyProcessedFile = 0;
    private String currentlyProcessedFileName = null;
    private String currentDirectory = null;
    public Color originalButtonColor = null;
    public Color greenColor = new Color(142, 207, 125);
    public JCheckBoxMenuItem autofluorescenceCorrectionMenuItem;

    public RiFRET_Plugin() {
        super();
        setTitle("RiFRET - Intensity-Based Three-Filter Set (Ratiometric) FRET");
        IJ.versionLessThan(imagej1Version);
        Locale.setDefault(Locale.ENGLISH);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        createGui();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(windowWidth, windowHeight);
        pack();
        setLocation(screen.width - getWidth(), screen.height / 2 - getHeight() / 2);
        setVisible(true);
        currentDirectory = System.getProperty("user.home");
        originalButtonColor = setDonorInDImageButton.getBackground();
        openImageButton.requestFocus();
    }

    public void createGui() {
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        imageMenu = new JMenu("Image");
        menuBar.add(imageMenu);
        correctionsMenu = new JMenu("Corrections");
        menuBar.add(correctionsMenu);
        autofluorescenceCorrectionMenuItem = new JCheckBoxMenuItem("Pixel-wise Autofluorescence Correction");
        autofluorescenceCorrectionMenuItem.setSelected(false);
        autofluorescenceCorrectionMenuItem.setActionCommand("pwAutofluorescenceCorrection");
        autofluorescenceCorrectionMenuItem.addActionListener(this);
        correctionsMenu.add(autofluorescenceCorrectionMenuItem);
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        openMenuItem = new JMenuItem("Open...");
        openMenuItem.setActionCommand("openImage");
        openMenuItem.addActionListener(this);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(openMenuItem);
        fileMenu.addSeparator();
        saveTiffMenuItem = new JMenuItem("Save as Tiff...");
        saveTiffMenuItem.setActionCommand("saveImageAsTiff");
        saveTiffMenuItem.addActionListener(this);
        fileMenu.add(saveTiffMenuItem);
        saveBmpMenuItem = new JMenuItem("Save as BMP...");
        saveBmpMenuItem.setActionCommand("saveImageAsBmp");
        saveBmpMenuItem.addActionListener(this);
        fileMenu.add(saveBmpMenuItem);
        fileMenu.addSeparator();
        saveMessagesMenuItem = new JMenuItem("Save Messages...");
        saveMessagesMenuItem.setActionCommand("saveMessages");
        saveMessagesMenuItem.addActionListener(this);
        fileMenu.add(saveMessagesMenuItem);
        clearMessagesMenuItem = new JMenuItem("Clear Messages");
        clearMessagesMenuItem.setActionCommand("clearMessages");
        clearMessagesMenuItem.addActionListener(this);
        fileMenu.add(clearMessagesMenuItem);
        fileMenu.addSeparator();
        loadParametersMenuItem = new JMenuItem("Load Parameters from CSV...");
        loadParametersMenuItem.setActionCommand("loadParameters");
        loadParametersMenuItem.addActionListener(this);
        fileMenu.add(loadParametersMenuItem);
        saveParametersMenuItem = new JMenuItem("Save Parameters to CSV...");
        saveParametersMenuItem.setActionCommand("saveParameters");
        saveParametersMenuItem.addActionListener(this);
        fileMenu.add(saveParametersMenuItem);
        fileMenu.addSeparator();
        semiAutomaticMenuItem = new JMenuItem("Semi-Automatic Processing...");
        semiAutomaticMenuItem.setActionCommand("semiAutomaticProcessing");
        semiAutomaticMenuItem.addActionListener(this);
        fileMenu.add(semiAutomaticMenuItem);
        fileMenu.addSeparator();
        resetImagesMenuItem = new JMenuItem("Reset All");
        resetImagesMenuItem.setActionCommand("resetImages");
        resetImagesMenuItem.addActionListener(this);
        fileMenu.add(resetImagesMenuItem);
        splitMenuItem = new JMenuItem("Stack to Images");
        splitMenuItem.setActionCommand("split");
        splitMenuItem.addActionListener(this);
        imageMenu.add(splitMenuItem);
        imageMenu.addSeparator();
        tileMenuItem = new JMenuItem("Tile Image Windows");
        tileMenuItem.setActionCommand("tile");
        tileMenuItem.addActionListener(this);
        imageMenu.add(tileMenuItem);
        imageMenu.addSeparator();
        applyMaskMenuItem = new JMenuItem("Apply Mask...");
        applyMaskMenuItem.setActionCommand("applyMask");
        applyMaskMenuItem.addActionListener(this);
        imageMenu.add(applyMaskMenuItem);
        registerMenuItem = new JMenuItem("Register to Donor Channel");
        registerMenuItem.setActionCommand("registerTransferCh");
        registerMenuItem.addActionListener(this);
        imageMenu.add(registerMenuItem);
        calculateRatioMenuItem = new JMenuItem("Calculate Ratio of Images...");
        calculateRatioMenuItem.setActionCommand("calculateRatio");
        calculateRatioMenuItem.addActionListener(this);
        imageMenu.add(calculateRatioMenuItem);
        calculateAFMenuItem = new JMenuItem("Calculate Autofluorescence...");
        calculateAFMenuItem.setActionCommand("calculateAF");
        calculateAFMenuItem.addActionListener(this);
        imageMenu.add(calculateAFMenuItem);
        imageMenu.addSeparator();
        convertMenuItem = new JMenuItem("Convert Image to 32-bit");
        convertMenuItem.setActionCommand("convertto32bit");
        convertMenuItem.addActionListener(this);
        imageMenu.add(convertMenuItem);
        imageMenu.addSeparator();
        blurMenuItem = new JMenuItem("Gaussian Blur...");
        blurMenuItem.setActionCommand("gaussianblur-menu");
        blurMenuItem.addActionListener(this);
        imageMenu.add(blurMenuItem);
        imageMenu.addSeparator();
        thresholdMenuItem = new JMenuItem("Threshold...");
        thresholdMenuItem.setActionCommand("threshold");
        thresholdMenuItem.addActionListener(this);
        thresholdMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        imageMenu.add(thresholdMenuItem);
        imageMenu.addSeparator();
        histogramMenuItem = new JMenuItem("Histogram");
        histogramMenuItem.setActionCommand("histogram");
        histogramMenuItem.addActionListener(this);
        imageMenu.add(histogramMenuItem);
        imageMenu.addSeparator();
        lutFireMenuItem = new JMenuItem("LUT Fire");
        lutFireMenuItem.setActionCommand("lutFire");
        lutFireMenuItem.addActionListener(this);
        imageMenu.add(lutFireMenuItem);
        lutSpectrumMenuItem = new JMenuItem("LUT Spectrum");
        lutSpectrumMenuItem.setActionCommand("lutSpectrum");
        lutSpectrumMenuItem.addActionListener(this);
        imageMenu.add(lutSpectrumMenuItem);
        exitMenuItem = new JMenuItem("Quit RiFRET");
        exitMenuItem.setActionCommand("exit");
        exitMenuItem.addActionListener(this);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setActionCommand("help");
        helpMenuItem.addActionListener(this);
        helpMenu.add(helpMenuItem);
        helpMenu.addSeparator();
        aboutMenuItem = new JMenuItem("About RiFRET");
        aboutMenuItem.setActionCommand("about");
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);
        helpMenu.addSeparator();
        debugMenuItem = new JCheckBoxMenuItem("Debug Mode");
        debugMenuItem.setSelected(false);
        debugMenuItem.setActionCommand("debugmode");
        debugMenuItem.addActionListener(this);
        helpMenu.add(debugMenuItem);
        setJMenuBar(menuBar);
        addWindowListener(this);
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        Container contentPane = getContentPane();
        JScrollPane mainScrollPane = new JScrollPane();
        JPanel container = new JPanel();
        container.setLayout(gridbaglayout);

        JPanel donorInDImageBleachingPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        donorInDImageBleachingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // S1 Factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(new JLabel("Calculate / set S1 factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 0;
        s1Field = new JTextField("", 4);
        s1Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s1Field, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 0;
        calculateS1S3Button = new JButton("Calculate S1, S3");
        calculateS1S3Button.setMargin(new Insets(2, 1, 2, 1));
        calculateS1S3Button.addActionListener(this);
        calculateS1S3Button.setActionCommand("calculateS1S3Button");
        container.add(calculateS1S3Button, gc);

        // S1/S3/S5 Dialog
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 0;
        gc.insets = new Insets(2, 2, 2, 2);
        calculateS1S3S5Button = new JButton("Calculate S1, S3, S5");
        calculateS1S3S5Button.setMargin(new Insets(2, 1, 2, 1));
        calculateS1S3S5Button.addActionListener(this);
        calculateS1S3S5Button.setActionCommand("calculateS1S3S5Button");
        container.add(calculateS1S3S5Button, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            calculateS1S3S5Button.setVisible(false);
        }

        // S2 Factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 1;
        container.add(new JLabel("Calculate / set S2 factor:"), gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 1;
        s2Field = new JTextField("", 4);
        s2Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s2Field, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 1;
        calculateS2S4Button = new JButton("Calculate S2, S4");
        calculateS2S4Button.addActionListener(this);
        calculateS2S4Button.setActionCommand("calculateS2S4Button");
        container.add(calculateS2S4Button, gc);

        //  S2/S4/S6
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 1;
        calculateS2S4S6Button = new JButton("Calculate S2, S4, S6");
        calculateS2S4S6Button.addActionListener(this);
        calculateS2S4S6Button.setActionCommand("calculateS2S4S6Button");
        container.add(calculateS2S4S6Button, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            calculateS2S4S6Button.setVisible(false);
        }

        // S3 Factor
        gc.gridwidth = 9;
        gc.insets = new Insets(5, 2, 5, 2);
        gc.gridx = 0;
        gc.gridy = 2;
        container.add(new JLabel("Calculate / set S3 factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 2;
        s3Field = new JTextField("", 4);
        s3Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s3Field, gc);

        // S4 Factor
        gc.gridwidth = 9;
        gc.insets = new Insets(8, 2, 8, 2);
        gc.gridx = 0;
        gc.gridy = 3;
        container.add(new JLabel("Calculate / set S4 factor:"), gc);
        gc.gridwidth = 1;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 9;
        gc.gridy = 3;
        s4Field = new JTextField("", 4);
        s4Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s4Field, gc);

        // S5 factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 4;
        s5Label = new JLabel("Calculate / set S5 factor:");
        container.add(s5Label, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            s5Label.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 4;
        s5Field = new JTextField("", 4);
        s5Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s5Field, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            s5Field.setVisible(false);
        }

        // S6 factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 5;
        s6Label = new JLabel("Calculate / set S6 factor:");
        container.add(s6Label, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            s6Label.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 5;
        s6Field = new JTextField("", 4);
        s6Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s6Field, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            s6Field.setVisible(false);
        }

        // B1 factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 6;
        b1Label = new JLabel("Calculate / set B1 factor:");
        container.add(b1Label, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            b1Label.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 6;
        b1Field = new JTextField("", 4);
        b1Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(b1Field, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            b1Field.setVisible(false);
        }

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 6;
        calculateB1B2B3Button = new JButton("Calculate B1, B2, B3");
        calculateB1B2B3Button.setMargin(new Insets(2, 2, 2, 2));
        calculateB1B2B3Button.addActionListener(this);
        calculateB1B2B3Button.setActionCommand("calculateB1B2B3Button");
        container.add(calculateB1B2B3Button, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            calculateB1B2B3Button.setVisible(false);
        }

        // B2 factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 7;
        b2Label = new JLabel("Calculate / set B2 factor:");
        container.add(b2Label, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            b2Label.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 7;
        b2Field = new JTextField("", 4);
        b2Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(b2Field, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            b2Field.setVisible(false);
        }

        // B3 factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 8;
        b3Label = new JLabel("Calculate / set B3 factor:");
        container.add(b3Label, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            b3Label.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 8;
        b3Field = new JTextField("", 4);
        b3Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(b3Field, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            b3Field.setVisible(false);
        }

        // Ratio of epsilons
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 9;
        eRatioLabel = new JLabel("Calculate / set \u03B5d / \u03B5a:");
        container.add(eRatioLabel, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            eRatioLabel.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 9;
        eRatioField = new JTextField("0", 4);
        eRatioField.setHorizontalAlignment(JTextField.RIGHT);
        eRatioField.setEnabled(false);
        container.add(eRatioField, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            eRatioField.setVisible(false);
        }

        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 9;
        eRatioCheckbox = new JCheckBox("Manual set", false);
        eRatioCheckbox.addActionListener(this);
        eRatioCheckbox.setActionCommand("setEpsratManually");
        container.add(eRatioCheckbox, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            eRatioCheckbox.setVisible(false);
        }

        // Alpha factor
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 10;
        container.add(new JLabel("Calculate / set \u03B1 (alpha) factor:"), gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 10;
        alphaField = new JTextField("", 4);
        alphaField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(alphaField, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 10;
        calculateAlphaButton = new JButton("Calculate \u03B1");
        calculateAlphaButton.addActionListener(this);
        calculateAlphaButton.setActionCommand("calculateAlphaButton");
        container.add(calculateAlphaButton, gc);

        // Separator panel 1
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 11;
        JPanel lineFactors = new JPanel();
        lineFactors.setPreferredSize(new Dimension(windowWidth - 35, 1));
        lineFactors.setBackground(Color.lightGray);
        container.add(lineFactors, gc);

        // Step 1a: Set donor image
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 12;
        gc.fill = GridBagConstraints.NONE;
        useImageStacks = new JCheckBox("Use stack", false);
        useImageStacks.setActionCommand("useImageStacks");
        useImageStacks.addActionListener(this);
        useImageStacks.setToolTipText("<html>If this checkbox is checked, the image stack containing donor, transfer and<BR>acceptor channel images (in this order) are set automatically after opening.<BR>Every previously opened image window will be closed. The results window<BR>can be left opened.</html>");
        donorInDImageBleachingPanel.add(new JLabel("Step 1a: open and set the donor channel image  "));
        donorInDImageBleachingPanel.add(useImageStacks);
        setDonorInDImageButton = new JButton("Set image");
        setDonorInDImageButton.setMargin(new Insets(2, 2, 2, 2));
        setDonorInDImageButton.addActionListener(this);
        setDonorInDImageButton.setActionCommand("setDonorInDImage");
        container.add(donorInDImageBleachingPanel, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 12;
        openImageButton = new JButton("Open");
        openImageButton.setToolTipText("Opens an arbitrary image.");
        openImageButton.setMargin(new Insets(0, 0, 0, 0));
        openImageButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        openImageButton.addActionListener(this);
        openImageButton.setActionCommand("openImage");
        container.add(openImageButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 12;
        container.add(setDonorInDImageButton, gc);

        // Step 1b: Set transfer image
        gc.gridwidth = 10;
        gc.gridx = 6;
        gc.gridy = 13;
        container.add(new JLabel("Step 1b: open and set the transfer channel image"), gc);
        setDonorInAImageButton = new JButton("Set image");
        setDonorInAImageButton.setMargin(new Insets(2, 2, 2, 2));
        setDonorInAImageButton.addActionListener(this);
        setDonorInAImageButton.setActionCommand("setDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 13;
        container.add(setDonorInAImageButton, gc);

        // Step 1c: Set acceptor image
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 14;
        container.add(new JLabel("Step 1c: open and set acceptor channel image"), gc);
        setAcceptorInAImageButton = new JButton("Set image");
        setAcceptorInAImageButton.setMargin(new Insets(2, 2, 2, 2));
        setAcceptorInAImageButton.addActionListener(this);
        setAcceptorInAImageButton.setActionCommand("setAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 14;
        container.add(setAcceptorInAImageButton, gc);

        // Step 1d: Set autofluorescence image
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 15;
        setAutofluorescenceImageLabel = new JLabel("Step 1d: open and set the autofluorescence channel image");
        container.add(setAutofluorescenceImageLabel, gc);
        setAutofluorescenceImageButton = new JButton("Set image");
        setAutofluorescenceImageButton.setMargin(new Insets(2, 2, 2, 2));
        setAutofluorescenceImageButton.addActionListener(this);
        setAutofluorescenceImageButton.setActionCommand("setAutofluorescenceImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 15;
        container.add(setAutofluorescenceImageButton, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            setAutofluorescenceImageButton.setVisible(false);
            setAutofluorescenceImageLabel.setVisible(false);
        }

        // Separator panel 2
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 16;
        JPanel line1 = new JPanel();
        line1.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line1.setBackground(Color.lightGray);
        container.add(line1, gc);

        // Step 2a: Blur donor channel
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 17;
        container.add(new JLabel("Step 2a (optional): blur donor channel image, sigma (radius):"), gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 17;
        sigmaFieldDD = new JTextField("0.8", 4);
        sigmaFieldDD.setHorizontalAlignment(JTextField.RIGHT);
        container.add(sigmaFieldDD, gc);

        smoothDonorInDImageButton = new JButton("Blur");
        smoothDonorInDImageButton.addActionListener(this);
        smoothDonorInDImageButton.setActionCommand("smoothDD");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 17;
        container.add(smoothDonorInDImageButton, gc);

        // Step 2b: Blur transfer channel
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 18;
        container.add(new JLabel("Step 2b (optional): blur transfer channel image, sigma (radius):"), gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 18;
        sigmaFieldDA = new JTextField("0.8", 4);
        sigmaFieldDA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(sigmaFieldDA, gc);

        smoothDonorInAImageButton = new JButton("Blur");
        smoothDonorInAImageButton.addActionListener(this);
        smoothDonorInAImageButton.setActionCommand("smoothDA");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 18;
        container.add(smoothDonorInAImageButton, gc);

        // Step 2c: Blur acceptor channel
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 19;
        container.add(new JLabel("Step 2c (optional): blur acceptor channel image, sigma (radius):"), gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 19;
        sigmaFieldAA = new JTextField("0.8", 4);
        sigmaFieldAA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(sigmaFieldAA, gc);

        smoothAcceptorInAImageButton = new JButton("Blur");
        smoothAcceptorInAImageButton.addActionListener(this);
        smoothAcceptorInAImageButton.setActionCommand("smoothAA");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 19;
        container.add(smoothAcceptorInAImageButton, gc);

        // Step 2d: Blur autofluorescence channel
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 20;
        smoothAutofluorescenceImageLabel = new JLabel("Step 2d (optional): blur autofl. channel image, sigma (radius):");
        container.add(smoothAutofluorescenceImageLabel, gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 20;
        sigmaFieldAF = new JTextField("0.8", 4);
        sigmaFieldAF.setHorizontalAlignment(JTextField.RIGHT);
        container.add(sigmaFieldAF, gc);

        smoothAutofluorescenceImageButton = new JButton("Blur");
        smoothAutofluorescenceImageButton.addActionListener(this);
        smoothAutofluorescenceImageButton.setActionCommand("smoothAF");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 20;
        container.add(smoothAutofluorescenceImageButton, gc);

        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            smoothAutofluorescenceImageLabel.setVisible(false);
            sigmaFieldAF.setVisible(false);
            smoothAutofluorescenceImageButton.setVisible(false);
        }

        // Separator panel 3
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 21;
        JPanel line3 = new JPanel();
        line3.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line3.setBackground(Color.lightGray);
        container.add(line3, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 22;
        JLabel backgroundInfo = new JLabel("Subtract average instrument background (and autofluorescence):");
        container.add(backgroundInfo, gc);
        gc.gridx = 10;
        gc.gridy = 22;
        JLabel autofluorescenceInfo = new JLabel("Autofluor.:");
        container.add(autofluorescenceInfo, gc);
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 23;
        container.add(new JLabel("Step 3a: subtract from donor channel"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 23;
        copyRoiButton = new JButton("Copy ROI");
        copyRoiButton.setToolTipText("Sets the same ROI for the two other images.");
        copyRoiButton.setMargin(new Insets(0, 0, 0, 0));
        copyRoiButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyRoi");
        container.add(copyRoiButton, gc);
        autoflDInDField = new JTextField("0", 5);
        autoflDInDField.setHorizontalAlignment(JTextField.RIGHT);
        autoflDInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated by<br>choosing <i>Image ▶ Calculate Autofluorescence...</i><html>");
        gc.gridx = 10;
        gc.gridy = 23;
        container.add(autoflDInDField, gc);
        subtractDonorInDImageButton = new JButton("Subtract");
        subtractDonorInDImageButton.addActionListener(this);
        subtractDonorInDImageButton.setActionCommand("subtractDonorInDImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 23;
        container.add(subtractDonorInDImageButton, gc);

        // Step 3b: Transfer channel background subtraction
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 24;
        container.add(new JLabel("Step 3b: subtract from transfer channel"), gc);
        autoflAInDField = new JTextField("0", 5);
        autoflAInDField.setHorizontalAlignment(JTextField.RIGHT);
        autoflAInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated by<br>choosing <i>Image ▶ Calculate Autofluorescence...</i><html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 24;
        container.add(autoflAInDField, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        subtractDonorInAImageButton = new JButton("Subtract");
        subtractDonorInAImageButton.addActionListener(this);
        subtractDonorInAImageButton.setActionCommand("subtractDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 24;
        container.add(subtractDonorInAImageButton, gc);

        // Step 3c: Acceptor channel background subtraction
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 25;
        container.add(new JLabel("Step 3c: subtract from acceptor channel"), gc);
        autoflAInAField = new JTextField("0", 5);
        autoflAInAField.setHorizontalAlignment(JTextField.RIGHT);
        autoflAInAField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated by<br>choosing <i>Image ▶ Calculate Autofluorescence...</i><html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 25;
        container.add(autoflAInAField, gc);
        subtractAcceptorInAImageButton = new JButton("Subtract");
        subtractAcceptorInAImageButton.addActionListener(this);
        subtractAcceptorInAImageButton.setActionCommand("subtractAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 25;
        container.add(subtractAcceptorInAImageButton, gc);

        // Step 3d: Autofluorescence channel background subtraction
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 26;
        subtractAutofluorescenceImageLabel = new JLabel("Step 3d: subtract from autofluorescence channel");
        container.add(subtractAutofluorescenceImageLabel, gc);
        autoflAFField = new JTextField("0", 5);
        autoflAFField.setHorizontalAlignment(JTextField.RIGHT);
        autoflAFField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated by<br>choosing <i>Image ▶ Calculate Autofluorescence...</i><html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 26;
        container.add(autoflAFField, gc);
        subtractAutofluorescenceImageButton = new JButton("Subtract");
        subtractAutofluorescenceImageButton.addActionListener(this);
        subtractAutofluorescenceImageButton.setActionCommand("subtractAutofluorescenceImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 26;
        container.add(subtractAutofluorescenceImageButton, gc);

        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            subtractAutofluorescenceImageLabel.setVisible(false);
            subtractAutofluorescenceImageButton.setVisible(false);
            autoflAFField.setVisible(false);
        }

        // Separator panel 4
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 27;
        JPanel line4 = new JPanel();
        line4.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line4.setBackground(Color.lightGray);
        container.add(line4, gc);

        // Threshold information label */
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 28;
        JLabel thInfo = new JLabel("Threshold setting: set threshold, click Apply, then click Set to NaN");
        container.add(thInfo, gc);

        // Step 4a: Threshold donor channel
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 29;
        container.add(new JLabel("Step 4a: set threshold for donor channel image"), gc);

        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 29;
        resetDDButton = new JButton("Reset");
        resetDDButton.setToolTipText("Resets blur and threshold settings");
        resetDDButton.setMargin(new Insets(0, 0, 0, 0));
        resetDDButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDDButton.addActionListener(this);
        resetDDButton.setActionCommand("resetDD");
        container.add(resetDDButton, gc);

        thresholdDonorInDImageButton = new JButton("Set threshold");
        thresholdDonorInDImageButton.addActionListener(this);
        thresholdDonorInDImageButton.setActionCommand("thresholdDonorInDImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 29;
        container.add(thresholdDonorInDImageButton, gc);

        // Step 4b: Threshold transfer channel
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 30;
        container.add(new JLabel("Step 4b: set threshold for transfer channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 30;
        resetDAButton = new JButton("Reset");
        resetDAButton.setToolTipText("Resets blur and threshold settings");
        resetDAButton.setMargin(new Insets(0, 0, 0, 0));
        resetDAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDAButton.addActionListener(this);
        resetDAButton.setActionCommand("resetDA");
        container.add(resetDAButton, gc);
        thresholdDonorInAImageButton = new JButton("Set threshold");
        thresholdDonorInAImageButton.addActionListener(this);
        thresholdDonorInAImageButton.setActionCommand("thresholdDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 30;
        container.add(thresholdDonorInAImageButton, gc);

        // Step 4c: Threshold acceptor channel
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 31;
        container.add(new JLabel("Step 4c: set threshold for acceptor channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 31;
        resetAAButton = new JButton("Reset");
        resetAAButton.setToolTipText("Resets blur and threshold settings");
        resetAAButton.setMargin(new Insets(0, 0, 0, 0));
        resetAAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetAAButton.addActionListener(this);
        resetAAButton.setActionCommand("resetAA");
        container.add(resetAAButton, gc);
        thresholdAcceptorInAImageButton = new JButton("Set threshold");
        thresholdAcceptorInAImageButton.addActionListener(this);
        thresholdAcceptorInAImageButton.setActionCommand("thresholdAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 31;
        container.add(thresholdAcceptorInAImageButton, gc);

        // Step 4d: Threshold autofluorescence channel
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 32;
        thresholdAutofluorescenceImageLabel = new JLabel("Step 4d: set threshold for autofluorescence channel image");
        container.add(thresholdAutofluorescenceImageLabel, gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 32;
        resetAFButton = new JButton("Reset");
        resetAFButton.setToolTipText("Resets blur and threshold settings");
        resetAFButton.setMargin(new Insets(0, 0, 0, 0));
        resetAFButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetAFButton.addActionListener(this);
        resetAFButton.setActionCommand("resetAF");
        container.add(resetAFButton, gc);
        thresholdAutofluorescenceImageButton = new JButton("Set threshold");
        thresholdAutofluorescenceImageButton.addActionListener(this);
        thresholdAutofluorescenceImageButton.setActionCommand("thresholdAutofluorescenceImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 32;
        container.add(thresholdAutofluorescenceImageButton, gc);
        if (!autofluorescenceCorrectionMenuItem.isSelected()) {
            thresholdAutofluorescenceImageLabel.setVisible(false);
            resetAFButton.setVisible(false);
            thresholdAutofluorescenceImageButton.setVisible(false);
        }

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 33;
        JPanel line5 = new JPanel();
        line5.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line5.setBackground(Color.lightGray);
        container.add(line5, gc);

        // Create FRET image panel
        JPanel createFretImgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 34;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.fill = GridBagConstraints.NONE;
        createFretImgPanel.add(new JLabel("Step 5a: create FRET image   "));
        autoThresholdingCB = new JCheckBox("Thresholding with min: ", true);
        autoThresholdingCB.setToolTipText("<html>If this checkbox is checked, the FRET image will be thresholded<br>with the given min and max values to exclude pixels with extreme<br>FRET efficiencies.</html>");
        autoThresholdingCB.setSelected(true);
        createFretImgPanel.add(autoThresholdingCB);
        autoThresholdMin = new JTextField("-2", 2);
        autoThresholdMin.setHorizontalAlignment(JTextField.RIGHT);
        createFretImgPanel.add(autoThresholdMin);
        createFretImgPanel.add(new JLabel(" and max: "));
        autoThresholdMax = new JTextField("2", 2);
        autoThresholdMax.setHorizontalAlignment(JTextField.RIGHT);
        createFretImgPanel.add(autoThresholdMax);
        container.add(createFretImgPanel, gc);
        createButton = new JButton("Create");
        createButton.addActionListener(this);
        createButton.setActionCommand("createFretImage");
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 34;
        container.add(createButton, gc);

        // Save FRET image panel
        JPanel saveFretImgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 35;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.fill = GridBagConstraints.NONE;
        saveFretImgPanel.add(new JLabel("Step 5b: save FRET image as TIFF       "));
        container.add(saveFretImgPanel, gc);
        saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        saveButton.setActionCommand("saveFretImage");
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 35;
        container.add(saveButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 36;
        JPanel line7 = new JPanel();
        line7.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line7.setBackground(Color.lightGray);
        container.add(line7, gc);

        gc.gridwidth = 7;
        gc.gridx = 0;
        gc.gridy = 37;
        container.add(new JLabel("Step 6: select ROIs and make measurements"), gc);
        gc.gridx = 9;
        gc.gridwidth = 1;
        closeImagesButton = new JButton("Close images");
        closeImagesButton.setToolTipText("Closes the source and transfer images and resets button colors");
        closeImagesButton.setMargin(new Insets(0, 0, 0, 0));
        closeImagesButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        closeImagesButton.addActionListener(this);
        closeImagesButton.setActionCommand("closeImages");
        container.add(closeImagesButton, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        measureButton = new JButton("Measure");
        measureButton.setMargin(new Insets(2, 2, 2, 2));
        measureButton.addActionListener(this);
        measureButton.setActionCommand("measureFretImage");
        gc.gridx = 10;
        gc.gridy = 37;
        gc.gridwidth = 6;
        container.add(measureButton, gc);
        nextButton = new JButton("Next");
        nextButton.setMargin(new Insets(2, 2, 2, 2));
        nextButton.addActionListener(this);
        nextButton.setActionCommand("nextImage");
        gc.gridx = 16;
        gc.gridy = 37;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        container.add(nextButton, gc);
        nextButton.setVisible(false);

        // Log panel
        gc.weighty = 20;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        log = new JTextPane();
        log.setEditable(false);
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style style;
        style = log.addStyle("RED", defaultStyle);
        StyleConstants.setForeground(style, Color.red.darker());
        style = log.addStyle("BLUE", defaultStyle);
        StyleConstants.setForeground(style, Color.blue.darker());
        style = log.addStyle("BLACK", defaultStyle);
        StyleConstants.setForeground(style, Color.black.darker());
        logScrollPane = new JScrollPane(log);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Messages"));
        logScrollPane.setPreferredSize(new Dimension(10, 60));
        contentPane.add(logScrollPane, BorderLayout.SOUTH);

        // Main scrollpane
        mainScrollPane.setViewportView(container);
        contentPane.add(mainScrollPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "exit":
                    exit();
                    break;
                case "split":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    if (WindowManager.getCurrentImage().isHyperStack()) {
                        HyperStackConverter hc = new HyperStackConverter();
                        hc.run("hstostack");
                    }
                    StackEditor se = new StackEditor();
                    se.run("toimages");
                    break;
                case "loadParameters": {
                    OpenDialog od = new OpenDialog("Load Parameters from CSV");
                    String directory = od.getDirectory();
                    String name = od.getFileName();
                    if (name == null) {
                        return;
                    }
                    String path = directory + name;
                    try {
                        Reader in = new FileReader(path);
                        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                                .withIgnoreHeaderCase()
                                .withIgnoreSurroundingSpaces()
                                .withFirstRecordAsHeader().parse(in);
                        for (CSVRecord record : records) {
                            String s1 = record.get("S1");
                            String s2 = record.get("S2");
                            String s3 = record.get("S3");
                            String s4 = record.get("S4");
                            String alpha = record.get("ALPHA");

                            if (autofluorescenceCorrectionMenuItem.isEnabled()) {
                                String s5 = record.get("S5");
                                String s6 = record.get("S6");
                                String b1 = record.get("B1");
                                String b2 = record.get("B2");
                                String b3 = record.get("B3");
                                String eRatio = record.get("EPSRAT");

                                String blurAF = record.get("BLUR_AF");
                                String ibgAF = record.get("IBG_AF");

                                setS5Factor(s5);
                                setS6Factor(s6);
                                setB1Factor(b1);
                                setB2Factor(b2);
                                setB3Factor(b3);
                                setERatio(eRatio);

                                sigmaFieldAF.setText(blurAF);
                                autoflAFField.setText(ibgAF);
                            }
                            String blurD = record.get("BLUR_D");
                            String blurT = record.get("BLUR_T");
                            String blurA = record.get("BlUR_A");

                            String ibgD = record.get("IBG_D");
                            String ibgT = record.get("IBG_T");
                            String ibgA = record.get("IBG_A");

                            String thresholdMin = record.get("THRES_MIN");
                            String thresholdMax = record.get("THRES_MAX");

                            setS1Factor(s1);
                            setS2Factor(s2);
                            setS3Factor(s3);
                            setS4Factor(s4);
                            setAlphaFactor(alpha);

                            sigmaFieldDD.setText(blurD);
                            sigmaFieldDA.setText(blurT);
                            sigmaFieldAA.setText(blurA);

                            autoflDInDField.setText(ibgD);
                            autoflAInDField.setText(ibgT);
                            autoflAInAField.setText(ibgA);

                            autoThresholdMin.setText(thresholdMin);
                            autoThresholdMax.setText(thresholdMax);
                        }
                        log("Loaded parameters from: " + path);
                    } catch (IOException ioe) {
                        logError("Could not load parameters from: " + path);
                    }
                    break;
                }
                case "saveParameters": {
                    SaveDialog sd = new SaveDialog("Save Parameters to CSV", "Parameters", ".csv");
                    String directory = sd.getDirectory();
                    String name = sd.getFileName();
                    if (name == null) {
                        return;
                    }
                    String path = directory + name;
                    try {
                        Writer writer = Files.newBufferedWriter(Paths.get(path));
                        CSVPrinter printer = CSVFormat.DEFAULT
                                .withIgnoreSurroundingSpaces()
                                .withHeader(
                                        "S1",
                                        "S2",
                                        "S3",
                                        "S4",
                                        "S5",
                                        "S6",
                                        "B1",
                                        "B2",
                                        "B3",
                                        "EPSRAT",
                                        "ALPHA",
                                        "BLUR_D",
                                        "BLUR_T",
                                        "BLUR_A",
                                        "BLUR_AF",
                                        "IBG_D",
                                        "IBG_T",
                                        "IBG_A",
                                        "IBG_AF",
                                        "THRES_MIN",
                                        "THRES_MAX").print(writer);
                        printer.printRecord(
                                getS1Factor(),
                                getS2Factor(),
                                getS3Factor(),
                                getS4Factor(),
                                getS5Factor(),
                                getS6Factor(),
                                getB1Factor(),
                                getB2Factor(),
                                getB3Factor(),
                                eRatioField.getText(),
                                alphaField.getText(),
                                sigmaFieldDD.getText(),
                                sigmaFieldDA.getText(),
                                sigmaFieldAA.getText(),
                                sigmaFieldAF.getText(),
                                autoflDInDField.getText(),
                                autoflAInDField.getText(),
                                autoflAInAField.getText(),
                                autoflAFField.getText(),
                                autoThresholdMin.getText(),
                                autoThresholdMax.getText());
                        printer.flush();
                        log("Saved parameters to: " + path);
                    } catch (IOException ioe) {
                        logError("Could not save parameters to: " + path);
                    }
                    break;
                }
                case "tile":
                    WindowOrganizer wo = new WindowOrganizer();
                    wo.run("tile");
                    break;
                case "applyMask":
                    if (applyMaskRiDialog != null) {
                        applyMaskRiDialog.setVisible(false);
                        applyMaskRiDialog.dispose();
                    }
                    applyMaskRiDialog = new ApplyMaskRiDialog(this);
                    applyMaskRiDialog.setVisible(true);
                    break;
                case "registerTransferCh":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                    } else if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                    } else {
                        FHT fht1 = new FHT(donorInDImage.getProcessor().duplicate());
                        fht1.setShowProgress(false);
                        fht1.transform();
                        FHT fht2 = new FHT(donorInAImage.getProcessor().duplicate());
                        fht2.setShowProgress(false);
                        fht2.transform();
                        FHT res = fht1.conjugateMultiply(fht2);
                        res.setShowProgress(false);
                        res.inverseTransform();
                        ImagePlus image = new ImagePlus("Result of registration", res);
                        ImageProcessor ip = image.getProcessor();
                        int width = ip.getWidth();
                        int height = ip.getHeight();
                        int maximum = 0;
                        int maxx = -1;
                        int maxy = -1;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ip.getPixel(i, j) > maximum) {
                                    maximum = ip.getPixel(i, j);
                                    maxx = i;
                                    maxy = j;
                                }
                            }
                        }
                        int shiftX = 0;
                        int shiftY = 0;
                        if (maxx != 0 || maxy != 0) {
                            ShiftDialogRi sd = new ShiftDialogRi(this);
                            if (maxy > height / 2) {
                                log("Shifting transfer channel image up " + (height - maxy) + " pixel" + ((height - maxy) > 1 ? "s" : "") + ".");
                                sd.shiftUp(donorInAImage, height - maxy);
                            } else if (maxy != 0) {
                                log("Shifting transfer channel image down " + maxy + " pixel" + (maxy > 1 ? "s" : "") + ".");
                                sd.shiftDown(donorInAImage, maxy);
                            }
                            if (maxx > width / 2) {
                                log("Shifting transfer channel image to the left " + (width - maxx) + " pixel" + ((width - maxx) > 1 ? "s" : "") + ".");
                                sd.shiftLeft(donorInAImage, width - maxx);
                            } else if (maxx != 0) {
                                log("Shifting transfer channel image to the right " + maxx + " pixel" + (maxx > 1 ? "s" : "") + ".");
                                sd.shiftRight(donorInAImage, maxx);
                            }
                            actionPerformed(new ActionEvent(registerMenuItem, 1, "registerTransferCh"));
                        } else {
                            log("Registration of transfer channel has been finished.");
                            actionPerformed(new ActionEvent(registerMenuItem, 1, "registerAcceptorCh"));
                        }
                    }
                    break;
                case "registerAcceptorCh":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                    } else if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                    } else {
                        FHT fht1 = new FHT(donorInDImage.getProcessor().duplicate());
                        fht1.setShowProgress(false);
                        fht1.transform();
                        FHT fht2 = new FHT(acceptorInAImage.getProcessor().duplicate());
                        fht2.setShowProgress(false);
                        fht2.transform();
                        FHT res = fht1.conjugateMultiply(fht2);
                        res.setShowProgress(false);
                        res.inverseTransform();
                        ImagePlus image = new ImagePlus("Result of registration", res);
                        ImageProcessor ip = image.getProcessor();
                        int width = ip.getWidth();
                        int height = ip.getHeight();
                        int maximum = 0;
                        int maxx = -1;
                        int maxy = -1;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ip.getPixel(i, j) > maximum) {
                                    maximum = ip.getPixel(i, j);
                                    maxx = i;
                                    maxy = j;
                                }
                            }
                        }
                        int shiftX = 0;
                        int shiftY = 0;
                        if (maxx != 0 || maxy != 0) {
                            ShiftDialogRi sd = new ShiftDialogRi(this);
                            if (maxy > height / 2) {
                                log("Shifting acceptor channel image up " + (height - maxy) + " pixel" + ((height - maxy) > 1 ? "s" : "") + ".");
                                sd.shiftUp(acceptorInAImage, height - maxy);
                            } else if (maxy != 0) {
                                log("Shifting acceptor channel image down " + maxy + " pixel" + (maxy > 1 ? "s" : "") + ".");
                                sd.shiftDown(acceptorInAImage, maxy);
                            }
                            if (maxx > width / 2) {
                                log("Shifting acceptor channel image to the left " + (width - maxx) + " pixel" + ((width - maxx) > 1 ? "s" : "") + ".");
                                sd.shiftLeft(acceptorInAImage, width - maxx);
                            } else if (maxx != 0) {
                                log("Shifting acceptor channel image to the right " + maxx + " pixel" + (maxx > 1 ? "s" : "") + ".");
                                sd.shiftRight(acceptorInAImage, maxx);
                            }
                            actionPerformed(new ActionEvent(registerMenuItem, 1, "registerAcceptorCh"));
                        } else {
                            log("Registration of acceptor channel has been finished.");
                        }
                    }
                    break;
                case "calculateRatio":
                    if (calculateRatioDialog != null) {
                        calculateRatioDialog.setVisible(false);
                        calculateRatioDialog.dispose();
                    }
                    calculateRatioDialog = new CalculateRatioDialog(this);
                    calculateRatioDialog.setVisible(true);
                    break;
                case "calculateAF":
                    if (autoflDialog != null) {
                        autoflDialog.setVisible(false);
                        autoflDialog.dispose();
                    }
                    autoflDialog = new AutoflDialog(this);
                    autoflDialog.setVisible(true);
                    break;
                case "threshold":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Threshold...");
                    break;
                case "convertto32bit":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    new ImageConverter(WindowManager.getCurrentImage()).convertToGray32();
                    break;
                case "gaussianblur-menu": {
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    String sigmaString = JOptionPane.showInputDialog(this, "Enter sigma (radius) for Gaussian blur", "Gaussian Blur...", JOptionPane.QUESTION_MESSAGE);
                    if (sigmaString == null || sigmaString.trim().isEmpty()) {
                        return;
                    }
                    GaussianBlur gb = new GaussianBlur();
                    int nSlices = WindowManager.getCurrentImage().getImageStackSize();
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        gb.blurGaussian(WindowManager.getCurrentImage().getStack().getProcessor(currentSlice), Float.parseFloat(sigmaString), Float.parseFloat(sigmaString), 0.01);
                    }
                    WindowManager.getCurrentImage().updateAndDraw();
                    log("Gaussian blurred current image (" + WindowManager.getCurrentImage().getTitle() + ") with sigma (radius) " + Float.parseFloat(sigmaString) + " px.");
                    break;
                }
                case "lutFire":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Fire");
                    break;
                case "lutSpectrum":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    IJ.run("Spectrum");
                    break;
                case "histogram":
                    if (WindowManager.getCurrentImage() == null) {
                        logError("No open image.");
                        return;
                    }
                    try {
                        IJ.run("Histogram");
                    } catch (RuntimeException rex) {
                        logError("Histogram canceled.");
                    }
                    break;
                case "openImage":
                    (new Opener()).open();
                    break;
                case "saveImageAsTiff": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    if (image == null) {
                        logError("No open image.");
                        return;
                    }
                    FileSaver fs = new FileSaver(image);
                    if (fs.saveAsTiff()) {
                        log("Saved " + image.getTitle() + ".");
                    }
                    image.updateAndDraw();
                    break;
                }
                case "saveImageAsBmp": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    if (image == null) {
                        logError("No open image.");
                        return;
                    }
                    FileSaver fs = new FileSaver(image);
                    if (fs.saveAsBmp()) {
                        log("Saved " + image.getTitle() + ".");
                    }
                    image.updateAndDraw();
                    break;
                }
                case "saveMessages": {
                    SaveDialog svd = new SaveDialog("Save Messages", "Messages", ".txt");
                    String directory = svd.getDirectory();
                    String name = svd.getFileName();
                    if (name == null) {
                        return;
                    }
                    String path = directory + name;
                    try {
                        try (BufferedWriter out = new BufferedWriter(new FileWriter(path))) {
                            out.write(log.getText());
                            log("Saved messages to: " + path);
                        }
                    } catch (IOException ioe) {
                        logError("Could not save messages.");
                    }
                    break;
                }
                case "clearMessages":
                    log.setText("");
                    break;
                case "openImageStack": {
                    OpenDialog od = new OpenDialog("Open Image Stack");
                    String directory = od.getDirectory();
                    String name = od.getFileName();
                    if (name == null) {
                        return;
                    }
                    String path = directory + name;
                    try {
                        boolean close = false;
                        boolean resultsWindow = false;
                        while (WindowManager.getCurrentImage() != null) {
                            WindowManager.getCurrentImage().close();
                        }

                        resetAllButtonColors();

                        File imageFile = new File(path);
                        (new Opener()).open(imageFile.getAbsolutePath());
                        log("Opened: " + path);
                        WindowManager.putBehind();
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "split"));
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorInAImage"));
                        WindowManager.putBehind();
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInAImage"));
                        WindowManager.putBehind();
                        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInDImage"));
                        if (autofluorescenceCorrectionMenuItem.isSelected()) {
                            WindowManager.putBehind();
                            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAutofluorescenceImage"));
                        }
                    } catch (Exception ex) {
                        logError("Could not open and set the selected image stack.");
                        logException(ex.getMessage(), ex);
                    }
                    break;
                }
                case "setDonorInDImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInAImage != null && donorInAImage.equals(ip)) {
                        logError("This image has already been set as transfer channel.");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                        logError("This image has already been set as acceptor channel.");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInAImage != null && ip.getImageStackSize() != donorInAImage.getImageStackSize()) {
                        logError("Transfer channel contains " + donorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                        logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInDImage = null;
                        setDonorInDImageButton.setBackground(originalButtonColor);
                        setDonorInDImageButton.setOpaque(false);
                        setDonorInDImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    donorInDImage = ip;
                    log("Set " + donorInDImage.getTitle() + " as donor channel.");
                    donorInDImage.setTitle("Donor channel - " + dateTimeFormat.format(OffsetDateTime.now()));
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(donorInDImage).convertToGray32();
                    } else {
                        new ImageConverter(donorInDImage).convertToGray32();
                    }
                    if (automaticallyProcessedFiles == null) {
                        currentlyProcessedFileName = null;
                    }
                    setDonorInDImageButton.setBackground(greenColor);
                    setDonorInDImageButton.setOpaque(true);
                    setDonorInDImageButton.setBorderPainted(false);
                    break;
                }
                case "setDonorInAImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && donorInDImage.equals(ip)) {
                        logError("This image has already been set as donor channel.");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                        logError("This image has already been set as acceptor channel.");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                        logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                        logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        donorInAImage = null;
                        setDonorInAImageButton.setBackground(originalButtonColor);
                        setDonorInAImageButton.setOpaque(false);
                        setDonorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    donorInAImage = ip;
                    log("Set " + donorInAImage.getTitle() + " as transfer channel.");
                    donorInAImage.setTitle("Transfer channel - " + dateTimeFormat.format(OffsetDateTime.now()));
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(donorInAImage).convertToGray32();
                    } else {
                        new ImageConverter(donorInAImage).convertToGray32();
                    }
                    setDonorInAImageButton.setBackground(greenColor);
                    setDonorInAImageButton.setOpaque(true);
                    setDonorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "setAcceptorInAImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && donorInDImage.equals(ip)) {
                        logError("This image has already been set as donor channel.");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);

                        return;
                    } else if (donorInAImage != null && donorInAImage.equals(ip)) {
                        logError("This image has already been set as transfer channel.");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                        logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (donorInAImage != null && ip.getImageStackSize() != donorInAImage.getImageStackSize()) {
                        logError("Transfer channel contains " + donorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        acceptorInAImage = null;
                        setAcceptorInAImageButton.setBackground(originalButtonColor);
                        setAcceptorInAImageButton.setOpaque(false);
                        setAcceptorInAImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    acceptorInAImage = ip;
                    log("Set " + acceptorInAImage.getTitle() + " as acceptor channel.");
                    acceptorInAImage.setTitle("Acceptor channel - " + dateTimeFormat.format(OffsetDateTime.now()));
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(acceptorInAImage).convertToGray32();
                    } else {
                        new ImageConverter(acceptorInAImage).convertToGray32();
                    }
                    setAcceptorInAImageButton.setBackground(greenColor);
                    setAcceptorInAImageButton.setOpaque(true);
                    setAcceptorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "setAutofluorescenceImage": {
                    ImagePlus ip = WindowManager.getCurrentImage();
                    if (ip == null) {
                        logError("No image is selected.");
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    }
                    if (ip.isHyperStack()) {
                        logError("Current image is a hyperstack.");
                        autofluorescenceImage = null;
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && donorInDImage.equals(ip)) {
                        logError("This image has already been set as donor channel.");
                        autofluorescenceImage = null;
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                        logError("This image has already been set as acceptor channel.");
                        autofluorescenceImage = null;
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    } else if (donorInAImage != null && donorInAImage.equals(ip)) {
                        logError("This image has already been set as transfer channel.");
                        autofluorescenceImage = null;
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    }
                    if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                        logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        autofluorescenceImage = null;
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                        logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                        autofluorescenceImage = null;
                        setAutofluorescenceImageButton.setBackground(originalButtonColor);
                        setAutofluorescenceImageButton.setOpaque(false);
                        setAutofluorescenceImageButton.setBorderPainted(true);
                        return;
                    } else if (ip.getImageStackSize() > 1) {
                        logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
                    }
                    autofluorescenceImage = ip;
                    log("Set " + autofluorescenceImage.getTitle() + " as autofluorescence channel.");
                    autofluorescenceImage.setTitle("Autofluorescence channel - " + dateTimeFormat.format(OffsetDateTime.now()));
                    if (ip.getImageStackSize() > 1) {
                        new StackConverter(autofluorescenceImage).convertToGray32();
                    } else {
                        new ImageConverter(autofluorescenceImage).convertToGray32();
                    }
                    setAutofluorescenceImageButton.setBackground(greenColor);
                    setAutofluorescenceImageButton.setOpaque(true);
                    setAutofluorescenceImageButton.setBorderPainted(false);
                    break;
                }
                case "copyRoi":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    }
                    if (donorInDImage.getRoi() != null) {
                        if (donorInAImage != null) {
                            donorInAImage.setRoi(donorInDImage.getRoi());
                        }
                        if (acceptorInAImage != null) {
                            acceptorInAImage.setRoi(donorInDImage.getRoi());
                        }
                        if (autofluorescenceImage != null) {
                            autofluorescenceImage.setRoi(donorInDImage.getRoi());
                        }
                    } else {
                        if (donorInAImage != null) {
                            donorInAImage.killRoi();
                        }
                        if (acceptorInAImage != null) {
                            acceptorInAImage.killRoi();
                        }
                        if (autofluorescenceImage != null) {
                            autofluorescenceImage.killRoi();
                        }
                    }
                    break;
                case "subtractDonorInDImage": {
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    } else if (donorInDImage.getRoi() == null) {
                        logError("No ROI is defined for donor channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflDInDField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflDInDField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = donorInDImage.getWidth();
                    int height = donorInDImage.getHeight();
                    int nSlices = donorInDImage.getImageStackSize();
                    donorInDImageSave = new ImageStack(donorInDImage.getProcessor().getWidth(), donorInDImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (donorInDImage.getRoi().contains(i, j)) {
                                    sum += donorInDImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg = backgroundAvg + autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = donorInDImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                donorInDImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(donorInDImage.getStack().getProcessor(currentSlice).getWidth(), donorInDImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInDImage.getStack().getProcessor(currentSlice).duplicate());
                        donorInDImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of donor channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of donor channel.");
                        }
                    }
                    donorInDImage.updateAndDraw();
                    donorInDImage.killRoi();
                    donorInDImageSave.setColorModel(donorInDImage.getProcessor().getColorModel());
                    subtractDonorInDImageButton.setBackground(greenColor);
                    subtractDonorInDImageButton.setOpaque(true);
                    subtractDonorInDImageButton.setBorderPainted(false);
                    break;
                }
                case "subtractDonorInAImage": {
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    } else if (donorInAImage.getRoi() == null) {
                        logError("No ROI is defined for transfer channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflAInDField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflAInDField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = donorInAImage.getWidth();
                    int height = donorInAImage.getHeight();
                    int nSlices = donorInAImage.getImageStackSize();
                    donorInAImageSave = new ImageStack(donorInAImage.getProcessor().getWidth(), donorInAImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (donorInAImage.getRoi().contains(i, j)) {
                                    sum += donorInAImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg = backgroundAvg + autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = donorInAImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                donorInAImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(donorInAImage.getStack().getProcessor(currentSlice).getWidth(), donorInAImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInAImage.getStack().getProcessor(currentSlice).duplicate());
                        donorInAImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of transfer channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of transfer channel.");
                        }
                    }
                    donorInAImage.updateAndDraw();
                    donorInAImage.killRoi();
                    donorInAImageSave.setColorModel(donorInAImage.getProcessor().getColorModel());
                    subtractDonorInAImageButton.setBackground(greenColor);
                    subtractDonorInAImageButton.setOpaque(true);
                    subtractDonorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "subtractAcceptorInAImage": {
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    } else if (acceptorInAImage.getRoi() == null) {
                        logError("No ROI is defined for acceptor channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflAInAField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflAInAField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = acceptorInAImage.getWidth();
                    int height = acceptorInAImage.getHeight();
                    int nSlices = acceptorInAImage.getImageStackSize();
                    acceptorInAImageSave = new ImageStack(acceptorInAImage.getProcessor().getWidth(), acceptorInAImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (acceptorInAImage.getRoi().contains(i, j)) {
                                    sum += acceptorInAImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg = backgroundAvg + autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = acceptorInAImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                acceptorInAImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(acceptorInAImage.getStack().getProcessor(currentSlice).getWidth(), acceptorInAImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) acceptorInAImage.getStack().getProcessor(currentSlice).duplicate());
                        acceptorInAImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of acceptor channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of acceptor channel.");
                        }
                    }
                    acceptorInAImage.updateAndDraw();
                    acceptorInAImage.killRoi();
                    acceptorInAImageSave.setColorModel(acceptorInAImage.getProcessor().getColorModel());
                    subtractAcceptorInAImageButton.setBackground(greenColor);
                    subtractAcceptorInAImageButton.setOpaque(true);
                    subtractAcceptorInAImageButton.setBorderPainted(false);
                    break;
                }
                case "subtractAutofluorescenceImage": {
                    if (autofluorescenceImage == null) {
                        logError("No image is set as autofluorescence channel.");
                        return;
                    } else if (autofluorescenceImage.getRoi() == null) {
                        logError("No ROI is defined for autofluorescence channel.");
                        return;
                    }
                    float autofl = 0;
                    if (!autoflAFField.getText().trim().isEmpty()) {
                        autofl = Float.parseFloat(autoflAFField.getText().trim());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    int width = autofluorescenceImage.getWidth();
                    int height = autofluorescenceImage.getHeight();
                    int nSlices = autofluorescenceImage.getImageStackSize();
                    autofluorescenceImageSave = new ImageStack(autofluorescenceImage.getProcessor().getWidth(), autofluorescenceImage.getProcessor().getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        double sum = 0;
                        int count = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (autofluorescenceImage.getRoi().contains(i, j)) {
                                    sum += autofluorescenceImage.getStack().getProcessor(currentSlice).getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                        float backgroundAvg = (float) (sum / count);

                        backgroundAvg += autofl;

                        float i = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                i = autofluorescenceImage.getStack().getProcessor(currentSlice).getPixelValue(x, y);
                                i = i - backgroundAvg;
                                if (i < 0) {
                                    i = 0;
                                }
                                autofluorescenceImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
                            }
                        }
                        FloatProcessor flp = new FloatProcessor(autofluorescenceImage.getStack().getProcessor(currentSlice).getWidth(), autofluorescenceImage.getStack().getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) autofluorescenceImage.getStack().getProcessor(currentSlice).duplicate());
                        autofluorescenceImageSave.addSlice("" + currentSlice, flp);
                        if (nSlices == 1) {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of autofluorescence channel.");
                        } else {
                            log("Subtracted background " + (autofl > 0 ? "and autofluorescence " : "") + "(" + df.format(backgroundAvg) + ") of slice " + currentSlice + " of autofluorescence channel.");
                        }
                    }
                    autofluorescenceImage.updateAndDraw();
                    autofluorescenceImage.killRoi();
                    autofluorescenceImageSave.setColorModel(autofluorescenceImage.getProcessor().getColorModel());
                    subtractAutofluorescenceImageButton.setBackground(greenColor);
                    subtractAutofluorescenceImageButton.setOpaque(true);
                    subtractAutofluorescenceImageButton.setBorderPainted(false);
                    break;
                }
                case "thresholdDonorInDImage":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    }
                    IJ.selectWindow(donorInDImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdDonorInDImageButton.setBackground(greenColor);
                    thresholdDonorInDImageButton.setOpaque(true);
                    thresholdDonorInDImageButton.setBorderPainted(false);
                    break;
                case "thresholdDonorInAImage":
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    }
                    IJ.selectWindow(donorInAImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdDonorInAImageButton.setBackground(greenColor);
                    thresholdDonorInAImageButton.setOpaque(true);
                    thresholdDonorInAImageButton.setBorderPainted(false);
                    break;
                case "thresholdAcceptorInAImage":
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    }
                    IJ.selectWindow(acceptorInAImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdAcceptorInAImageButton.setBackground(greenColor);
                    thresholdAcceptorInAImageButton.setOpaque(true);
                    thresholdAcceptorInAImageButton.setBorderPainted(false);
                    break;
                case "thresholdAutofluorescenceImage":
                    if (autofluorescenceImage == null) {
                        logError("No image is set as autofluorescence channel.");
                        return;
                    }
                    IJ.selectWindow(autofluorescenceImage.getTitle());
                    IJ.run("Threshold...");
                    thresholdAutofluorescenceImageButton.setBackground(greenColor);
                    thresholdAutofluorescenceImageButton.setOpaque(true);
                    thresholdAutofluorescenceImageButton.setBorderPainted(false);
                    break;
                case "resetDD": {
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    }
                    if (donorInDImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = donorInDImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(donorInDImageSave.getWidth(), donorInDImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(donorInDImageSave.getProcessor(currentSlice).getWidth(), donorInDImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInDImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    donorInDImage.setStack(donorInDImage.getTitle(), newStack);
                    donorInDImage.getProcessor().setColorModel(donorInDImageSave.getColorModel());
                    donorInDImage.updateAndDraw();
                    thresholdDonorInDImageButton.setBackground(originalButtonColor);
                    thresholdDonorInDImageButton.setOpaque(false);
                    thresholdDonorInDImageButton.setBorderPainted(true);
                    smoothDonorInDImageButton.setBackground(originalButtonColor);
                    smoothDonorInDImageButton.setOpaque(false);
                    smoothDonorInDImageButton.setBorderPainted(true);
                    break;
                }
                case "resetDA": {
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    }
                    if (donorInAImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = donorInAImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(donorInAImageSave.getWidth(), donorInAImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(donorInAImageSave.getProcessor(currentSlice).getWidth(), donorInAImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) donorInAImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    donorInAImage.setStack(donorInAImage.getTitle(), newStack);
                    donorInAImage.getProcessor().setColorModel(donorInAImageSave.getColorModel());
                    donorInAImage.updateAndDraw();
                    thresholdDonorInAImageButton.setBackground(originalButtonColor);
                    thresholdDonorInAImageButton.setOpaque(false);
                    thresholdDonorInAImageButton.setBorderPainted(true);
                    smoothDonorInAImageButton.setBackground(originalButtonColor);
                    smoothDonorInAImageButton.setOpaque(false);
                    smoothDonorInAImageButton.setBorderPainted(true);
                    break;
                }
                case "resetAA": {
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    }
                    if (acceptorInAImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = acceptorInAImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(acceptorInAImageSave.getWidth(), acceptorInAImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(acceptorInAImageSave.getProcessor(currentSlice).getWidth(), acceptorInAImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) acceptorInAImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    acceptorInAImage.setStack(acceptorInAImage.getTitle(), newStack);
                    acceptorInAImage.getProcessor().setColorModel(acceptorInAImageSave.getColorModel());
                    acceptorInAImage.updateAndDraw();
                    thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
                    thresholdAcceptorInAImageButton.setOpaque(false);
                    thresholdAcceptorInAImageButton.setBorderPainted(true);
                    smoothAcceptorInAImageButton.setBackground(originalButtonColor);
                    smoothAcceptorInAImageButton.setOpaque(false);
                    smoothAcceptorInAImageButton.setBorderPainted(true);
                    break;
                }
                case "resetAF": {
                    if (autofluorescenceImage == null) {
                        logError("No image is set as autofluorescence channel.");
                        return;
                    }
                    if (autofluorescenceImageSave == null) {
                        logError("No saved image.");
                        return;
                    }
                    int nSlices = autofluorescenceImage.getImageStackSize();
                    ImageStack newStack = new ImageStack(autofluorescenceImageSave.getWidth(), autofluorescenceImageSave.getHeight());
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        FloatProcessor flp = new FloatProcessor(autofluorescenceImageSave.getProcessor(currentSlice).getWidth(), autofluorescenceImageSave.getProcessor(currentSlice).getHeight());
                        flp.setPixels(currentSlice, (FloatProcessor) autofluorescenceImageSave.getProcessor(currentSlice).duplicate());
                        newStack.addSlice("" + currentSlice, flp);
                    }
                    autofluorescenceImage.setStack(autofluorescenceImage.getTitle(), newStack);
                    autofluorescenceImage.getProcessor().setColorModel(autofluorescenceImageSave.getColorModel());
                    autofluorescenceImage.updateAndDraw();
                    thresholdAutofluorescenceImageButton.setBackground(originalButtonColor);
                    thresholdAutofluorescenceImageButton.setOpaque(false);
                    thresholdAutofluorescenceImageButton.setBorderPainted(true);
                    smoothAutofluorescenceImageButton.setBackground(originalButtonColor);
                    smoothAutofluorescenceImageButton.setOpaque(false);
                    smoothAutofluorescenceImageButton.setBorderPainted(true);
                    break;
                }
                case "smoothDD":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                    } else {
                        if (sigmaFieldDD.getText().trim().isEmpty()) {
                            logError("Sigma (radius) has to be given for Gaussian blur.");
                        } else {
                            double sigma = 0;
                            try {
                                sigma = Double.parseDouble(sigmaFieldDD.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Sigma (radius) has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = donorInDImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                gb.blurGaussian(donorInDImage.getStack().getProcessor(currentSlice), sigma, sigma, 0.01);
                            }
                            donorInDImage.updateAndDraw();
                            smoothDonorInDImageButton.setBackground(greenColor);
                            smoothDonorInDImageButton.setOpaque(true);
                            smoothDonorInDImageButton.setBorderPainted(false);
                            log("Gaussian blurred donor channel with sigma (radius) " + Double.parseDouble(sigmaFieldDD.getText().trim()) + " px.");
                        }
                    }
                    break;
                case "smoothDA":
                    if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                    } else {
                        if (sigmaFieldDA.getText().trim().isEmpty()) {
                            logError("Sigma (radius) has to be given for Gaussian blur.");
                        } else {
                            double sigma = 0;
                            try {
                                sigma = Double.parseDouble(sigmaFieldDA.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Sigma (radius) has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = donorInAImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                gb.blurGaussian(donorInAImage.getStack().getProcessor(currentSlice), sigma, sigma, 0.01);
                            }
                            donorInAImage.updateAndDraw();
                            smoothDonorInAImageButton.setBackground(greenColor);
                            smoothDonorInAImageButton.setOpaque(true);
                            smoothDonorInAImageButton.setBorderPainted(false);
                            log("Gaussian blurred transfer channel with sigma (radius) " + Double.parseDouble(sigmaFieldDA.getText().trim()) + " px.");
                        }
                    }
                    break;
                case "smoothAA":
                    if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                    } else {
                        if (sigmaFieldAA.getText().trim().isEmpty()) {
                            logError("Sigma (radius) has to be given for Gaussian blur.");
                        } else {
                            double sigma = 0;
                            try {
                                sigma = Double.parseDouble(sigmaFieldAA.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Sigma (radius) has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = acceptorInAImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                gb.blurGaussian(acceptorInAImage.getStack().getProcessor(currentSlice), sigma, sigma, 0.01);
                            }
                            acceptorInAImage.updateAndDraw();
                            smoothAcceptorInAImageButton.setBackground(greenColor);
                            smoothAcceptorInAImageButton.setOpaque(true);
                            smoothAcceptorInAImageButton.setBorderPainted(false);
                            log("Gaussian blurred acceptor channel with sigma (radius) " + Double.parseDouble(sigmaFieldAA.getText().trim()) + " px.");
                        }
                    }
                    break;
                case "smoothAF":
                    if (autofluorescenceImage == null) {
                        logError("No image is set as autofluorescence channel.");
                    } else {
                        if (sigmaFieldAF.getText().trim().isEmpty()) {
                            logError("Sigma (radius) has to be given for Gaussian blur.");
                        } else {
                            double sigma = 0;
                            try {
                                sigma = Double.parseDouble(sigmaFieldAF.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Sigma (radius) has to be given for Gaussian blur.");
                                return;
                            }
                            GaussianBlur gb = new GaussianBlur();
                            int nSlices = autofluorescenceImage.getImageStackSize();
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                gb.blurGaussian(autofluorescenceImage.getStack().getProcessor(currentSlice), sigma, sigma, 0.01);
                            }
                            autofluorescenceImage.updateAndDraw();
                            smoothAutofluorescenceImageButton.setBackground(greenColor);
                            smoothAutofluorescenceImageButton.setOpaque(true);
                            smoothAutofluorescenceImageButton.setBorderPainted(false);
                            log("Gaussian blurred autofluorescence channel with sigma (radius) " + Double.parseDouble(sigmaFieldAF.getText().trim()) + " px.");
                        }
                    }
                    break;
                case "useImageStacks":
                    if (useImageStacks.isSelected()) {
                        setDonorInDImageButton.setText("Open & set stack");
                        setDonorInDImageButton.setActionCommand("openImageStack");
                        setDonorInAImageButton.setEnabled(false);
                        setAcceptorInAImageButton.setEnabled(false);
                        setAutofluorescenceImageButton.setEnabled(false);
                    } else {
                        setDonorInDImageButton.setText("Set image");
                        setDonorInDImageButton.setActionCommand("setDonorInDImage");
                        setDonorInAImageButton.setEnabled(true);
                        setAcceptorInAImageButton.setEnabled(true);
                        setAutofluorescenceImageButton.setEnabled(true);
                    }
                    break;
                case "calculateS1S3Button":
                    if (s1S3Dialog != null) {
                        s1S3Dialog.setVisible(false);
                        s1S3Dialog.dispose();
                    }
                    s1S3Dialog = new S1S3Dialog(this);
                    s1S3Dialog.setVisible(true);
                    break;
                case "calculateS1S3S5Button":
                    if (s1S3S5Dialog != null) {
                        s1S3S5Dialog.setVisible(false);
                        s1S3S5Dialog.dispose();
                    }
                    s1S3S5Dialog = new S1S3S5Dialog(this);
                    s1S3S5Dialog.setVisible(true);
                    break;
                case "calculateS2S4Button":
                    if (s2S4Dialog != null) {
                        s2S4Dialog.setVisible(false);
                        s2S4Dialog.dispose();
                    }
                    s2S4Dialog = new S2S4Dialog(this);
                    s2S4Dialog.setVisible(true);
                    break;
                case "calculateS2S4S6Button":
                    if (s2S4S6Dialog != null) {
                        s2S4S6Dialog.setVisible(false);
                        s2S4S6Dialog.dispose();
                    }
                    s2S4S6Dialog = new S2S4S6Dialog(this);
                    s2S4S6Dialog.setVisible(true);
                    break;
                case "calculateB1B2B3Button":
                    if (b1B2B3Dialog != null) {
                        b1B2B3Dialog.setVisible(false);
                        b1B2B3Dialog.dispose();
                    }
                    b1B2B3Dialog = new B1B2B3Dialog(this);
                    b1B2B3Dialog.setVisible(true);
                    break;
                case "setEpsratManually":
                    if (eRatioCheckbox.isSelected()) {
                        eRatioField.setEnabled(true);
                    } else {
                        eRatioField.setEnabled(false);
                    }
                    break;
                case "calculateAlphaButton":
                    if (alphaDialog != null) {
                        alphaDialog.setVisible(true);
                    } else {
                        alphaDialog = new AlphaDialog(this);
                        alphaDialog.setVisible(true);
                    }
                    break;
                case "createFretImage":
                    if (donorInDImage == null) {
                        logError("No image is set as donor channel.");
                        return;
                    } else if (donorInAImage == null) {
                        logError("No image is set as transfer channel.");
                        return;
                    } else if (acceptorInAImage == null) {
                        logError("No image is set as acceptor channel.");
                        return;
                    } else {
                        if (autoThresholdingCB.isSelected()) {
                            if (autoThresholdMin.getText().trim().isEmpty()) {
                                logError("Auto-threshold min value has to be given.");
                                return;
                            } else if (autoThresholdMax.getText().trim().isEmpty()) {
                                logError("Auto-threshold max value has to be given.");
                                return;
                            }
                        }
                        if (s1Field.getText().trim().isEmpty()) {
                            logError("S1 factor has to be given.");
                            return;
                        } else if (s2Field.getText().trim().isEmpty()) {
                            logError("S2 factor has to be given.");
                            return;
                        } else if (s3Field.getText().trim().isEmpty()) {
                            logError("S3 factor has to be given.");
                            return;
                        } else if (s4Field.getText().trim().isEmpty()) {
                            logError("S4 factor has to be given.");
                            return;
                        } else if (alphaField.getText().trim().isEmpty()) {
                            logError("Alpha factor has to be given.");
                            return;
                        } else {
                            double s1Factor = 0;
                            try {
                                s1Factor = Double.parseDouble(s1Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S1 factor has to be given.");
                                return;
                            }
                            if (s1Factor <= 0) {
                                logWarning("S1 factor should be higher than 0.");
                            }
                            double s2Factor = 0;
                            try {
                                s2Factor = Double.parseDouble(s2Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S2 factor has to be given.");
                                return;
                            }
                            if (s2Factor <= 0) {
                                logWarning("S2 factor should be higher than 0.");
                            }
                            double s3Factor = 0;
                            try {
                                s3Factor = Double.parseDouble(s3Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S3 factor has to be given.");
                                return;
                            }
                            if (s3Factor < 0) {
                                logWarning("S3 factor should be higher than 0.");
                            }
                            double s4Factor = 0;
                            try {
                                s4Factor = Double.parseDouble(s4Field.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("S4 factor has to be given.");
                                return;
                            }
                            if (s4Factor < 0) {
                                logWarning("S4 factor should be higher than 0.");
                            }
                            double alphaFactor = 0;
                            try {
                                alphaFactor = Double.parseDouble(alphaField.getText().trim());
                            } catch (NumberFormatException ex) {
                                logError("Alpha factor has to be given.");
                                return;
                            }

                            double autoThMin = 0;
                            double autoThMax = 0;
                            if (autoThresholdingCB.isSelected()) {
                                try {
                                    autoThMin = Double.parseDouble(autoThresholdMin.getText().trim());
                                } catch (NumberFormatException ex) {
                                    logError("Auto-threshold min value has to be given.");
                                    return;
                                }

                                try {
                                    autoThMax = Double.parseDouble(autoThresholdMax.getText().trim());
                                } catch (NumberFormatException ex) {
                                    logError("Auto-threshold max value has to be given.");
                                    return;
                                }
                            }

                            int nSlices = donorInDImage.getImageStackSize();
                            ImageStack transferStack = new ImageStack(donorInDImage.getProcessor().getWidth(), donorInDImage.getProcessor().getHeight());
                            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                                ImageProcessor ipDD = donorInDImage.getStack().getProcessor(currentSlice).duplicate();
                                ImageProcessor ipDA = donorInAImage.getStack().getProcessor(currentSlice).duplicate();
                                ImageProcessor ipAA = acceptorInAImage.getStack().getProcessor(currentSlice).duplicate();

                                float[] ipDDP = (float[]) ipDD.getPixels();
                                float[] ipDAP = (float[]) ipDA.getPixels();
                                float[] ipAAP = (float[]) ipAA.getPixels();

                                if (autofluorescenceCorrectionMenuItem.isSelected()) {
                                    ImageProcessor ipAF = autofluorescenceImage.getStack().getProcessor(currentSlice).duplicate();

                                    float[] ipAFP = (float[]) ipAF.getPixels();

                                    double s5Factor = 0;
                                    try {
                                        s5Factor = Double.parseDouble(s5Field.getText().trim());
                                    } catch (NumberFormatException ex) {
                                        logError("S5 factor has to be given.");
                                        return;
                                    }
                                    if (s5Factor < 0) {
                                        logWarning("S5 factor should be higher than 0.");
                                    }
                                    double s6Factor = 0;
                                    try {
                                        s6Factor = Double.parseDouble(s6Field.getText().trim());
                                    } catch (NumberFormatException ex) {
                                        logError("S6 factor has to be given.");
                                        return;
                                    }
                                    if (s6Factor < 0) {
                                        logWarning("S6 factor should be higher than 0.");
                                    }
                                    double b1Factor = 0;
                                    try {
                                        b1Factor = Double.parseDouble(b1Field.getText().trim());
                                    } catch (NumberFormatException ex) {
                                        logError("B1 factor has to be given.");
                                        return;
                                    }
                                    if (b1Factor < 0) {
                                        logWarning("B1 factor should be higher than 0.");
                                    }
                                    double b2Factor = 0;
                                    try {
                                        b2Factor = Double.parseDouble(b2Field.getText().trim());
                                    } catch (NumberFormatException ex) {
                                        logError("B2 factor has to be given.");
                                        return;
                                    }
                                    if (b2Factor < 0) {
                                        logWarning("B2 factor should be higher than 0.");
                                    }
                                    double b3Factor = 0;
                                    try {
                                        b3Factor = Double.parseDouble(b3Field.getText().trim());
                                    } catch (NumberFormatException ex) {
                                        logError("B3 factor has to be given.");
                                        return;
                                    }
                                    if (b3Factor < 0) {
                                        logWarning("B3 factor should be higher than 0.");
                                    }
                                    double eRatio = 0;
                                    try {
                                        eRatio = Double.parseDouble(eRatioField.getText().trim());
                                    } catch (NumberFormatException ex) {
                                        logError("Ratio of epsilons has to be given.");
                                        return;
                                    }
                                    if (eRatio < 0) {
                                        logWarning("Ratio of epsilons should be higher than 0.");
                                    }
                                    for (int i = 0; i < ipDDP.length; i++) {
                                        if (!Float.isNaN(ipDDP[i]) && !Float.isNaN(ipDAP[i]) && !Float.isNaN(ipAAP[i]) && !Float.isNaN(ipAFP[i])) {
                                            ipDDP[i] = (float) ((s2Factor * (b2Factor * ipAFP[i] + ipDDP[i] * s1Factor + ipAAP[i] * s2Factor - s2Factor * (b3Factor * ipAFP[i] + ipDDP[i] * s3Factor) + b1Factor * ipAFP[i] * (-s1Factor + s2Factor * s3Factor) + (-ipAAP[i] + b3Factor * ipAFP[i]) * s1Factor * s4Factor + (b3Factor * ipDDP[i] - b1Factor * ipAAP[i]) * (s2Factor * s5Factor - s1Factor * s6Factor) + ipDAP[i] * (-1 + b1Factor * s5Factor - b3Factor * s4Factor * s5Factor + b3Factor * s6Factor + s3Factor * (s4Factor - b1Factor * s6Factor)) - b2Factor * (ipAFP[i] * s3Factor * s4Factor + ipAAP[i] * (-s4Factor * s5Factor + s6Factor) + ipDDP[i] * (s5Factor - s3Factor * s6Factor)))) / (alphaFactor * (-1 + eRatio) * (-b1Factor * ipAFP[i] * s2Factor - ipDAP[i] * s4Factor + b2Factor * ipAFP[i] * s4Factor + b1Factor * ipDAP[i] * s6Factor + ipDDP[i] * (s2Factor - b2Factor * s6Factor)) + s2Factor * (b2Factor * ipAFP[i] + ipDDP[i] * s1Factor + ipAAP[i] * s2Factor - s2Factor * (b3Factor * ipAFP[i] + ipDDP[i] * s3Factor) + b1Factor * ipAFP[i] * (-s1Factor + s2Factor * s3Factor) + (-ipAAP[i] + b3Factor * ipAFP[i]) * s1Factor * s4Factor + (b3Factor * ipDDP[i] - b1Factor * ipAAP[i]) * (s2Factor * s5Factor - s1Factor * s6Factor) + ipDAP[i] * (-1 + b1Factor * s5Factor - b3Factor * s4Factor * s5Factor + b3Factor * s6Factor + s3Factor * (s4Factor - b1Factor * s6Factor)) - b2Factor * (ipAFP[i] * s3Factor * s4Factor + ipAAP[i] * (-s4Factor * s5Factor + s6Factor) + ipDDP[i] * (s5Factor - s3Factor * s6Factor)))));
                                        } else {
                                            ipDDP[i] = Float.NaN;
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < ipDDP.length; i++) {
                                        if (!Float.isNaN(ipDDP[i]) && !Float.isNaN(ipDAP[i]) && !Float.isNaN(ipAAP[i])) {
                                            ipDDP[i] = (float) ((s1Factor * s2Factor * (ipDAP[i] * (1 - s3Factor * s4Factor) - ipDDP[i] * (s1Factor - s2Factor * s3Factor) - ipAAP[i] * (s2Factor - s1Factor * s4Factor))) / ((s1Factor - s2Factor * s3Factor) * (ipDDP[i] * s2Factor - ipDAP[i] * s4Factor) * alphaFactor));
                                        } else {
                                            ipDDP[i] = Float.NaN;
                                        }
                                    }

                                    for (int i = 0; i < ipDDP.length; i++) {
                                        ipDDP[i] = ipDDP[i] / ((float) 1 + ipDDP[i]);
                                    }
                                }

                                int width = ipDD.getWidth();
                                int height = ipDD.getHeight();
                                float[][] tiPoints = new float[width][height];
                                for (int i = 0; i < width; i++) {
                                    for (int j = 0; j < height; j++) {
                                        if (autoThresholdingCB.isSelected()) {
                                            if (ipDDP[width * j + i] >= autoThMin && ipDDP[width * j + i] <= autoThMax) {
                                                tiPoints[i][j] = ipDDP[width * j + i];
                                            } else {
                                                tiPoints[i][j] = Float.NaN;
                                            }
                                        } else {
                                            tiPoints[i][j] = ipDDP[width * j + i];
                                        }
                                    }
                                }

                                FloatProcessor tiFp = new FloatProcessor(tiPoints);
                                transferStack.addSlice("" + currentSlice, tiFp);
                            }
                            if (transferImage != null) {
                                transferImage.close();
                            }
                            transferImage = new ImagePlus("Transfer (FRET) image", transferStack);
                            transferImage.setCalibration(donorInDImage.getCalibration());
                            transferImage.show();

                            analyzer = new Analyzer();
                            resultsTable = Analyzer.getResultsTable();
                            resultsTable.setPrecision(3);
                            resultsTable.incrementCounter();
                            int widthTi = transferImage.getWidth();
                            int heightTi = transferImage.getHeight();
                            int currentRow = resultsTable.getCounter();
                            if (currentlyProcessedFileName != null) {
                                resultsTable.setValue("File", currentRow, currentlyProcessedFileName);
                            }
                            if (transferImage.getRoi() != null) {
                                Roi roi = transferImage.getRoi();
                                int count = 0;
                                int notNan = 0;
                                for (int i = 0; i < widthTi; i++) {
                                    for (int j = 0; j < heightTi; j++) {
                                        if (roi.contains(i, j)) {
                                            count++;
                                            if (transferImage.getStack().getProcessor(1).getPixelValue(i, j) >= -1) {
                                                notNan++;
                                            }
                                        }
                                    }
                                }
                                resultsTable.addValue("Pixels", count);
                                resultsTable.addValue("Not NaN p.", notNan);
                            } else {
                                int notNan = 0;
                                for (int i = 0; i < widthTi; i++) {
                                    for (int j = 0; j < heightTi; j++) {
                                        if (transferImage.getStack().getProcessor(1).getPixelValue(i, j) >= -1) {
                                            notNan++;
                                        }
                                    }
                                }
                                resultsTable.addValue("Pixels", widthTi * heightTi);
                                resultsTable.addValue("Not NaN p.", notNan);
                            }
                            ImageStatistics isMean = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MEAN, null);
                            resultsTable.addValue("Mean", (float) isMean.mean);
                            ImageStatistics isMedian = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MEDIAN, null);
                            resultsTable.addValue("Median", (float) isMedian.median);
                            ImageStatistics isStdDev = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.STD_DEV, null);
                            resultsTable.addValue("Std. dev.", (float) isStdDev.stdDev);
                            ImageStatistics isMinMax = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MIN_MAX, null);
                            resultsTable.addValue("Min", (float) isMinMax.min);
                            resultsTable.addValue("Max", (float) isMinMax.max);
                            if (transferImage.getRoi() != null) {
                                donorInDImage.setRoi(transferImage.getRoi());
                                donorInAImage.setRoi(transferImage.getRoi());
                                acceptorInAImage.setRoi(transferImage.getRoi());
                            } else {
                                donorInDImage.killRoi();
                                donorInAImage.killRoi();
                                acceptorInAImage.killRoi();
                            }
                            analyzer.displayResults();
                            analyzer.updateHeadings();
                        }
                    }
                    donorInDImage.changes = false;
                    donorInAImage.changes = false;
                    acceptorInAImage.changes = false;
                    if (autofluorescenceCorrectionMenuItem.isSelected()) {
                        autofluorescenceImage.changes = false;
                    }
                    break;
                case "saveFretImage": {
                    if (transferImage == null) {
                        logError("Transfer (FRET) image is required.");
                        return;
                    }
                    FileSaver fs = new FileSaver(transferImage);
                    if (fs.saveAsTiff()) {
                        log("Saved " + transferImage.getTitle() + ".");
                    }
                    transferImage.updateAndDraw();
                    break;
                }
                case "measureFretImage": {
                    if (transferImage == null) {
                        logError("Transfer (FRET) image is required.");
                        return;
                    }
                    resultsTable.incrementCounter();
                    int currentSlice = transferImage.getCurrentSlice();
                    int width = transferImage.getWidth();
                    int height = transferImage.getHeight();
                    int currentRow = resultsTable.getCounter();
                    if (currentlyProcessedFileName != null) {
                        resultsTable.setValue("File", currentRow, currentlyProcessedFileName);
                    }
                    ImageProcessor trProc = transferImage.getStack().getProcessor(currentSlice);
                    trProc.setRoi(transferImage.getRoi());
                    if (transferImage.getRoi() != null) {
                        Roi roi = transferImage.getRoi();
                        int count = 0;
                        int notNan = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (roi.contains(i, j)) {
                                    count++;
                                    if (trProc.getPixelValue(i, j) >= -1) {
                                        notNan++;
                                    }
                                }
                            }
                        }
                        resultsTable.addValue("Pixels", count);
                        resultsTable.addValue("Not NaN p.", notNan);
                    } else {
                        int notNan = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (!Float.isNaN(trProc.getPixelValue(i, j))) {
                                    notNan++;
                                }
                            }
                        }
                        resultsTable.addValue("Pixels", width * height);
                        resultsTable.addValue("Not NaN p.", notNan);
                    }
                    ImageStatistics isMean = ImageStatistics.getStatistics(trProc, Measurements.MEAN, null);
                    resultsTable.addValue("Mean", (float) isMean.mean);
                    ImageStatistics isMedian = ImageStatistics.getStatistics(trProc, Measurements.MEDIAN, null);
                    resultsTable.addValue("Median", (float) isMedian.median);
                    ImageStatistics isStdDev = ImageStatistics.getStatistics(trProc, Measurements.STD_DEV, null);
                    resultsTable.addValue("Std. dev.", (float) isStdDev.stdDev);
                    ImageStatistics isMinMax = ImageStatistics.getStatistics(trProc, Measurements.MIN_MAX, null);
                    resultsTable.addValue("Min", (float) isMinMax.min);
                    resultsTable.addValue("Max", (float) isMinMax.max);
                    if (transferImage.getRoi() != null) {
                        donorInDImage.setRoi(transferImage.getRoi());
                        donorInAImage.setRoi(transferImage.getRoi());
                        acceptorInAImage.setRoi(transferImage.getRoi());
                    } else {
                        donorInDImage.killRoi();
                        donorInAImage.killRoi();
                        acceptorInAImage.killRoi();
                    }
                    analyzer.displayResults();
                    analyzer.updateHeadings();
                    break;
                }
                case "semiAutomaticProcessing":
                    int choice = JOptionPane.showConfirmDialog(this, "Semi-automatic processing of images\n\nOpens and processes FRET images in a given directory. It works with\n"
                            + "Zeiss CZI and LSM images (tested with LSM 880/ZEN 2.1 SP1 (black) Version 12.0.0.0),\n"
                            + "which contain the following channels:\n"
                            + "1. autofluorescence channel (optional)\n"
                            + "2. donor channel\n"
                            + "3. transfer channel\n"
                            + "4. acceptor channel\n\n"
                            + "The upper left corner (1/6 x 1/6 of the image) is considered as background.\n"
                            + "Values for blurring and autofluorescence correction (if desired) should\n"
                            + "be entered in the main window before continuing.\n"
                            + "Threshold settings, creation of FRET image and measurements have to be\n"
                            + "made manually.\n\n"
                            + "Every previously opened image and result window will be closed when you\n"
                            + "click OK.\n\n"
                            + "Click OK to select the directory. To continue with the next "
                            + "image, do\nnot close any windows, just click the Next button.\n", "Semi-Automatic Processing of Images", JOptionPane.OK_CANCEL_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        currentlyProcessedFile = 0;
                        automaticallyProcessedFiles = null;
                        currentlyProcessedFileName = null;
                        WindowManager.closeAllWindows();
                        JFileChooser chooser = new JFileChooser(currentDirectory);
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        chooser.setDialogTitle("Select Directory");
                        chooser.setAcceptAllFileFilterUsed(false);
                        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            log("Processing files in directory: " + chooser.getSelectedFile());
                            currentDirectory = chooser.getSelectedFile().toString();
                        } else {
                            log("Semi-automatic processing: no directory is selected.");
                            return;
                        }
                        nextButton.setVisible(true);
                        useImageStacks.setSelected(true);
                        automaticallyProcessedFiles = chooser.getSelectedFile().listFiles();
                        processFile(0);
                    }
                    break;
                case "nextImage":
                    if (transferImage != null) {
                        transferImage.changes = false;
                        transferImage.close();
                    }
                    if (donorInDImage != null) {
                        donorInDImage.changes = false;
                        donorInDImage.close();
                    }
                    if (donorInAImage != null) {
                        donorInAImage.changes = false;
                        donorInAImage.close();
                    }
                    if (acceptorInAImage != null) {
                        acceptorInAImage.changes = false;
                        acceptorInAImage.close();
                    }
                    if (autofluorescenceImage != null) {
                        autofluorescenceImage.changes = false;
                        autofluorescenceImage.close();
                    }
                    IJ.selectWindow("Results");
                    WindowManager.putBehind();
                    if (WindowManager.getCurrentImage() != null) {
                        WindowManager.getCurrentImage().close();
                    }
                    processFile(++currentlyProcessedFile);
                    break;
                case "resetImages":
                    resetAll();
                    break;
                case "closeImages":
                    if (transferImage != null) {
                        transferImage.changes = false;
                        transferImage.close();
                    }
                    if (donorInDImage != null) {
                        donorInDImage.changes = false;
                        donorInDImage.close();
                    }
                    if (donorInAImage != null) {
                        donorInAImage.changes = false;
                        donorInAImage.close();
                    }
                    if (acceptorInAImage != null) {
                        acceptorInAImage.changes = false;
                        acceptorInAImage.close();
                    }
                    if (autofluorescenceImage != null) {
                        autofluorescenceImage.changes = false;
                        autofluorescenceImage.close();
                    }
                    resetAll();
                    break;
                case "help":
                    String url = "https://imagej.net/RiFRET";
                    try {
                        BrowserLauncher.openURL(url);
                    } catch (IOException ioe) {
                        logError("Could not open " + url + " in browser.");
                    }
                    break;
                case "about":
                    JOptionPane optionPane = new JOptionPane();
                    optionPane.setMessage("RiFRET - an ImageJ plugin for intensity-based three-filter set (ratiometric) FRET\n"
                            + "Homepage: https://imagej.net/RiFRET\n"
                            + "Written by: János Roszik (janosr@med.unideb.hu), Duarte Lisboa (duarte@med.unideb.hu),\n"
                            + "János Szöllősi (szollo@med.unideb.hu) and György Vereb (vereb@med.unideb.hu)\n"
                            + "Tested with: (Fiji Is Just) ImageJ " + imagejVersion + "/" + imagej1Version + ";" + " Java " + javaVersion + ".\n");
                    optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                    JDialog dialog = optionPane.createDialog(this, "About");
                    dialog.setVisible(true);
                    break;
                case "pwAutofluorescenceCorrection":
                    if (autofluorescenceCorrectionMenuItem.isSelected()) {
                        calculateS1S3Button.setVisible(false);
                        calculateS1S3S5Button.setVisible(true);
                        calculateS2S4Button.setVisible(false);
                        calculateS2S4S6Button.setVisible(true);
                        s5Label.setVisible(true);
                        s5Field.setVisible(true);
                        s6Label.setVisible(true);
                        s6Field.setVisible(true);
                        b1Label.setVisible(true);
                        b1Field.setVisible(true);
                        calculateB1B2B3Button.setVisible(true);
                        b2Label.setVisible(true);
                        b2Field.setVisible(true);
                        b3Label.setVisible(true);
                        b3Field.setVisible(true);
                        eRatioLabel.setVisible(true);
                        eRatioField.setVisible(true);
                        eRatioCheckbox.setVisible(true);
                        setAutofluorescenceImageLabel.setVisible(true);
                        setAutofluorescenceImageButton.setVisible(true);
                        subtractAutofluorescenceImageLabel.setVisible(true);
                        subtractAutofluorescenceImageButton.setVisible(true);
                        autoflAFField.setVisible(true);
                        smoothAutofluorescenceImageLabel.setVisible(true);
                        sigmaFieldAF.setVisible(true);
                        smoothAutofluorescenceImageButton.setVisible(true);
                        thresholdAutofluorescenceImageLabel.setVisible(true);
                        resetAFButton.setVisible(true);
                        thresholdAutofluorescenceImageButton.setVisible(true);
                        if (s1S3Dialog != null) {
                            s1S3Dialog.setVisible(false);
                            s1S3Dialog.dispose();
                        }
                        if (s2S4Dialog != null) {
                            s2S4Dialog.setVisible(false);
                            s2S4Dialog.dispose();
                        }
                    } else {
                        calculateS1S3Button.setVisible(true);
                        calculateS1S3S5Button.setVisible(false);
                        calculateS2S4Button.setVisible(true);
                        calculateS2S4S6Button.setVisible(false);
                        s5Label.setVisible(false);
                        s5Field.setVisible(false);
                        s6Label.setVisible(false);
                        s6Field.setVisible(false);
                        b1Label.setVisible(false);
                        b1Field.setVisible(false);
                        calculateB1B2B3Button.setVisible(false);
                        b2Label.setVisible(false);
                        b2Field.setVisible(false);
                        b3Label.setVisible(false);
                        b3Field.setVisible(false);
                        eRatioLabel.setVisible(false);
                        eRatioField.setVisible(false);
                        eRatioCheckbox.setVisible(false);
                        setAutofluorescenceImageLabel.setVisible(false);
                        setAutofluorescenceImageButton.setVisible(false);
                        subtractAutofluorescenceImageLabel.setVisible(false);
                        subtractAutofluorescenceImageButton.setVisible(false);
                        autoflAFField.setVisible(false);
                        smoothAutofluorescenceImageLabel.setVisible(false);
                        sigmaFieldAF.setVisible(false);
                        smoothAutofluorescenceImageButton.setVisible(false);
                        thresholdAutofluorescenceImageLabel.setVisible(false);
                        resetAFButton.setVisible(false);
                        thresholdAutofluorescenceImageButton.setVisible(false);
                        if (s1S3S5Dialog != null) {
                            s1S3S5Dialog.setVisible(false);
                            s1S3S5Dialog.dispose();
                        }
                        if (s2S4S6Dialog != null) {
                            s2S4S6Dialog.setVisible(false);
                            s2S4S6Dialog.dispose();
                        }
                        if (b1B2B3Dialog != null) {
                            b1B2B3Dialog.setVisible(false);
                            b1B2B3Dialog.dispose();
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (HeadlessException | NumberFormatException t) {
            logException(t.toString(), t);
        }
    }

    private void processFile(int currentFile) {
        resetAllButtonColors();
        if (currentFile >= automaticallyProcessedFiles.length) {
            log("Processing files has been finished.");
            nextButton.setVisible(false);
            IJ.selectWindow("Results");
            currentlyProcessedFile = 0;
            automaticallyProcessedFiles = null;
            currentlyProcessedFileName = null;
            return;
        }
        if (!automaticallyProcessedFiles[currentFile].isFile() || !(automaticallyProcessedFiles[currentFile].getName().endsWith(".lsm") || automaticallyProcessedFiles[currentFile].getName().endsWith(".LSM") || automaticallyProcessedFiles[currentFile].getName().endsWith(".czi") || automaticallyProcessedFiles[currentFile].getName().endsWith(".CZI"))) {
            processFile(++currentlyProcessedFile);
            return;
        }
        log("Current file is: " + automaticallyProcessedFiles[currentFile].getName());
        currentlyProcessedFileName = automaticallyProcessedFiles[currentFile].getName();
        (new Opener()).open(automaticallyProcessedFiles[currentFile].getAbsolutePath());
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "split"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorInAImage"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInAImage"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorInDImage"));
        if (autofluorescenceCorrectionMenuItem.isSelected()) {
            WindowManager.putBehind();
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAutofluorescenceImage"));
        }
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothDD"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothDA"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothAA"));
        if (autofluorescenceCorrectionMenuItem.isSelected()) {
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothAF"));
        }
        donorInDImage.setRoi(new Roi(0, 0, donorInDImage.getWidth() / 6, donorInDImage.getHeight() / 6));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copyRoi"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractDonorInDImage"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractDonorInAImage"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractAcceptorInAImage"));
        if (autofluorescenceCorrectionMenuItem.isSelected()) {
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractAutofluorescenceImage"));
        }
        donorInDImage.setRoi(new Roi(0, 0, donorInDImage.getWidth() / 6, donorInDImage.getHeight() / 6));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copyRoi"));
        donorInDImage.getProcessor().setValue(0);
        donorInDImage.getProcessor().fill();
        donorInAImage.getProcessor().setValue(0);
        donorInAImage.getProcessor().fill();
        acceptorInAImage.getProcessor().setValue(0);
        acceptorInAImage.getProcessor().fill();
        if (autofluorescenceCorrectionMenuItem.isSelected()) {
            autofluorescenceImage.getProcessor().setValue(0);
            autofluorescenceImage.getProcessor().fill();
        }
        donorInDImage.killRoi();
        donorInAImage.killRoi();
        acceptorInAImage.killRoi();
        if (autofluorescenceCorrectionMenuItem.isSelected()) {
            autofluorescenceImage.killRoi();
        }
    }

    void registerToDonorChannel() {

    }

    private void resetAll() {
        donorInDImage = null;
        donorInDImageSave = null;
        donorInAImage = null;
        donorInAImageSave = null;
        acceptorInAImage = null;
        acceptorInAImageSave = null;
        autofluorescenceImage = null;
        autofluorescenceImageSave = null;
        resetAllButtonColors();

        nextButton.setVisible(false);
        currentlyProcessedFile = 0;
        automaticallyProcessedFiles = null;
        currentlyProcessedFileName = null;
    }

    private void resetAllButtonColors() {
        setDonorInDImageButton.setBackground(originalButtonColor);
        setDonorInDImageButton.setOpaque(false);
        setDonorInDImageButton.setBorderPainted(true);
        setDonorInAImageButton.setBackground(originalButtonColor);
        setDonorInAImageButton.setOpaque(false);
        setDonorInAImageButton.setBorderPainted(true);
        setAcceptorInAImageButton.setBackground(originalButtonColor);
        setAcceptorInAImageButton.setOpaque(false);
        setAcceptorInAImageButton.setBorderPainted(true);
        setAutofluorescenceImageButton.setBackground(originalButtonColor);
        setAutofluorescenceImageButton.setOpaque(false);
        setAutofluorescenceImageButton.setBorderPainted(true);
        subtractDonorInDImageButton.setBackground(originalButtonColor);
        subtractDonorInDImageButton.setOpaque(false);
        subtractDonorInDImageButton.setBorderPainted(true);
        subtractDonorInAImageButton.setBackground(originalButtonColor);
        subtractDonorInAImageButton.setOpaque(false);
        subtractDonorInAImageButton.setBorderPainted(true);
        subtractAcceptorInAImageButton.setBackground(originalButtonColor);
        subtractAcceptorInAImageButton.setOpaque(false);
        subtractAcceptorInAImageButton.setBorderPainted(true);
        subtractAutofluorescenceImageButton.setBackground(originalButtonColor);
        subtractAutofluorescenceImageButton.setOpaque(false);
        subtractAutofluorescenceImageButton.setBorderPainted(true);
        smoothDonorInDImageButton.setBackground(originalButtonColor);
        smoothDonorInDImageButton.setOpaque(false);
        smoothDonorInDImageButton.setBorderPainted(true);
        smoothDonorInAImageButton.setBackground(originalButtonColor);
        smoothDonorInAImageButton.setOpaque(false);
        smoothDonorInAImageButton.setBorderPainted(true);
        smoothAcceptorInAImageButton.setBackground(originalButtonColor);
        smoothAcceptorInAImageButton.setOpaque(false);
        smoothAcceptorInAImageButton.setBorderPainted(true);
        smoothAutofluorescenceImageButton.setBackground(originalButtonColor);
        smoothAutofluorescenceImageButton.setOpaque(false);
        smoothAutofluorescenceImageButton.setBorderPainted(true);
        thresholdDonorInDImageButton.setBackground(originalButtonColor);
        thresholdDonorInDImageButton.setOpaque(false);
        thresholdDonorInDImageButton.setBorderPainted(true);
        thresholdDonorInAImageButton.setBackground(originalButtonColor);
        thresholdDonorInAImageButton.setOpaque(false);
        thresholdDonorInAImageButton.setBorderPainted(true);
        thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
        thresholdAcceptorInAImageButton.setOpaque(false);
        thresholdAcceptorInAImageButton.setBorderPainted(true);
        calculateS1S3Button.setBackground(originalButtonColor);
        calculateS1S3Button.setOpaque(false);
        calculateS1S3Button.setBorderPainted(true);
        calculateS1S3S5Button.setBackground(originalButtonColor);
        calculateS1S3S5Button.setOpaque(false);
        calculateS1S3S5Button.setBorderPainted(true);
        calculateS2S4Button.setBackground(originalButtonColor);
        calculateS2S4Button.setOpaque(false);
        calculateS2S4Button.setBorderPainted(true);
        calculateS2S4S6Button.setBackground(originalButtonColor);
        calculateS2S4S6Button.setOpaque(false);
        calculateS2S4S6Button.setBorderPainted(true);
        calculateB1B2B3Button.setBackground(originalButtonColor);
        calculateB1B2B3Button.setOpaque(false);
        calculateB1B2B3Button.setBorderPainted(true);
        calculateAlphaButton.setBackground(originalButtonColor);
        calculateAlphaButton.setOpaque(false);
        calculateAlphaButton.setBorderPainted(true);
    }

    public void log(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + timeFormat.format(LocalTime.now()) + " " + text, log.getStyle("BLACK"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logError(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + timeFormat.format(LocalTime.now()) + " ERROR: " + text, log.getStyle("RED"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logWarning(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + timeFormat.format(LocalTime.now()) + " WARNING: " + text, log.getStyle("BLUE"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logException(String message, Throwable t) {
        try {
            if (debugMenuItem.isSelected()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.flush();
                log.getDocument().insertString(log.getDocument().getLength(), "\n" + timeFormat.format(LocalTime.now()) + " ERROR: " + sw.toString(), log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            } else {
                log.getDocument().insertString(log.getDocument().getLength(), "\n" + timeFormat.format(LocalTime.now()) + " ERROR: " + message, log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            }
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void exit() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit RiFRET?", "Quit", JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (s1S3Dialog != null) {
                s1S3Dialog.setVisible(false);
                s1S3Dialog.dispose();
            }
            if (s1S3S5Dialog != null) {
                s1S3S5Dialog.setVisible(false);
                s1S3S5Dialog.dispose();
            }
            if (s2S4Dialog != null) {
                s2S4Dialog.setVisible(false);
                s2S4Dialog.dispose();
            }
            if (s2S4S6Dialog != null) {
                s2S4S6Dialog.setVisible(false);
                s2S4S6Dialog.dispose();
            }
            if (b1B2B3Dialog != null) {
                b1B2B3Dialog.setVisible(false);
                b1B2B3Dialog.dispose();
            }
            if (alphaDialog != null) {
                alphaDialog.setVisible(false);
                alphaDialog.dispose();
            }
            if (applyMaskRiDialog != null) {
                applyMaskRiDialog.setVisible(false);
                applyMaskRiDialog.dispose();
            }
            if (calculateRatioDialog != null) {
                calculateRatioDialog.setVisible(false);
                calculateRatioDialog.dispose();
            }
            if (autoflDialog != null) {
                autoflDialog.setVisible(false);
                autoflDialog.dispose();
            }
            setVisible(false);
            dispose();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        exit();
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    public ImagePlus getDonorInDImage() {
        return donorInDImage;
    }

    public ImagePlus getDonorInAImage() {
        return donorInAImage;
    }

    public ImagePlus getAcceptorInAImage() {
        return acceptorInAImage;
    }

    public float getS1Factor() {
        float s1Factor = -1;
        try {
            s1Factor = Float.parseFloat(s1Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s1Factor;
    }

    public void setS1Factor(String value) {
        s1Field.setText(value);
    }

    public float getS2Factor() {
        float s2Factor = -1;
        try {
            s2Factor = Float.parseFloat(s2Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s2Factor;
    }

    public void setS2Factor(String value) {
        s2Field.setText(value);
    }

    public float getS3Factor() {
        float s3Factor = -1;
        try {
            s3Factor = Float.parseFloat(s3Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s3Factor;
    }

    public void setS3Factor(String value) {
        s3Field.setText(value);
    }

    public float getS4Factor() {
        float s4Factor = -1;
        try {
            s4Factor = Float.parseFloat(s4Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s4Factor;
    }

    public void setS4Factor(String value) {
        s4Field.setText(value);
    }

    public float getS5Factor() {
        float s5Factor = -1;
        try {
            s5Factor = Float.parseFloat(s5Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s5Factor;
    }

    public void setS5Factor(String value) {
        s5Field.setText(value);
    }

    public float getS6Factor() {
        float s6Factor = -1;
        try {
            s6Factor = Float.parseFloat(s6Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return s6Factor;
    }

    public void setS6Factor(String value) {
        s6Field.setText(value);
    }

    public float getB1Factor() {
        float b1Factor = -1;
        try {
            b1Factor = Float.parseFloat(b1Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return b1Factor;
    }

    public void setB1Factor(String value) {
        b1Field.setText(value);
    }

    public float getB2Factor() {
        float b2Factor = -1;
        try {
            b2Factor = Float.parseFloat(b2Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return b2Factor;
    }

    public void setB2Factor(String value) {
        b2Field.setText(value);
    }

    public float getB3Factor() {
        float b3Factor = -1;
        try {
            b3Factor = Float.parseFloat(b3Field.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
        return b3Factor;
    }

    public void setB3Factor(String value) {
        b3Field.setText(value);
    }

    public void setAlphaFactor(String value) {
        alphaField.setText(value);
    }

    public void setERatio(String value) {
        eRatioField.setText(value);
    }

    public static void main(String args[]) {
        new RiFRET_Plugin();
    }

}
