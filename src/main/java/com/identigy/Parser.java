package com.identigy;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static void main(String[] args) {
        Parser parser = new Parser();
        File fileIn = parser.choose();
        try {
            parser.parse(fileIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    File choose() {
        //Устанавливаем Look & Feel как в системе
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        String direct = new File(".").getAbsolutePath();
        JFileChooser jFileChooser = new JFileChooser(direct);
        int res = jFileChooser.showOpenDialog(null);

        if (res == JFileChooser.APPROVE_OPTION) {
            if (isPropertiesFile(jFileChooser)){
                File chosen;
                chosen = jFileChooser.getSelectedFile();
                return chosen;
            }
        } else {
            return null;
        }
        return null;
    }

    boolean isPropertiesFile(JFileChooser jFileChooser) {
        String name = jFileChooser.getSelectedFile().getName();
        return name.endsWith(".properties");
    }

    void parse(File inFile) throws IOException {

        if (inFile != null) {

            List<String> inStrings = Files.readAllLines(inFile.toPath());
            List<String> outStrings = new ArrayList<>();
            boolean startOfTheLine = true;
            StringBuilder keyResult = new StringBuilder();

            for (String currentLine : inStrings) {
                if (!currentLine.trim().startsWith("#") & !currentLine.isEmpty()) {
                    String backslashString = "\\";
                    if (startOfTheLine) {
                        if (!currentLine.trim().startsWith("<") && !currentLine.trim().endsWith(">")) {
                            //Очищаем билдер
                            if (!keyResult.isEmpty()) keyResult.setLength(0);
                            keyResult.append("export ");
                            String equalsChar = "=";
                            //Если строка заканчивается разделителем
                            if (currentLine.trim().endsWith("\\")) {
                                keyResult.append(currentLine, 0, currentLine.indexOf(equalsChar) + 1);
                                keyResult.append('"');
                                startOfTheLine = false;
                                //Если строка с key/value идет одной строкой
                            } else {
                                keyResult.append(currentLine, 0, currentLine.indexOf(equalsChar) + 1);
                                keyResult.append('"');
                                keyResult.append(currentLine.substring(currentLine.indexOf(equalsChar) + 1));
                                keyResult.append('"');
                                var result = keyResult.toString();
                                outStrings.add(result);
                            }
                        }
                        //Если это продолжение строки с разделителем и еще не закончилась
                    } else if (currentLine.trim().endsWith(backslashString)) {
                        keyResult.append(currentLine.substring(0, currentLine.indexOf(backslashString)).trim());
                        keyResult.append(" ");
                        //Участок, где строки с разделителем закончился
                    } else {
                        keyResult.append(currentLine.substring(0, currentLine.lastIndexOf(">") + 1).trim());
                        keyResult.append('"');
                        startOfTheLine = true;
                        var result = keyResult.toString();
                        outStrings.add(result);
                    }

                }

            }

            String outPath = inFile.getParent() + "\\eggsport.env";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
                for (String str : outStrings) {
                    writer.write(str);
                    writer.newLine();
                }
                System.out.println("Great success.");
            }
        }
    }
}