/*
RiFRET - an ImageJ plugin for intensity-based ratiometric FRET imaging.

Written by Janos Roszik (janosr@med.unideb.hu), Duarte Lisboa (duarte@med.unideb.hu),
           Janos Szollosi (szollo@med.unideb.hu) and Gyorgy Vereb (vereb@med.unideb.hu)

The program is provided free of charge on an "as is" basis without warranty of any kind.

http://biophys.med.unideb.hu/rifret/
*/

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.plugin.frame.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;


public class RiFRET_Plugin extends JFrame implements ActionListener, WindowListener {
    private float version = 1.83F;
    private String lastModified = "24 May 2010";
    private String imageJVersion = "1.42k";
    private String javaVersion = "1.6.0_05";
    private int windowWidth = 620;
    private int windowHeight = 800;
    private ImagePlus donorInDImage, donorInAImage, acceptorInAImage, transferImage = null;
    private ImageStack donorInDImageSave = null, donorInAImageSave = null, acceptorInAImageSave = null;
    private ResultsTable resultsTable;
    private Analyzer analyzer;
    private ApplyMaskRiDialog applyMaskRiDialog;
    private CalculateRatioDialog calculateRatioDialog;
    private AutoflDialog autoflDialog;
    private S1S3Dialog s1S3Dialog;
    private S2S4Dialog s2S4Dialog;
    private AlphaDialog alphaDialog;
    private RiHelpWindow helpWindow;
    private JMenuBar menuBar;
    private JMenu fileMenu, imageMenu, helpMenu;
    private JMenuItem openMenuItem, saveTiffMenuItem, saveBmpMenuItem, splitMenuItem, applyMaskMenuItem, registerMenuItem, calculateRatioMenuItem, calculateAFMenuItem, thresholdMenuItem;
    private JMenuItem lutFireMenuItem, lutSpectrumMenuItem, histogramMenuItem, convertMenuItem, blurMenuItem, exitMenuItem, helpMenuItem, aboutMenuItem, checkVersionMenuItem;
    private JMenuItem saveMessagesMenuItem, clearMessagesMenuItem;
    private JMenuItem semiAutomaticMenuItem, resetImagesMenuItem;
    private JCheckBoxMenuItem debugMenuItem;
    private JButton setDonorInDImageButton, setDonorInAImageButton, setAcceptorInAImageButton;
    private JButton subtractDonorInDImageButton, subtractDonorInAImageButton, subtractAcceptorInAImageButton;
    private JButton thresholdDonorInDImageButton, thresholdDonorInAImageButton, thresholdAcceptorInAImageButton;
    private JButton smoothDonorInDImageButton, smoothDonorInAImageButton, smoothAcceptorInAImageButton;
    private JButton openImageButton, resetDDButton, resetDAButton, resetAAButton;
    private JButton copyRoiButton;
    public JTextField autoflDInDField, autoflAInDField, autoflAInAField;
    private JTextField radiusFieldDD, radiusFieldDA, radiusFieldAA;
    public JTextField autoThresholdMin, autoThresholdMax;
    private JButton createButton, measureButton, nextButton, closeImagesButton;
    private JCheckBox useLsmImages, autoThresholdingCB;
    private JTextField s1Field, s2Field, s3Field, s4Field, alphaField;
    public JButton calculateS1S3Button, calculateS2S4Button, calculateAlphaButton;
    private JTextPane log;
    private JScrollPane logScrollPane;
    private SimpleDateFormat format;
    private File[] automaticallyProcessedFiles = null;
    private int currentlyProcessedFile = 0;
    private String currentlyProcessedFileName = null;
    private String currentDirectory = null;
    public Color originalButtonColor = null;
    public Color greenColor = new Color(142, 207, 125);

