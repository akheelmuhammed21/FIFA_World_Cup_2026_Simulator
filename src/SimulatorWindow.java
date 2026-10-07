import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

// Disambiguate Timer — always use the Swing version throughout this file


public class SimulatorWindow extends JFrame implements SimulationListener {

    // ═══════════════════════════════════════════════════════════════════════════
    //  PALETTE
    // ═══════════════════════════════════════════════════════════════════════════
    static final Color BG_VOID    = new Color(0x05, 0x07, 0x10);
    static final Color BG_SURFACE = new Color(0x0A, 0x0D, 0x1C);
    static final Color BG_RAISED  = new Color(0x0F, 0x13, 0x26);
    static final Color BG_CARD    = new Color(0x0D, 0x11, 0x22);
    static final Color BG_CARD2   = new Color(0x12, 0x18, 0x2E);
    static final Color GOLD       = new Color(0xFF, 0xBB, 0x00);
    static final Color GOLD_HOT   = new Color(0xFF, 0xD8, 0x50);
    static final Color GOLD_DIM   = new Color(0x80, 0x55, 0x00);
    static final Color CYAN       = new Color(0x00, 0xC8, 0xFF);
    static final Color GREEN      = new Color(0x2E, 0xE8, 0x8A);
    static final Color AMBER      = new Color(0xFF, 0x8C, 0x2F);
    static final Color RED_MED    = new Color(0xFF, 0x4C, 0x4C);
    static final Color SNOW       = new Color(0xEE, 0xF2, 0xFF);
    static final Color MIST       = new Color(0xAA, 0xB2, 0xCC);
    static final Color SLATE      = new Color(0x52, 0x5C, 0x7A);
    static final Color RULE       = new Color(0x18, 0x1E, 0x38);

