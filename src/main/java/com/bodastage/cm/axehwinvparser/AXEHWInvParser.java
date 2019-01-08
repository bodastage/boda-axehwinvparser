/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bodastage.cm.axehwinvparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataSource;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Emmanuel
 */
public final class AXEHWInvParser {   
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AXEHWInvParser.class);
    
    /**
     * Current release version
     *
     * @since 0.1.0
     */
    final static String VERSION = "0.1.0";

    /**
     * The AXE printout generation timestamp.
     *
     * @since 0.1.0
     */
    String dateTime = "";
    
    /**
     * File or directory to parse
     * 
     * @since 0.1.0
     */
    String dataSource;
    
    /**
     * Current state of the parser state machine
     * 
     * @since 0.1.0
     */
    int parserState = ParserStates.EXTRACTING_VALUES;
    
    /**
     * Output file printout writer.
     * 
     */
    PrintWriter printWriter = null;
    
    /**
     * File to parse
     */
    String dataFile;
    
    String baseFileName;
    
    /**
     * Start element tag.
     *
     * Use in the character event to determine the data parent XML tag.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String startElementTag = "";

    /**
     * Start element NS prefix.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String startElementTagPrefix = "";

    /**
     * Parser start time.
     *
     * @since 0.1.0
     * @version 0.1.0
     */
    final long startTime = System.currentTimeMillis();
    
    /**
     * Tag data.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String tagData = "";
    
    /**
     * Output directory.
     *
     * @since 0.1.0
     */
    String outputFile = "";
    
    String exportDate   = "";
    String exportTime = "";
    String description = "";
    String nodeAdjustDate="";
    String nodeFunctionType = "";
    String nodeName = "";
    String nodeSite = "";
    String nodeType = "";
    String nodeUserLabel = "";
    
    String equipmentBuildingPractice = "";
    
    String cabinetPosition = "";
    
    String subrackComment = "";
    String subrackName = "";
    String subrackPosition = "";
    String subrackType = "";
    
    String boardName = "";
    String boardSlotPosition = "";
    String boardType = "";
    
    String productDataFirstOperation = "";
    String productDataLastChangedDate = "";
    String productDataManufacturedDate = "";
    String productDataProductName = "";
    String productDataProductNumber = "";
    String productDataProductRevision = "";
    String productDataSerialNumber = "";
    String productDataSupplier = "";
    
    
    String parameterFile = null;

    /**
     * Output directory.
     *
     * @since 0.1.0
     * @version 0.1.0
     */
    String outputDirectory = "/tmp";
    
    public AXEHWInvParser(String outputFile) throws FileNotFoundException {
        System.out.print("Here we are ");
        System.out.print("outputFile:" + outputFile);
                    
        setOutputFile(outputFile);
            
        printWriter = new PrintWriter(new File(outputFile));
        System.out.print("Here we are 2");
    }
    
    /**
     * @param args the command line arguments
     *
     * @since 0.1.0
     * @version 0.1.0
     */
    public static void main(String[] args) {
        //Define
        Options options = new Options();
        CommandLine cmd = null;
        String outputFile = null;
        String inputFile = null;
        Boolean onlyExtractParameters = false;
        Boolean showHelpMessage = false;
        Boolean showVersion = false;
        Boolean attachMetaFields = false; //Attach mattachMetaFields FILENAME,DATETIME,TECHNOLOGY,VENDOR,VERSION,NETYPE

        try {
            options.addOption("v", "version", false, "display version");
            options.addOption(Option.builder("i")
                    .longOpt("input-file")
                    .desc("input file or directory name")
                    .hasArg()
                    .argName("INPUT_FILE").build());
            options.addOption(Option.builder("o")
                    .longOpt("output-file")
                    .desc("output file name")
                    .hasArg()
                    .argName("OUTPUT_FILE").build());
            options.addOption("h", "help", false, "show help");

            //Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                showHelpMessage = true;
            }

            if (cmd.hasOption("v")) {
                showVersion = true;
            }

            if (cmd.hasOption('o')) {
                outputFile = cmd.getOptionValue("o");
            }

            if (cmd.hasOption('i')) {
                inputFile = cmd.getOptionValue("i");
            }

        } catch (IllegalArgumentException e) {

        } catch (ParseException ex) {
//            java.util.logging.Logger.getLogger(HuaweiCMObjectParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            
            if(showVersion == true ){
                System.out.println(VERSION);
                System.out.println("Copyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)");
                System.exit(0);
            }
            
            //show help
            if( showHelpMessage == true || 
                inputFile == null || 
                ( outputFile == null ) ){
                     HelpFormatter formatter = new HelpFormatter();
                     String header = "Parses Ericsson SMO cpp hardware export dumps to csv\n\n";
                     String footer = "\n";
                     footer += "Examples: \n";
                     footer += "java -jar boda-axehwinvparser.jar -i input_file -o out_file.csv\n";
                     footer += "java -jar boda-axehwinvparser -i input_folder -o out_file.csv\n";
                     footer += "\nCopyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)";
                     formatter.printHelp( "java -jar boda-axehwinvparser.jar", header, options, footer );
                     System.exit(0);
            }
            
            //Confirm that the output directory is a directory and has write 
            //privileges
            if(outputFile == null ){
                System.err.println("ERROR: Output file is required.");
            }
            
            //Get parser instance
            AXEHWInvParser cmParser = new AXEHWInvParser(outputFile);
            
            cmParser.setDataSource(inputFile);

            cmParser.parse();

        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }    
    }

    void setDataSource(String fileName){
        dataSource = fileName;  
    }
    
    /**
     * Set the output file
     *
     * @since 0.1.0
     * @version 0.1.0
     * @param String output file name
     */
    void setOutputFile(String outputFile){
        this.outputFile = outputFile;
    }
    
    /**
     * Set name of file to parser.
     *
     * @param filename
     */
    private void setFileName(String filename) {
        this.dataFile = filename;
    }
    
    /**
     * Determines if the source data file is a regular file or a directory and 
     * parses it accordingly
     * 
     * @since 1.1.0
     * @version 1.0.0
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void processFileOrDirectory()
            throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
        //this.dataFILe;
                
//        LOGGER.info("dataSource:" + dataSource);
        
        Path file = Paths.get(this.dataSource);
        boolean isRegularExecutableFile = Files.isRegularFile(file)
                & Files.isReadable(file);

        boolean isReadableDirectory = Files.isDirectory(file)
                & Files.isReadable(file);

        if (isRegularExecutableFile) {
            this.setFileName(this.dataSource);
            baseFileName =  getFileBasename(this.dataFile);
            System.out.print("Parsing " + this.baseFileName + "...");
            
            //Parse file
            this.parseFile(this.dataSource);
            
            System.out.println("Done.");
        }

        if (isReadableDirectory) {

            File directory = new File(this.dataSource);

            //get all the files from a directory
            File[] fList = directory.listFiles();

            for (File f : fList) {
                this.setFileName(f.getAbsolutePath());
                try {
                    baseFileName =  getFileBasename(this.dataFile);
                    System.out.print("Parsing " + this.baseFileName + "...");
                    
                    //Parse dump file 
                    this.parseFile(f.getAbsolutePath());
                    
                    System.out.println("Done.");
                   
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Skipping file: " + this.baseFileName + "\n");
                }
            }
        }

    }
    
    /**
     * Parser entry point 
     * 
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    public void parse() throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
//        LOGGER.info("parse(");
        String headerString = "FILENAME,DATETIME,NODE_ADJUSTDATE,NODE_FUNCTIONTYPE,NODE_NAME,NODE_SITE,NODE_TYPE,NODE_USERLABEL,"
                + "EQUIPMENT_BUILDINGPRACTICE,CABINET_POSITION,SUBRACK_COMMENT,SUBRACK_NAME,SUBRACK_POSISITON.SUBRACK_TYPE,"
                + "BOARD_NAME,BOARD_SLOTPOSITION,BOARD_TYPE,"
                + "PRODUCTDATA_FIRSTOPERATIONDATE,PRODUCTDATA_LASTCHANGEDATE,PRODUCTDATA_MANUFACTUREDDATE, PRODUCTDATA_PRODUCTNAME,"
                + "PRODUCTDATA_PRODUCTNUMBER,PRODUCTDATA_PRODUCTREVISION,PRODUCTDATA_SERIALNUMBER,PRODUCTDATA_SERIALNUMBER,PRODUCTDATA_SUPPLIER";

        printWriter.println(headerString);        
                    
        processFileOrDirectory();
       
        printWriter.close();
        printExecutionTime();
    }
    
    /**
     * Print program's execution time.
     *
     * @since 1.0.0
     */
    public void printExecutionTime() {
        float runningTime = System.currentTimeMillis() - startTime;

        String s = "Parsing completed.\n";
        s = s + "Total time:";

        //Get hours
        if (runningTime > 1000 * 60 * 60) {
            int hrs = (int) Math.floor(runningTime / (1000 * 60 * 60));
            s = s + hrs + " hours ";
            runningTime = runningTime - (hrs * 1000 * 60 * 60);
        }

        //Get minutes
        if (runningTime > 1000 * 60) {
            int mins = (int) Math.floor(runningTime / (1000 * 60));
            s = s + mins + " minutes ";
            runningTime = runningTime - (mins * 1000 * 60);
        }

        //Get seconds
        if (runningTime > 1000) {
            int secs = (int) Math.floor(runningTime / (1000));
            s = s + secs + " seconds ";
            runningTime = runningTime - (secs / 1000);
        }

        //Get milliseconds
        if (runningTime > 0) {
            int msecs = (int) Math.floor(runningTime / (1000));
            s = s + msecs + " milliseconds ";
            runningTime = runningTime - (msecs / 1000);
        }

        System.out.println(s);
    }
    
    /**
     * The parser's entry point.
     * 
     * @param filename 
     */
    public void parseFile(String filename) 
    throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException
    {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            XMLEventReader eventReader = factory.createXMLEventReader(
                    new FileReader(filename));
            baseFileName = getFileBasename(filename);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        startElementEvent(event);
                        break;
                    case XMLStreamConstants.SPACE:
                    case XMLStreamConstants.CHARACTERS:
                        characterEvent(event);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endELementEvent(event);
                        break;
                }
            }

            
    }
    
    
    /**
     * Handle start element event.
     *
     * @param xmlEvent
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     */
    public void startElementEvent(XMLEvent xmlEvent) {
        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        String prefix = startElement.getName().getPrefix();

        startElementTag = qName;
        startElementTagPrefix = prefix;
        
        

        Iterator<Attribute> attributes = startElement.getAttributes();
    
        //<ExportDateTime
        if(qName.equals("ExportDateTime") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("Date")) {
                    exportDate = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Time")) {
                    exportTime = attribute.getValue();
                }
            }
        }
        
        //<Node
        if(qName.equals("Node") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("AdjustDate")) {
                    nodeAdjustDate = attribute.getValue();
                }
                if (attribute.getName().toString().equals("FunctionType")) {
                    nodeFunctionType = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Name")) {
                    nodeName = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Site")) {
                    nodeSite = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Type")) {
                    nodeType = attribute.getValue();
                }
                if (attribute.getName().toString().equals("UserLabel")) {
                    nodeUserLabel = attribute.getValue();
                }
            }
        }
        
        if(qName.equals("Equipment") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("BuildingPractice")) {
                    equipmentBuildingPractice = attribute.getValue();
                }

            }
        }
        
        if(qName.equals("Cabinet") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("Position")) {
                    cabinetPosition = attribute.getValue();
                }
            }
        }
        
        if(qName.equals("Subrack") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("Name")) {
                    subrackName = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Position")) {
                    subrackPosition = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Type")) {
                    subrackType = attribute.getValue();
                }
            }
        }
        
        if(qName.equals("Board") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("Name")) {
                    boardName = attribute.getValue();
                }
                if (attribute.getName().toString().equals("SlotPosition")) {
                    boardSlotPosition = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Type")) {
                    boardType = attribute.getValue();
                }
            }
        }
        
        if(qName.equals("ProductData") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("FirstOperationDate")) {
                    productDataFirstOperation = attribute.getValue();
                }
                if (attribute.getName().toString().equals("LastChangedDate")) {
                    productDataLastChangedDate = attribute.getValue();
                }
                if (attribute.getName().toString().equals("ManufacturedDate")) {
                    productDataManufacturedDate = attribute.getValue();
                }
                if (attribute.getName().toString().equals("ProductName")) {
                    productDataProductName = attribute.getValue();
                }
                if (attribute.getName().toString().equals("ProductNumber")) {
                    productDataProductNumber = attribute.getValue();
                }
                if (attribute.getName().toString().equals("ProductRevision")) {
                    productDataProductRevision = attribute.getValue();
                }
                if (attribute.getName().toString().equals("SerialNumber")) {
                    productDataSerialNumber = attribute.getValue();
                }
                if (attribute.getName().toString().equals("Supplier")) {
                    productDataSupplier = attribute.getValue();
                }
            }
        }
    }
    
    public void endELementEvent(XMLEvent xmlEvent)
            throws FileNotFoundException, UnsupportedEncodingException {
        EndElement endElement = xmlEvent.asEndElement();
        String prefix = endElement.getName().getPrefix();
        String qName = endElement.getName().getLocalPart();

        startElementTag = "";
        
        if (qName.equals("Board")) {
            String baseDataFileName = getFileBasename(dataFile);
            String boardString = baseDataFileName + "," + exportDate + " " + exportTime + ","
                    + nodeAdjustDate + "," + nodeFunctionType + "," + nodeName + ","
                    + nodeSite + "," + nodeType + "," + nodeUserLabel + ","
                    + equipmentBuildingPractice + "," + cabinetPosition + ","
                    + subrackComment + "," + subrackName + "," + subrackPosition + ","
                    + subrackType + "," + boardName + "," + boardSlotPosition + "," 
                    + boardType + "," + productDataFirstOperation + "," + productDataLastChangedDate + ","
                    + productDataManufacturedDate + "," + productDataProductName + ","+ productDataProductNumber + ","
                    + productDataProductRevision + "," + productDataSerialNumber + "," + productDataSupplier;
            printWriter.println(boardString);
            
            
            //Reset board
            productDataFirstOperation = "";
            productDataLastChangedDate = "";
            productDataManufacturedDate = "";
            productDataProductName = "";
            productDataProductNumber = "";
            productDataProductRevision = "";
            productDataSerialNumber = "";
            productDataSupplier = "";
        }
    
    }
    public void characterEvent(XMLEvent xmlEvent) {
    
    }
    
    /**
     * Get file base name.
     *
     * @since 1.0.0
     */
    public String getFileBasename(String filename) {
        try {
            return new File(filename).getName();
        } catch (Exception e) {
            return filename;
        }
    }
}