	public RiFRET_Plugin () {
        super();
        setTitle("RiFRET v"+version+" - intensity-based ratiometric FRET imaging");
        IJ.versionLessThan(imageJVersion);
		Locale.setDefault(Locale.ENGLISH);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
        format = new SimpleDateFormat("HH:mm:ss");
        createGui();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(windowWidth, windowHeight);
        setLocation(screen.width - getWidth(), screen.height/2 - getHeight()/2);
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
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        openMenuItem = new JMenuItem("Open image");
        openMenuItem.setActionCommand("openImage");
        openMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);
        saveTiffMenuItem = new JMenuItem("Save image as tiff");
        saveTiffMenuItem.setActionCommand("saveImageAsTiff");
        saveTiffMenuItem.addActionListener(this);
        fileMenu.add(saveTiffMenuItem);
        saveBmpMenuItem = new JMenuItem("Save image as bmp");
        saveBmpMenuItem.setActionCommand("saveImageAsBmp");
        saveBmpMenuItem.addActionListener(this);
        fileMenu.add(saveBmpMenuItem);
        saveMessagesMenuItem = new JMenuItem("Save messages");
        saveMessagesMenuItem.setActionCommand("saveMessages");
        saveMessagesMenuItem.addActionListener(this);
        fileMenu.add(saveMessagesMenuItem);
        clearMessagesMenuItem = new JMenuItem("Clear messages");
        clearMessagesMenuItem.setActionCommand("clearMessages");
        clearMessagesMenuItem.addActionListener(this);
        fileMenu.add(clearMessagesMenuItem);
        semiAutomaticMenuItem = new JMenuItem("Semi-automatic processing");
        semiAutomaticMenuItem.setActionCommand("semiAutomaticProcessing");
        semiAutomaticMenuItem.addActionListener(this);
        fileMenu.add(semiAutomaticMenuItem);
        resetImagesMenuItem = new JMenuItem("Reset all");
        resetImagesMenuItem.setActionCommand("resetImages");
        resetImagesMenuItem.addActionListener(this);
        fileMenu.add(resetImagesMenuItem);
        splitMenuItem = new JMenuItem("Split image");
        splitMenuItem.setActionCommand("split");
        splitMenuItem.addActionListener(this);
        imageMenu.add(splitMenuItem);
        applyMaskMenuItem = new JMenuItem("Apply mask");
        applyMaskMenuItem.setActionCommand("applyMask");
        applyMaskMenuItem.addActionListener(this);
        imageMenu.add(applyMaskMenuItem);
        registerMenuItem = new JMenuItem("Register to donor channel");
        registerMenuItem.setActionCommand("registerTransferCh");
        registerMenuItem.addActionListener(this);
        imageMenu.add(registerMenuItem);
        calculateRatioMenuItem = new JMenuItem("Calculate ratio of images");
        calculateRatioMenuItem.setActionCommand("calculateRatio");
        calculateRatioMenuItem.addActionListener(this);
        imageMenu.add(calculateRatioMenuItem);
        calculateAFMenuItem = new JMenuItem("Calculate autofluorescence");
        calculateAFMenuItem.setActionCommand("calculateAF");
        calculateAFMenuItem.addActionListener(this);
        imageMenu.add(calculateAFMenuItem);
        convertMenuItem = new JMenuItem("Convert image to 32 bit");
        convertMenuItem.setActionCommand("convertto32bit");
        convertMenuItem.addActionListener(this);
        imageMenu.add(convertMenuItem);
        blurMenuItem = new JMenuItem("Gaussian blur");
        blurMenuItem.setActionCommand("gaussianblur-menu");
        blurMenuItem.addActionListener(this);
        imageMenu.add(blurMenuItem);
        thresholdMenuItem = new JMenuItem("Set threshold");
        thresholdMenuItem.setActionCommand("threshold");
        thresholdMenuItem.addActionListener(this);
        imageMenu.add(thresholdMenuItem);
        histogramMenuItem = new JMenuItem("Histogram");
        histogramMenuItem.setActionCommand("histogram");
        histogramMenuItem.addActionListener(this);
        imageMenu.add(histogramMenuItem);
        lutFireMenuItem = new JMenuItem("LUT Fire");
        lutFireMenuItem.setActionCommand("lutFire");
        lutFireMenuItem.addActionListener(this);
        imageMenu.add(lutFireMenuItem);
        lutSpectrumMenuItem = new JMenuItem("LUT Spectrum");
        lutSpectrumMenuItem.setActionCommand("lutSpectrum");
        lutSpectrumMenuItem.addActionListener(this);
        imageMenu.add(lutSpectrumMenuItem);
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setActionCommand("exit");
        exitMenuItem.addActionListener(this);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setActionCommand("help");
        helpMenuItem.addActionListener(this);
        helpMenu.add(helpMenuItem);
        checkVersionMenuItem = new JMenuItem("Check for latest version");
        checkVersionMenuItem.setActionCommand("checkVersion");
        checkVersionMenuItem.addActionListener(this);
        helpMenu.add(checkVersionMenuItem);
        debugMenuItem = new JCheckBoxMenuItem("Debug mode");
        debugMenuItem.setSelected(false);
        debugMenuItem.setActionCommand("debugmode");
        debugMenuItem.addActionListener(this);
        helpMenu.add(debugMenuItem);
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setActionCommand("about");
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);
        setJMenuBar(menuBar);
        addWindowListener(this);
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        Container container = getContentPane();
        container.setLayout(gridbaglayout);

        JPanel donorInDImageBleachingPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
       	donorInDImageBleachingPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(2,2,2,2);
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
        calculateS1S3Button.setMargin(new Insets(2,1,2,1));
        calculateS1S3Button.addActionListener(this);
        calculateS1S3Button.setActionCommand("calculateS1S3Button");
        container.add(calculateS1S3Button, gc);

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

        gc.gridwidth = 9;
        gc.insets = new Insets(5,2,5,2);
        gc.gridx = 0;
        gc.gridy = 2;
        container.add(new JLabel("Calculate / set S3 factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 2;
        s3Field = new JTextField("", 4);
        s3Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s3Field, gc);


        gc.gridwidth = 9;
        gc.insets = new Insets(8,2,8,2);
        gc.gridx = 0;
        gc.gridy = 3;
        container.add(new JLabel("Calculate / set S4 factor:"), gc);
        gc.gridwidth = 1;
        gc.insets = new Insets(2,2,2,2);
        gc.gridx = 9;
        gc.gridy = 3;
        s4Field = new JTextField("", 4);
        s4Field.setHorizontalAlignment(JTextField.RIGHT);
        container.add(s4Field, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 4;
        container.add(new JLabel("Calculate / set \u03B1 (alpha) factor:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 4;
        alphaField = new JTextField("", 4);
        alphaField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(alphaField, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 4;
        calculateAlphaButton = new JButton("Calculate \u03B1");
        calculateAlphaButton.addActionListener(this);
        calculateAlphaButton.setActionCommand("calculateAlphaButton");
        container.add(calculateAlphaButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 5;
        JPanel lineFactors = new JPanel();
        lineFactors.setPreferredSize(new Dimension(windowWidth-35, 2));
        lineFactors.setBackground(Color.darkGray);
        container.add(lineFactors, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 6;
        gc.fill = GridBagConstraints.NONE;
        useLsmImages = new JCheckBox("use LSM", false);
        useLsmImages.setActionCommand("useLsmImages");
        useLsmImages.addActionListener(this);
        useLsmImages.setToolTipText("<html>If this checkbox is checked, the LSM image containing donor, transfer and<BR>acceptor channel images (in this order) are set automatically after opening.<BR>Every previously opened image window will be closed. The results window<BR>can be left opened.</html>");
        donorInDImageBleachingPanel.add(new JLabel("Step 1a: open and set the donor channel image  "));
        donorInDImageBleachingPanel.add(useLsmImages);
        setDonorInDImageButton = new JButton("Set image");
        setDonorInDImageButton.setMargin(new Insets(2,2,2,2));
        setDonorInDImageButton.addActionListener(this);
        setDonorInDImageButton.setActionCommand("setDonorInDImage");
        container.add(donorInDImageBleachingPanel, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 6;
        openImageButton = new JButton("Open");
        openImageButton.setToolTipText("Opens an arbitrary image.");
        openImageButton.setMargin(new Insets(0,0,0,0));
        openImageButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        openImageButton.addActionListener(this);
        openImageButton.setActionCommand("openImage");
        container.add(openImageButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 6;
        container.add(setDonorInDImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 6;
        gc.gridy = 7;
        container.add(new JLabel("Step 1b: open and set the transfer channel image"), gc);
        setDonorInAImageButton = new JButton("Set image");
        setDonorInAImageButton.setMargin(new Insets(2,2,2,2));
        setDonorInAImageButton.addActionListener(this);
        setDonorInAImageButton.setActionCommand("setDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 7;
        container.add(setDonorInAImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 8;
        container.add(new JLabel("Step 1c: open and set acceptor channel image"), gc);
        setAcceptorInAImageButton = new JButton("Set image");
        setAcceptorInAImageButton.setMargin(new Insets(2,2,2,2));
        setAcceptorInAImageButton.addActionListener(this);
        setAcceptorInAImageButton.setActionCommand("setAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 8;
        container.add(setAcceptorInAImageButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 9;
        JPanel line1 = new JPanel();
        line1.setPreferredSize(new Dimension(windowWidth-35, 1));
        line1.setBackground(Color.lightGray);
        container.add(line1, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 10;
        container.add(new JLabel("Step 2a: subtract avg. of a background ROI of donor channel"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 10;
        copyRoiButton = new JButton("Copy");
        copyRoiButton.setToolTipText("Sets the same ROI for the two other images.");
        copyRoiButton.setMargin(new Insets(0,0,0,0));
        copyRoiButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyRoi");
        container.add(copyRoiButton, gc);
        autoflDInDField = new JTextField("0", 3);
        autoflDInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        gc.gridx = 10;
        gc.gridy = 10;
        container.add(autoflDInDField, gc);
        subtractDonorInDImageButton = new JButton("Subtract");
        subtractDonorInDImageButton.addActionListener(this);
        subtractDonorInDImageButton.setActionCommand("subtractDonorInDImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 10;
        container.add(subtractDonorInDImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 11;
        container.add(new JLabel("Step 2b: subtract avg. of a background ROI of transfer channel"), gc);
        autoflAInDField = new JTextField("0", 3);
        autoflAInDField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 11;
        container.add(autoflAInDField, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        subtractDonorInAImageButton = new JButton("Subtract");
        subtractDonorInAImageButton.addActionListener(this);
        subtractDonorInAImageButton.setActionCommand("subtractDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 11;
        container.add(subtractDonorInAImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 12;
        container.add(new JLabel("Step 2c: subtract avg. of a background ROI of acceptor channel"), gc);
        autoflAInAField = new JTextField("0", 3);
        autoflAInAField.setToolTipText("<html><b>Correction for autofluorescence</b><br>If this value is set, it will be subtracted from each pixel along with<br>the background. Average autofluorescence can be calculated with<br>the \"Calculate autofluorescence\" item in the \"Image\" menu.<html>");
        gc.gridwidth = 1;
        gc.gridx = 10;
        gc.gridy = 12;
        container.add(autoflAInAField, gc);
        subtractAcceptorInAImageButton = new JButton("Subtract");
        subtractAcceptorInAImageButton.addActionListener(this);
        subtractAcceptorInAImageButton.setActionCommand("subtractAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 13;
        gc.gridy = 12;
        container.add(subtractAcceptorInAImageButton, gc);
        gc.gridx = 0;
        gc.gridy = 13;
        JLabel afInfo = new JLabel("Autofluorescence fields (if necessary) can be filled in using the \"Calculate autofluorescence\" item in the \"Image\" menu.");
        afInfo.setFont(new Font("Helvetica", Font.PLAIN, 10));
        container.add(afInfo, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 14;
        JPanel line3 = new JPanel();
        line3.setPreferredSize(new Dimension(windowWidth-35, 1));
        line3.setBackground(Color.lightGray);
        container.add(line3, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 15;
        container.add(new JLabel("Step 3a (optional): blur donor channel image, radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 15;
        radiusFieldDD = new JTextField("2", 4);
        radiusFieldDD.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldDD, gc);
        smoothDonorInDImageButton = new JButton("Blur");
        smoothDonorInDImageButton.addActionListener(this);
        smoothDonorInDImageButton.setActionCommand("smoothDD");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 15;
        container.add(smoothDonorInDImageButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 16;
        container.add(new JLabel("Step 3b (optional): blur transfer channel image, radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 16;
        radiusFieldDA = new JTextField("2", 4);
        radiusFieldDA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldDA, gc);
        smoothDonorInAImageButton = new JButton("Blur");
        smoothDonorInAImageButton.addActionListener(this);
        smoothDonorInAImageButton.setActionCommand("smoothDA");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 16;
        container.add(smoothDonorInAImageButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 17;
        container.add(new JLabel("Step 3c (optional): blur acceptor channel image, radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 17;
        radiusFieldAA = new JTextField("2", 4);
        radiusFieldAA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldAA, gc);
        smoothAcceptorInAImageButton = new JButton("Blur");
        smoothAcceptorInAImageButton.addActionListener(this);
        smoothAcceptorInAImageButton.setActionCommand("smoothAA");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 17;
        container.add(smoothAcceptorInAImageButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 18;
        JPanel line4 = new JPanel();
        line4.setPreferredSize(new Dimension(windowWidth-35, 1));
        line4.setBackground(Color.lightGray);
        container.add(line4, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 19;
        container.add(new JLabel("Step 4a: set threshold for donor channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 19;
        resetDDButton = new JButton("Reset");
        resetDDButton.setToolTipText("Resets blur and threshold settings");
        resetDDButton.setMargin(new Insets(0,0,0,0));
        resetDDButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDDButton.addActionListener(this);
        resetDDButton.setActionCommand("resetDD");
        container.add(resetDDButton, gc);
        thresholdDonorInDImageButton = new JButton("Set threshold");
        thresholdDonorInDImageButton.addActionListener(this);
        thresholdDonorInDImageButton.setActionCommand("thresholdDonorInDImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 19;
        container.add(thresholdDonorInDImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 20;
        container.add(new JLabel("Step 4b: set threshold for transfer channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 20;
        resetDAButton = new JButton("Reset");
        resetDAButton.setToolTipText("Resets blur and threshold settings");
        resetDAButton.setMargin(new Insets(0,0,0,0));
        resetDAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDAButton.addActionListener(this);
        resetDAButton.setActionCommand("resetDA");
        container.add(resetDAButton, gc);
        thresholdDonorInAImageButton = new JButton("Set threshold");
        thresholdDonorInAImageButton.addActionListener(this);
        thresholdDonorInAImageButton.setActionCommand("thresholdDonorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 20;
        container.add(thresholdDonorInAImageButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 21;
        container.add(new JLabel("Step 4c: set threshold for acceptor channel image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 21;
        resetAAButton = new JButton("Reset");
        resetAAButton.setToolTipText("Resets blur and threshold settings");
        resetAAButton.setMargin(new Insets(0,0,0,0));
        resetAAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetAAButton.addActionListener(this);
        resetAAButton.setActionCommand("resetAA");
        container.add(resetAAButton, gc);
        thresholdAcceptorInAImageButton = new JButton("Set threshold");
        thresholdAcceptorInAImageButton.addActionListener(this);
        thresholdAcceptorInAImageButton.setActionCommand("thresholdAcceptorInAImage");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 21;
        container.add(thresholdAcceptorInAImageButton, gc);
        gc.gridx = 0;
        gc.gridy = 22;
        JLabel thInfo = new JLabel("Threshold setting: set threshold, press \"Apply\", select \"Set bg pixels to NaN\", press \"Ok\" (and then \"No\" in case of stacks)");
        thInfo.setFont(new Font("Helvetica", Font.PLAIN, 10));
        container.add(thInfo, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 23;
        JPanel line5 = new JPanel();
        line5.setPreferredSize(new Dimension(windowWidth-35, 1));
        line5.setBackground(Color.lightGray);
        container.add(line5, gc);


        JPanel createFretImgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 24;
        gc.insets = new Insets(2,2,2,2);
        gc.fill = GridBagConstraints.NONE;
        createFretImgPanel.add(new JLabel("Step 5: create FRET image   "));
        autoThresholdingCB = new JCheckBox("thresholding with min: ", true);
        autoThresholdingCB.setToolTipText("<html>If this checkbox is checked, the FRET image will be thresholded<br>with the given min and max values to exclude pixels with extreme<br>FRET efficiencies.</html>");
        autoThresholdingCB.setSelected(true);
        createFretImgPanel.add(autoThresholdingCB);
        autoThresholdMin = new JTextField("-2", 2);
        createFretImgPanel.add(autoThresholdMin);
        createFretImgPanel.add(new JLabel(" and max: "));
        autoThresholdMax = new JTextField("2", 2);
        createFretImgPanel.add(autoThresholdMax);
        container.add(createFretImgPanel, gc);
        createButton = new JButton("Create");
        createButton.addActionListener(this);
        createButton.setActionCommand("createFretImage");
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 24;
        container.add(createButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 25;
        JPanel line7 = new JPanel();
        line7.setPreferredSize(new Dimension(windowWidth-35, 1));
        line7.setBackground(Color.lightGray);
        container.add(line7, gc);

        gc.gridwidth = 7;
        gc.gridx = 0;
        gc.gridy = 26;
        container.add(new JLabel("Step 6: select ROIs and make measurements"), gc);
        gc.gridx = 9;
        gc.gridwidth = 1;
        closeImagesButton = new JButton("Close images");
        closeImagesButton.setToolTipText("Closes the source and transfer images and resets button colors");
        closeImagesButton.setMargin(new Insets(0,0,0,0));
        closeImagesButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        closeImagesButton.addActionListener(this);
        closeImagesButton.setActionCommand("closeImages");
        container.add(closeImagesButton, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        measureButton = new JButton("Measure");
        measureButton.setMargin(new Insets(2,2,2,2));
        measureButton.addActionListener(this);
        measureButton.setActionCommand("measureFretImage");
        gc.gridx = 10;
        gc.gridy = 26;
        gc.gridwidth = 6;
        container.add(measureButton, gc);
        nextButton = new JButton("Next");
        nextButton.setMargin(new Insets(2,2,2,2));
        nextButton.addActionListener(this);
        nextButton.setActionCommand("nextImage");
        gc.gridx = 16;
        gc.gridy = 26;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        container.add(nextButton, gc);
        nextButton.setVisible(false);

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
	    container.add(logScrollPane, gc);
    }


    public void actionPerformed(ActionEvent e) {
    	try {
        if (e.getActionCommand().equals("exit")) {
	        exit();
      	} else if (e.getActionCommand().equals("split")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
            if (WindowManager.getCurrentImage().isHyperStack()) {
                ij.plugin.HyperStackConverter hc = new ij.plugin.HyperStackConverter();
                hc.run("hstostack");
            }
            StackEditor se = new StackEditor();
            se.run("toimages");
      	} else if (e.getActionCommand().equals("applyMask")) {
            if (applyMaskRiDialog != null) {
                applyMaskRiDialog.setVisible(false);
                applyMaskRiDialog.dispose();
            }
            applyMaskRiDialog = new ApplyMaskRiDialog(this);
		    applyMaskRiDialog.setVisible(true);
      	} else if (e.getActionCommand().equals("registerTransferCh")) {
      	    if (donorInDImage == null) {
                logError("No image is set as donor channel.");
                return;
            } else if (donorInAImage == null) {
                logError("No image is set as transfer channel.");
                return;
            } else {
                FHT fht1 = new FHT(donorInDImage.getProcessor().duplicate());
                fht1.transform();
                FHT fht2 = new FHT(donorInAImage.getProcessor().duplicate());
                fht2.transform();
                FHT res = fht1.conjugateMultiply(fht2);
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
                if (maxx != 0 || maxy != 0){
                    ShiftDialogRi sd = new ShiftDialogRi(this);
                    if (maxy > height/2) {
                        log("Shifting transfer channel image up " + (height-maxy) + " pixel" + ((height-maxy)>1?"s":"") + ".");
                        sd.shiftUp(donorInAImage, height-maxy);
                    } else if (maxy != 0) {
                        log("Shifting transfer channel image down " + maxy + " pixel" + (maxy>1?"s":"") + ".");
                        sd.shiftDown(donorInAImage, maxy);
                    }
                    if (maxx > width/2) {
                        log("Shifting transfer channel image to the left " + (width-maxx) + " pixel" + ((width-maxx)>1?"s":"") + ".");
                        sd.shiftLeft(donorInAImage, width-maxx);
                    } else if (maxx != 0) {
                        log("Shifting transfer channel image to the right " + maxx + " pixel" + (maxx>1?"s":"") + ".");
                        sd.shiftRight(donorInAImage, maxx);
                    }
                    actionPerformed(new ActionEvent(registerMenuItem, 1, "registerTransferCh"));
                } else {
                    log("Registration of transfer channel has been finished.");
                    actionPerformed(new ActionEvent(registerMenuItem, 1, "registerAcceptorCh"));
                }
            }
      	} else if (e.getActionCommand().equals("registerAcceptorCh")) {
      	    if (donorInDImage == null) {
                logError("No image is set as donor channel.");
                return;
            } else if (acceptorInAImage == null) {
                logError("No image is set as acceptor channel.");
                return;
            } else {
                FHT fht1 = new FHT(donorInDImage.getProcessor().duplicate());
                fht1.transform();
                FHT fht2 = new FHT(acceptorInAImage.getProcessor().duplicate());
                fht2.transform();
                FHT res = fht1.conjugateMultiply(fht2);
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
                if (maxx != 0 || maxy != 0){
                    ShiftDialogRi sd = new ShiftDialogRi(this);
                    if (maxy > height/2) {
                        log("Shifting acceptor channel image up " + (height-maxy) + " pixel" + ((height-maxy)>1?"s":"") + ".");
                        sd.shiftUp(acceptorInAImage, height-maxy);
                    } else if (maxy != 0) {
                        log("Shifting acceptor channel image down " + maxy + " pixel" + (maxy>1?"s":"") + ".");
                        sd.shiftDown(acceptorInAImage, maxy);
                    }
                    if (maxx > width/2) {
                        log("Shifting acceptor channel image to the left " + (width-maxx) + " pixel" + ((width-maxx)>1?"s":"") + ".");
                        sd.shiftLeft(acceptorInAImage, width-maxx);
                    } else if (maxx != 0) {
                        log("Shifting acceptor channel image to the right " + maxx + " pixel" + (maxx>1?"s":"") + ".");
                        sd.shiftRight(acceptorInAImage, maxx);
                    }
                    actionPerformed(new ActionEvent(registerMenuItem, 1, "registerAcceptorCh"));
                } else {
                    log("Registration of acceptor channel has been finished.");
                }
            }
      	} else if (e.getActionCommand().equals("calculateRatio")) {
            if (calculateRatioDialog != null) {
                calculateRatioDialog.setVisible(false);
                calculateRatioDialog.dispose();
            }
            calculateRatioDialog = new CalculateRatioDialog(this);
		    calculateRatioDialog.setVisible(true);
      	} else if (e.getActionCommand().equals("calculateAF")) {
            if (autoflDialog != null) {
                autoflDialog.setVisible(false);
                autoflDialog.dispose();
            }
            autoflDialog = new AutoflDialog(this);
		    autoflDialog.setVisible(true);
        } else if (e.getActionCommand().equals("threshold")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
		    IJ.run("Threshold...");
      	} else if (e.getActionCommand().equals("convertto32bit")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
		    new ImageConverter(WindowManager.getCurrentImage()).convertToGray32();
      	} else if (e.getActionCommand().equals("gaussianblur-menu")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
            String radiusString =  (String)JOptionPane.showInputDialog(this, "Enter radius (in pixels) for Gaussian blur", "Gaussian blur", JOptionPane.QUESTION_MESSAGE);
            if (radiusString == null || radiusString.trim().equals("")) {
                return;
            }
            GaussianBlur gb = new GaussianBlur();
            int nSlices = WindowManager.getCurrentImage().getImageStackSize();
            for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                if(!gb.blur(WindowManager.getCurrentImage().getStack().getProcessor(currentSlice), Float.parseFloat(radiusString)))  {
                    return;
                }
            }
    		WindowManager.getCurrentImage().updateAndDraw();
    		log("Gaussian blurred the current image with radius " + Float.parseFloat(radiusString) + ".");
      	} else if (e.getActionCommand().equals("lutFire")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
		    IJ.run("Fire");
      	} else if (e.getActionCommand().equals("lutSpectrum")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
		    IJ.run("Spectrum");
      	} else if (e.getActionCommand().equals("histogram")) {
            if (WindowManager.getCurrentImage() == null) {
                logError("No open image.");
                return;
            }
		    IJ.run("Histogram");
      	} else if (e.getActionCommand().equals("openImage")) {
      	    (new Opener()).open();
      	} else if (e.getActionCommand().equals("saveImageAsTiff")) {
            ImagePlus image = WindowManager.getCurrentImage();
            if (image == null) {
                logError("No open image.");
                return;
            }
			FileSaver fs = new FileSaver(image);
	 		if (fs.saveAsTiff()){
                log("Tiff file is saved.");
            }
            image.updateAndDraw();
      	} else if (e.getActionCommand().equals("saveImageAsBmp")) {
            ImagePlus image = WindowManager.getCurrentImage();
            if (image == null) {
                logError("No open image.");
                return;
            }
			FileSaver fs = new FileSaver(image);
	 		if (fs.saveAsBmp()){
                log("Bmp file is saved.");
            }
            image.updateAndDraw();
      	} else if (e.getActionCommand().equals("saveMessages")) {
            JFileChooser jfc = new JFileChooser(currentDirectory);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setDialogTitle("Save messages...");
            jfc.showSaveDialog(this);
            if (jfc.getSelectedFile() == null) {
                return;
            }
            if (jfc.getSelectedFile().exists()) {
                currentDirectory = jfc.getCurrentDirectory().toString();
                int resp = JOptionPane.showConfirmDialog(this,
                    "Overwrite existing file?","Confirmation",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(jfc.getSelectedFile().getAbsolutePath()));
                out.write(log.getText());
                out.close();
            } catch (IOException ioe) {
                logError("Could not save messages.");
            }
      	} else if (e.getActionCommand().equals("clearMessages")) {
            log.setText("");
      	} else if (e.getActionCommand().equals("openLsmImage")) {
            JFileChooser jfc = new JFileChooser(currentDirectory);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setDialogTitle("Open LSM image...");
            jfc.showOpenDialog(this);
            if (jfc.getSelectedFile() == null) {
                return;
            }
            if (!jfc.getSelectedFile().exists()) {
                logError("Selected file does not exist.");
                return;
            }
            try {
                currentDirectory = jfc.getCurrentDirectory().toString();
                boolean close = false;
                boolean resultsWindow = false;
                while(WindowManager.getCurrentImage() != null) {
                    WindowManager.getCurrentImage().close();
                }

                resetAllButtonColors();

                File imageFile = jfc.getSelectedFile();
                (new Opener()).open(imageFile.getAbsolutePath());
                WindowManager.putBehind();
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"split"));
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"setAcceptorInAImage"));
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"lutSpectrum"));
                WindowManager.putBehind();
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"setDonorInAImage"));
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"lutSpectrum"));
                WindowManager.putBehind();
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"setDonorInDImage"));
                this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"lutSpectrum"));
            } catch (Exception ex) {
                logError("Could not open and set the selected LSM image.");
                logException(ex.getMessage(), ex);
            }
      	} else if (e.getActionCommand().equals("setDonorInDImage")) {
            ImagePlus ip = WindowManager.getCurrentImage();
      	    if (ip == null) {
                logError("No image is selected.");
                setDonorInDImageButton.setBackground(originalButtonColor);
                return;
            }
            if (ip.isHyperStack()) {
                logError("Current image is a hyperstack.");
                donorInDImage = null;
                setDonorInDImageButton.setBackground(originalButtonColor);
                return;
            }
            if (donorInAImage != null && donorInAImage.equals(ip)) {
                logError("This image has already been set as transfer channel.");
                donorInDImage = null;
                setDonorInDImageButton.setBackground(originalButtonColor);
                return;
            } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                logError("This image has already been set as acceptor channel.");
                donorInDImage = null;
                setDonorInDImageButton.setBackground(originalButtonColor);
                return;
            }
            if (donorInAImage != null && ip.getImageStackSize() != donorInAImage.getImageStackSize()) {
                logError("Transfer channel contains " + donorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                donorInDImage = null;
                setDonorInDImageButton.setBackground(originalButtonColor);
                return;
            } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                donorInDImage = null;
                setDonorInDImageButton.setBackground(originalButtonColor);
                return;
            } else if (ip.getImageStackSize() > 1) {
                logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
            }
            donorInDImage = ip;
            donorInDImage.setTitle("Donor channel - " + new Date().toString());
            if (ip.getImageStackSize() > 1) {
                new StackConverter(donorInDImage).convertToGray32();
            } else {
                new ImageConverter(donorInDImage).convertToGray32();
            }
            if (automaticallyProcessedFiles == null) {
                currentlyProcessedFileName = null;
            }
            setDonorInDImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("setDonorInAImage")) {
            ImagePlus ip = WindowManager.getCurrentImage();
      	    if (ip == null) {
                logError("No image is selected.");
                setDonorInAImageButton.setBackground(originalButtonColor);
                return;
            }
            if (ip.isHyperStack()) {
                logError("Current image is a hyperstack.");
                donorInAImage = null;
                setDonorInAImageButton.setBackground(originalButtonColor);
                return;
            }
            if (donorInDImage != null && donorInDImage.equals(ip)) {
                logError("This image has already been set as donor channel.");
                donorInAImage = null;
                setDonorInAImageButton.setBackground(originalButtonColor);
                return;
            } else if (acceptorInAImage != null && acceptorInAImage.equals(ip)) {
                logError("This image has already been set as acceptor channel.");
                donorInAImage = null;
                setDonorInAImageButton.setBackground(originalButtonColor);
                return;
            }
            if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                donorInAImage = null;
                setDonorInAImageButton.setBackground(originalButtonColor);
                return;
            } else if (acceptorInAImage != null && ip.getImageStackSize() != acceptorInAImage.getImageStackSize()) {
                logError("Acceptor channel contains " + acceptorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                donorInAImage = null;
                setDonorInAImageButton.setBackground(originalButtonColor);
                return;
            } else if (ip.getImageStackSize() > 1) {
                logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
            }
            donorInAImage = ip;
            donorInAImage.setTitle("Transfer channel - " + new Date().toString());
            if (ip.getImageStackSize() > 1) {
                new StackConverter(donorInAImage).convertToGray32();
            } else {
                new ImageConverter(donorInAImage).convertToGray32();
            }
            setDonorInAImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("setAcceptorInAImage")) {
            ImagePlus ip = WindowManager.getCurrentImage();
      	    if (ip == null) {
                logError("No image is selected.");
                setAcceptorInAImageButton.setBackground(originalButtonColor);
                return;
            }
            if (ip.isHyperStack()) {
                logError("Current image is a hyperstack.");
                acceptorInAImage = null;
                setAcceptorInAImageButton.setBackground(originalButtonColor);
                return;
            }
            if (donorInDImage != null && donorInDImage.equals(ip)) {
                logError("This image has already been set as donor channel.");
                acceptorInAImage = null;
                setAcceptorInAImageButton.setBackground(originalButtonColor);
                return;
            } else if (donorInAImage != null && donorInAImage.equals(ip)) {
                logError("This image has already been set as transfer channel.");
                acceptorInAImage = null;
                setAcceptorInAImageButton.setBackground(originalButtonColor);
                return;
            }
            if (donorInDImage != null && ip.getImageStackSize() != donorInDImage.getImageStackSize()) {
                logError("Donor channel contains " + donorInDImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                acceptorInAImage = null;
                setAcceptorInAImageButton.setBackground(originalButtonColor);
                return;
            } else if (donorInAImage != null && ip.getImageStackSize() != donorInAImage.getImageStackSize()) {
                logError("Transfer channel contains " + donorInAImage.getImageStackSize() + " image(s), not " + ip.getImageStackSize() + ".");
                acceptorInAImage = null;
                setAcceptorInAImageButton.setBackground(originalButtonColor);
                return;
            } else if (ip.getImageStackSize() > 1) {
                logWarning("A stack has been set. Thresholds have to be set one by one for the images in it.");
            }
            acceptorInAImage = ip;
            acceptorInAImage.setTitle("Acceptor channel - " + new Date().toString());
            if (ip.getImageStackSize() > 1) {
                new StackConverter(acceptorInAImage).convertToGray32();
            } else {
                new ImageConverter(acceptorInAImage).convertToGray32();
            }
            setAcceptorInAImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("copyRoi")) {
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
            } else {
          	    if (donorInAImage != null) {
                    donorInAImage.killRoi();
                }
          	    if (acceptorInAImage != null) {
                    acceptorInAImage.killRoi();
                }
            }
      	} else if (e.getActionCommand().equals("subtractDonorInDImage")) {
      	    if (donorInDImage == null) {
                logError("No image is set as donor channel.");
                return;
            } else if (donorInDImage.getRoi() == null) {
                logError("No ROI is defined for donor channel.");
                return;
            }

            float autofl = 0;
            if (!autoflDInDField.getText().trim().equals("")) {
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
                for (int i=0; i < width; i++) {
                    for (int j=0; j < height; j++) {
                        if (donorInDImage.getRoi().contains(i, j)) {
                            sum += donorInDImage.getStack().getProcessor(currentSlice).getPixelValue(i,j);
                            count++;
                        }
		            }
		        }
		        float backgroundAvg = (float)(sum/count);

		        backgroundAvg = backgroundAvg + autofl;

                float i = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        i = donorInDImage.getStack().getProcessor(currentSlice).getPixelValue(x,y);
                        i = i - backgroundAvg;
                        if (i < 0) {
                           i=0;
                        }
		                donorInDImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
		            }
		        }
     		    FloatProcessor flp = new FloatProcessor(donorInDImage.getStack().getProcessor(currentSlice).getWidth(), donorInDImage.getStack().getProcessor(currentSlice).getHeight());
                flp.setPixels(currentSlice, (FloatProcessor)donorInDImage.getStack().getProcessor(currentSlice).duplicate());
                donorInDImageSave.addSlice(""+currentSlice, flp);
                if (nSlices == 1) {
                    log("Subtracted background "+(autofl>0?"and autofluorescence ":"")+"(" + df.format(backgroundAvg).toString() + ") of donor channel.");
                } else {
                    log("Subtracted background "+(autofl>0?"and autofluorescence ":"")+"(" + df.format(backgroundAvg).toString() + ") of slice " + currentSlice + " of donor channel.");
                }
		    }
		    donorInDImage.updateAndDraw();
		    donorInDImage.killRoi();
		    donorInDImageSave.setColorModel(donorInDImage.getProcessor().getColorModel());
            subtractDonorInDImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("subtractDonorInAImage")) {
      	    if (donorInAImage == null) {
                logError("No image is set as transfer channel.");
                return;
            } else if (donorInAImage.getRoi() == null) {
                logError("No ROI is defined for transfer channel.");
                return;
            }

            float autofl = 0;
            if (!autoflAInDField.getText().trim().equals("")) {
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
                for (int i=0; i < width; i++) {
                    for (int j=0; j < height; j++) {
                        if (donorInAImage.getRoi().contains(i, j)) {
                            sum += donorInAImage.getStack().getProcessor(currentSlice).getPixelValue(i,j);
                            count++;
                        }
		            }
		        }
		        float backgroundAvg = (float)(sum/count);

		        backgroundAvg = backgroundAvg + autofl;

                float i = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        i = donorInAImage.getStack().getProcessor(currentSlice).getPixelValue(x,y);
                        i = i - backgroundAvg;
                        if (i < 0) {
                           i=0;
                        }
		                donorInAImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
		            }
		        }
     		    FloatProcessor flp = new FloatProcessor(donorInAImage.getStack().getProcessor(currentSlice).getWidth(), donorInAImage.getStack().getProcessor(currentSlice).getHeight());
                flp.setPixels(currentSlice, (FloatProcessor)donorInAImage.getStack().getProcessor(currentSlice).duplicate());
                donorInAImageSave.addSlice(""+currentSlice, flp);
                if (nSlices == 1) {
                    log("Subtracted background "+(autofl>0?"and autofluorescence ":"")+"(" + df.format(backgroundAvg).toString() + ") of transfer channel.");
                } else {
                    log("Subtracted background "+(autofl>0?"and autofluorescence ":"")+"(" + df.format(backgroundAvg).toString() + ") of slice " + currentSlice + " of transfer channel.");
                }
		    }
		    donorInAImage.updateAndDraw();
		    donorInAImage.killRoi();
		    donorInAImageSave.setColorModel(donorInAImage.getProcessor().getColorModel());
            subtractDonorInAImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("subtractAcceptorInAImage")) {
      	    if (acceptorInAImage == null) {
                logError("No image is set as acceptor channel.");
                return;
            } else if (acceptorInAImage.getRoi() == null) {
                logError("No ROI is defined for acceptor channel.");
                return;
            }

            float autofl = 0;
            if (!autoflAInAField.getText().trim().equals("")) {
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
                for (int i=0; i < width; i++) {
                    for (int j=0; j < height; j++) {
                        if (acceptorInAImage.getRoi().contains(i, j)) {
                            sum += acceptorInAImage.getStack().getProcessor(currentSlice).getPixelValue(i,j);
                            count++;
                        }
		            }
		        }
		        float backgroundAvg = (float)(sum/count);

		        backgroundAvg = backgroundAvg + autofl;

                float i = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        i = acceptorInAImage.getStack().getProcessor(currentSlice).getPixelValue(x,y);
                        i = i - backgroundAvg;
                        if (i < 0) {
                           i=0;
                        }
		                acceptorInAImage.getStack().getProcessor(currentSlice).putPixelValue(x, y, i);
		            }
		        }
     		    FloatProcessor flp = new FloatProcessor(acceptorInAImage.getStack().getProcessor(currentSlice).getWidth(), acceptorInAImage.getStack().getProcessor(currentSlice).getHeight());
                flp.setPixels(currentSlice, (FloatProcessor)acceptorInAImage.getStack().getProcessor(currentSlice).duplicate());
                acceptorInAImageSave.addSlice(""+currentSlice, flp);
                if (nSlices == 1) {
                    log("Subtracted background "+(autofl>0?"and autofluorescence ":"")+"(" + df.format(backgroundAvg).toString() + ") of acceptor channel.");
                } else {
                    log("Subtracted background "+(autofl>0?"and autofluorescence ":"")+"(" + df.format(backgroundAvg).toString() + ") of slice " + currentSlice + " of acceptor channel.");
                }
		    }
		    acceptorInAImage.updateAndDraw();
		    acceptorInAImage.killRoi();
		    acceptorInAImageSave.setColorModel(acceptorInAImage.getProcessor().getColorModel());
            subtractAcceptorInAImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("thresholdDonorInDImage")) {
      	    if (donorInDImage == null) {
                logError("No image is set as donor channel.");
                return;
            }
            IJ.selectWindow(donorInDImage.getTitle());
            IJ.run("Threshold...");
            thresholdDonorInDImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("thresholdDonorInAImage")) {
      	    if (donorInAImage == null) {
                logError("No image is set as transfer channel.");
                return;
            }
            IJ.selectWindow(donorInAImage.getTitle());
            IJ.run("Threshold...");
            thresholdDonorInAImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("thresholdAcceptorInAImage")) {
      	    if (acceptorInAImage == null) {
                logError("No image is set as acceptor channel.");
                return;
            }
            IJ.selectWindow(acceptorInAImage.getTitle());
            IJ.run("Threshold...");
            thresholdAcceptorInAImageButton.setBackground(greenColor);
      	} else if (e.getActionCommand().equals("resetDD")) {
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
                flp.setPixels(currentSlice, (FloatProcessor)donorInDImageSave.getProcessor(currentSlice).duplicate());
                newStack.addSlice(""+currentSlice, flp);
            }
            donorInDImage.setStack(donorInDImage.getTitle(), newStack);
            donorInDImage.getProcessor().setColorModel(donorInDImageSave.getColorModel());
            donorInDImage.updateAndDraw();
            thresholdDonorInDImageButton.setBackground(originalButtonColor);
            smoothDonorInDImageButton.setBackground(originalButtonColor);
      	} else if (e.getActionCommand().equals("resetDA")) {
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
                flp.setPixels(currentSlice, (FloatProcessor)donorInAImageSave.getProcessor(currentSlice).duplicate());
                newStack.addSlice(""+currentSlice, flp);
            }
            donorInAImage.setStack(donorInAImage.getTitle(), newStack);
            donorInAImage.getProcessor().setColorModel(donorInAImageSave.getColorModel());
            donorInAImage.updateAndDraw();
            thresholdDonorInAImageButton.setBackground(originalButtonColor);
            smoothDonorInAImageButton.setBackground(originalButtonColor);
      	} else if (e.getActionCommand().equals("resetAA")) {
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
                flp.setPixels(currentSlice, (FloatProcessor)acceptorInAImageSave.getProcessor(currentSlice).duplicate());
                newStack.addSlice(""+currentSlice, flp);
            }
            acceptorInAImage.setStack(acceptorInAImage.getTitle(), newStack);
            acceptorInAImage.getProcessor().setColorModel(acceptorInAImageSave.getColorModel());
            acceptorInAImage.updateAndDraw();
            thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
            smoothAcceptorInAImageButton.setBackground(originalButtonColor);
      	} else if (e.getActionCommand().equals("smoothDD")) {
      	    if (donorInDImage == null) {
                logError("No image is set as donor channel.");
                return;
            } else {
                if (radiusFieldDD.getText().trim().equals("")) {
                    logError("Radius has to be given for Gaussian blur.");
                    return;
                } else {
                    double radius = 0;
                    try {
                        radius = Double.parseDouble(radiusFieldDD.getText().trim());
                    } catch (Exception ex) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    }
                    GaussianBlur gb = new GaussianBlur();
                    int nSlices = donorInDImage.getImageStackSize();
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        if(!gb.blur(donorInDImage.getStack().getProcessor(currentSlice), radius))  {
                            return;
                        }
                    }
    		        donorInDImage.updateAndDraw();
                    smoothDonorInDImageButton.setBackground(greenColor);
    		        log("Gaussian blurred donor channel.");
    		    }
            }
      	} else if (e.getActionCommand().equals("smoothDA")) {
      	    if (donorInAImage == null) {
                logError("No image is set as transfer channel.");
                return;
            } else {
                if (radiusFieldDA.getText().trim().equals("")) {
                    logError("Radius has to be given for Gaussian blur.");
                    return;
                } else {
                    double radius = 0;
                    try {
                        radius = Double.parseDouble(radiusFieldDA.getText().trim());
                    } catch (Exception ex) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    }
                    GaussianBlur gb = new GaussianBlur();
                    int nSlices = donorInAImage.getImageStackSize();
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        if(!gb.blur(donorInAImage.getStack().getProcessor(currentSlice), radius))  {
                            return;
                        }
                    }
    		        donorInAImage.updateAndDraw();
                    smoothDonorInAImageButton.setBackground(greenColor);
    		        log("Gaussian blurred transfer channel.");
    		    }
            }
      	} else if (e.getActionCommand().equals("smoothAA")) {
      	    if (acceptorInAImage == null) {
                logError("No image is set as acceptor channel.");
                return;
            } else {
                if (radiusFieldAA.getText().trim().equals("")) {
                    logError("Radius has to be given for Gaussian blur.");
                    return;
                } else {
                    double radius = 0;
                    try {
                        radius = Double.parseDouble(radiusFieldAA.getText().trim());
                    } catch (Exception ex) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    }
                    GaussianBlur gb = new GaussianBlur();
                    int nSlices = acceptorInAImage.getImageStackSize();
                    for (int currentSlice = 1; currentSlice <= nSlices; currentSlice++) {
                        if(!gb.blur(acceptorInAImage.getStack().getProcessor(currentSlice), radius))  {
                            return;
                        }
                    }
    		        acceptorInAImage.updateAndDraw();
                    smoothAcceptorInAImageButton.setBackground(greenColor);
    		        log("Gaussian blurred acceptor channel.");
    		    }
            }
      	} else if (e.getActionCommand().equals("useLsmImages")) {
          	if (useLsmImages.isSelected()) {
          	    setDonorInDImageButton.setText("Open & Set LSM");
          	    setDonorInDImageButton.setActionCommand("openLsmImage");
          	    setDonorInAImageButton.setEnabled(false);
          	    setAcceptorInAImageButton.setEnabled(false);
          	} else {
          	    setDonorInDImageButton.setText("Set image");
          	    setDonorInDImageButton.setActionCommand("setDonorInDImage");
          	    setDonorInAImageButton.setEnabled(true);
          	    setAcceptorInAImageButton.setEnabled(true);
          	}
            logScrollPane.setPreferredSize(new Dimension(10,10));
      	} else if (e.getActionCommand().equals("calculateS1S3Button")) {
            if (s1S3Dialog != null) {
                s1S3Dialog.setVisible(false);
                s1S3Dialog.dispose();
            }
            s1S3Dialog = new S1S3Dialog(this);
		    s1S3Dialog.setVisible(true);
      	} else if (e.getActionCommand().equals("calculateS2S4Button")) {
            if (s2S4Dialog != null) {
                s2S4Dialog.setVisible(false);
                s2S4Dialog.dispose();
            }
            s2S4Dialog = new S2S4Dialog(this);
		    s2S4Dialog.setVisible(true);
      	} else if (e.getActionCommand().equals("calculateAlphaButton")) {
            if (alphaDialog != null) {
                alphaDialog.setVisible(true);
            } else {
                alphaDialog = new AlphaDialog(this);
		        alphaDialog.setVisible(true);
		    }
      	} else if (e.getActionCommand().equals("createFretImage")) {
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
                    if (autoThresholdMin.getText().trim().equals("")) {
                        logError("Auto-threshold min value has to be given.");
                        return;
                    } else if (autoThresholdMax.getText().trim().equals("")) {
                        logError("Auto-threshold max value has to be given.");
                        return;
                    }
                }
                if (s1Field.getText().trim().equals("")) {
                    logError("S1 factor has to be given.");
                    return;
                } else if (s2Field.getText().trim().equals("")) {
                    logError("S2 factor has to be given.");
                    return;
                } else if (s3Field.getText().trim().equals("")) {
                    logError("S3 factor has to be given.");
                    return;
                } else if (s4Field.getText().trim().equals("")) {
                    logError("S4 factor has to be given.");
                    return;
                } else if (alphaField.getText().trim().equals("")) {
                    logError("Alpha factor has to be given.");
                    return;
                } else {
                    double s1Factor = 0;
                    try {
                        s1Factor = Double.parseDouble(s1Field.getText().trim());
                    } catch (Exception ex) {
                        logError("S1 factor has to be given.");
                        return;
                    }
                    if (s1Factor <= 0) {
                        logWarning("S1 factor should be higher than 0.");
                    }
                    double s2Factor = 0;
                    try {
                        s2Factor = Double.parseDouble(s2Field.getText().trim());
                    } catch (Exception ex) {
                        logError("S2 factor has to be given.");
                        return;
                    }
                    if (s2Factor <= 0) {
                        logWarning("S2 factor should be higher than 0.");
                    }
                    double s3Factor = 0;
                    try {
                        s3Factor = Double.parseDouble(s3Field.getText().trim());
                    } catch (Exception ex) {
                        logError("S3 factor has to be given.");
                        return;
                    }
                    if (s3Factor < 0) {
                        logWarning("S3 factor should be higher than 0.");
                    }
                    double s4Factor = 0;
                    try {
                        s4Factor = Double.parseDouble(s4Field.getText().trim());
                    } catch (Exception ex) {
                        logError("S4 factor has to be given.");
                        return;
                    }
                    if (s4Factor < 0) {
                        logWarning("S4 factor should be higher than 0.");
                    }
                    double alphaFactor = 0;
                    try {
                        alphaFactor = Double.parseDouble(alphaField.getText().trim());
                    } catch (Exception ex) {
                        logError("Alpha factor has to be given.");
                        return;
                    }

                    double autoThMin = 0;
                    double autoThMax = 0;
                    if (autoThresholdingCB.isSelected()) {
                        try {
                            autoThMin = Double.parseDouble(autoThresholdMin.getText().trim());
                        } catch (Exception ex) {
                            logError("Auto-threshold min value has to be given.");
                            return;
                        }

                        try {
                            autoThMax = Double.parseDouble(autoThresholdMax.getText().trim());
                        } catch (Exception ex) {
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

                        float[] ipDDP = (float[])ipDD.getPixels();
                        float[] ipDAP = (float[])ipDA.getPixels();
                        float[] ipAAP = (float[])ipAA.getPixels();

                        for (int i = 0; i < ipDDP.length; i++) {
                            if (!Float.isNaN(ipDDP[i]) && !Float.isNaN(ipDAP[i]) && !Float.isNaN(ipAAP[i])) {
                                ipDDP[i] = (float)((s1Factor*s2Factor*(ipDAP[i]*((double)1-s3Factor*s4Factor)-ipDDP[i]*(s1Factor-s2Factor*s3Factor)-ipAAP[i]*(s2Factor-s1Factor*s4Factor)))/((s1Factor-s2Factor*s3Factor)*(ipDDP[i]*s2Factor-ipDAP[i]*s4Factor)*alphaFactor));
                            } else {
                                ipDDP[i] = Float.NaN;
                            }
                        }

                        for (int i = 0; i < ipDDP.length; i++) {
                            ipDDP[i] = ipDDP[i]/((float)1 + ipDDP[i]);
                        }

                        int width = ipDD.getWidth();
                        int height = ipDD.getHeight();
                        float[][] tiPoints = new float[width][height];
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (autoThresholdingCB.isSelected()) {
                                    if (ipDDP[width*j+i] >= autoThMin && ipDDP[width*j+i] <= autoThMax) {
                                        tiPoints[i][j] = ipDDP[width*j+i];
                                    } else {
                                        tiPoints[i][j] = Float.NaN;
                                    }
                                } else {
                                    tiPoints[i][j] = ipDDP[width*j+i];
                                }
                            }
                        }

                        FloatProcessor tiFp = new FloatProcessor(tiPoints);
                        transferStack.addSlice(""+currentSlice, tiFp);
                    }
                    if (transferImage != null) {
                        transferImage.close();
                    }
                    transferImage = new ImagePlus("Transfer (FRET) image", transferStack);
                    transferImage.setCalibration(donorInDImage.getCalibration());
                    transferImage.show();

                    analyzer = new Analyzer();
            		resultsTable =Analyzer.getResultsTable();
		            resultsTable.setPrecision(3);
                    resultsTable.incrementCounter();
                    int widthTi = transferImage.getWidth();
                    int heightTi = transferImage.getHeight();
                    if(currentlyProcessedFileName != null) {
                        resultsTable.addLabel("File", currentlyProcessedFileName);
                    }
                    if (transferImage.getRoi() != null) {
                        Roi roi = transferImage.getRoi();
                        int count = 0;
                        int notNan = 0;
                        for (int i=0; i<widthTi; i++) {
                            for (int j=0; j<heightTi; j++) {
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
                        for (int i=0; i<widthTi; i++) {
                            for (int j=0; j<heightTi; j++) {
                               if (transferImage.getStack().getProcessor(1).getPixelValue(i, j) >= -1) {
                                   notNan++;
                               }
		                    }
		                }
                        resultsTable.addValue("Pixels", widthTi*heightTi);
                        resultsTable.addValue("Not NaN p.", notNan);
                    }
                    ImageStatistics isMean = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MEAN,null);
                    resultsTable.addValue("Mean", (float)isMean.mean);
                    ImageStatistics isMedian = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MEDIAN,null);
                    resultsTable.addValue("Median", (float)isMedian.median);
                    ImageStatistics isStdDev = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.STD_DEV,null);
                    resultsTable.addValue("Std. dev.", (float)isStdDev.stdDev);
                    ImageStatistics isMinMax = ImageStatistics.getStatistics(transferImage.getStack().getProcessor(1), Measurements.MIN_MAX,null);
                    resultsTable.addValue("Min", (float)isMinMax.min);
                    resultsTable.addValue("Max", (float)isMinMax.max);
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
      	} else if (e.getActionCommand().equals("measureFretImage")) {
            if (transferImage == null) {
                logError("Transfer (FRET) image is required.");
                return;
            }
            resultsTable.incrementCounter();
            int currentSlice = transferImage.getCurrentSlice();
            int width = transferImage.getWidth();
            int height = transferImage.getHeight();
            if(currentlyProcessedFileName != null) {
                resultsTable.addLabel("File", currentlyProcessedFileName);
            }
            ImageProcessor trProc = transferImage.getStack().getProcessor(currentSlice);
            trProc.setRoi(transferImage.getRoi());
            if (transferImage.getRoi() != null) {
                Roi roi = transferImage.getRoi();
                int count = 0;
                int notNan = 0;
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (!Float.isNaN(trProc.getPixelValue(i, j))) {
                            notNan++;
                        }
		            }
		        }
                resultsTable.addValue("Pixels", width*height);
                resultsTable.addValue("Not NaN p.", notNan);
            }
            ImageStatistics isMean = ImageStatistics.getStatistics(trProc, Measurements.MEAN,null);
            resultsTable.addValue("Mean", (float)isMean.mean);
            ImageStatistics isMedian = ImageStatistics.getStatistics(trProc, Measurements.MEDIAN,null);
            resultsTable.addValue("Median", (float)isMedian.median);
            ImageStatistics isStdDev = ImageStatistics.getStatistics(trProc, Measurements.STD_DEV,null);
            resultsTable.addValue("Std. dev.", (float)isStdDev.stdDev);
            ImageStatistics isMinMax = ImageStatistics.getStatistics(trProc, Measurements.MIN_MAX,null);
            resultsTable.addValue("Min", (float)isMinMax.min);
            resultsTable.addValue("Max", (float)isMinMax.max);
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
      	} else if (e.getActionCommand().equals("semiAutomaticProcessing")) {
        	int choice = JOptionPane.showConfirmDialog(this, "Semi-automatic processing of images\n\nOpens and processes FRET images in a given directory. It works with\n"+
                                                             "Zeiss LSM images (tested with LSM 510 Version 4.0), which contain three\n"+
                                                             "channels:\n"+
                                                             "1. donor channel\n"+
                                                             "2. transfer channel\n"+
                                                             "3. acceptor channel\n\n"+
                                                             "The upper left corner (1/6 x 1/6 of the image) is considered as background.\n"+
                                                             "Threshold settings, creation of FRET image and measurements have to be\n"+
                                                             "made manually.\n\n"+
                                                             "Every previously opened image and result window will be closed when you\n"+
                                                             "press \"Ok\".\n\n"+
                                                             "Press \"Ok\" to select the directory. To continue with the next "+
                                                             "image, do\nnot close any windows, just press the \"Next\" button.\n", "Semi-automatic processing of images", JOptionPane.OK_CANCEL_OPTION);
            if(choice == JOptionPane.YES_OPTION) {
                currentlyProcessedFile = 0;
                automaticallyProcessedFiles = null;
                currentlyProcessedFileName = null;
                WindowManager.closeAllWindows();
                JFileChooser chooser = new JFileChooser(currentDirectory);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Select directory");
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    log("Processing files in directory: " + chooser.getSelectedFile());
                    currentDirectory = chooser.getSelectedFile().toString();
                } else {
                    log("Semi-automatic processing: no directory is selected.");
                    return;
                }
                nextButton.setVisible(true);
                useLsmImages.setSelected(true);
                logScrollPane.setPreferredSize(new Dimension(10,10));
                automaticallyProcessedFiles = chooser.getSelectedFile().listFiles();
                processFile(0);
            }
      	} else if (e.getActionCommand().equals("nextImage")) {
            if(transferImage != null) {
                transferImage.changes = false;
                transferImage.close();
      	    }
            if(donorInDImage != null) {
                donorInDImage.changes = false;
                donorInDImage.close();
      	    }
      	    if(donorInAImage != null) {
      	        donorInAImage.changes = false;
                donorInAImage.close();
      	    }
      	    if(acceptorInAImage != null) {
      	        acceptorInAImage.changes = false;
                acceptorInAImage.close();
      	    }
            IJ.selectWindow("Results");
       	    WindowManager.putBehind();
       	    if(WindowManager.getCurrentImage() != null) {
                WindowManager.getCurrentImage().close();
            }
         	processFile(++currentlyProcessedFile);
      	} else if (e.getActionCommand().equals("resetImages")) {
      	    resetAll();
      	} else if (e.getActionCommand().equals("closeImages")) {
            if(transferImage != null) {
                transferImage.changes = false;
                transferImage.close();
      	    }
            if(donorInDImage != null) {
                donorInDImage.changes = false;
                donorInDImage.close();
      	    }
      	    if(donorInAImage != null) {
      	        donorInAImage.changes = false;
                donorInAImage.close();
      	    }
      	    if(acceptorInAImage != null) {
      	        acceptorInAImage.changes = false;
                acceptorInAImage.close();
      	    }
            resetAll();
      	} else if (e.getActionCommand().equals("help")) {
            if (helpWindow != null) {
                helpWindow.setVisible(false);
                helpWindow.dispose();
            }
      	    helpWindow = new RiHelpWindow(this);
      	    helpWindow.setVisible(true);
      	} else if (e.getActionCommand().equals("checkVersion")) {
	        InputStream is = null;
            try{
                URL url= new URL("http://biophys.med.unideb.hu/rifret/riversion.txt");
                byte[] buffer = new byte[4];
                URLConnection urlCon = url.openConnection();
                is = urlCon.getInputStream();
                String ver = "";
                while (is.read(buffer) != -1) {
                    ver += new String(buffer);
                }
                float verf = Float.parseFloat(ver);
                if(verf > version){
                    int choice = JOptionPane.showConfirmDialog(this, "There is a newer version on the RiFRET homepage.\n" +
                                  "Your version: " + version + "\n" +
                                  "New version: " + ver + "\n" +
                                  "You can download it from: http://biophys.med.unideb.hu/rifret/ \n" +
                                  "Do you want to download it now?", "Checking for latest version", JOptionPane.YES_NO_OPTION);
                    if(choice == JOptionPane.YES_OPTION) {
                        IJ.runPlugIn("ij.plugin.BrowserLauncher", "http://biophys.med.unideb.hu/rifret/");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "There is no newer version on the RiFRET homepage.\n" +
                                  "Your version: " + version + "\n" +
                                  "Version on the server: " + ver, "Checking for latest version", JOptionPane.INFORMATION_MESSAGE);
                }
        	} catch (Exception e1) {
                logException(e1.toString(), e1);
            } finally {
                try {
                    if (is != null) {is.close();}
                } catch (Exception e2) {
                    logException(e2.getMessage(), e2);
                }
            }
      	} else if (e.getActionCommand().equals("about")) {
            JOptionPane optionPane = new JOptionPane();
            optionPane.setMessage("RiFRET - an ImageJ plugin for intensity-based ratiometric FRET imaging\n" +
                                  "Homepage: http://biophys.med.unideb.hu/rifret/\n" +
			                      "Written by: János Roszik (janosr@med.unideb.hu), Duarte Lisboa (duarte@med.unideb.hu),\n" +
                                  "János Szöllõsi (szollo@med.unideb.hu) and György Vereb (vereb@med.unideb.hu)\n" +
                                  "Version: " + version + " (" + lastModified + ")\n" +
                                  "The plugin was tested with ImageJ version " + imageJVersion + " using Java " + javaVersion + ".\n");
            optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog(this, "About");
            dialog.setVisible(true);
        }
        } catch (Throwable t) {
            logException(t.toString(), t);
        }
    }


    private void processFile(int currentFile) {
        resetAllButtonColors();
        if(currentFile >= automaticallyProcessedFiles.length) {
            log("Processing files has been finished.");
            nextButton.setVisible(false);
            logScrollPane.setPreferredSize(new Dimension(10,10));
            IJ.selectWindow("Results");
            currentlyProcessedFile = 0;
            automaticallyProcessedFiles = null;
            currentlyProcessedFileName = null;
            return;
        }
        if(!automaticallyProcessedFiles[currentFile].isFile() || !(automaticallyProcessedFiles[currentFile].getName().endsWith(".lsm") || automaticallyProcessedFiles[currentFile].getName().endsWith(".LSM"))) {
            processFile(++currentlyProcessedFile);
            return;
        }
        log("Current file is: " + automaticallyProcessedFiles[currentFile].getName());
        currentlyProcessedFileName = automaticallyProcessedFiles[currentFile].getName();
        (new Opener()).open(automaticallyProcessedFiles[currentFile].getAbsolutePath());
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"split"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"setAcceptorInAImage"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"lutSpectrum"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"setDonorInAImage"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"lutSpectrum"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"setDonorInDImage"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"lutSpectrum"));
        donorInDImage.setRoi(new Roi(0, 0, donorInDImage.getWidth()/6, donorInDImage.getHeight()/6));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"copyRoi"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"subtractDonorInDImage"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"subtractDonorInAImage"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"subtractAcceptorInAImage"));
        donorInDImage.setRoi(new Roi(0, 0, donorInDImage.getWidth()/6, donorInDImage.getHeight()/6));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"copyRoi"));
        donorInDImage.getProcessor().setValue(0);
        donorInDImage.getProcessor().fill();
        donorInAImage.getProcessor().setValue(0);
        donorInAImage.getProcessor().fill();
        acceptorInAImage.getProcessor().setValue(0);
        acceptorInAImage.getProcessor().fill();
        donorInDImage.killRoi();
        donorInAImage.killRoi();
        acceptorInAImage.killRoi();
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"smoothDD"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"smoothDA"));
        this.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"smoothAA"));
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
        setDonorInDImageButton.setBackground(originalButtonColor);
        setDonorInAImageButton.setBackground(originalButtonColor);
        setAcceptorInAImageButton.setBackground(originalButtonColor);
        subtractDonorInDImageButton.setBackground(originalButtonColor);
        subtractDonorInAImageButton.setBackground(originalButtonColor);
        subtractAcceptorInAImageButton.setBackground(originalButtonColor);
        smoothDonorInDImageButton.setBackground(originalButtonColor);
        smoothDonorInAImageButton.setBackground(originalButtonColor);
        smoothAcceptorInAImageButton.setBackground(originalButtonColor);
        thresholdDonorInDImageButton.setBackground(originalButtonColor);
        thresholdDonorInAImageButton.setBackground(originalButtonColor);
        thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
        calculateS1S3Button.setBackground(originalButtonColor);
        calculateS2S4Button.setBackground(originalButtonColor);
        calculateAlphaButton.setBackground(originalButtonColor);

        nextButton.setVisible(false);
        logScrollPane.setPreferredSize(new Dimension(10,10));
        currentlyProcessedFile = 0;
        automaticallyProcessedFiles = null;
        currentlyProcessedFileName = null;
    }


    private void resetAllButtonColors() {
        setDonorInDImageButton.setBackground(originalButtonColor);
        setDonorInAImageButton.setBackground(originalButtonColor);
        setAcceptorInAImageButton.setBackground(originalButtonColor);
        subtractDonorInDImageButton.setBackground(originalButtonColor);
        subtractDonorInAImageButton.setBackground(originalButtonColor);
        subtractAcceptorInAImageButton.setBackground(originalButtonColor);
        smoothDonorInDImageButton.setBackground(originalButtonColor);
        smoothDonorInAImageButton.setBackground(originalButtonColor);
        smoothAcceptorInAImageButton.setBackground(originalButtonColor);
        thresholdDonorInDImageButton.setBackground(originalButtonColor);
        thresholdDonorInAImageButton.setBackground(originalButtonColor);
        thresholdAcceptorInAImageButton.setBackground(originalButtonColor);
        calculateS1S3Button.setBackground(originalButtonColor);
        calculateS2S4Button.setBackground(originalButtonColor);
        calculateAlphaButton.setBackground(originalButtonColor);
    }


    public void log(String text) {
	    try{
	        log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " " + text, log.getStyle("BLACK"));
	        log.setCaretPosition(log.getDocument().getLength());
    	} catch (javax.swing.text.BadLocationException e) {}
    }


    public void logError(String text) {
	    try{
	        log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + text, log.getStyle("RED"));
	        log.setCaretPosition(log.getDocument().getLength());
    	} catch (javax.swing.text.BadLocationException e) {}
    }


    public void logWarning(String text) {
	    try{
	        log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " WARNING: " + text, log.getStyle("BLUE"));
	        log.setCaretPosition(log.getDocument().getLength());
    	} catch (javax.swing.text.BadLocationException e) {}
    }


    public void logException(String message, Throwable t) {
    	try{
    	    if (debugMenuItem.isSelected()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.flush();
	            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + sw.toString(), log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            } else {
	            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + message, log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            }
    	} catch (javax.swing.text.BadLocationException e) {}
    }


    public void exit() {
    	int choice = JOptionPane.showConfirmDialog(this, "Do you really want to exit?", "Exit", JOptionPane.OK_CANCEL_OPTION);
        if(choice == JOptionPane.YES_OPTION) {
            if (s1S3Dialog != null) {
                s1S3Dialog.setVisible(false);
                s1S3Dialog.dispose();
            }
            if (s2S4Dialog != null) {
                s2S4Dialog.setVisible(false);
                s2S4Dialog.dispose();
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
            if (helpWindow != null) {
                helpWindow.setVisible(false);
                helpWindow.dispose();
            }
            setVisible(false);
            dispose();
        }
    }


	public void windowClosing(WindowEvent e){
		exit();
	}


	public void windowActivated(WindowEvent e){}


	public void windowClosed(WindowEvent e){}


	public void windowDeactivated(WindowEvent e){}


	public void windowDeiconified(WindowEvent e){
        logScrollPane.setPreferredSize(new Dimension(10,10));
    }


	public void windowIconified(WindowEvent e){}


	public void windowOpened(WindowEvent e){}


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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            return -1;
        }
        return s4Factor;
    }


    public void setS4Factor(String value) {
        s4Field.setText(value);
    }


    public void setAlphaFactor(String value) {
        alphaField.setText(value);
    }


    public static void main(String args[]) {
        new RiFRET_Plugin();
    }

}


