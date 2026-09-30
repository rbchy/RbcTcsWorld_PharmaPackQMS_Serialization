package com.pharmapack.qms.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Collections;

/**
 * Desktop edition only: when the server is ready, opens the UI in the default browser and adds a
 * system-tray icon (Open / Quit), so a non-technical user can run the app like any desktop program.
 * Other devices on the same network (phones, tablets, PCs) can use the printed LAN address.
 */
@Component
@Profile("desktop")
public class DesktopLauncher {
    private static final Logger log = LoggerFactory.getLogger(DesktopLauncher.class);

    private final ApplicationContext context;
    @Value("${server.port:8080}") private int port;
    @Value("${app.desktop.open-browser:true}") private boolean openBrowser;

    public DesktopLauncher(ApplicationContext context) { this.context = context; }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String local = "http://localhost:" + port + "/";
        String lan = lanAddress();
        log.info("==============================================================");
        log.info(" PharmaPack QMS is running");
        log.info("   This computer : {}", local);
        if (lan != null) log.info("   Other devices : http://{}:{}/  (same Wi-Fi / network)", lan, port);
        log.info("   Login         : admin / admin123");
        log.info("   Close this window (or tray icon > Quit) to stop");
        log.info("==============================================================");

        if (GraphicsEnvironment.isHeadless()) return;
        if (openBrowser) open(local);
        addTrayIcon(local, lan);
    }

    private void addTrayIcon(String local, String lan) {
        try {
            if (!SystemTray.isSupported()) return;
            PopupMenu menu = new PopupMenu();
            MenuItem openItem = new MenuItem("Open PharmaPack QMS");
            openItem.addActionListener(e -> open(local));
            menu.add(openItem);
            if (lan != null) {
                MenuItem lanItem = new MenuItem("Other devices: http://" + lan + ":" + port + "/");
                lanItem.setEnabled(false);
                menu.add(lanItem);
            }
            menu.addSeparator();
            MenuItem quit = new MenuItem("Quit");
            quit.addActionListener(e -> System.exit(SpringApplication.exit(context, () -> 0)));
            menu.add(quit);

            TrayIcon icon = new TrayIcon(iconImage(), "PharmaPack QMS", menu);
            icon.setImageAutoSize(true);
            icon.addActionListener(e -> open(local));
            SystemTray.getSystemTray().add(icon);
        } catch (Exception | Error e) {
            log.info("Tray icon not available on this system ({}). The app is still running.", e.getMessage());
        }
    }

    private static void open(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
            String os = System.getProperty("os.name").toLowerCase();
            String[] cmd = os.contains("win") ? new String[]{"rundll32", "url.dll,FileProtocolHandler", url}
                    : os.contains("mac") ? new String[]{"open", url} : new String[]{"xdg-open", url};
            new ProcessBuilder(cmd).start();
        } catch (Exception e) {
            log.info("Open {} in your browser.", url);
        }
    }

    /** First site-local IPv4 address (e.g. 192.168.1.20), used by phones/other PCs on the same network. */
    private static String lanAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (var addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    private static Image iconImage() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x1F, 0x3A, 0x5F));
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.drawString("Q", 9, 24);
        g.dispose();
        return img;
    }
}