    static Color confColor(String c) {
        return switch (c) {
            case "UEFA"     -> new Color(0x40, 0x88, 0xFF);
            case "CONMEBOL" -> new Color(0x00, 0xCC, 0x66);
            case "CONCACAF" -> new Color(0xFF, 0x66, 0x00);
            case "CAF"      -> new Color(0xFF, 0xCC, 0x00);
            case "AFC"      -> new Color(0xFF, 0x30, 0x60);
            case "OFC"      -> new Color(0xAA, 0x66, 0xFF);
            default         -> SLATE;
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TYPOGRAPHY
    // ═══════════════════════════════════════════════════════════════════════════
    static final Font F_HERO  = pick("Helvetica Neue", Font.BOLD,  34);
    static final Font F_HEAD  = pick("Helvetica Neue", Font.BOLD,  13);
    static final Font F_LABEL = pick("Helvetica Neue", Font.BOLD,  12);
    static final Font F_BODY  = pick("Helvetica Neue", Font.PLAIN, 12);
    static final Font F_SMALL = pick("Helvetica Neue", Font.PLAIN, 11);
    static final Font F_TINY  = pick("Helvetica Neue", Font.BOLD,  10);
    static final Font F_MONO  = pick("Menlo",          Font.PLAIN, 13);

    static Font pick(String name, int style, int size) {
        Font f = new Font(name, style, size);
        if (f.getFamily().equalsIgnoreCase(name)) return f;
        for (String fb : new String[]{"SF Pro Display", "Trebuchet MS", "Verdana", "Dialog"}) {
            Font g = new Font(fb, style, size);
            if (g.getFamily().equalsIgnoreCase(fb)) return g;
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DATA MODELS
    // ═══════════════════════════════════════════════════════════════════════════
    static class BracketSlot {
        Team home, away, winner;
        Integer hg, ag;
        boolean pens;
        boolean flashing;
        float flashAmt = 0f;

        BracketSlot(Team h, Team a) { home = h; away = a; }

        boolean played() { return hg != null; }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  STATE
    // ═══════════════════════════════════════════════════════════════════════════
    private final Map<String, Group> completedGroups = new LinkedHashMap<>();
    // rounds[0..4] = R32(16), R16(8), QF(4), SF(2), Final(1)
    private final BracketSlot[][] rounds = {
        new BracketSlot[16], new BracketSlot[8], new BracketSlot[4],
        new BracketSlot[2],  new BracketSlot[1]
    };
    private final int[] roundCount = new int[5];
    private BracketSlot thirdSlot;
    private Team champion, runnerUp, thirdTeam;

    // UI
    private GroupsPanel    groupsPanel;
    private BracketPanel   bracketPanel;
    private JTextPane      logPane;
    private StyledDocument logDoc;
    private JButton        runBtn;
    private JLabel         statusLabel;
    private JComponent     statusDot;
    private Color          dotColor   = SLATE;
    private float          pulseAlpha = 1f;
    private boolean        pulseUp    = false;
    private javax.swing.Timer pulseTimer;
    private CardLayout     cardLayout;
    private JPanel         cardPanel;
    private String         activeCard = "groups";
    private NavButton[]    navBtns;

    // Champion overlay
    private float  champAlpha = 0f;
    private javax.swing.Timer champTimer;
    private boolean showChamp = false;

    // ═══════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════════
    public SimulatorWindow() {
        super("FIFA World Cup 2026 · Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 860);
        setMinimumSize(new Dimension(920, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_VOID);
        buildUI();
        initTimers();
    }

    private void initTimers() {
        pulseTimer = new javax.swing.Timer(45, e -> {
            pulseAlpha += pulseUp ? 0.04f : -0.04f;
            if (pulseAlpha >= 1f) { pulseAlpha = 1f; pulseUp = false; }
            if (pulseAlpha <= 0.2f) { pulseAlpha = 0.2f; pulseUp = true; }
            if (statusDot != null) statusDot.repaint();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LAYOUT
    // ═══════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(BG_VOID);
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildContent(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(BG_SURFACE);
                g2.fillRect(0, 0, w, h);

                // Diagonal mesh
                g2.setStroke(new BasicStroke(0.4f));
                g2.setColor(new Color(255, 255, 255, 5));
                for (int x = -h; x < w + h; x += 24)
                    g2.drawLine(x, 0, x + h, h);

                // Radial glow from top-center
                for (int r = 200; r > 0; r -= 20) {
                    int a = (int)(14 * (1 - r / 200f));
                    g2.setColor(new Color(0xFF, 0xBB, 0x00, a));
                    g2.fillOval(w / 2 - r, -r / 2, r * 2, r * 2);
                }
                // Top gold line
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(0xFF, 0xBB, 0x00, 180));
                g2.drawLine(0, 0, w, 0);
                // Bottom rule
                g2.setColor(RULE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, h - 1, w, h - 1);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(0, 108));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(14, 32, 12, 32));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleRow.setOpaque(false);
        titleRow.add(hexGlyph());
        titleRow.add(makeHeroLabel("FIFA WORLD CUP 2026"));
        titleRow.add(hexGlyph());

        p.add(titleRow);
        p.add(Box.createVerticalStrut(5));
        p.add(centeredLabel("UNITED 2026  ·  USA  ·  CANADA  ·  MEXICO",
            F_HEAD, new Color(0x88, 0x96, 0xCC)));
        p.add(Box.createVerticalStrut(3));
        p.add(centeredLabel(
            "Team Strength  ·  Form  ·  Squad & Injuries  ·  H2H Records  ·  NA Venue Boost  ·  Live FotMob Data",
            F_SMALL, SLATE));
        return p;
    }

    private JLabel hexGlyph() {
        JLabel l = new JLabel("⬡");
        l.setFont(new Font("Dialog", Font.PLAIN, 20));
        l.setForeground(GOLD_DIM);
        return l;
    }

    private JLabel makeHeroLabel(String text) {
        return new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = fm.getAscent();
                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawString(text, 2, y + 2);
                g2.setPaint(new GradientPaint(0, 0, GOLD_HOT, getWidth(), 0, GOLD));
                g2.drawString(text, 0, y);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                return new Dimension(fm.stringWidth(getText()) + 4, fm.getHeight() + 4);
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(BG_SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(RULE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(148, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        String[] ids     = {"groups",  "bracket", "log"};
        String[] labels  = {"GROUPS",  "BRACKET", "LOG"};
        String[] glyphs  = {"⊞",       "◈",       "≡"};

        navBtns = new NavButton[3];
        for (int i = 0; i < 3; i++) {
            navBtns[i] = new NavButton(ids[i], labels[i], glyphs[i]);
            p.add(navBtns[i]);
            p.add(Box.createVerticalStrut(6));
        }
        navBtns[0].setActive(true);
        return p;
    }

    class NavButton extends JPanel {
        private final String cardId, label, glyph;
        private boolean active = false;
        private boolean hovered = false;
        private float hoverAmt = 0f;
        private javax.swing.Timer hoverTimer;

        NavButton(String id, String lbl, String gly) {
            this.cardId = id; this.label = lbl; this.glyph = gly;
            setPreferredSize(new Dimension(124, 48));
            setMaximumSize(new Dimension(124, 48));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; animHover(); }
                public void mouseExited(MouseEvent e)  { hovered = false; animHover(); }
                public void mouseClicked(MouseEvent e) { navigate(cardId); }
            });
        }

        private void animHover() {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new javax.swing.Timer(16, e -> {
                hoverAmt += hovered ? 0.12f : -0.12f;
                hoverAmt = Math.max(0f, Math.min(1f, hoverAmt));
                repaint();
                if ((hovered && hoverAmt >= 1f) || (!hovered && hoverAmt <= 0f))
                    hoverTimer.stop();
            });
            hoverTimer.start();
        }

        void setActive(boolean a) { active = a; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();

            // Background
            if (active) {
                g2.setColor(new Color(0xFF, 0xBB, 0x00, 30));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(new Color(0xFF, 0xBB, 0x00, 80));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                // Left accent bar
                g2.setColor(GOLD);
                g2.fillRoundRect(0, 8, 3, h - 16, 2, 2);
            } else if (hoverAmt > 0) {
                int bg = (int)(hoverAmt * 18);
                g2.setColor(new Color(255, 255, 255, bg));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            // Glyph
            g2.setFont(new Font("Dialog", Font.PLAIN, 16));
            g2.setColor(active ? GOLD : blend(SLATE, MIST, hoverAmt));
            g2.drawString(glyph, 14, h / 2 + 6);

            // Label
            g2.setFont(F_TINY);
            g2.setColor(active ? GOLD : blend(SLATE, SNOW, hoverAmt));
            g2.drawString(label, 36, h / 2 + 4);

            g2.dispose();
        }
    }

    private void navigate(String id) {
        activeCard = id;
        cardLayout.show(cardPanel, id);
        for (NavButton nb : navBtns) nb.setActive(nb.cardId.equals(id));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CONTENT AREA
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG_VOID);

        groupsPanel  = new GroupsPanel();
        bracketPanel = new BracketPanel();

        cardPanel.add(groupsPanel,     "groups");
        cardPanel.add(bracketPanel,    "bracket");
        cardPanel.add(buildLogPane(),  "log");

        return cardPanel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  GROUPS PANEL  — 3-row × 4-col grid of group cards
    // ═══════════════════════════════════════════════════════════════════════════
    class GroupsPanel extends JPanel {
        private final Map<String, GroupCard> groupCards = new LinkedHashMap<>();

        GroupsPanel() {
            setLayout(new GridBagLayout());
            setBackground(BG_VOID);
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            String[] names = {"A","B","C","D","E","F","G","H","I","J","K","L"};
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 1.0; gc.weighty = 1.0;
            gc.insets = new Insets(5, 5, 5, 5);

            for (int i = 0; i < names.length; i++) {
                GroupCard card = new GroupCard(names[i]);
                groupCards.put(names[i], card);
                gc.gridx = i % 4;
                gc.gridy = i / 4;
                add(card, gc);
            }
        }

        void updateGroup(Group g) {
            GroupCard card = groupCards.get(g.name);
            if (card != null) card.setGroup(g);
        }
    }

    // ─── Individual group card ────────────────────────────────────────────────
    class GroupCard extends JPanel {
        private final String gname;
        private Group group;
        private float flashAmt = 0f;
        private javax.swing.Timer flashTimer;

        GroupCard(String n) {
            gname = n;
            setOpaque(false);
            setPreferredSize(new Dimension(200, 195));
        }

        void setGroup(Group g) {
            group = g;
            // Trigger flash
            flashAmt = 1f;
            if (flashTimer != null) flashTimer.stop();
            flashTimer = new javax.swing.Timer(40, null);
            flashTimer.addActionListener(e -> {
                flashAmt -= 0.06f;
                if (flashAmt <= 0) { flashAmt = 0; flashTimer.stop(); }
                repaint();
            });
            flashTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();

            // Shadow / glow
            if (flashAmt > 0) {
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(),
                        (int)(flashAmt * 30 / i)));
                    g2.fillRoundRect(-i, -i, w + i * 2, h + i * 2, 14, 14);
                }
            }

            // Card body
            g2.setColor(BG_CARD);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            // Border
            Color borderCol = flashAmt > 0
                ? new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), (int)(flashAmt * 200))
                : RULE;
            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // Header strip
            int hdrH = 32;
            GradientPaint hdrGrad = new GradientPaint(0, 0,
                new Color(0x18, 0x1E, 0x38), 0, hdrH, new Color(0x10, 0x14, 0x28));
            g2.setPaint(hdrGrad);
            g2.fillRoundRect(0, 0, w, hdrH + 8, 12, 12);
            g2.fillRect(0, 8, w, hdrH);

            g2.setColor(RULE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(1, hdrH, w - 2, hdrH);

            // Group name
            g2.setFont(F_HEAD);
            g2.setColor(GOLD);
            g2.drawString("GROUP " + gname, 12, 21);

            if (group == null) {
                g2.setFont(F_SMALL);
                g2.setColor(new Color(0x40, 0x48, 0x66));
                g2.drawString("Awaiting draw results...", 12, hdrH + 20);
                g2.dispose();
                return;
            }

            // Team rows
            List<Team> st = group.standings();
            int rowsH = h - hdrH - 6;
            int rowH  = rowsH / 4;

            for (int i = 0; i < st.size(); i++) {
                Team t  = st.get(i);
                int ry  = hdrH + 3 + i * rowH;
                int ry2 = ry + rowH;

                // Row background
                Color rowBg = switch (i) {
                    case 0, 1 -> new Color(GREEN.getRed(),  GREEN.getGreen(),  GREEN.getBlue(),  14);
                    case 2    -> new Color(AMBER.getRed(),  AMBER.getGreen(),  AMBER.getBlue(),  12);
                    default   -> new Color(RED_MED.getRed(), RED_MED.getGreen(), RED_MED.getBlue(), 8);
                };
                g2.setColor(rowBg);
                g2.fillRect(1, ry, w - 2, rowH);

                // Separator
                if (i == 1) {
                    g2.setColor(new Color(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), 60));
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawLine(8, ry2, w - 8, ry2);
                }

                // Position chip
                Color posCol = switch (i) {
                    case 0, 1 -> GREEN;
                    case 2    -> AMBER;
                    default   -> SLATE;
                };
                int chipX = 8, chipY = ry + 4, chipW = 16, chipH = rowH - 8;
                g2.setColor(new Color(posCol.getRed(), posCol.getGreen(), posCol.getBlue(), 40));
                g2.fillRoundRect(chipX, chipY, chipW, chipH, 4, 4);
                g2.setFont(F_TINY);
                g2.setColor(posCol);
                FontMetrics cfm = g2.getFontMetrics();
                g2.drawString(String.valueOf(i + 1),
                    chipX + (chipW - cfm.stringWidth(String.valueOf(i + 1))) / 2,
                    chipY + (chipH + cfm.getAscent() - cfm.getDescent()) / 2);

                // Confederation dot
                Color cc = confColor(t.confederation);
                int dotX = 30, dotY = ry + rowH / 2 - 4;
                g2.setColor(new Color(cc.getRed(), cc.getGreen(), cc.getBlue(), 200));
                g2.fillOval(dotX, dotY, 7, 7);

                // Team name
                String tname = t.name.length() > 12 ? t.name.substring(0, 11) + "." : t.name;
                g2.setFont(F_BODY);
                g2.setColor(i < 2 ? SNOW : (i == 2 ? MIST : SLATE));
                g2.drawString(tname, 42, ry + rowH / 2 + 5);

                // Stats (right-aligned)
                String pts = t.points + "pt";
                String gd  = (t.goalDifference() >= 0 ? "+" : "") + t.goalDifference();
                g2.setFont(F_TINY);
                g2.setColor(posCol);
                int statsX = w - 42;
                g2.drawString(pts, statsX, ry + rowH / 2 + 5);
                g2.setColor(new Color(posCol.getRed(), posCol.getGreen(), posCol.getBlue(), 160));
                g2.drawString(gd, statsX - 24, ry + rowH / 2 + 5);
            }

            g2.dispose();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BRACKET PANEL — fully responsive, fills available space with no scroll
    // ═══════════════════════════════════════════════════════════════════════════
    class BracketPanel extends JPanel {

        // Computed fresh every paint from actual component size
        private int cardW, cardH, colStep, pitch0, padL, padT;

        BracketPanel() { setBackground(BG_VOID); }

        // ── Layout helpers ───────────────────────────────────────────────────
        private void computeLayout() {
            int W = getWidth(), H = getHeight();
            padL = 12;
            padT = 44;
            int padR = 12, padB = 14;
            int availW = W - padL - padR;
            int availH = H - padT - padB;

            // 5 rounds across the width
            colStep = availW / 5;
            cardW   = colStep - 12;          // 12 px inter-column gap

            // 16 R32 matches must fit in the height
            pitch0  = availH / 16;
            // Card height: leave 3 px margin inside each pitch slot
            cardH   = Math.max(26, pitch0 - 3);
        }

        private int colX(int col) { return padL + col * colStep; }

        private int cardY(int round, int idx) {
            int pitch = pitch0 << round;           // doubles each round
            int firstY = pitch / 2 - cardH / 2;   // centre first card in its slot
            return padT + firstY + idx * pitch;
        }

        // ── Paint ────────────────────────────────────────────────────────────
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            g2.setColor(BG_VOID);
            g2.fillRect(0, 0, getWidth(), getHeight());

            computeLayout();

            // Column headers + accent lines
            String[] hdrs = {"ROUND OF 32", "ROUND OF 16", "QUARTER-FINALS",
                             "SEMI-FINALS",  "THE FINAL"};
            for (int r = 0; r < 5; r++) {
                int x = colX(r);
                g2.setFont(F_TINY);
                g2.setColor(new Color(0x44, 0x50, 0x72));
                g2.drawString(hdrs[r], x + 2, padT - 14);
                g2.setColor(new Color(0x20, 0x28, 0x48));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(x, padT - 10, x + cardW, padT - 10);
            }

            // 3rd-place label — sits below SF column (col 3)
            int sfBot  = cardY(3, 1) + cardH;      // bottom of SF[1]
            int thirdY = sfBot + Math.max(10, pitch0 / 4);
            int thirdX = colX(3);
            g2.setFont(F_TINY);
            g2.setColor(new Color(0x44, 0x50, 0x72));
            g2.drawString("3RD PLACE", thirdX + 2, thirdY - 5);

            // Connectors (behind cards)
            drawConnectors(g2);

            // Main bracket cards: R32 → SF
            for (int r = 0; r < 4; r++) {
                int count = switch (r) { case 0->16; case 1->8; case 2->4; default->2; };
                for (int m = 0; m < count; m++) {
                    BracketSlot slot = rounds[r] != null ? rounds[r][m] : null;
                    drawCard(g2, colX(r), cardY(r, m), slot);
                }
            }

            // Final card — centred vertically in col 4
            drawCard(g2, colX(4), cardY(4, 0), rounds[4] != null ? rounds[4][0] : null);

            // 3rd-place card
            drawCard(g2, thirdX, thirdY, thirdSlot);

            // Champion banner (fades in over Final card)
            if (champion != null) drawChampionBanner(g2);

            g2.dispose();
        }

        // ── Elbow connectors ─────────────────────────────────────────────────
        private void drawConnectors(Graphics2D g2) {
            for (int r = 0; r < 4; r++) {
                int count = switch (r) { case 0->16; case 1->8; case 2->4; default->2; };
                for (int m = 0; m < count / 2; m++) {
                    BracketSlot s0 = rounds[r] != null ? rounds[r][2 * m]     : null;
                    BracketSlot s1 = rounds[r] != null ? rounds[r][2 * m + 1] : null;
                    boolean both   = s0 != null && s0.played() && s1 != null && s1.played();

                    int x0   = colX(r) + cardW;
                    int x1   = colX(r + 1);
                    int jx   = x0 + (x1 - x0) / 2;
                    int y0c  = cardY(r, 2 * m) + cardH / 2;
                    int y1c  = cardY(r, 2 * m + 1) + cardH / 2;
                    int ymc  = (y0c + y1c) / 2;

                    // Base lines
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.setColor(new Color(0x16, 0x1C, 0x32));
                    g2.drawLine(x0, y0c, jx, y0c);
                    g2.drawLine(x0, y1c, jx, y1c);
                    g2.drawLine(jx, y0c, jx, y1c);
                    g2.drawLine(jx, ymc, x1, ymc);

                    // Winner-advance highlight
                    g2.setStroke(new BasicStroke(1.4f));
                    if (s0 != null && s0.winner != null) {
                        g2.setColor(new Color(0x2E, 0xE8, 0x8A, 80));
                        g2.drawLine(x0, y0c, jx, y0c);
                    }
                    if (s1 != null && s1.winner != null) {
                        g2.setColor(new Color(0x2E, 0xE8, 0x8A, 80));
                        g2.drawLine(x0, y1c, jx, y1c);
                    }
                    if (both) {
                        g2.setColor(new Color(0x2E, 0xE8, 0x8A, 60));
                        g2.drawLine(jx, y0c, jx, y1c);
                        g2.drawLine(jx, ymc, x1, ymc);
                    }
                }
            }
        }

        // ── Match card ───────────────────────────────────────────────────────
        private void drawCard(Graphics2D g2, int x, int y, BracketSlot slot) {
            int cw = cardW, ch = cardH, rowH = ch / 2;

            if (slot == null || (slot.home == null && slot.away == null)) {
                // Dashed empty slot
                g2.setColor(new Color(0x09, 0x0C, 0x1A));
                g2.fillRoundRect(x, y, cw, ch, 5, 5);
                g2.setColor(new Color(0x16, 0x1C, 0x32));
                g2.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{3f, 3f}, 0f));
                g2.drawRoundRect(x, y, cw - 1, ch - 1, 5, 5);
                g2.setFont(F_TINY);
                g2.setColor(new Color(0x28, 0x30, 0x50));
                String tbd = "TBD";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(tbd, x + (cw - fm.stringWidth(tbd)) / 2, y + ch / 2 + fm.getAscent() / 2 - 1);
                return;
            }

            // Card fill
            g2.setColor(BG_CARD2);
            g2.fillRoundRect(x, y, cw, ch, 5, 5);

            boolean played = slot.played();
            Team[]    teams = {slot.home, slot.away};
            Integer[] goals = {slot.hg,   slot.ag};

            for (int i = 0; i < 2; i++) {
                Team t = teams[i];
                if (t == null) continue;
                int ry = y + i * rowH;
                boolean win = played && t == slot.winner;

                // Winner row tint
                if (win) {
                    g2.setColor(new Color(GREEN.getRed(), GREEN.getGreen(), GREEN.getBlue(), 24));
                    g2.fillRect(x + 1, ry + (i == 0 ? 1 : 0), cw - 2, rowH - 1);
                }

                // Confederation stripe
                Color cc = confColor(t.confederation);
                g2.setColor(new Color(cc.getRed(), cc.getGreen(), cc.getBlue(), win ? 230 : 130));
                g2.fillRect(x + 1, ry + (i == 0 ? 1 : 0), 3, rowH - (i == 0 ? 1 : 1));

                // Team name — max chars based on card width
                int maxChars = Math.max(6, cw / 9);
                String nm = t.name.length() > maxChars ? t.name.substring(0, maxChars - 1) + "." : t.name;
                g2.setFont(win ? F_LABEL.deriveFont(10f) : F_TINY);
                g2.setColor(win ? SNOW : MIST);
                g2.drawString(nm, x + 8, ry + rowH / 2 + 4);

                // Score (right-aligned)
                if (goals[i] != null) {
                    String sc = String.valueOf(goals[i]);
                    g2.setFont(F_LABEL.deriveFont(10f));
                    g2.setColor(win ? GREEN : SLATE);
                    FontMetrics sfm = g2.getFontMetrics();
                    g2.drawString(sc, x + cw - sfm.stringWidth(sc) - 6, ry + rowH / 2 + 4);
                }
            }

            // Row divider
            g2.setColor(RULE);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawLine(x + 4, y + rowH, x + cw - 4, y + rowH);

            // Card border
            g2.setColor(played ? new Color(0x20, 0x2E, 0x52) : new Color(0x16, 0x1C, 0x36));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, cw - 1, ch - 1, 5, 5);

            // Pen tag
            if (played && slot.pens) {
                g2.setFont(F_TINY.deriveFont(8f));
                g2.setColor(AMBER);
                g2.drawString("P", x + cw - 10, y + ch - 2);
            }
        }

        // ── Champion banner ──────────────────────────────────────────────────
        private void drawChampionBanner(Graphics2D g2) {
            if (champion == null || champAlpha <= 0) return;

            // Banner spans col 3–4, sits above the Final card
            int bx = colX(3);
            int bw = colX(4) + cardW - bx;
            int bh = Math.min(80, cardY(4, 0) - padT - 10);
            int by = padT + 4;

            if (bh < 30) return; // not enough space

            // Outer glow
            for (int i = 5; i >= 1; i--) {
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(),
                    (int)(champAlpha * 18 * (1 - i / 6f))));
                g2.fillRoundRect(bx - i, by - i, bw + i * 2, bh + i * 2, 12, 12);
            }

            // Background
            g2.setPaint(new GradientPaint(bx, by, new Color(0x18, 0x10, 0x00),
                bx, by + bh, new Color(0x0C, 0x07, 0x00)));
            g2.fillRoundRect(bx, by, bw, bh, 8, 8);

            // Gold border
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(),
                (int)(champAlpha * 210)));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(bx, by, bw - 1, bh - 1, 8, 8);

            int alpha = (int)(champAlpha * 255);

            // Title line
            g2.setFont(F_TINY);
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), alpha));
            String lbl = "FIFA WORLD CUP 2026  ·  CHAMPION";
            FontMetrics lfm = g2.getFontMetrics();
            g2.drawString(lbl, bx + (bw - lfm.stringWidth(lbl)) / 2, by + 16);

            // Stars
            g2.setFont(new Font("Dialog", Font.PLAIN, 12));
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(),
                Math.max(0, alpha - 40)));
            String stars = "★ ★ ★ ★ ★";
            FontMetrics sfm = g2.getFontMetrics();
            if (bh >= 52)
                g2.drawString(stars, bx + (bw - sfm.stringWidth(stars)) / 2, by + 34);

            // Champion name
            float nameSz = Math.min(20f, Math.max(13f, bh * 0.28f));
            g2.setFont(F_HERO.deriveFont(Font.BOLD, nameSz));
            g2.setPaint(new GradientPaint(bx, by, GOLD_HOT, bx + bw, by + bh, GOLD));
            FontMetrics nfm = g2.getFontMetrics();
            g2.drawString(champion.name,
                bx + (bw - nfm.stringWidth(champion.name)) / 2,
                by + bh - 10);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LOG PANEL  (styled terminal view)
    // ═══════════════════════════════════════════════════════════════════════════
    private JScrollPane buildLogPane() {
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(BG_VOID);
        logPane.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        logDoc = logPane.getStyledDocument();
        Style base = logDoc.addStyle("base",
            StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setFontFamily(base, F_MONO.getFamily());
        StyleConstants.setFontSize(base, 13);
        StyleConstants.setForeground(base, SNOW);
        StyleConstants.setBackground(base, BG_VOID);

        JScrollPane scroll = new JScrollPane(logPane);
        scroll.setBackground(BG_VOID);
        scroll.getViewport().setBackground(BG_VOID);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, RULE));
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(thinScrollBar());
        return scroll;
    }

    private void appendLog(String raw) {
        String s = raw.trim();
        if (s.contains("CHAMPION") || s.contains("finishes 3rd"))
            { logInsert(raw, GOLD,            17, true,  true);  return; }
        if (s.startsWith("+==") || s.startsWith("|  FIFA") || s.startsWith("|  Factors") || s.startsWith("|  United"))
            { logInsert(raw, GOLD_DIM,         13, false, false); return; }
        if ((s.startsWith("=") && s.length() > 20) || s.contains("THE FINAL") || s.contains("QUALIFIERS") || s.equals("GROUP STAGE") || s.equals("KNOCKOUT STAGE"))
            { logInsert(raw, GOLD,             13, true,  false); return; }
        if (s.startsWith("--") && (s.contains("ROUND") || s.contains("QUARTER") || s.contains("SEMI") || s.contains("FINAL") || s.contains("PLAY-OFF")))
            { logInsert(raw, AMBER,            13, true,  false); return; }
        if (s.startsWith("GROUP ") && s.length() <= 8)
            { logInsert(raw, CYAN,             13, true,  false); return; }
        if (s.startsWith("[#") || s.startsWith("H2H:") || s.startsWith("Win probability"))
            { logInsert(raw, SLATE,            12, false, false); return; }
        if (s.startsWith("Result:") || s.contains("->") || s.contains("advances"))
            { logInsert(raw, GREEN,            13, false, false); return; }
        if (s.matches(".*\\d+\\s*-\\s*\\d+.*"))
            { logInsert(raw, SNOW,             13, false, false); return; }
        if (s.startsWith("#") || (s.contains("Pts") && s.contains("GF")))
            { logInsert(raw, new Color(0x88, 0x92, 0xB4), 13, false, false); return; }
        logInsert(raw, new Color(0x70, 0x7A, 0x9E), 13, false, false);
    }

    private void logInsert(String text, Color color, int size, boolean bold, boolean large) {
        try {
            SimpleAttributeSet a = new SimpleAttributeSet();
            StyleConstants.setForeground(a, color);
            StyleConstants.setBackground(a, BG_VOID);
            StyleConstants.setFontFamily(a, F_MONO.getFamily());
            StyleConstants.setFontSize(a, large ? size + 1 : size);
            StyleConstants.setBold(a, bold);
            if (large) StyleConstants.setSpaceAbove(a, 2f);
            logDoc.insertString(logDoc.getLength(), text, a);
        } catch (BadLocationException ignored) {}
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  FOOTER
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(BG_RAISED);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(0xFF, 0xBB, 0x00, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, 0, w, 0);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(0, 70));
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        p.add(buildStatus(), BorderLayout.WEST);
        p.add(buildButtonRow(), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStatus() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        statusDot = new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(9, 9); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(),
                    (int)(pulseAlpha * 255)));
                g2.fillOval(0, 0, 8, 8);
                g2.dispose();
            }
        };
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(F_SMALL);
        statusLabel.setForeground(SLATE);
        row.add(statusDot);
        row.add(statusLabel);
        return row;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 14));
        row.setOpaque(false);

        runBtn = goldButton("▶  RUN SIMULATION");
        JButton clearBtn = ghostButton("CLEAR");
        JButton logBtn   = ghostButton("VIEW LOG");

        runBtn.addActionListener(e -> runSimulation());
        clearBtn.addActionListener(e -> resetState());
        logBtn.addActionListener(e -> navigate("log"));

        row.add(runBtn);
        row.add(clearBtn);
        row.add(logBtn);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SIMULATION LISTENER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════════════════
    @Override public void onGroupComplete(Group group) {
        SwingUtilities.invokeLater(() -> {
            completedGroups.put(group.name, group);
            groupsPanel.updateGroup(group);
        });
    }

    @Override public void onKnockoutBracketReady(List<Team> bracket) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 16; i++)
                rounds[0][i] = new BracketSlot(bracket.get(i * 2), bracket.get(i * 2 + 1));
            navigate("bracket");
            bracketPanel.repaint();
            setStatus("Knockout bracket set — Round of 32 begins", CYAN, true);
        });
    }

    @Override public void onKnockoutMatch(String round, Match match, Team winner, boolean penalties) {
        SwingUtilities.invokeLater(() -> {
            if ("THIRD-PLACE PLAY-OFF".equals(round)) {
                thirdSlot = new BracketSlot(match.home, match.away);
                thirdSlot.hg = match.homeGoals;
                thirdSlot.ag = match.awayGoals;
                thirdSlot.winner = winner;
                thirdSlot.pens = penalties;
            } else {
                int ri = roundIndex(round);
                if (ri < 0 || ri >= 5) return;
                int mi = roundCount[ri];
                if (mi >= rounds[ri].length) return;

                BracketSlot slot = rounds[ri][mi];
                if (slot == null) {
                    slot = new BracketSlot(match.home, match.away);
                    rounds[ri][mi] = slot;
                }
                slot.hg = match.homeGoals;
                slot.ag = match.awayGoals;
                slot.winner = winner;
                slot.pens = penalties;
                roundCount[ri]++;

                // Pre-populate next round when both feeders are done
                if (ri < 4) {
                    int pairIdx = roundCount[ri] - 1; // zero-based index of just-completed match
                    int nextMi  = pairIdx / 2;
                    if (pairIdx % 2 == 1 && rounds[ri + 1][nextMi] == null) {
                        BracketSlot s0 = rounds[ri][pairIdx - 1];
                        BracketSlot s1 = rounds[ri][pairIdx];
                        if (s0 != null && s0.winner != null && s1 != null && s1.winner != null)
                            rounds[ri + 1][nextMi] = new BracketSlot(s0.winner, s1.winner);
                    }
                }
            }
            bracketPanel.repaint();
        });
    }

    @Override public void onChampion(Team champ, Team runner, Team third) {
        SwingUtilities.invokeLater(() -> {
            champion = champ; runnerUp = runner; thirdTeam = third;
            setStatus("CHAMPION: " + champ.name, GOLD, false);
            runBtn.setEnabled(true);
            runBtn.setText("▶  RUN AGAIN");
            bracketPanel.repaint();
            startChampionAnim();
        });
    }

    private int roundIndex(String round) {
        return switch (round) {
            case "ROUND OF 32"   -> 0;
            case "ROUND OF 16"   -> 1;
            case "QUARTER-FINALS"-> 2;
            case "SEMI-FINALS"   -> 3;
            case "THE FINAL"     -> 4;
            default              -> -1;
        };
    }

    private void startChampionAnim() {
        if (champTimer != null) champTimer.stop();
        champAlpha = 0f;
        champTimer = new javax.swing.Timer(30, null);
        champTimer.addActionListener(e -> {
            champAlpha += 0.04f;
            if (champAlpha >= 1f) { champAlpha = 1f; champTimer.stop(); }
            bracketPanel.repaint();
        });
        champTimer.start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SIMULATION RUNNER
    // ═══════════════════════════════════════════════════════════════════════════
    private void runSimulation() {
        resetState();
        runBtn.setEnabled(false);
        runBtn.setText("⏳  SIMULATING...");
        setStatus("Loading tournament data...", GOLD, true);
        navigate("groups");

        PrintStream logStream = new PrintStream(new OutputStream() {
            private final StringBuilder buf = new StringBuilder();
            @Override public void write(int b) {
                char c = (char) b;
                buf.append(c);
                if (c == '\n') {
                    final String line = buf.toString();
                    buf.setLength(0);
                    SwingUtilities.invokeLater(() -> {
                        appendLog(line);
                        logPane.setCaretPosition(logDoc.getLength());
                    });
                }
            }
            @Override public void write(byte[] b, int off, int len) {
                for (char c : new String(b, off, len).toCharArray()) write(c);
            }
        }, true);

        PrintStream original = System.out;
        System.setOut(logStream);

        new Thread(() -> {
            try {
                Tournament t = new Tournament();
                t.setListener(SimulatorWindow.this);
                t.simulate();
                logStream.flush();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    setStatus("Error: " + ex.getMessage(), RED_MED, false));
            } finally {
                System.setOut(original);
                SwingUtilities.invokeLater(() -> {
                    if (champion == null) {
                        runBtn.setEnabled(true);
                        runBtn.setText("▶  RUN SIMULATION");
                        if (statusLabel.getText().startsWith("Loading"))
                            setStatus("Ready", SLATE, false);
                    }
                });
            }
        }, "sim").start();
    }

    private void resetState() {
        completedGroups.clear();
        for (int r = 0; r < 5; r++) {
            rounds[r] = new BracketSlot[switch (r) {
                case 0 -> 16; case 1 -> 8; case 2 -> 4; case 3 -> 2; default -> 1;
            }];
            roundCount[r] = 0;
        }
        thirdSlot = null; champion = null; runnerUp = null; thirdTeam = null;
        champAlpha = 0f;
        if (champTimer != null) champTimer.stop();
        try { logDoc.remove(0, logDoc.getLength()); } catch (BadLocationException ignored) {}
        groupsPanel.repaint();
        bracketPanel.repaint();
        setStatus("Ready", SLATE, false);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  BUTTON FACTORIES
    // ═══════════════════════════════════════════════════════════════════════════
    private JButton goldButton(String text) {
        JButton btn = new JButton(text) {
            boolean hot = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hot = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hot = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                int w = getWidth(), h = getHeight();
                if (!isEnabled()) {
                    g2.setColor(new Color(0x1C, 0x22, 0x3A));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                    g2.setColor(SLATE);
                } else {
                    Color top = hot ? GOLD_HOT : GOLD;
                    Color bot = hot ? GOLD : new Color(0xCC, 0x90, 0x00);
                    g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                    g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,65), 0, h/2, new Color(255,255,255,0)));
                    g2.fillRoundRect(1, 1, w-2, h/2, 7, 7);
                    g2.setColor(BG_VOID);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (w - fm.stringWidth(getText()))/2,
                    (h + fm.getAscent() - fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(F_LABEL.deriveFont(13f));
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            boolean hot = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hot = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hot = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                int w = getWidth(), h = getHeight();
                g2.setColor(hot ? new Color(0x1C, 0x24, 0x42) : BG_RAISED);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(new Color(0x2C, 0x38, 0x60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w-1, h-1, 8, 8);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(hot ? SNOW : SLATE);
                g2.drawString(getText(), (w - fm.stringWidth(getText()))/2,
                    (h + fm.getAscent() - fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(F_LABEL.deriveFont(12f));
        btn.setPreferredSize(new Dimension(96, 42));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  STATUS
    // ═══════════════════════════════════════════════════════════════════════════
    private void setStatus(String text, Color color, boolean animate) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
        dotColor = color;
        if (animate) pulseTimer.start();
        else { pulseTimer.stop(); pulseAlpha = 1f; }
        if (statusDot != null) statusDot.repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════════
    private static JPanel centeredLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false); p.add(l);
        return p;
    }

    private static Color blend(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            (int)(a.getRed()   + t * (b.getRed()   - a.getRed())),
            (int)(a.getGreen() + t * (b.getGreen() - a.getGreen())),
            (int)(a.getBlue()  + t * (b.getBlue()  - a.getBlue()))
        );
    }

    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    private static BasicScrollBarUI thinScrollBar() {
        return new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x24, 0x2E, 0x52);
                trackColor = BG_VOID;
            }
            @Override protected JButton createDecreaseButton(int o) { return invisible(); }
            @Override protected JButton createIncreaseButton(int o) { return invisible(); }
            private JButton invisible() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = aa(g);
                g2.setColor(new Color(0x2E, 0x3C, 0x68));
                g2.fillRoundRect(r.x+2, r.y+3, r.width-4, r.height-6, 8, 8);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(BG_VOID); g.fillRect(r.x, r.y, r.width, r.height);
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═══════════════════════════════════════════════════════════════════════════
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new SimulatorWindow().setVisible(true);
        });
    }
}