class ApplyMaskRiDialog extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    private ImagePlus toMaskImg, maskImg;
    private JPanel panel;
    private JButton setToMaskImgButton, setMaskImgButton, createImagesButton;

    public ApplyMaskRiDialog(RiFRET_Plugin mainWindow) {
        setTitle("Apply mask to an image");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(275, 240);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>After setting an image to mask and a mask image (with NaN background pixels), two images will be created. The first one will contain the pixles which are not NaN in the mask, and the second one the others.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2,2,2,2);
        gc.gridx = 0;
        gc.gridy = 1;
        setToMaskImgButton = new JButton("Set image to be masked");
        setToMaskImgButton.addActionListener(this);
        setToMaskImgButton.setActionCommand("setImageToMask");
        panel.add(setToMaskImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setMaskImgButton = new JButton("Set mask image (with NaN bg. pixels)");
        setMaskImgButton.addActionListener(this);
        setMaskImgButton.setActionCommand("setMaskImage");
        panel.add(setMaskImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        createImagesButton = new JButton("Create masked images");
        createImagesButton.addActionListener(this);
        createImagesButton.setActionCommand("createImages");
        panel.add(createImagesButton, gc);

        getContentPane().add(panel);
    }

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("setImageToMask")) {
                toMaskImg = WindowManager.getCurrentImage();
      	        if (toMaskImg == null) {
                    mainWindow.logError("No image is selected. (Masking)");
                    return;
                }
                if (toMaskImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+toMaskImg.getImageStackSize()+"). Please split it into parts. (Masking)");
                   toMaskImg = null;
                   return;
                } else if (toMaskImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+toMaskImg.getNSlices()+"). Please split it into parts. (Masking)");
                   toMaskImg = null;
                   return;
                }
                toMaskImg.setTitle("Image to mask - " + new Date().toString());
                new ImageConverter(toMaskImg).convertToGray32();
                setToMaskImgButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setMaskImage")) {
                maskImg = WindowManager.getCurrentImage();
      	        if (maskImg == null) {
                    mainWindow.logError("No image is selected. (Masking)");
                    return;
                }
                if (maskImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+maskImg.getImageStackSize()+"). Please split it into parts. (Masking)");
                   maskImg = null;
                   return;
                } else if (maskImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+maskImg.getNSlices()+"). Please split it into parts. (Masking)");
                   maskImg = null;
                   return;
                }
                maskImg.setTitle("Mask image - " + new Date().toString());
                new ImageConverter(maskImg).convertToGray32();
                setMaskImgButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("createImages")) {
      	        if (toMaskImg == null) {
                    mainWindow.logError("No image to mask is set. (Masking)");
                    return;
                } else if (maskImg == null) {
                    mainWindow.logError("No mask image is set. (Masking)");
                    return;
                }
                ImageProcessor ipTM = toMaskImg.getProcessor();
                ImageProcessor ipM = maskImg.getProcessor();

                float[] ipTMP = (float[])ipTM.getPixels();
                float[] ipMP = (float[])ipM.getPixels();

                int width = ipTM.getWidth();
                int height = ipTM.getHeight();
                float[][] img1Points = new float[width][height];
                float[][] img2Points = new float[width][height];
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (!Float.isNaN(ipMP[width*j+i])) {
                            img1Points[i][j] = ipTMP[width*j+i];
                            img2Points[i][j] = Float.NaN;
                        } else {
                            img1Points[i][j] = Float.NaN;
                            img2Points[i][j] = ipTMP[width*j+i];
                        }
                    }
                }
                FloatProcessor fp1 = new FloatProcessor(img1Points);
                FloatProcessor fp2 = new FloatProcessor(img2Points);
                ImagePlus img2 = new ImagePlus("Masked image 2 (pixels outside the mask)", fp2);
                img2.show();
                ImagePlus img1 = new ImagePlus("Masked image 1 (pixels in the mask)", fp1);
                img1.show();
           }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}


