package ru.eltech.studproject.client;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CustomButtonStyle {

    // Основные цвета Bootstrap
    private static final Color BOOTSTRAP_LIGHT = Color.WHITE;
    private static final Color BOOTSTRAP_LIGHT_HOVER = new Color(248, 249, 250);
    private static final Color BOOTSTRAP_LIGHT_ACTIVE = new Color(233, 236, 239);
    private static final Color BOOTSTRAP_TEXT_WHITE = Color.WHITE; // Исправлено: был темный цвет, теперь белый
    private static final Color BOOTSTRAP_TEXT_DARK = new Color(33, 37, 41);
    private static final Color BOOTSTRAP_BORDER = new Color(222, 226, 230); // #dee2e6
    private static final Color BOOTSTRAP_BORDER_HOVER = new Color(206, 212, 218); // #ced4da

    // Тени Bootstrap
    private static final Color BOOTSTRAP_SHADOW = new Color(0, 0, 0, 0.15f);

    // Точные размеры и отступы Bootstrap
    private static final int BOOTSTRAP_BORDER_RADIUS = 6; // 0.375rem
    private static final int BOOTSTRAP_BORDER_WIDTH = 1;
    private static final Insets BOOTSTRAP_PADDING = new Insets(6, 12, 6, 12); // .375rem .75rem
    private static final Font BOOTSTRAP_FONT = new Font("Segoe UI", Font.BOLD, 16); // 1rem

    // Размеры для кнопок
    private static final Dimension FIXED_BUTTON_SIZE = new Dimension(180, 50);
    private static final Dimension SPIN_BUTTON_SIZE = new Dimension(250, 60);

    public static void styleOutlineLightButton(JButton button) {
        // Сбрасываем все стандартные стили
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(true);

        // Устанавливаем шрифт Bootstrap
        button.setFont(BOOTSTRAP_FONT);

        // Устанавливаем белый цвет текста по умолчанию
        button.setForeground(BOOTSTRAP_TEXT_WHITE);

        // Устанавливаем курсор
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Устанавливаем фиксированный размер для обычных кнопок
        if (!button.getText().equals("Крутить колесо!")) {
            button.setPreferredSize(FIXED_BUTTON_SIZE);
            button.setMinimumSize(FIXED_BUTTON_SIZE);
            button.setMaximumSize(FIXED_BUTTON_SIZE);
        } else {
            // Для кнопки "Крутить колесо!" больший размер и шрифт
            button.setPreferredSize(SPIN_BUTTON_SIZE);
            button.setMinimumSize(SPIN_BUTTON_SIZE);
            button.setMaximumSize(SPIN_BUTTON_SIZE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        }

        // Создаем кастомную модель для правильного поведения состояний
        button.setModel(new DefaultButtonModel() {
            @Override
            public boolean isPressed() {
                return super.isPressed();
            }

            @Override
            public boolean isRollover() {
                return super.isRollover();
            }
        });

        // Устанавливаем рендерер для кастомной отрисовки
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();

                // Включаем сглаживание
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int width = b.getWidth();
                int height = b.getHeight();

                // Определяем состояние кнопки
                boolean isPressed = b.getModel().isPressed();
                boolean isRollover = b.getModel().isRollover();
                boolean isEnabled = b.isEnabled();

                // Рисуем фон
                if (isEnabled) {
                    if (isPressed) {
                        // Активное состояние (нажато)
                        g2.setColor(BOOTSTRAP_LIGHT_ACTIVE);
                    } else if (isRollover) {
                        // Hover состояние
                        g2.setColor(BOOTSTRAP_LIGHT_HOVER);
                    } else {
                        // Нормальное состояние
                        g2.setColor(new Color(255, 255, 255, 0)); // Прозрачный фон для outline
                    }
                } else {
                    // Отключенное состояние
                    g2.setColor(new Color(233, 236, 239, 100));
                }

                // Рисуем закругленный прямоугольник фона
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                        BOOTSTRAP_BORDER_WIDTH / 2f,
                        BOOTSTRAP_BORDER_WIDTH / 2f,
                        width - BOOTSTRAP_BORDER_WIDTH,
                        height - BOOTSTRAP_BORDER_WIDTH,
                        BOOTSTRAP_BORDER_RADIUS,
                        BOOTSTRAP_BORDER_RADIUS
                );

                if (isPressed || isRollover || !isEnabled) {
                    g2.fill(roundedRect);
                }

                // Рисуем border
                if (isEnabled) {
                    if (isPressed) {
                        g2.setColor(BOOTSTRAP_BORDER_HOVER);
                    } else if (isRollover) {
                        g2.setColor(BOOTSTRAP_BORDER_HOVER);
                    } else {
                        g2.setColor(Color.WHITE); // Белый border по умолчанию
                    }
                } else {
                    g2.setColor(new Color(222, 226, 230, 100));
                }

                g2.setStroke(new BasicStroke(BOOTSTRAP_BORDER_WIDTH));
                g2.draw(roundedRect);

                // Добавляем тень при наведении (как в Bootstrap)
                if (isRollover && !isPressed) {
                    g2.setColor(BOOTSTRAP_SHADOW);
                    RoundRectangle2D shadowRect = new RoundRectangle2D.Float(
                            0, 0, width, height,
                            BOOTSTRAP_BORDER_RADIUS + 1,
                            BOOTSTRAP_BORDER_RADIUS + 1
                    );
                    g2.fill(shadowRect);
                }

                // Рисуем текст с учетом состояния
                String text = b.getText();
                FontMetrics fm = g2.getFontMetrics(b.getFont());
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();

                int x = (width - textWidth) / 2;
                int y = (height - textHeight) / 2 + fm.getAscent();

                // Цвет текста по умолчанию белый, при наведении - темный
                if (isEnabled) {
                    if (isRollover || isPressed) {
                        // При наведении или нажатии - темный текст
                        g2.setColor(BOOTSTRAP_TEXT_DARK);
                    } else {
                        // По умолчанию - белый текст
                        g2.setColor(BOOTSTRAP_TEXT_WHITE);
                    }
                } else {
                    // Отключенное состояние
                    g2.setColor(new Color(255, 255, 255, 100)); // Полупрозрачный белый
                }

                g2.drawString(text, x, y);

                // Рисуем фокус (outline как в Bootstrap)
                if (b.isFocusOwner()) {
                    g2.setColor(new Color(255, 255, 255, 100)); // Белый фокус с прозрачностью
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    RoundRectangle2D focusRect = new RoundRectangle2D.Float(
                            2, 2, width - 4, height - 4,
                            BOOTSTRAP_BORDER_RADIUS,
                            BOOTSTRAP_BORDER_RADIUS
                    );
                    g2.draw(focusRect);
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                JButton b = (JButton) c;

                // Возвращаем фиксированный размер для всех кнопок, кроме "Крутить колесо!"
                if (b.getText().equals("Крутить колесо!")) {
                    return SPIN_BUTTON_SIZE;
                } else {
                    return FIXED_BUTTON_SIZE;
                }
            }
        });

        // Добавляем слушатели для анимации состояний
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.getModel().setRollover(true);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.getModel().setRollover(false);
                button.repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.getModel().setPressed(true);
                button.repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.getModel().setPressed(false);
                button.repaint();
            }
        });

        // Добавляем слушатель для фокуса
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                button.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                button.repaint();
            }
        });
    }

    // Упрощенный метод для outline-light (как в вашем примере)
    public static void styleOutlineButton(JButton button) {
        styleOutlineLightButton(button);
    }

    // Метод для ghost кнопки (как у вас в примере)
    public static void styleGhostButton(JButton button) {
        Color ghostBorder = new Color(108, 117, 125); // Bootstrap secondary
        Color ghostText = new Color(108, 117, 125);
        Color ghostHover = new Color(233, 236, 239);
        Color ghostActive = new Color(222, 226, 230);

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.setFont(BOOTSTRAP_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(ghostText);

        // Устанавливаем фиксированный размер для ghost кнопок
        button.setPreferredSize(FIXED_BUTTON_SIZE);
        button.setMinimumSize(FIXED_BUTTON_SIZE);
        button.setMaximumSize(FIXED_BUTTON_SIZE);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int width = b.getWidth();
                int height = b.getHeight();

                boolean isPressed = b.getModel().isPressed();
                boolean isRollover = b.getModel().isRollover();
                boolean isEnabled = b.isEnabled();

                Color currentBorder = isEnabled ? ghostBorder :
                        new Color(ghostBorder.getRed(), ghostBorder.getGreen(),
                                ghostBorder.getBlue(), 100);
                Color currentText = isEnabled ? ghostText :
                        new Color(ghostText.getRed(), ghostText.getGreen(),
                                ghostText.getBlue(), 100);

                // Фон
                if (isEnabled && (isPressed || isRollover)) {
                    g2.setColor(isPressed ? ghostActive : ghostHover);
                    RoundRectangle2D rect = new RoundRectangle2D.Float(
                            0, 0, width, height,
                            BOOTSTRAP_BORDER_RADIUS, BOOTSTRAP_BORDER_RADIUS
                    );
                    g2.fill(rect);
                }

                // Border
                g2.setColor(currentBorder);
                g2.setStroke(new BasicStroke(BOOTSTRAP_BORDER_WIDTH));
                RoundRectangle2D borderRect = new RoundRectangle2D.Float(
                        BOOTSTRAP_BORDER_WIDTH / 2f,
                        BOOTSTRAP_BORDER_WIDTH / 2f,
                        width - BOOTSTRAP_BORDER_WIDTH,
                        height - BOOTSTRAP_BORDER_WIDTH,
                        BOOTSTRAP_BORDER_RADIUS,
                        BOOTSTRAP_BORDER_RADIUS
                );
                g2.draw(borderRect);

                // Текст
                String text = b.getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (width - fm.stringWidth(text)) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(currentText);
                g2.drawString(text, x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return FIXED_BUTTON_SIZE;
            }
        });
    }

    // Методы для других типов кнопок Bootstrap
    public static void stylePrimaryButton(JButton button) {
        Color primary = new Color(0, 123, 255); // Bootstrap primary
        Color primaryHover = new Color(0, 105, 217);
        Color primaryActive = new Color(0, 98, 204);

        styleSolidButton(button, primary, Color.WHITE, primaryHover, primaryActive);
    }

    public static void styleSuccessButton(JButton button) {
        Color success = new Color(40, 167, 69); // Bootstrap success
        Color successHover = new Color(33, 136, 56);
        Color successActive = new Color(30, 126, 52);

        styleSolidButton(button, success, Color.WHITE, successHover, successActive);
    }

    public static void styleDangerButton(JButton button) {
        Color danger = new Color(220, 53, 69); // Bootstrap danger
        Color dangerHover = new Color(200, 35, 51);
        Color dangerActive = new Color(178, 31, 45);

        styleSolidButton(button, danger, Color.WHITE, dangerHover, dangerActive);
    }

    private static void styleSolidButton(JButton button, Color bgColor, Color textColor,
                                         Color hoverBgColor, Color activeBgColor) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.setFont(BOOTSTRAP_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(textColor);

        // Устанавливаем фиксированный размер для solid кнопок
        button.setPreferredSize(FIXED_BUTTON_SIZE);
        button.setMinimumSize(FIXED_BUTTON_SIZE);
        button.setMaximumSize(FIXED_BUTTON_SIZE);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int width = b.getWidth();
                int height = b.getHeight();

                boolean isPressed = b.getModel().isPressed();
                boolean isRollover = b.getModel().isRollover();
                boolean isEnabled = b.isEnabled();

                Color currentBg = isEnabled ?
                        (isPressed ? activeBgColor :
                                (isRollover ? hoverBgColor : bgColor)) :
                        new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 100);

                Color currentText = isEnabled ? textColor :
                        new Color(textColor.getRed(), textColor.getGreen(),
                                textColor.getBlue(), 100);

                // Фон
                g2.setColor(currentBg);
                RoundRectangle2D rect = new RoundRectangle2D.Float(
                        0, 0, width, height,
                        BOOTSTRAP_BORDER_RADIUS, BOOTSTRAP_BORDER_RADIUS
                );
                g2.fill(rect);

                // Текст
                String text = b.getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (width - fm.stringWidth(text)) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(currentText);
                g2.drawString(text, x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return FIXED_BUTTON_SIZE;
            }
        });
    }

    // Метод для outline кнопок с кастомными цветами
    public static void styleOutlineButton(JButton button, Color borderColor, Color textColor,
                                          Color hoverBgColor, Color activeBgColor) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.setFont(BOOTSTRAP_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(textColor);

        // Устанавливаем фиксированный размер для outline кнопок
        button.setPreferredSize(FIXED_BUTTON_SIZE);
        button.setMinimumSize(FIXED_BUTTON_SIZE);
        button.setMaximumSize(FIXED_BUTTON_SIZE);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int width = b.getWidth();
                int height = b.getHeight();

                boolean isPressed = b.getModel().isPressed();
                boolean isRollover = b.getModel().isRollover();
                boolean isEnabled = b.isEnabled();

                Color currentBorder = isEnabled ? borderColor :
                        new Color(borderColor.getRed(), borderColor.getGreen(),
                                borderColor.getBlue(), 100);
                Color currentText = isEnabled ? textColor :
                        new Color(textColor.getRed(), textColor.getGreen(),
                                textColor.getBlue(), 100);

                // Фон
                if (isEnabled && (isPressed || isRollover)) {
                    g2.setColor(isPressed ? activeBgColor : hoverBgColor);
                    RoundRectangle2D rect = new RoundRectangle2D.Float(
                            0, 0, width, height,
                            BOOTSTRAP_BORDER_RADIUS, BOOTSTRAP_BORDER_RADIUS
                    );
                    g2.fill(rect);
                }

                // Border
                g2.setColor(currentBorder);
                g2.setStroke(new BasicStroke(BOOTSTRAP_BORDER_WIDTH));
                RoundRectangle2D borderRect = new RoundRectangle2D.Float(
                        BOOTSTRAP_BORDER_WIDTH / 2f,
                        BOOTSTRAP_BORDER_WIDTH / 2f,
                        width - BOOTSTRAP_BORDER_WIDTH,
                        height - BOOTSTRAP_BORDER_WIDTH,
                        BOOTSTRAP_BORDER_RADIUS,
                        BOOTSTRAP_BORDER_RADIUS
                );
                g2.draw(borderRect);

                // Текст
                String text = b.getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (width - fm.stringWidth(text)) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(currentText);
                g2.drawString(text, x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return FIXED_BUTTON_SIZE;
            }
        });
    }
}