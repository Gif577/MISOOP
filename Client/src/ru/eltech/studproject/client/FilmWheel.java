package ru.eltech.studproject.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.List;
import java.util.Random;

public class FilmWheel extends JPanel {
    private List<String> films;
    private Color[] colors;
    private Color[] originalColors;
    private double rotation = 0;
    private boolean isSpinning = false;
    private WheelTimer animationTimer;
    private int selectedSegment = -1;
    private int winningSegment = -1;
    private String resultFilm = "";
    private boolean showWinner = false;

    private double animationDuration = 30;
    private AnimationStyle animationStyle = AnimationStyle.STANDARD;

    private static final Color[] WHEEL_COLORS = {
            new Color(255, 50, 50),
            new Color(50, 150, 255),
            new Color(255, 200, 50),
            new Color(50, 200, 200),
            new Color(180, 80, 255),
            new Color(255, 120, 50),
            new Color(50, 220, 150),
            new Color(120, 255, 120),
            new Color(255, 80, 255),
            new Color(100, 180, 255),
            new Color(255, 150, 50),
            new Color(100, 255, 100),
            new Color(200, 100, 255),
            new Color(255, 100, 150),
            new Color(100, 200, 255),
            new Color(255, 220, 100)
    };

    public enum AnimationStyle {
        STANDARD, FAST, SLOW, RANDOM
    }