class CalculateRatioDialog extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    private ImagePlus firstImg, secondImg;
    private JPanel panel;
    private JButton setFirstImgButton, setSecondImgButton, createRatioImageButton;
    private JCheckBox useMainWindowImages;

    public CalculateRatioDialog (RiFRET_Plugin mainWindow) {
        setTitle("Calculate ratio of two images");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(275, 250);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>After setting the two images and pressing the \"Create ratio image\" button, the ratio of the images (image 1 / image 2) will be calculated pixel-by-pixel and displayed as a new 32 bit image.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2,2,2,2);
        gc.gridx = 0;
        gc.gridy = 1;
        useMainWindowImages = new JCheckBox("use images of the main window (1a/1b)", false);
        useMainWindowImages.setActionCommand("useMainWindowImages");
        useMainWindowImages.addActionListener(this);
        useMainWindowImages.setToolTipText("<html>If this checkbox is checked, donor and transfer channel images<BR>which are set in the main window will be used as image 1 and<BR>image 2.</html>");
        panel.add(useMainWindowImages, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setFirstImgButton = new JButton("Set first image (numerator)");
        setFirstImgButton.addActionListener(this);
        setFirstImgButton.setActionCommand("setFirstImage");
        panel.add(setFirstImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setSecondImgButton = new JButton("Set second image (denominator)");
        setSecondImgButton.addActionListener(this);
        setSecondImgButton.setActionCommand("setSecondImage");
        panel.add(setSecondImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        createRatioImageButton = new JButton("Create ratio image");
        createRatioImageButton.addActionListener(this);
        createRatioImageButton.setActionCommand("createRatioImage");
        panel.add(createRatioImageButton, gc);

        getContentPane().add(panel);
    }

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("setFirstImage")) {
                firstImg = WindowManager.getCurrentImage();
      	        if (firstImg == null) {
                    mainWindow.logError("No image is selected. (Ratio)");
                    return;
                }
                if (firstImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+firstImg.getImageStackSize()+"). Please split it into parts. (Ratio)");
                   firstImg = null;
                   return;
                } else if (firstImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+firstImg.getNSlices()+"). Please split it into parts. (Ratio)");
                   firstImg = null;
                   return;
                }
                firstImg.setTitle("Image 1 - " + new Date().toString());
                new ImageConverter(firstImg).convertToGray32();
                setFirstImgButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setSecondImage")) {
                secondImg = WindowManager.getCurrentImage();
      	        if (secondImg == null) {
                    mainWindow.logError("No image is selected. (Ratio)");
                    return;
                }
                if (secondImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+secondImg.getImageStackSize()+"). Please split it into parts. (Ratio)");
                   secondImg = null;
                   return;
                } else if (secondImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+secondImg.getNSlices()+"). Please split it into parts. (Ratio)");
                   secondImg = null;
                   return;
                }
                secondImg.setTitle("Image 2 - " + new Date().toString());
                new ImageConverter(secondImg).convertToGray32();
                setSecondImgButton.setBackground(mainWindow.greenColor);
            } else if (e.getActionCommand().equals("useMainWindowImages")) {
          	    if (useMainWindowImages.isSelected()) {
	                setFirstImgButton.setEnabled(false);
	                setSecondImgButton.setEnabled(false);
       	        } else {
	                setFirstImgButton.setEnabled(true);
	                setSecondImgButton.setEnabled(true);
          	    }
      	    } else if (e.getActionCommand().equals("createRatioImage")) {
      	        ImageProcessor ip1 = null;
      	        ImageProcessor ip2 = null;
      	        if (!useMainWindowImages.isSelected()) {
                    if (firstImg == null) {
                        mainWindow.logError("No image 1 is set. (Ratio)");
                        return;
                    } else if (secondImg == null) {
                        mainWindow.logError("No image 2 is set. (Ratio)");
                        return;
                    }
                    ip1 = firstImg.getProcessor();
                    ip2 = secondImg.getProcessor();
                } else {
                    if (mainWindow.getDonorInDImage() == null) {
                        mainWindow.logError("No donor channel image is set. (Ratio)");
                        return;
                    } else if (mainWindow.getDonorInAImage() == null) {
                        mainWindow.logError("No transfer channel image is set. (Ratio)");
                        return;
                    }
                    ip1 = mainWindow.getDonorInDImage().getProcessor();
                    ip2 = mainWindow.getDonorInAImage().getProcessor();
                }

                float[] ip1P = (float[])ip1.getPixels();
                float[] ip2P = (float[])ip2.getPixels();

                int width = ip1.getWidth();
                int height = ip1.getHeight();
                float[][] ratioImgPoints = new float[width][height];
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        ratioImgPoints[i][j] = ip1P[width*j+i] / ip2P[width*j+i];
                    }
                }
                FloatProcessor fp = new FloatProcessor(ratioImgPoints);
                ImagePlus ratioImg = new ImagePlus("Ratio of images", fp);
                ratioImg.show();
           }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}


