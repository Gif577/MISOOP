package ru.eltech.studproject.client;

import com.caucho.hessian.client.HessianProxyFactory;
import ru.eltech.studproject.api.DataService;
import ru.eltech.studproject.api.Films;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ClientApplication extends JFrame {
    private DataService dataService;
    private FilmWheel filmWheel;
    private DefaultTableModel tableModel;
    private JTable filmsTable;
    private JTextField filmInputField;
    private JLabel statsLabel;
    private JLabel participantsLabel;

    private JSpinner durationSpinner;
    private JComboBox<String> animationComboBox;

    private JLabel resultLabel;

    private static final Color DARK_BG = new Color(40, 44, 52);
    private static final Color DARK_PANEL = new Color(50, 54, 63);
    private static final Color DARK_BORDER = new Color(80, 84, 92);
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color TEXT_MUTED = new Color(150, 150, 150);

    private Timer countdownTimer;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ClientApplication() {
        initializeHessianConnection();
        initializeUI();
        loadFilms();
    }

    private void initializeHessianConnection() {
        try {
            String url = "http://localhost:8081/DataService";
            HessianProxyFactory factory = new HessianProxyFactory();
            factory.setOverloadEnabled(true);
            factory.setConnectTimeout(5000);
            dataService = (DataService) factory.create(DataService.class, url);

            dataService.ping();
            System.out.println("Подключено к серверу колеса фортуны");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка подключения к серверу: " + e.getMessage() +
                            "\nЗапустите Tomcat и разверните приложение KinoAuk",
                    "Ошибка подключения",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("Что сегодня смотрим");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        getContentPane().setBackground(DARK_BG);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("ФИЛЬМЫ НА ОЦЕНКУ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);

        // Создаем панель для заголовка с фиксированной высотой
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.CENTER);

        // Устанавливаем минимальную, предпочтительную и максимальную высоту для заголовка
        titleContainer.setMinimumSize(new Dimension(0, 40));
        titleContainer.setPreferredSize(new Dimension(0, 40));
        titleContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // ResultLabel теперь всегда видим, но с пустым текстом
        resultLabel = new JLabel(" ", SwingConstants.CENTER); // Пробел вместо пустой строки
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resultLabel.setForeground(new Color(255, 215, 0));
        resultLabel.setVisible(true); // Всегда видим

        // Устанавливаем фиксированную высоту для resultLabel
        resultLabel.setMinimumSize(new Dimension(0, 35));
        resultLabel.setPreferredSize(new Dimension(0, 35));
        resultLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // BoxLayout для вертикального расположения
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setOpaque(false);

        // заголовок
        verticalPanel.add(titleContainer);

        // жесткое пространство (отступ) между заголовком и результатом
        verticalPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // resultLabel (всегда видимый)
        verticalPanel.add(resultLabel);

        // еще немного пространства снизу
        verticalPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        headerPanel.add(verticalPanel, BorderLayout.CENTER);

        // Используем GridBagLayout для более точного контроля пропорций
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 15); // Отступ между панелями

        // Левая панель - управление фильмами (30% ширины)
        gbc.weightx = 0.30;
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(createLeftPanel(), gbc);

        // Центральная панель - колесо (50% ширины)
        gbc.weightx = 0.50;
        gbc.gridx = 1;
        centerPanel.add(createCenterPanel(), gbc);

        // Правая панель - настройки (20% ширины, уменьшена в 2 раза)
        gbc.weightx = 0.20;
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0); // Убираем правый отступ
        centerPanel.add(createRightPanel(), gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        setSize(1800, 1000); // Увеличили размер окна
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(DARK_PANEL);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 2),
                "Управление фильмами",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                Color.WHITE
        ));

        // Верхняя панель с полем ввода и кнопками
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel addLabel = new JLabel("Название фильма:");
        addLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addLabel.setForeground(TEXT_LIGHT);

        filmInputField = new JTextField();
        filmInputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filmInputField.setForeground(Color.WHITE);
        filmInputField.setCaretColor(Color.WHITE);
        filmInputField.setBackground(new Color(60, 64, 72));
        filmInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Панель для кнопок с GridBagLayout для выравнивания
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        btnGbc.weightx = 1.0;
        btnGbc.insets = new Insets(0, 2, 0, 2);
        btnGbc.gridy = 0;

        // Добавить кнопку
        btnGbc.gridx = 0;
        JButton addButton = createButton("Добавить", this::addFilm);
        buttonPanel.add(addButton, btnGbc);

        // Импорт кнопка
        btnGbc.gridx = 1;
        JButton importButton = createButton("Импорт", this::showImportDialog);
        buttonPanel.add(importButton, btnGbc);

        // Убрать кнопка
        btnGbc.gridx = 2;
        JButton removeButton = createButton("Убрать", this::deleteSelectedFilm);
        buttonPanel.add(removeButton, btnGbc);

        // Добавляем "растягивающий" компонент чтобы кнопки не растягивались слишком сильно
        btnGbc.gridx = 3;
        btnGbc.weightx = 0.0;
        buttonPanel.add(Box.createHorizontalStrut(10), btnGbc);

        filmInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    addFilm();
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setOpaque(false);
        inputPanel.add(addLabel, BorderLayout.NORTH);
        inputPanel.add(filmInputField, BorderLayout.CENTER);

        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        leftPanel.add(topPanel, BorderLayout.NORTH);

        // Таблица фильмов
        initializeTable();

        JScrollPane scrollPane = new JScrollPane(filmsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(DARK_BORDER, 1));
        scrollPane.getViewport().setBackground(new Color(55, 59, 67));

        // Панель для кнопки "Очистить все" по центру снизу
        JPanel clearButtonPanel = new JPanel(new GridBagLayout());
        clearButtonPanel.setOpaque(false);
        clearButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints clearGbc = new GridBagConstraints();
        clearGbc.gridx = 0;
        clearGbc.gridy = 0;
        clearGbc.weightx = 1.0;
        clearGbc.anchor = GridBagConstraints.CENTER;

        JButton clearButton = createButton("Очистить все", this::clearAllFilms);
        clearButton.setPreferredSize(new Dimension(150, 35)); // Фиксированный размер
        clearButtonPanel.add(clearButton, clearGbc);

        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(clearButtonPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        CustomButtonStyle.styleOutlineLightButton(button);
        button.addActionListener(e -> action.run());
        button.setPreferredSize(new Dimension(100, 35)); // Фиксированный размер для всех кнопок
        return button;
    }

    private void initializeTable() {
        String[] columns = {"#", "Название фильма", "Шанс"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        filmsTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(60, 64, 72));
                    } else {
                        c.setBackground(new Color(55, 59, 67));
                    }
                } else {
                    c.setBackground(new Color(70, 130, 180));
                }
                c.setForeground(Color.WHITE);
                return c;
            }
        };

        filmsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filmsTable.setRowHeight(35);
        filmsTable.setShowGrid(false);
        filmsTable.setIntercellSpacing(new Dimension(0, 0));
        filmsTable.setSelectionBackground(new Color(70, 130, 180));
        filmsTable.setSelectionForeground(Color.WHITE);

        JTableHeader header = filmsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(70, 70, 80));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        // Центрирование всех ячеек в таблице
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Для всех столбцов кроме названия фильма центрируем
                if (column != 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        };
        centerRenderer.setForeground(Color.WHITE);

        // Применяем рендерер ко всем столбцам
        for (int i = 0; i < filmsTable.getColumnCount(); i++) {
            filmsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            if (i == 0) { // Столбец №
                filmsTable.getColumnModel().getColumn(i).setPreferredWidth(50);
            } else if (i == 1) { // Название фильма
                filmsTable.getColumnModel().getColumn(i).setPreferredWidth(250);
            } else if (i == 2) { // Шанс
                filmsTable.getColumnModel().getColumn(i).setPreferredWidth(80);
            }
        }
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(DARK_PANEL);
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 2),
                "Колесо Фортуны",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 18),
                Color.WHITE
        ));

        filmWheel = new FilmWheel();
        JPanel wheelContainer = new JPanel(new GridBagLayout());
        wheelContainer.setOpaque(false);

        // Используем GridBagConstraints для растягивания колеса
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        wheelContainer.add(filmWheel, gbc);

        JButton spinButton = new JButton("Крутить колесо!");
        spinButton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        CustomButtonStyle.styleOutlineLightButton(spinButton);
        spinButton.addActionListener(e -> spinWheel());
        spinButton.setPreferredSize(new Dimension(200, 50));

        JPanel buttonContainer = new JPanel(new GridBagLayout());
        buttonContainer.setOpaque(false);
        buttonContainer.setBorder(new EmptyBorder(0, 0, 20, 0));

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.weighty = 1.0;
        buttonContainer.add(Box.createVerticalGlue(), buttonGbc);

        buttonGbc.gridy = 1;
        buttonGbc.weighty = 0;
        buttonGbc.insets = new Insets(15, 0, 15, 0);
        buttonContainer.add(spinButton, buttonGbc);

        buttonGbc.gridy = 2;
        buttonGbc.weighty = 1.0;
        buttonContainer.add(Box.createVerticalGlue(), buttonGbc);

        centerPanel.add(wheelContainer, BorderLayout.CENTER);
        centerPanel.add(buttonContainer, BorderLayout.SOUTH);

        return centerPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(DARK_PANEL);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 2),
                "Настройки",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                Color.WHITE
        ));

        // Основная панель настроек
        JPanel mainSettingsPanel = new JPanel(new BorderLayout());
        mainSettingsPanel.setOpaque(false);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setOpaque(false);
        settingsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Длительность
        JLabel durationLabel = new JLabel("Длительность (сек):");
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        durationLabel.setForeground(TEXT_LIGHT);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(durationLabel, gbc);

        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 30, 120, 1));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) durationSpinner.getEditor();
        editor.getTextField().setColumns(8);
        editor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editor.getTextField().setForeground(Color.WHITE);
        editor.getTextField().setBackground(new Color(60, 64, 72));
        editor.getTextField().setCaretColor(Color.WHITE);
        editor.getTextField().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        durationSpinner.setPreferredSize(new Dimension(120, 35));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(durationSpinner, gbc);

        // Стиль анимации
        JLabel styleLabel = new JLabel("Стиль анимации:");
        styleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        styleLabel.setForeground(TEXT_LIGHT);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(styleLabel, gbc);

        animationComboBox = new JComboBox<>(new String[]{"Стандартный", "Быстрый", "Медленный", "Случайный"});
        animationComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        animationComboBox.setBackground(new Color(60, 64, 72));
        animationComboBox.setForeground(Color.WHITE);
        animationComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        animationComboBox.setPreferredSize(new Dimension(120, 35));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(animationComboBox, gbc);

        // Разделитель
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 15, 0);
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(DARK_BORDER);
        settingsPanel.add(separator, gbc);

        // Метод генерации
        JLabel methodLabel = new JLabel("Метод генерации:");
        methodLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        methodLabel.setForeground(TEXT_LIGHT);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(methodLabel, gbc);

        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"Стандартный"});
        methodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        methodCombo.setBackground(new Color(60, 64, 72));
        methodCombo.setForeground(Color.WHITE);
        methodCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        methodCombo.setPreferredSize(new Dimension(120, 35));
        methodCombo.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        settingsPanel.add(methodCombo, gbc);

        // Кнопка "Узнать больше"
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 5, 5, 5);
        JButton learnMoreButton = new JButton("Узнать больше");
        CustomButtonStyle.styleGhostButton(learnMoreButton);
        learnMoreButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        learnMoreButton.setPreferredSize(new Dimension(120, 30));
        settingsPanel.add(learnMoreButton, gbc);

        // Добавляем растягивающий компонент для выравнивания вверху
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        settingsPanel.add(Box.createVerticalGlue(), gbc);

        // Панель статистики
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        statsPanel.setBackground(new Color(45, 49, 58));

        GridBagConstraints statsGbc = new GridBagConstraints();
        statsGbc.gridwidth = GridBagConstraints.REMAINDER;
        statsGbc.fill = GridBagConstraints.HORIZONTAL;
        statsGbc.insets = new Insets(5, 0, 5, 0);

        participantsLabel = new JLabel("Участников: 0", SwingConstants.CENTER);
        participantsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        participantsLabel.setForeground(new Color(219, 191, 52));

        statsGbc.gridx = 0;
        statsGbc.gridy = 0;
        statsPanel.add(participantsLabel, statsGbc);

        statsLabel = new JLabel("Шанс: 0.0%", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(TEXT_MUTED);

        statsGbc.gridy = 1;
        statsPanel.add(statsLabel, statsGbc);

        mainSettingsPanel.add(settingsPanel, BorderLayout.CENTER);
        mainSettingsPanel.add(statsPanel, BorderLayout.SOUTH);

        rightPanel.add(mainSettingsPanel, BorderLayout.CENTER);

        return rightPanel;
    }

    private void showImportDialog() {
        ImportFilmsDialog dialog = new ImportFilmsDialog(this);
        dialog.setVisible(true);

        List<String> importedFilms = dialog.getImportedFilms();
        if (!importedFilms.isEmpty()) {
            executorService.execute(() -> {
                int added = 0;
                int skipped = 0;

                for (String filmTitle : importedFilms) {
                    try {
                        boolean success = dataService.addFilm(filmTitle);
                        if (success) {
                            added++;
                        } else {
                            skipped++;
                        }
                    } catch (Exception e) {
                        skipped++;
                    }
                }

                int finalAdded = added;
                int finalSkipped = skipped;
                SwingUtilities.invokeLater(() -> {
                    loadFilms();
                    JOptionPane.showMessageDialog(this,
                            String.format("Импорт завершен!\nДобавлено: %d\nПропущено (дубликаты): %d",
                                    finalAdded, finalSkipped),
                            "Результат импорта",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            });
        }
    }

    private void addFilm() {
        String title = filmInputField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Введите название фильма",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        executorService.execute(() -> {
            try {
                boolean success = dataService.addFilm(title);
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        filmInputField.setText("");
                        filmInputField.requestFocus();
                        loadFilms();
                    } else {
                        JOptionPane.showMessageDialog(ClientApplication.this,
                                "Фильм с таким названием уже существует",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientApplication.this,
                            "Ошибка при добавлении фильма: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void deleteSelectedFilm() {
        int selectedRow = filmsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите фильм для удаления",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String filmName = (String) tableModel.getValueAt(selectedRow, 1);

        executorService.execute(() -> {
            try {
                List<Films> allFilms = dataService.getAllFilms();
                Films filmToRemove = allFilms.stream().filter(film -> film.getTitle().equals(filmName)).findFirst().orElse(null);

                if (filmToRemove == null) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientApplication.this,
                                "Фильм не найден в базе данных",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    int confirm = JOptionPane.showConfirmDialog(
                            ClientApplication.this,
                            "Удалить фильм \"" + filmName + "\"?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        executorService.execute(() -> {
                            try {
                                boolean success = dataService.removeFilm(filmToRemove.getId());
                                SwingUtilities.invokeLater(() -> {
                                    if (success) {
                                        loadFilms();
                                        JOptionPane.showMessageDialog(ClientApplication.this,
                                                "Фильм \"" + filmName + "\" успешно удален",
                                                "Успех",
                                                JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        JOptionPane.showMessageDialog(ClientApplication.this,
                                                "Ошибка при удалении фильма",
                                                "Ошибка",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } catch (Exception e) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(ClientApplication.this,
                                            "Ошибка при удалении фильма: " + e.getMessage(),
                                            "Ошибка",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        });
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientApplication.this,
                            "Ошибка при получении данных: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void clearAllFilms() {
        int filmCount = dataService.getFilmsCount();
        if (filmCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "Список фильмов уже пуст",
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить все " + filmCount + " фильмов?\nЭто действие нельзя отменить!",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            executorService.execute(() -> {
                try {
                    dataService.clearAllFilms();
                    SwingUtilities.invokeLater(() -> {
                        loadFilms();
                        JOptionPane.showMessageDialog(ClientApplication.this,
                                "Все фильмы удалены",
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientApplication.this,
                                "Ошибка при удалении фильмов: " + e.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            });
        }
    }

    private void loadFilms() {
        executorService.execute(() -> {
            try {
                List<Films> films = dataService.getAllFilms();
                SwingUtilities.invokeLater(() -> {
                    updateFilmsTable(films);
                    updateWheel(films);
                    updateStatistics(films.size());
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientApplication.this,
                            "Ошибка загрузки фильмов: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void updateFilmsTable(List<Films> films) {
        tableModel.setRowCount(0);

        int total = films.size();
        for (int i = 0; i < films.size(); i++) {
            Films film = films.get(i);
            double chance = total > 0 ? (100.0 / total) : 0;
            Object[] row = {
                    i + 1,
                    film.getTitle(),
                    String.format("%.1f%%", chance)
            };
            tableModel.addRow(row);
        }
    }

    private void updateWheel(List<Films> films) {
        List<String> filmNames = films.stream()
                .map(Films::getTitle)
                .collect(Collectors.toList());
        filmWheel.setFilms(filmNames);
    }

    private void updateStatistics(int filmCount) {
        double chance = filmCount > 0 ? (100.0 / filmCount) : 0;
        participantsLabel.setText("Участников: " + filmCount);
        statsLabel.setText(String.format("Шанс: %.1f%%", chance));
    }

    private void spinWheel() {
        if (filmWheel.isSpinning()) {
            JOptionPane.showMessageDialog(this,
                    "Колесо уже крутится!",
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int filmCount = dataService.getFilmsCount();
        if (filmCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте фильмы перед запуском колеса!",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (filmCount == 1) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте больше фильмов для работы колеса!",
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Вместо изменения видимости, просто устанавливаем текст
        resultLabel.setText(" "); // Очищаем пробелом

        filmWheel.resetColors();

        double duration = (Integer) durationSpinner.getValue();
        String style = (String) animationComboBox.getSelectedItem();

        FilmWheel.AnimationStyle animationStyle;
        switch (style) {
            case "Быстрый":
                animationStyle = FilmWheel.AnimationStyle.FAST;
                break;
            case "Медленный":
                animationStyle = FilmWheel.AnimationStyle.SLOW;
                break;
            case "Случайный":
                animationStyle = FilmWheel.AnimationStyle.RANDOM;
                break;
            default:
                animationStyle = FilmWheel.AnimationStyle.STANDARD;
        }

        filmWheel.setAnimationSettings(duration, animationStyle);

        String winner = filmWheel.prepareSpin();

        if (winner != null) {
            participantsLabel.setText("Поехали!");

            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }

            countdownTimer = new Timer(1000, new ActionListener() {
                int remainingSeconds = (int) duration;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!filmWheel.isSpinning()) {
                        ((Timer)e.getSource()).stop();

                        participantsLabel.setText("Участников: " + filmCount);

                        String finalWinner = filmWheel.getResultFilm();
                        if (finalWinner != null) {
                            resultLabel.setText("ПОБЕДИТЕЛЬ: " + finalWinner);
                        }
                        return;
                    }

                    remainingSeconds--;
                    if (remainingSeconds <= 0) {
                        participantsLabel.setText("Завершение...");
                    } else {
                        participantsLabel.setText("Вращение... " + remainingSeconds + " сек");
                    }
                }
            });

            filmWheel.startSpin();
            countdownTimer.start();

        } else {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при подготовке вращения",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        executorService.shutdown();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                UIManager.put("OptionPane.background", DARK_BG);
                UIManager.put("Panel.background", DARK_BG);
                UIManager.put("OptionPane.messageForeground", Color.WHITE);
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));

                UIManager.put("Button.foreground", Color.BLACK);

            } catch (Exception e) {
                e.printStackTrace();
            }

            ClientApplication app = new ClientApplication();
            app.setVisible(true);
        });
    }
}