    public FilmWheel() {
        // Увеличиваем размер колеса
        setPreferredSize(new Dimension(900, 900));
        setMinimumSize(new Dimension(700, 700));
        setOpaque(false);

        animationTimer = new WheelTimer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSpinning) {
                    long elapsed = System.currentTimeMillis() - animationTimer.startTime;
                    double progress = Math.min(1.0, elapsed / (animationDuration * 1000.0));

                    double easedProgress = 1 - Math.pow(1 - progress, 3);
                    rotation = easedProgress * animationTimer.targetRotation;

                    if (progress >= 1.0) {
                        stopSpinning();
                    }
                    repaint();
                }
            }
        });
    }

    public void setAnimationSettings(double duration, AnimationStyle style) {
        this.animationDuration = Math.max(duration, 30);
        this.animationStyle = style;
    }

    public void setFilms(List<String> films) {
        this.films = films;
        this.showWinner = false;
        if (films != null && !films.isEmpty()) {
            colors = new Color[films.size()];
            originalColors = new Color[films.size()];
            for (int i = 0; i < films.size(); i++) {
                colors[i] = WHEEL_COLORS[i % WHEEL_COLORS.length];
                colors[i] = new Color(
                        Math.min(255, colors[i].getRed()),
                        Math.min(255, colors[i].getGreen()),
                        Math.min(255, colors[i].getBlue())
                );
                originalColors[i] = colors[i];
            }
        }
        repaint();
    }

    public String prepareSpin() {
        if (films == null || films.isEmpty()) {
            return null;
        }

        if (isSpinning) {
            return null;
        }

        Random rand = new Random();
        winningSegment = rand.nextInt(films.size());
        resultFilm = films.get(winningSegment);

        double segmentAngle = 360.0 / films.size();

        double targetSegmentCenter = winningSegment * segmentAngle + segmentAngle / 2;

        double targetRotation = 360 - targetSegmentCenter + 90;

        targetRotation = targetRotation % 360;
        if (targetRotation < 0) {
            targetRotation += 360;
        }

        int fullTurns = 5 + rand.nextInt(3);
        targetRotation += fullTurns * 360;

        animationTimer.targetRotation = targetRotation;

        return resultFilm;
    }

    public void startSpin() {
        if (isSpinning) {
            return;
        }

        isSpinning = true;
        selectedSegment = -1;
        showWinner = false;
        rotation = 0;

        animationTimer.startTime = System.currentTimeMillis();
        animationTimer.start();
    }

    public void stopSpinning() {
        isSpinning = false;
        animationTimer.stop();
        selectedSegment = winningSegment;
        showWinner = true;

        if (colors != null && originalColors != null) {
            for (int i = 0; i < colors.length; i++) {
                if (i == winningSegment) {
                    colors[i] = originalColors[i];
                } else {
                    colors[i] = new Color(150, 150, 150);
                }
            }
        }

        repaint();
    }

    public void resetColors() {
        if (colors != null && originalColors != null) {
            for (int i = 0; i < colors.length; i++) {
                colors[i] = originalColors[i];
            }
        }
        showWinner = false;
        repaint();
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public String getResultFilm() {
        return resultFilm;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Увеличиваем радиус колеса для большего размера
        int availableSpace = Math.min(width, height);
        int radius = Math.max(220, (int)(availableSpace * 0.45));

        g2d.setColor(new Color(60, 60, 60, 100));
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(10)); // Увеличили толщину обводки
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        if (films != null && !films.isEmpty()) {
            double segmentAngle = 360.0 / films.size();

            // Увеличиваем базовый размер шрифта для большего колеса
            int baseFontSize = calculateOptimalFontSize(radius, segmentAngle);

            for (int i = 0; i < films.size(); i++) {
                Color segmentColor = colors[i];
                if (i == selectedSegment) {
                    segmentColor = segmentColor.brighter();
                }

                double startAngle = i * segmentAngle + rotation;
                Arc2D segment = new Arc2D.Double(
                        centerX - radius, centerY - radius,
                        radius * 2, radius * 2,
                        startAngle, segmentAngle,
                        Arc2D.PIE
                );

                g2d.setColor(segmentColor);
                g2d.fill(segment);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3)); // Увеличили толщину границ сегментов
                g2d.draw(segment);

                // Рисуем текст для секции
                drawSegmentText(g2d, i, startAngle, segmentAngle, centerX, centerY, radius, baseFontSize);
            }
        } else {
            drawEmptyWheelMessage(g2d, centerX, centerY);
        }

        drawWheelCenter(g2d, centerX, centerY);
        drawWheelPointer(g2d, centerX, centerY, radius);

        if (isSpinning) {
            drawSpinningTimer(g2d, centerX);
        }
    }

    private int calculateOptimalFontSize(int radius, double segmentAngle) {
        if (films == null || films.isEmpty()) {
            return 16;
        }

        // Рассчитываем длину дуги секции
        double arcLength = 2 * Math.PI * (radius - 60) * (segmentAngle / 360.0); // Увеличили отступ

        // Определяем максимальную длину текста среди всех фильмов
        int maxTextLength = films.stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        // Увеличиваем размер шрифта для большего колеса
        int fontSize = (int) Math.min(24, Math.max(14, arcLength / Math.max(1, maxTextLength / 1.8)));

        return fontSize;
    }

    private void drawSegmentText(Graphics2D g2d, int segmentIndex, double startAngle,
                                 double segmentAngle, int centerX, int centerY,
                                 int radius, int baseFontSize) {
        double textAngleRad = Math.toRadians(startAngle + segmentAngle / 2);
        int textRadius = radius - 60; // Увеличили отступ текста от края

        String filmName = films.get(segmentIndex);

        // Автоматически подгоняем текст
        String displayText = adjustTextForSegment(filmName, segmentAngle, radius, g2d, baseFontSize);

        // Позиционируем текст
        int textX = (int) (centerX + textRadius * Math.cos(textAngleRad));
        int textY = (int) (centerY - textRadius * Math.sin(textAngleRad));

        // Сохраняем текущую трансформацию
        AffineTransform originalTransform = g2d.getTransform();

        // Применяем трансформацию для поворота текста
        g2d.translate(textX, textY);
        g2d.rotate(textAngleRad + Math.PI / 2);

        // Устанавливаем шрифт с рассчитанным размером
        Font font = new Font("Segoe UI", Font.BOLD, baseFontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(displayText);

        // Рисуем тень текста
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(displayText, -textWidth/2 + 2, 2);

        // Рисуем основной текст
        g2d.setColor(Color.WHITE);
        g2d.drawString(displayText, -textWidth/2, 0);

        // Восстанавливаем трансформацию
        g2d.setTransform(originalTransform);
    }

    private String adjustTextForSegment(String text, double segmentAngle,
                                        int radius, Graphics2D g2d, int baseFontSize) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Рассчитываем максимальную ширину текста для секции
        double arcLength = 2 * Math.PI * (radius - 60) * (segmentAngle / 360.0);
        int maxWidth = (int) (arcLength * 0.8); // 80% от длины дуги (увеличили)

        Font testFont = new Font("Segoe UI", Font.BOLD, baseFontSize);
        g2d.setFont(testFont);
        FontMetrics fm = g2d.getFontMetrics();

        // Если текст помещается, возвращаем его
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        // Иначе пытаемся сократить текст
        for (int i = text.length() - 1; i > 3; i--) {
            String shortened = text.substring(0, i) + "...";
            if (fm.stringWidth(shortened) <= maxWidth) {
                return shortened;
            }
        }

        // Если даже с "..." не помещается, уменьшаем шрифт и пытаемся снова
        for (int fontSize = baseFontSize - 1; fontSize >= 10; fontSize--) { // Минимум 10 вместо 8
            Font smallerFont = new Font("Segoe UI", Font.BOLD, fontSize);
            g2d.setFont(smallerFont);
            fm = g2d.getFontMetrics();

            if (fm.stringWidth(text) <= maxWidth) {
                return text;
            }

            for (int i = text.length() - 1; i > 3; i--) {
                String shortened = text.substring(0, i) + "...";
                if (fm.stringWidth(shortened) <= maxWidth) {
                    return shortened;
                }
            }
        }

        // Если даже с минимальным шрифтом не помещается, возвращаем очень короткий вариант
        return text.length() > 3 ? text.substring(0, 3) + "..." : text;
    }

    private void drawEmptyWheelMessage(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Увеличили шрифт
        String message = "Добавьте фильмы";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        g2d.drawString(message, centerX - textWidth / 2, centerY);
    }

    private void drawWheelCenter(Graphics2D g2d, int centerX, int centerY) {
        // Увеличиваем центр колеса
        GradientPaint centerGradient = new GradientPaint(
                centerX - 20, centerY - 20, new Color(30, 30, 30),
                centerX + 20, centerY + 20, Color.BLACK
        );
        g2d.setPaint(centerGradient);
        g2d.fillOval(centerX - 20, centerY - 20, 40, 40); // Увеличили

        // Внутренний круг центра
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - 12, centerY - 12, 24, 24); // Увеличили

        // Обводка центра
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2)); // Увеличили толщину
        g2d.drawOval(centerX - 20, centerY - 20, 40, 40);
    }

    private void drawWheelPointer(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Увеличиваем указатель
        int pointerHeight = 60;
        int pointerWidth = 35;

        Polygon pointer = new Polygon();
        pointer.addPoint(centerX, centerY - radius);
        pointer.addPoint(centerX - pointerWidth, centerY - radius - pointerHeight);
        pointer.addPoint(centerX + pointerWidth, centerY - radius - pointerHeight);

        // Градиент для указателя
        GradientPaint pointerGradient = new GradientPaint(
                centerX, centerY - radius, new Color(255, 50, 50),
                centerX, centerY - radius - pointerHeight, new Color(180, 0, 0)
        );
        g2d.setPaint(pointerGradient);
        g2d.fill(pointer);

        // Обводка указателя
        g2d.setColor(new Color(255, 200, 200));
        g2d.setStroke(new BasicStroke(3)); // Увеличили толщину
        g2d.draw(pointer);

        // Тень указателя
        g2d.setColor(new Color(0, 0, 0, 120));
        pointer.translate(3, 3);
        g2d.fill(pointer);
        pointer.translate(-3, -3);

        // Декоративная линия на указателе
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(2)); // Увеличили толщину
        g2d.drawLine(centerX, centerY - radius, centerX, centerY - radius - pointerHeight + 15);
    }

    private void drawSpinningTimer(Graphics2D g2d, int centerX) {
        long elapsed = animationTimer.isRunning() ?
                System.currentTimeMillis() - animationTimer.startTime : 0;
        double progress = Math.min(1.0, elapsed / (animationDuration * 1000.0));
        int secondsLeft = (int) Math.ceil(animationDuration * (1 - progress));

        // Фон таймера
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRoundRect(centerX - 150, 30, 300, 50, 20, 20); // Увеличили

        // Обводка таймера
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2)); // Увеличили
        g2d.drawRoundRect(centerX - 150, 30, 300, 50, 20, 20);

        // Текст таймера
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Увеличили
        String spinningText = "Вращение... " + secondsLeft + " сек";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(spinningText);
        g2d.drawString(spinningText, centerX - textWidth / 2, 60); // Подвинули вниз
    }

    class WheelTimer extends javax.swing.Timer {
        public long startTime;
        public double targetRotation;

        public WheelTimer(int delay, ActionListener listener) {
            super(delay, listener);
        }
    }
}