class AutoflDialog extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    private ImagePlus donorImg, transferImg, acceptorImg;
    private JPanel panel;
    private JButton setDonorButton, setTransferButton, setAcceptorButton;
    private JButton subtractDonorButton, subtractTransferButton, subtractAcceptorButton;
    private JButton calculateDonorAfButton, calculateTransferAfButton, calculateAcceptorAfButton;

    public AutoflDialog(RiFRET_Plugin mainWindow) {
        setTitle("Autofluorescence calculation");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 415);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>For the calculation of autofluorescence, donor, transfer and acceptor channel images of an unlabeled sample can be set and background subtracted. Then, the averages of given ROIs are calculated and set in the main window.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2,2,2,2);
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
        line.setPreferredSize(new Dimension(getWidth()-35, 1));
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

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("reset")) {
                donorImg = null;
                transferImg = null;
                acceptorImg = null;
	            setDonorButton.setBackground(mainWindow.originalButtonColor);
	            setTransferButton.setBackground(mainWindow.originalButtonColor);
	            setAcceptorButton.setBackground(mainWindow.originalButtonColor);
                subtractDonorButton.setBackground(mainWindow.originalButtonColor);
                subtractTransferButton.setBackground(mainWindow.originalButtonColor);
                subtractAcceptorButton.setBackground(mainWindow.originalButtonColor);
	            calculateDonorAfButton.setBackground(mainWindow.originalButtonColor);
                calculateTransferAfButton.setBackground(mainWindow.originalButtonColor);
                calculateAcceptorAfButton.setBackground(mainWindow.originalButtonColor);
      	    } else if (e.getActionCommand().equals("setAutoFlDonor")) {
                donorImg = WindowManager.getCurrentImage();
      	        if (donorImg == null) {
                    mainWindow.logError("No image is selected. (Autofl.)");
                    return;
                }
                if (donorImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+donorImg.getImageStackSize()+"). Please split it into parts. (Autofl.)");
                   donorImg = null;
                   return;
                } else if (donorImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+donorImg.getNSlices()+"). Please split it into parts. (Autofl.)");
                   donorImg = null;
                   return;
                }
                donorImg.setTitle("Donor channel (Autofl.) - " + new Date().toString());
                new ImageConverter(donorImg).convertToGray32();
                setDonorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setAutoFlTransfer")) {
                transferImg = WindowManager.getCurrentImage();
      	        if (transferImg == null) {
                    mainWindow.logError("No image is selected. (Autofl.)");
                    return;
                }
                if (transferImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+transferImg.getImageStackSize()+"). Please split it into parts. (Autofl.)");
                   transferImg = null;
                   return;
                } else if (transferImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+transferImg.getNSlices()+"). Please split it into parts. (Autofl.)");
                   transferImg = null;
                   return;
                }
                transferImg.setTitle("Transfer channel (Autofl.) - " + new Date().toString());
                new ImageConverter(transferImg).convertToGray32();
                setTransferButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setAutoFlAcceptor")) {
                acceptorImg = WindowManager.getCurrentImage();
      	        if (acceptorImg == null) {
                    mainWindow.logError("No image is selected. (Autofl.)");
                    return;
                }
                if (acceptorImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+acceptorImg.getImageStackSize()+"). Please split it into parts. (Autofl.)");
                   acceptorImg = null;
                   return;
                } else if (acceptorImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+acceptorImg.getNSlices()+"). Please split it into parts. (Autofl.)");
                   acceptorImg = null;
                   return;
                }
                acceptorImg.setTitle("Acceptor channel (Autofl.) - " + new Date().toString());
                new ImageConverter(acceptorImg).convertToGray32();
                setAcceptorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractAutoFlDonor")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorImg.getRoi().contains(i, j)) {
                            sum += ipD.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgD = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipD.getPixelValue(x,y);
                        value = value - backgroundAvgD;
		                ipD.putPixelValue(x, y, value);
        		    }
		        }
		        donorImg.updateAndDraw();
		        donorImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgD+") of donor channel. (Autofl.)");
                subtractDonorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractAutoFlTransfer")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (transferImg.getRoi().contains(i, j)) {
                            sum += ipT.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgT = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipT.getPixelValue(x,y);
                        value = value - backgroundAvgT;
		                ipT.putPixelValue(x, y, value);
        		    }
		        }
		        transferImg.updateAndDraw();
		        transferImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgT+") of transfer channel. (Autofl.)");
                subtractTransferButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractAutoFlAcceptor")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (acceptorImg.getRoi().contains(i, j)) {
                            sum += ipA.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgA = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipA.getPixelValue(x,y);
                        value = value - backgroundAvgA;
		                ipA.putPixelValue(x, y, value);
        		    }
		        }
		        acceptorImg.updateAndDraw();
		        acceptorImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgA+") of acceptor channel. (Autofl.)");
                subtractAcceptorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("calculateDonorAF")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorImg.getRoi().contains(i, j)) {
                            sum += donorImg.getProcessor().getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float autoflAvgD = (float)(sum/count);
                mainWindow.autoflDInDField.setText(df.format(autoflAvgD));
                calculateDonorAfButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("calculateTransferAF")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (transferImg.getRoi().contains(i, j)) {
                            sum += transferImg.getProcessor().getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float autoflAvgT = (float)(sum/count);
                mainWindow.autoflAInDField.setText(df.format(autoflAvgT));
                calculateTransferAfButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("calculateAcceptorAF")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (acceptorImg.getRoi().contains(i, j)) {
                            sum += acceptorImg.getProcessor().getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float autoflAvgA = (float)(sum/count);
                mainWindow.autoflAInAField.setText(df.format(autoflAvgA));
                calculateAcceptorAfButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("close")) {
      	        setVisible(false);
      	        dispose();
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}


class S1S3Dialog extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    private ImagePlus donorImg, transferImg, acceptorImg;
    private JPanel panel;
    private JButton setDonorButton, setTransferButton, setAcceptorButton;
    private JButton setDonorThresholdButton, setTransferThresholdButton, setAcceptorThresholdButton, calculateButton, setButton;
    private JButton subtractDonorButton, subtractTransferButton, subtractAcceptorButton;
    private JButton resetButton;
    private JCheckBox showSImagesCB;
    private JLabel s1ResultLabel, s3ResultLabel;

    public S1S3Dialog(RiFRET_Plugin mainWindow) {
        setTitle("S1/S3 factor calculation");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 450);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>S1 and S3 are calculated based on images of the donor, transfer and acceptor channels of a donor only labeled sample.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2,2,2,2);
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
        subtractDonorButton = new JButton("Subtract background of donor channel");
        subtractDonorButton.addActionListener(this);
        subtractDonorButton.setActionCommand("subtractS1S3Donor");
        panel.add(subtractDonorButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractTransferButton = new JButton("Subtract background of transfer channel");
        subtractTransferButton.addActionListener(this);
        subtractTransferButton.setActionCommand("subtractS1S3Transfer");
        panel.add(subtractTransferButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        subtractAcceptorButton = new JButton("Subtract background of acceptor channel");
        subtractAcceptorButton.addActionListener(this);
        subtractAcceptorButton.setActionCommand("subtractS1S3Acceptor");
        panel.add(subtractAcceptorButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        setDonorThresholdButton = new JButton("Set threshold for donor channel");
        setDonorThresholdButton.addActionListener(this);
        setDonorThresholdButton.setActionCommand("setS1S3DonorThreshold");
        panel.add(setDonorThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 8;
        setTransferThresholdButton = new JButton("Set threshold for transfer channel");
        setTransferThresholdButton.addActionListener(this);
        setTransferThresholdButton.setActionCommand("setS1S3TransferThreshold");
        panel.add(setTransferThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        setAcceptorThresholdButton = new JButton("Set threshold for acceptor channel");
        setAcceptorThresholdButton.addActionListener(this);
        setAcceptorThresholdButton.setActionCommand("setS1S3AcceptorThreshold");
        panel.add(setAcceptorThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 10;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0,4,4,4);
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
        gc.gridy = 12;
        showSImagesCB = new JCheckBox("show S1 and S3 images (for manual calc.)");
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
        setButton = new JButton("Set S1 and S3");
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

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("reset")) {
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
                s1ResultLabel.setText("");
                s3ResultLabel.setText("");
      	    } else if (e.getActionCommand().equals("setS1S3Donor")) {
                donorImg = WindowManager.getCurrentImage();
      	        if (donorImg == null) {
                    mainWindow.logError("No image is selected. (S1/S3 calc.)");
                    return;
                }
                if (donorImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+donorImg.getImageStackSize()+"). Please split it into parts. (S1/S3 calc.)");
                   donorImg = null;
                   return;
                } else if (donorImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+donorImg.getNSlices()+"). Please split it into parts. (S1/S3 calc.)");
                   donorImg = null;
                   return;
                }
                donorImg.setTitle("Donor channel (S1/S3 calc.) - " + new Date().toString());
                new ImageConverter(donorImg).convertToGray32();
                setDonorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS1S3Transfer")) {
                transferImg = WindowManager.getCurrentImage();
      	        if (transferImg == null) {
                    mainWindow.logError("No image is selected. (S1/S3 calc.)");
                    return;
                }
                if (transferImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+transferImg.getImageStackSize()+"). Please split it into parts. (S1/S3 calc.)");
                   transferImg = null;
                   return;
                } else if (transferImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+transferImg.getNSlices()+"). Please split it into parts. (S1/S3 calc.)");
                   transferImg = null;
                   return;
                }
                transferImg.setTitle("Transfer channel (S1/S3 calc.) - " + new Date().toString());
                new ImageConverter(transferImg).convertToGray32();
                setTransferButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS1S3Acceptor")) {
                acceptorImg = WindowManager.getCurrentImage();
      	        if (acceptorImg == null) {
                    mainWindow.logError("No image is selected. (S1/S3 calc.)");
                    return;
                }
                if (acceptorImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+acceptorImg.getImageStackSize()+"). Please split it into parts. (S1/S3 calc.)");
                   acceptorImg = null;
                   return;
                } else if (acceptorImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+acceptorImg.getNSlices()+"). Please split it into parts. (S1/S3 calc.)");
                   acceptorImg = null;
                   return;
                }
                acceptorImg.setTitle("Acceptor channel (S1/S3 calc.) - " + new Date().toString());
                new ImageConverter(acceptorImg).convertToGray32();
                setAcceptorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractS1S3Donor")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorImg.getRoi().contains(i, j)) {
                            sum += ipD.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgD = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipD.getPixelValue(x,y);
                        value = value - backgroundAvgD;
		                ipD.putPixelValue(x, y, value);
        		    }
		        }
		        donorImg.updateAndDraw();
		        donorImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgD+") of donor channel. (S1/S3 calc.)");
                subtractDonorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractS1S3Transfer")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (transferImg.getRoi().contains(i, j)) {
                            sum += ipT.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgT = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipT.getPixelValue(x,y);
                        value = value - backgroundAvgT;
		                ipT.putPixelValue(x, y, value);
        		    }
		        }
		        transferImg.updateAndDraw();
		        transferImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgT+") of transfer channel. (S1/S3 calc.)");
                subtractTransferButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractS1S3Acceptor")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (acceptorImg.getRoi().contains(i, j)) {
                            sum += ipA.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgA = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipA.getPixelValue(x,y);
                        value = value - backgroundAvgA;
		                ipA.putPixelValue(x, y, value);
        		    }
		        }
		        acceptorImg.updateAndDraw();
		        acceptorImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgA+") of acceptor channel. (S1/S3 calc.)");
                subtractAcceptorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS1S3DonorThreshold")) {
      	        if (donorImg == null) {
                    mainWindow.logError("No image is set as donor channel. (S1/S3 calc.)");
                    return;
                }
                IJ.selectWindow(donorImg.getTitle());
                IJ.run("Threshold...");
                setDonorThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS1S3TransferThreshold")) {
      	        if (transferImg == null) {
                    mainWindow.logError("No image is set as transfer channel. (S1/S3 calc.)");
                    return;
                }
                IJ.selectWindow(transferImg.getTitle());
                IJ.run("Threshold...");
                setTransferThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS1S3AcceptorThreshold")) {
      	        if (acceptorImg == null) {
                    mainWindow.logError("No image is set as acceptor channel. (S1/S3 calc.)");
                    return;
                }
                IJ.selectWindow(acceptorImg.getTitle());
                IJ.run("Threshold...");
                setAcceptorThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("calculate")) {
                if (donorImg == null) {
                    mainWindow.logError("No image is set as donor channel. (S1/S3 calc.)");
                    return;
      	        } else if (transferImg == null) {
                    mainWindow.logError("No image is set as transfer channel. (S1/S3 calc.)");
                    return;
      	        } else if (acceptorImg == null) {
                    mainWindow.logError("No image is set as acceptor channel. (S1/S3 calc.)");
                    return;
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
                    if(showSImagesCB.isSelected()) {
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
                            if(showSImagesCB.isSelected()) {
                                imgS1Points[i][j] = currentS1;
                                imgS3Points[i][j] = currentS3;
                            }
                        }
                    }
                    if(showSImagesCB.isSelected()) {
                        ImagePlus s1Img = new ImagePlus("S1 image", new FloatProcessor(imgS1Points));
                        s1Img.show();
                        ImagePlus s3Img = new ImagePlus("S3 image", new FloatProcessor(imgS3Points));
                        s3Img.show();
                    }
                    float avgS1 = (float)(s1c / countc);
                    float avgS3 = (float)(s3c / countc);
                    s1ResultLabel.setText(df.format(avgS1).toString());
                    s3ResultLabel.setText(df.format(avgS3).toString());
                    calculateButton.setBackground(mainWindow.greenColor);
                    donorImg.changes = false;
                    transferImg.changes = false;
                    acceptorImg.changes = false;
                }
      	    } else if (e.getActionCommand().equals("setfactor")) {
                if (s1ResultLabel.getText().equals("") || s3ResultLabel.getText().equals("")) {
                    mainWindow.logError("S1 and S3 have to be calculated before setting them. (S1/S3 calc.)");
                    return;
                }
                mainWindow.setS1Factor(s1ResultLabel.getText());
                mainWindow.setS3Factor(s3ResultLabel.getText());
                setButton.setBackground(mainWindow.greenColor);
                mainWindow.calculateS1S3Button.setBackground(mainWindow.greenColor);
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}


