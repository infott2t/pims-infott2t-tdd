package swing.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Swing UI 공통 폰트 및 색상 팔레트
 */
public final class UiTheme {

    public static final String FONT_NAME = "맑은 고딕";

    public static final Font FONT_REGULAR = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_SUBTITLE = new Font(FONT_NAME, Font.BOLD, 15);
    public static final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 20);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color SURFACE = new Color(248, 250, 252);
    public static final Color HEADER_BG = new Color(30, 41, 59);
    public static final Color MUTED_TEXT = new Color(100, 116, 139);

    private UiTheme() {
    }

    public static void applyGlobalLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // 기본 LookAndFeel 유지
        }
        FontUIResource fontRes = new FontUIResource(FONT_REGULAR);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}
