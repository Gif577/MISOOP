package ru.eltech.studproject.client;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ImportFilmsDialog extends JDialog {
    private JTextArea inputArea;
    private JButton importButton;
    private JButton cancelButton;
    private List<String> importedFilms;
    private JLabel dropLabel;

    public ImportFilmsDialog(Frame parent) {
        super(parent, "Импорт фильмов", true);
        importedFilms = new ArrayList<>();
        initUI();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(40, 44, 52));
        setSize(600, 500);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 44, 52));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Импорт участников", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("<html><div style='text-align: center; color: #cccccc;'>"
                + "Поддерживаемые форматы: CSV, TXT<br>"
                + "Каждая строка - название фильма</div></html>", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(new Color(50, 54, 63));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel dropPanel = new JPanel(new BorderLayout());
        dropPanel.setBackground(new Color(60, 64, 72));
        dropPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 2, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        dropLabel = new JLabel("<html><div style='text-align: center;'>"
                + "<h3>📂 Перетащите сюда файл или нажмите</h3>"
                + "<p style='color: #aaaaaa; font-size: 12px;'>"
                + "Поддерживаются: .txt, .csv<br>"
                + "Или введите фильмы в поле ниже (каждый с новой строки)</p>"
                + "</div></html>", SwingConstants.CENTER);
        dropLabel.setForeground(Color.WHITE);

        dropPanel.add(dropLabel, BorderLayout.CENTER);

        JLabel inputLabel = new JLabel("Или введите фильмы (каждый с новой строки):");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputLabel.setForeground(new Color(200, 200, 200));

        inputArea = new JTextArea(8, 40);
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(new Color(30, 33, 40));
        inputArea.setForeground(Color.WHITE);
        inputArea.setCaretColor(Color.WHITE);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(inputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        JPanel examplePanel = new JPanel(new BorderLayout());
        examplePanel.setBackground(new Color(55, 59, 67));
        examplePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel exampleLabel = new JLabel("<html><b>Пример формата:</b><br>"
                + "<span style='color: #4fc3f7;'>Убить Билла</span><br>"
                + "<span style='color: #4fc3f7;'>Звездные войны</span><br>"
                + "<span style='color: #4fc3f7;'>Криминальное чтиво</span><br>"
                + "<span style='color: #4fc3f7;'>Начало</span></html>");
        exampleLabel.setForeground(new Color(180, 180, 180));
        exampleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        examplePanel.add(exampleLabel, BorderLayout.CENTER);

        centerPanel.add(dropPanel, BorderLayout.NORTH);
        centerPanel.add(inputLabel, BorderLayout.CENTER);
        centerPanel.add(scrollPane, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(new Color(40, 44, 52));

        importButton = new JButton("Импортировать");
        CustomButtonStyle.styleSuccessButton(importButton);
        importButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importFilms();
            }
        });

        cancelButton = new JButton("Отмена");
        CustomButtonStyle.styleOutlineButton(cancelButton);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importedFilms.clear();
                dispose();
            }
        });

        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(examplePanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        new DropTarget(dropLabel, new java.awt.dnd.DropTargetListener() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>)
                            dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    if (!droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0);
                        if (file.getName().toLowerCase().endsWith(".txt") ||
                                file.getName().toLowerCase().endsWith(".csv")) {

                            readFile(file);
                            dropLabel.setText("<html><div style='text-align: center; color: #4CAF50;'>"
                                    + "✅ Файл загружен: " + file.getName() + "<br>"
                                    + "<small>" + importedFilms.size() + " фильмов найдено</small>"
                                    + "</div></html>");
                        } else {
                            dropLabel.setText("<html><div style='text-align: center; color: #F44336;'>"
                                    + "❌ Неподдерживаемый формат файла<br>"
                                    + "<small>Используйте .txt или .csv</small>"
                                    + "</div></html>");
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.dropComplete(false);
                }
            }

            @Override
            public void dragEnter(java.awt.dnd.DropTargetDragEvent dtde) {
                dropLabel.setText("<html><div style='text-align: center; color: #2196F3;'>"
                        + "⬆️ Отпустите файл здесь</div></html>");
            }

            @Override
            public void dragExit(java.awt.dnd.DropTargetEvent dte) {
                dropLabel.setText("<html><div style='text-align: center;'>"
                        + "📂 Перетащите сюда файл или нажмите</div></html>");
            }

            @Override
            public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {}
        });
    }

    private void readFile(File file) {
        importedFilms.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.contains("|")) {
                        line = line.split("\\|")[0].trim();
                    }
                    importedFilms.add(line);
                }
            }

            StringBuilder sb = new StringBuilder();
            for (String film : importedFilms) {
                sb.append(film).append("\n");
            }
            inputArea.setText(sb.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка чтения файла: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importFilms() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Введите фильмы для импорта",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        importedFilms.clear();
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.contains(",")) {
                        String[] parts = line.split(",");
                        for (String part : parts) {
                            part = part.trim();
                            if (!part.isEmpty()) {
                                importedFilms.add(part);
                            }
                        }
                    } else if (line.contains("|")) {
                        String[] parts = line.split("\\|");
                        String filmName = parts[0].trim();
                        if (!filmName.isEmpty()) {
                            importedFilms.add(filmName);
                        }
                    } else {
                        importedFilms.add(line);
                    }
                }
            }

            if (importedFilms.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Не найдено фильмов для импорта",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка обработки данных: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<String> getImportedFilms() {
        return importedFilms;
    }
}