class S2S4Dialog extends JDialog implements ActionListener{
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
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>S2 and S4 are calculated based on images of the donor, transfer and acceptor channels of an acceptor only labeled sample.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2,2,2,2);
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
        gcr.insets = new Insets(0,4,4,4);
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

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("reset")) {
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
      	    } else if (e.getActionCommand().equals("setS2S4Donor")) {
                donorImg = WindowManager.getCurrentImage();
      	        if (donorImg == null) {
                    mainWindow.logError("No image is selected. (S2/S4 calc.)");
                    return;
                }
                if (donorImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+donorImg.getImageStackSize()+"). Please split it into parts. (S2/S4 calc.)");
                   donorImg = null;
                   return;
                } else if (donorImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+donorImg.getNSlices()+"). Please split it into parts. (S2/S4 calc.)");
                   donorImg = null;
                   return;
                }
                donorImg.setTitle("Donor channel (S2/S4 calc.) - " + new Date().toString());
                new ImageConverter(donorImg).convertToGray32();
                setDonorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS2S4Transfer")) {
                transferImg = WindowManager.getCurrentImage();
      	        if (transferImg == null) {
                    mainWindow.logError("No image is selected. (S2/S4 calc.)");
                    return;
                }
                if (transferImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+transferImg.getImageStackSize()+"). Please split it into parts. (S2/S4 calc.)");
                   transferImg = null;
                   return;
                } else if (transferImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+transferImg.getNSlices()+"). Please split it into parts. (S2/S4 calc.)");
                   transferImg = null;
                   return;
                }
                transferImg.setTitle("Transfer channel (S2/S4 calc.) - " + new Date().toString());
                new ImageConverter(transferImg).convertToGray32();
                setTransferButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS2S4Acceptor")) {
                acceptorImg = WindowManager.getCurrentImage();
      	        if (acceptorImg == null) {
                    mainWindow.logError("No image is selected. (S2/S4 calc.)");
                    return;
                }
                if (acceptorImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+acceptorImg.getImageStackSize()+"). Please split it into parts. (S2/S4 calc.)");
                   acceptorImg = null;
                   return;
                } else if (acceptorImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+acceptorImg.getNSlices()+"). Please split it into parts. (S2/S4 calc.)");
                   acceptorImg = null;
                   return;
                }
                acceptorImg.setTitle("Acceptor channel (S2/S4 calc.) - " + new Date().toString());
                new ImageConverter(acceptorImg).convertToGray32();
                setAcceptorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractS2S4Donor")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorImg.getRoi().contains(i, j)) {
                            sum += ipD.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgD = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipD.getPixelValue(x,y);
                        value = value - backgroundAvgD;
		                ipD.putPixelValue(x, y, value);
        		    }
		        }
		        donorImg.updateAndDraw();
		        donorImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgD+") of donor channel. (S2/S4 calc.)");
                subtractDonorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractS2S4Transfer")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (transferImg.getRoi().contains(i, j)) {
                            sum += ipT.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgT = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipT.getPixelValue(x,y);
                        value = value - backgroundAvgT;
		                ipT.putPixelValue(x, y, value);
        		    }
		        }
		        transferImg.updateAndDraw();
		        transferImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgT+") of transfer channel. (S2/S4 calc.)");
                subtractTransferButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractS2S4Acceptor")) {
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
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (acceptorImg.getRoi().contains(i, j)) {
                            sum += ipA.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgA = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipA.getPixelValue(x,y);
                        value = value - backgroundAvgA;
		                ipA.putPixelValue(x, y, value);
        		    }
		        }
		        acceptorImg.updateAndDraw();
		        acceptorImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgA+") of acceptor channel. (S2/S4 calc.)");
                subtractAcceptorButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS2S4DonorThreshold")) {
      	        if (donorImg == null) {
                    mainWindow.logError("No image is set as donor channel. (S2/S4 calc.)");
                    return;
                }
                IJ.selectWindow(donorImg.getTitle());
                IJ.run("Threshold...");
                setDonorThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS2S4TransferThreshold")) {
      	        if (transferImg == null) {
                    mainWindow.logError("No image is set as transfer channel. (S2/S4 calc.)");
                    return;
                }
                IJ.selectWindow(transferImg.getTitle());
                IJ.run("Threshold...");
                setTransferThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setS2S4AcceptorThreshold")) {
      	        if (acceptorImg == null) {
                    mainWindow.logError("No image is set as acceptor channel. (S2/S4 calc.)");
                    return;
                }
                IJ.selectWindow(acceptorImg.getTitle());
                IJ.run("Threshold...");
                setAcceptorThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("calculate")) {
                if (donorImg == null) {
                    mainWindow.logError("No image is set as donor channel. (S2/S4 calc.)");
                    return;
      	        } else if (transferImg == null) {
                    mainWindow.logError("No image is set as transfer channel. (S2/S4 calc.)");
                    return;
      	        } else if (acceptorImg == null) {
                    mainWindow.logError("No image is set as acceptor channel. (S2/S4 calc.)");
                    return;
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
                    if(showSImagesCB.isSelected()) {
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
                            if(showSImagesCB.isSelected()) {
                                imgS2Points[i][j] = currentS2;
                                imgS4Points[i][j] = currentS4;
                            }
                        }
                    }
                    if(showSImagesCB.isSelected()) {
                        ImagePlus s2Img = new ImagePlus("S2 image", new FloatProcessor(imgS2Points));
                        s2Img.show();
                        ImagePlus s4Img = new ImagePlus("S4 image", new FloatProcessor(imgS4Points));
                        s4Img.show();
                    }
                    float avgS2 = (float)(s2c / countc);
                    float avgS4 = (float)(s4c / countc);
                    s2ResultLabel.setText(df.format(avgS2).toString());
                    s4ResultLabel.setText(df.format(avgS4).toString());
                    calculateButton.setBackground(mainWindow.greenColor);
                    donorImg.changes = false;
                    transferImg.changes = false;
                    acceptorImg.changes = false;
                }
      	    } else if (e.getActionCommand().equals("setfactor")) {
                if (s2ResultLabel.getText().equals("") || s4ResultLabel.getText().equals("")) {
                    mainWindow.logError("S2 and S4 have to be calculated before setting them. (S2/S4 calc.)");
                    return;
                }
                mainWindow.setS2Factor(s2ResultLabel.getText());
                mainWindow.setS4Factor(s4ResultLabel.getText());
                setButton.setBackground(mainWindow.greenColor);
                mainWindow.calculateS2S4Button.setBackground(mainWindow.greenColor);
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}


class AlphaDialog extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    private ImagePlus donorBeforeImg, donorAfterImg;
    private JPanel panel;
    private JCheckBox calculateRatioEps;
    private JCheckBox setEblManually;
    private JButton setDonorBeforeButton, setDonorAfterButton;
    private JButton setDonorBThresholdButton, setDonorAThresholdButton;
    private JButton subtractDonorBButton, subtractDonorAButton;
    private JButton epsilonButton, calculateButton, setButton, resetButton;
    private JTextField i1dField, i2aField, ratioEpsilonsField, eBlField;
    private JTextField ldField, laField, bdField, baField;
    private JLabel alphaResultLabel;

    public AlphaDialog(RiFRET_Plugin mainWindow) {
        setTitle("Alpha factor calculation");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 590);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>Alpha is calculated based on average of images of the donor (I1) and transfer (I2) channel images of donor and acceptor only samples, respectively, as well as on the Ld, La, Bd, Ba and \u03B5d / \u03B5a constants.</center></html>");
        panel.add(infoLabel, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        calculateRatioEps = new JCheckBox("calculate the ratio of epsilons", false);
        calculateRatioEps.setActionCommand("calculateRatioEps");
        calculateRatioEps.addActionListener(this);
        calculateRatioEps.setToolTipText("<html>If this checkbox is checked, the ratio of epsilons is<br>calculated. It requires donor images of the double<br>labeled sample before and after photobleaching<br>the acceptor.</html>");
        panel.add(calculateRatioEps, gc);
        gc.insets = new Insets(2,2,2,2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 2;
        setDonorBeforeButton = new JButton("Set donor before photobleaching");
        setDonorBeforeButton.addActionListener(this);
        setDonorBeforeButton.setActionCommand("setDonorBefore");
        setDonorBeforeButton.setEnabled(false);
        panel.add(setDonorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        setDonorAfterButton = new JButton("Set donor after photobleaching");
        setDonorAfterButton.addActionListener(this);
        setDonorAfterButton.setActionCommand("setDonorAfter");
        setDonorAfterButton.setEnabled(false);
        panel.add(setDonorAfterButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractDonorBButton = new JButton("Subtract background of donor before");
        subtractDonorBButton.addActionListener(this);
        subtractDonorBButton.setActionCommand("subtractAlphaDonorBefore");
        subtractDonorBButton.setEnabled(false);
        panel.add(subtractDonorBButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractDonorAButton = new JButton("Subtract background of donor after");
        subtractDonorAButton.addActionListener(this);
        subtractDonorAButton.setActionCommand("subtractAlphaDonorAfter");
        subtractDonorAButton.setEnabled(false);
        panel.add(subtractDonorAButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        setDonorBThresholdButton = new JButton("Set threshold for donor before");
        setDonorBThresholdButton.addActionListener(this);
        setDonorBThresholdButton.setActionCommand("setAlphaDonorBThreshold");
        setDonorBThresholdButton.setEnabled(false);
        panel.add(setDonorBThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        setDonorAThresholdButton = new JButton("Set threshold for donor after");
        setDonorAThresholdButton.addActionListener(this);
        setDonorAThresholdButton.setActionCommand("setAlphaDonorAThreshold");
        setDonorAThresholdButton.setEnabled(false);
        panel.add(setDonorAThresholdButton, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 8;
        JLabel i1dLabel = new JLabel("I1(donor)", JLabel.CENTER);
        i1dLabel.setToolTipText("<HTML>The average of fluorescence intensities calculated from<BR>at least of 5-10 images (donor channel of donor only<BR>labeled sample).</HTML>");
        panel.add(i1dLabel, gc);
        gc.gridx = 1;
        gc.gridy = 8;
        i1dField = new JTextField("", 4);
        panel.add(i1dField, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        JLabel i2aLabel = new JLabel("I2(acceptor)", JLabel.CENTER);
        i2aLabel.setToolTipText("<HTML>The average of fluorescence intensities calculated from<BR>at least of 5-10 images (transfer channel of acceptor<BR>only labeled sample).</HTML>");
        panel.add(i2aLabel, gc);
        gc.gridx = 1;
        gc.gridy = 9;
        i2aField = new JTextField("", 4);
        panel.add(i2aField, gc);
        gc.gridx = 0;
        gc.gridy = 10;
        JLabel ldLabel = new JLabel("Ld", JLabel.CENTER);
        ldLabel.setToolTipText("<HTML>The mean number of dye molecules attached to the<BR>donor antibody.</HTML>");
        panel.add(ldLabel, gc);
        gc.gridx = 1;
        gc.gridy = 10;
        ldField = new JTextField("", 4);
        panel.add(ldField, gc);
        gc.gridx = 0;
        gc.gridy = 11;
        JLabel laLabel = new JLabel("La", JLabel.CENTER);
        laLabel.setToolTipText("<HTML>The mean number of dye molecules attached to the<BR>acceptor antibody.</HTML>");
        panel.add(laLabel, gc);
        gc.gridx = 1;
        gc.gridy = 11;
        laField = new JTextField("", 4);
        panel.add(laField, gc);
        gc.gridx = 0;
        gc.gridy = 12;
        JLabel bdLabel = new JLabel("Bd", JLabel.CENTER);
        bdLabel.setToolTipText("<HTML>The mean number of receptors per cell labeled by the<BR>donor antibody.</HTML>");
        panel.add(bdLabel, gc);
        gc.gridx = 1;
        gc.gridy = 12;
        bdField = new JTextField("", 4);
        panel.add(bdField, gc);
        gc.gridx = 0;
        gc.gridy = 13;
        JLabel baLabel = new JLabel("Ba", JLabel.CENTER);
        baLabel.setToolTipText("<HTML>The mean number of receptors per cell labeled by the<BR>acceptor antibody.</HTML>");
        panel.add(baLabel, gc);
        gc.gridx = 1;
        gc.gridy = 13;
        baField = new JTextField("", 4);
        panel.add(baField, gc);
        gc.gridx = 0;
        gc.gridy = 14;
        JLabel lEBlvalue = new JLabel("Ebl", JLabel.CENTER);
        lEBlvalue.setToolTipText("<HTML>FRET efficiency calculated based on the donor<br>before and after photobleching images.</HTML>");
        panel.add(lEBlvalue, gc);
        gc.gridx = 1;
        gc.gridy = 14;
        eBlField = new JTextField("", 4);
        eBlField.setEditable(false);
        panel.add(eBlField, gc);
        gc.gridx = 2;
        gc.gridy = 14;
        setEblManually = new JCheckBox("manual set", false);
        setEblManually.setActionCommand("setEblManually");
        setEblManually.addActionListener(this);
        setEblManually.setToolTipText("<html>Don't check this checkbox unless you are really sure what you are doing.</html>");
        panel.add(setEblManually, gc);
        gc.gridx = 0;
        gc.gridy = 15;
        JLabel lRatio = new JLabel("\u03B5d / \u03B5a", JLabel.CENTER);
        lRatio.setToolTipText("<HTML>Ratio of molar absorption coefficients of the donor and<BR>acceptor dyes (for the wavelength of donor excitation).</HTML>");
        panel.add(lRatio, gc);
        gc.gridx = 1;
        gc.gridy = 15;
        ratioEpsilonsField = new JTextField("", 4);
        panel.add(ratioEpsilonsField, gc);
        gc.gridx = 2;
        gc.gridy = 15;
        epsilonButton = new JButton("Calculate");
        epsilonButton.addActionListener(this);
        epsilonButton.setMargin(new Insets(0,0,0,0));
        epsilonButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        epsilonButton.setActionCommand("epsilonButton");
        epsilonButton.setEnabled(false);
        panel.add(epsilonButton, gc);

        gc.gridx = 0;
        gc.gridy = 16;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0,4,4,4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel resultLabel = new JLabel("Result (\u03B1):");
        alphaResultLabel = new JLabel("", JLabel.LEFT);
        gcr.gridx = 0;
        gcr.gridy = 0;
        radioPanel.add(resultLabel, gcr);
        gcr.weightx = 10;
        gcr.gridx = 1;
        gcr.gridy = 0;
        radioPanel.add(alphaResultLabel, gcr);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(radioPanel, gc);
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 17;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);
        gc.gridx = 1;
        gc.gridy = 17;
        setButton = new JButton("Set Alpha");
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

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("reset")) {
                donorBeforeImg = null;
                donorAfterImg = null;
	            setDonorBeforeButton.setBackground(mainWindow.originalButtonColor);
	            setDonorAfterButton.setBackground(mainWindow.originalButtonColor);
                subtractDonorBButton.setBackground(mainWindow.originalButtonColor);
                subtractDonorAButton.setBackground(mainWindow.originalButtonColor);
                setDonorBThresholdButton.setBackground(mainWindow.originalButtonColor);
                setDonorAThresholdButton.setBackground(mainWindow.originalButtonColor);
                calculateButton.setBackground(mainWindow.originalButtonColor);
                setButton.setBackground(mainWindow.originalButtonColor);
                alphaResultLabel.setText("");
            } else if (e.getActionCommand().equals("calculateRatioEps")) {
          	    if (calculateRatioEps.isSelected()) {
          	        mainWindow.logWarning("S1, S2, S3, S4 factors and thresholded images (Steps 1-4) are required in the main window to calculate the \u03B5d / \u03B5a ratio.");
	                setDonorBeforeButton.setEnabled(true);
	                setDonorAfterButton.setEnabled(true);
                    subtractDonorBButton.setEnabled(true);
                    subtractDonorAButton.setEnabled(true);
                    setDonorBThresholdButton.setEnabled(true);
                    setDonorAThresholdButton.setEnabled(true);
                    ratioEpsilonsField.setEditable(false);
                    epsilonButton.setEnabled(true);
       	        } else {
	                setDonorBeforeButton.setEnabled(false);
	                setDonorAfterButton.setEnabled(false);
                    subtractDonorBButton.setEnabled(false);
                    subtractDonorAButton.setEnabled(false);
                    setDonorBThresholdButton.setEnabled(false);
                    setDonorAThresholdButton.setEnabled(false);
                    ratioEpsilonsField.setEditable(true);
                    epsilonButton.setEnabled(false);
          	    }
            } else if (e.getActionCommand().equals("setEblManually")) {
          	    if (setEblManually.isSelected()) {
                    eBlField.setEditable(true);
       	        } else {
                    eBlField.setEditable(false);
          	    }
      	    } else if (e.getActionCommand().equals("setDonorBefore")) {
                donorBeforeImg = WindowManager.getCurrentImage();
      	        if (donorBeforeImg == null) {
                    mainWindow.logError("No image is selected. (\u03B1 calc.)");
                    return;
                }
                if (donorBeforeImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+donorBeforeImg.getImageStackSize()+"). Please split it into parts. (\u03B1 calc.)");
                   donorBeforeImg = null;
                   return;
                } else if (donorBeforeImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+donorBeforeImg.getNSlices()+"). Please split it into parts. (\u03B1 calc.)");
                   donorBeforeImg = null;
                   return;
                }
                donorBeforeImg.setTitle("Donor before bleaching (\u03B1 calc.) - " + new Date().toString());
                new ImageConverter(donorBeforeImg).convertToGray32();
                setDonorBeforeButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setDonorAfter")) {
                donorAfterImg = WindowManager.getCurrentImage();
      	        if (donorAfterImg == null) {
                    mainWindow.logError("No image is selected. (\u03B1 calc.)");
                    return;
                }
                if (donorAfterImg.getImageStackSize() > 1) {
                   mainWindow.logError("Current image contains more than 1 channel ("+donorAfterImg.getImageStackSize()+"). Please split it into parts. (\u03B1 calc.)");
                   donorAfterImg = null;
                   return;
                } else if (donorAfterImg.getNSlices() > 1) {
                   mainWindow.logError("Current image contains more than 1 slice ("+donorAfterImg.getNSlices()+"). Please split it into parts. (\u03B1 calc.)");
                   donorAfterImg = null;
                   return;
                }
                donorAfterImg.setTitle("Donor after bleaching (\u03B1 calc.) - " + new Date().toString());
                new ImageConverter(donorAfterImg).convertToGray32();
                setDonorAfterButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractAlphaDonorBefore")) {
      	        if (donorBeforeImg == null) {
                    mainWindow.logError("No image is set as donor before bleaching. (\u03B1 calc.)");
                    return;
                } else if (donorBeforeImg.getRoi() == null) {
                    mainWindow.logError("No ROI is defined for donor before bleaching. (\u03B1 calc.)");
                    return;
                }
                ImageProcessor ipT = donorBeforeImg.getProcessor();
                int width = donorBeforeImg.getWidth();
                int height = donorBeforeImg.getHeight();
                double sum = 0;
                int count = 0;
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorBeforeImg.getRoi().contains(i, j)) {
                            sum += ipT.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgT = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipT.getPixelValue(x,y);
                        value = value - backgroundAvgT;
		                ipT.putPixelValue(x, y, value);
        		    }
		        }
		        donorBeforeImg.updateAndDraw();
		        donorBeforeImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgT+") of donor before bleaching. (\u03B1 calc.)");
                subtractDonorBButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractAlphaDonorAfter")) {
      	        if (donorAfterImg == null) {
                    mainWindow.logError("No image is set as donor after bleaching. (\u03B1 calc.)");
                    return;
                } else if (donorAfterImg.getRoi() == null) {
                    mainWindow.logError("No ROI is defined for donor after bleaching. (\u03B1 calc.)");
                    return;
                }
                ImageProcessor ipA = donorAfterImg.getProcessor();
                int width = donorAfterImg.getWidth();
                int height = donorAfterImg.getHeight();
                double sum = 0;
                int count = 0;
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorAfterImg.getRoi().contains(i, j)) {
                            sum += ipA.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgA = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipA.getPixelValue(x,y);
                        value = value - backgroundAvgA;
		                ipA.putPixelValue(x, y, value);
        		    }
		        }
		        donorAfterImg.updateAndDraw();
		        donorAfterImg.killRoi();
                mainWindow.log("Subtracted background ("+backgroundAvgA+") of donor after bleaching. (\u03B1 calc.)");
                subtractDonorAButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setAlphaDonorBThreshold")) {
      	        if (donorBeforeImg == null) {
                    mainWindow.logError("No image is set as donor before bleaching. (\u03B1 calc.)");
                    return;
                }
                IJ.selectWindow(donorBeforeImg.getTitle());
                IJ.run("Threshold...");
                setDonorBThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setAlphaDonorAThreshold")) {
      	        if (donorAfterImg == null) {
                    mainWindow.logError("No image is set as donor after bleaching. (\u03B1 calc.)");
                    return;
                }
                IJ.selectWindow(donorAfterImg.getTitle());
                IJ.run("Threshold...");
                setDonorAThresholdButton.setBackground(mainWindow.greenColor);
      	    } else if (e.getActionCommand().equals("epsilonButton")) {
                DecimalFormat df = new DecimalFormat("#.###");
                if(!setEblManually.isSelected()) {
                    if (donorBeforeImg == null) {
                        mainWindow.logError("No image is set as donor before bleaching. (\u03B1 calc.)");
                        return;
      	            } else if (donorAfterImg == null) {
                        mainWindow.logError("No image is set as donor in after bleaching. (\u03B1 calc.)");
                        return;
          	        }
          	    } else {
      	            if (eBlField.getText().trim().equals("")) {
                        mainWindow.logError("Ebl is not given. (\u03B1 calc.)");
                        return;
      	            }
          	    }
      	        float s1 = mainWindow.getS1Factor();
      	        if (s1 < 0) {
                    mainWindow.logError("Constant S1 is not given. (\u03B1 calc.)");
                    return;
      	        }
      	        float s2 = mainWindow.getS2Factor();
      	        if (s2 < 0) {
                    mainWindow.logError("Constant S2 is not given. (\u03B1 calc.)");
                    return;
      	        }
      	        float s3 = mainWindow.getS3Factor();
      	        if (s3 < 0) {
                    mainWindow.logError("Constant S3 is not given. (\u03B1 calc.)");
                    return;
      	        }
      	        float s4 = mainWindow.getS4Factor();
      	        if (s4 < 0) {
                    mainWindow.logError("Constant S4 is not given. (\u03B1 calc.)");
                    return;
      	        }
      	        ImagePlus ddImage = mainWindow.getDonorInDImage();
      	        if (ddImage == null) {
                    mainWindow.logError("Donor channel image is not given. (\u03B1 calc.)");
                    return;
      	        }
      	        ImagePlus dtImage = mainWindow.getDonorInAImage();
      	        if (dtImage == null) {
                    mainWindow.logError("Transfer channel image is not given. (\u03B1 calc.)");
                    return;
      	        }
      	        ImagePlus aaImage = mainWindow.getAcceptorInAImage();
      	        if (aaImage == null) {
                    mainWindow.logError("Acceptor channel image is not given. (\u03B1 calc.)");
                    return;
      	        }

                double sumDD = 0;
                int countDD = 0;
                ImageProcessor ipDD = ddImage.getStack().getProcessor(ddImage.getCurrentSlice());
                for (int i=0; i<ddImage.getWidth(); i++) {
                    for (int j=0; j<ddImage.getHeight(); j++) {
                        if (!Float.isNaN(ipDD.getPixelValue(i, j))) {
                            sumDD += ipDD.getPixelValue(i,j);
                            countDD++;
                        }
		            }
        		}
		        float avgDD = (float)(sumDD/countDD);

                double sumDT = 0;
                int countDT = 0;
                ImageProcessor ipDT = dtImage.getStack().getProcessor(dtImage.getCurrentSlice());
                for (int i=0; i<dtImage.getWidth(); i++) {
                    for (int j=0; j<dtImage.getHeight(); j++) {
                        if (!Float.isNaN(ipDT.getPixelValue(i, j))) {
                            sumDT += ipDT.getPixelValue(i,j);
                            countDT++;
                        }
		            }
        		}
		        float avgDT = (float)(sumDT/countDT);

                double sumAA = 0;
                int countAA = 0;
                ImageProcessor ipAA = aaImage.getStack().getProcessor(aaImage.getCurrentSlice());
                for (int i=0; i<aaImage.getWidth(); i++) {
                    for (int j=0; j<aaImage.getHeight(); j++) {
                        if (!Float.isNaN(ipAA.getPixelValue(i, j))) {
                            sumAA += ipAA.getPixelValue(i,j);
                            countAA++;
                        }
		            }
        		}
		        float avgAA = (float)(sumAA/countAA);

                float ebl = 0;
                if(!setEblManually.isSelected()) {
                    double sumDBefore = 0;
                    int countDBefore = 0;
                    ImageProcessor ipDBefore = donorBeforeImg.getProcessor();
                    for (int i=0; i<donorBeforeImg.getWidth(); i++) {
                        for (int j=0; j<donorBeforeImg.getHeight(); j++) {
                            if (!Float.isNaN(ipDBefore.getPixelValue(i, j))) {
                                sumDBefore += ipDBefore.getPixelValue(i,j);
                                countDBefore++;
                            }
    		            }
            		}
		            float avgDBefore = (float)(sumDBefore/countDBefore);

                    double sumDAfter = 0;
                    int countDAfter = 0;
                    ImageProcessor ipDAfter = donorAfterImg.getProcessor();
                    for (int i=0; i<donorAfterImg.getWidth(); i++) {
                        for (int j=0; j<donorAfterImg.getHeight(); j++) {
                            if (!Float.isNaN(ipDAfter.getPixelValue(i, j))) {
                                sumDAfter += ipDAfter.getPixelValue(i,j);
                                countDAfter++;
                            }
    		            }
            		}
		            float avgDAfter = (float)(sumDAfter/countDAfter);

                    ebl = (float)(((double)avgDAfter-(((double)avgDBefore-(double)s4*(double)avgDT)/((double)1-(double)s1*(double)s4)))/(double)avgDAfter);
                    eBlField.setText(df.format(ebl).toString());
                } else {
                    ebl = Float.parseFloat(eBlField.getText().trim());
                    eBlField.setText(df.format(ebl).toString());
                }

                float eRatio = (float)(((double)avgDT - (double)s1*(double)avgDD - ((double)1-(double)s1*(double)s4)*(double)s2*(double)avgAA)/(((double)1-(double)s1*(double)s4)*(double)s2*(double)avgAA*(double)ebl));
                ratioEpsilonsField.setText(df.format(eRatio).toString());
      	    } else if (e.getActionCommand().equals("calculate")) {
                if (i1dField.getText().trim().equals("")) {
                    mainWindow.logError("Constant I1(donor) is not given. (\u03B1 calc.)");
                    return;
      	        } else if (i2aField.getText().trim().equals("")) {
                    mainWindow.logError("Constant I2(acceptor) is not given. (\u03B1 calc.)");
                    return;
      	        } else if (ldField.getText().trim().equals("")) {
                    mainWindow.logError("Constant Ld is not given. (\u03B1 calc.)");
                    return;
      	        } else if (laField.getText().trim().equals("")) {
                    mainWindow.logError("Constant La is not given. (\u03B1 calc.)");
                    return;
      	        } else if (bdField.getText().trim().equals("")) {
                    mainWindow.logError("Constant Bd is not given. (\u03B1 calc.)");
                    return;
      	        } else if (baField.getText().trim().equals("")) {
                    mainWindow.logError("Constant Ba is not given. (\u03B1 calc.)");
                    return;
      	        } else if (ratioEpsilonsField.getText().trim().equals("")) {
                    mainWindow.logError("Ratio \u03B5d / \u03B5a is not given. (\u03B1 calc.)");
                    return;
      	        } else {
                    DecimalFormat df = new DecimalFormat("#.###");
                    float i1 = Float.parseFloat(i1dField.getText().trim());
                    float i2 = Float.parseFloat(i2aField.getText().trim());
                    float ld = Float.parseFloat(ldField.getText().trim());
                    float la = Float.parseFloat(laField.getText().trim());
                    float bd = Float.parseFloat(bdField.getText().trim());
                    float ba = Float.parseFloat(baField.getText().trim());
                    float er = Float.parseFloat(ratioEpsilonsField.getText().trim());
                    float alpha = i2*ld*bd*er/(i1*la*ba);
                    alphaResultLabel.setText(df.format(alpha).toString());
                    calculateButton.setBackground(mainWindow.greenColor);
                }
      	    } else if (e.getActionCommand().equals("setfactor")) {
                if (alphaResultLabel.getText().equals("")) {
                    mainWindow.logError("\u03B1 has to be calculated before setting it. (\u03B1 calc.)");
                    return;
                }
                mainWindow.setAlphaFactor(alphaResultLabel.getText());
                setButton.setBackground(mainWindow.greenColor);
                mainWindow.calculateAlphaButton.setBackground(mainWindow.greenColor);
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }
}


class ShiftDialogRi extends JDialog implements ActionListener{
    private RiFRET_Plugin mainWindow;
    JPanel panel;
    JButton leftButton, rightButton, upButton, downButton;
    JButton cancelButton = new JButton("Close");

    public ShiftDialogRi(RiFRET_Plugin mainWindow) {
        setTitle("32bit image shifter");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        getRootPane().setDefaultButton(cancelButton);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(150, 150);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(0,0,0,0);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 1;
        gc.gridx = 1;
        gc.gridy = 0;
        upButton = new JButton("^");
        upButton.addActionListener(this);
        upButton.setActionCommand("up");
        panel.add(upButton, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        leftButton = new JButton("<");
        leftButton.addActionListener(this);
        leftButton.setActionCommand("left");
        panel.add(leftButton, gc);
        gc.gridx = 2;
        gc.gridy = 1;
        rightButton = new JButton(">");
        rightButton.addActionListener(this);
        rightButton.setActionCommand("right");
        panel.add(rightButton, gc);
        gc.insets = new Insets(2,2,2,2);
        gc.gridx = 1;
        gc.gridy = 2;
        downButton = new JButton("v");
        downButton.addActionListener(this);
        downButton.setActionCommand("down");
        panel.add(downButton, gc);
        gc.insets = new Insets(0,0,4,0);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 3;
        panel.add(cancelButton, gc);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");

        getContentPane().add(panel);
    }

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("cancel")) {
	            setVisible(false);
      	    } else if (e.getActionCommand().equals("up")) {
                ImagePlus image = WindowManager.getCurrentImage();
                shiftUp(image, 1);
      	    } else if (e.getActionCommand().equals("down")) {
                ImagePlus image = WindowManager.getCurrentImage();
                shiftDown(image, 1);
      	    } else if (e.getActionCommand().equals("left")) {
                ImagePlus image = WindowManager.getCurrentImage();
                shiftLeft(image, 1);
      	    } else if (e.getActionCommand().equals("right")) {
                ImagePlus image = WindowManager.getCurrentImage();
                shiftRight(image, 1);
            }
        } catch (Throwable t) {
            mainWindow.logException(t.toString(), t);
        }
    }

    public void shiftUp(ImagePlus image, int value){
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor)image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[])fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height-value; j++) {
                fpPixels2[i][j] = fpPixels[width*(j+value)+i];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }


    public void shiftDown(ImagePlus image, int value){
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor)image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[])fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = value; j < height; j++) {
                fpPixels2[i][j] = fpPixels[width*(j-value)+i];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }


    public void shiftLeft(ImagePlus image, int value){
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor)image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[])fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = 0; i < width-value; i++) {
            for (int j = 0; j < height; j++) {
                fpPixels2[i][j] = fpPixels[width*j+(i+value)];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }


    public void shiftRight(ImagePlus image, int value){
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor)image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[])fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = value; i < width; i++) {
            for (int j = 0; j < height; j++) {
                fpPixels2[i][j] = fpPixels[width*j+(i-value)];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }
}


class RiHelpWindow extends JFrame {
    private RiFRET_Plugin mainWindow;
    private JPanel panel;

    public RiHelpWindow(RiFRET_Plugin mainWindow) {
        setTitle("RiFRET Help");
        this.mainWindow = mainWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(600, 800);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);
        setFont(new Font("Helvetica", Font.PLAIN, 12));

        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 2;
        JLabel label0 = new JLabel("<html><center><b>RiFRET Help</b></center></html>");
        label0.setFont(new Font("Helvetica", Font.BOLD, 14));
        panel.add(label0, gc);

        gc.gridy = GridBagConstraints.RELATIVE;

        JLabel label1 = new JLabel("<html><b><br><u>Menu structure</u></b></html>");
        panel.add(label1, gc);
        JLabel label2 = new JLabel("<html>The \"File\" and \"Image\" menus include ImageJ commands that are likely to be frequently used in<br>the analysis process. Also, messages generated during image processing can be saved or cleared<br>from the \"File\" menu.<br>Furthermore, analysis data can be reset from the \"File\" menu and the plugin can be switched to<br>semi-automatic mode here as well.<br></html>");
        panel.add(label2, gc);

        JLabel label3 = new JLabel("<html><b><br><u>Calculation/setting of factors S1, S2, S3, S4 and \u03B1</u></b></html>");
        panel.add(label3, gc);
        JLabel label4 = new JLabel("<html>After pressing a \"Calculate\" button, a new window pops up, where the calculation of the<br>corresponding factor(s) can be done. These constants can be entered on the main program<br>window without calculation, too.<br></html>");
        panel.add(label4, gc);

        JLabel label5 = new JLabel("<html><b><br><u>Step 1: Opening and setting images</u></b></html>");
        panel.add(label5, gc);
        JLabel label6 = new JLabel("<html>Image files can be opened with the \"Open\" button, or with the \"Open image\" item in the \"File\"<br>menu. After opening, images can be set using the \"Set image\" buttons. If multichannel images are<br>used in stacked image files, the opened image has to be split (item available from the \"Image\"<br>menu) before setting. If the \"Use LSM\" checkbox is checked, the LSM image files (up to AIM v. 4.x,<br>files with *.lsm extension) containing donor, transfer and acceptor channels (in this order) are split<BR>and set automatically after opening with the \"Open & Set LSM\" button. Every previously opened<BR>image window will be closed after pressing this button.<br>If it is necessary, the images can be registered by selecting the \"Register to donor channel\" item<br>in the \"Image\" menu.<br></html>");
        panel.add(label6, gc);

        JLabel label7 = new JLabel("<html><b><br><u>Step 2: Subtraction of background of images</u></b></html>");
        panel.add(label7, gc);
        JLabel label8 = new JLabel("<html>To subtract background (the average of pixels in a selected ROI), the \"Subtract\" button has to<br>be pressed for each relevant image. The \"Copy\" button copies the ROI of the first image to the<br>others. This should be done after marking the ROI and before applying the subtraction. To avoid<br>incidental reusing of the ROI in further operations (such as Gaussian blurring), the ROI is<br>automatically deleted after applying the background correction.<br>If an autofluorescence value is given in the corresponding textfield, it is subtracted along with<br>the background.</html>");
        panel.add(label8, gc);

        JLabel label9 = new JLabel("<html><b><br><u>Step 3: Gaussian blurring of images</u></b></html>");
        panel.add(label9, gc);
        JLabel label10 = new JLabel("<html>Images can be blurred with the given radius by pressing the corresponding \"Blur\" button.<br>Blurring (together with thresholding) can be reverted using the \"Reset\" buttons in Step 4.</html>");
        panel.add(label10, gc);

        JLabel label11 = new JLabel("<html><b><br><u>Step 4: Setting thresholds for the images</u></b></html>");
        panel.add(label11, gc);
        JLabel label12 = new JLabel("<html>Thresholds can be applied to the images by pressing the corresponding \"Threshold\" button.<br>After setting the threshold, the \"Apply\" button has to be pressed on bottom menu of the<br>\"Threshold\" window. After this, select \"Set background pixels to NaN\" and press \"Ok\".<br>Closing \"Threshold\" window will apply the thresholding LUT to the active image and therefore<br>should be avoided.<br>The \"Reset\" buttons reset both blur and threshold settings of the corresponding image.</html>");
        panel.add(label12, gc);

        JLabel label13 = new JLabel("<html><b><br><u>Step 5: Creation of the transfer (FRET efficiency) image</u></b></html>");
        panel.add(label13, gc);
        JLabel label14 = new JLabel("<html>After pressing the \"Create\" button, the transfer (FRET) image will be calculated and displayed.<br>This image can be thresholded automatically by the given min and max values at Step 5. If the<br>\"Results\" window is not open it will be opened too.<BR>For details on the calculations please refer to Roszik et al., (citation to be added later).</html>");
        panel.add(label14, gc);

        JLabel label15 = new JLabel("<html><b><br><u>Step 6: Making measurements</u></b></html>");
        panel.add(label15, gc);
        JLabel label16 = new JLabel("<html>After the creation of the transfer (FRET) image, ROIs can be selected on it, and measurements can<br>be made by pressing the \"Measure\" button. FRET histograms can be most easily viewed and<br>exported by clicking the \"Histogram\" item in the \"Image\" menu. Images used in the current<br>analysis and the transfer image can be closed with the \"Close images\" button.</html>");
        panel.add(label16, gc);

        JLabel label17 = new JLabel("<html><br></html>");
        panel.add(label17, gc);

        JScrollPane logScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(logScrollPane);
    }
}