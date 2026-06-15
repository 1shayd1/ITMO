package org.shayd1.gui.localization;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {

    public static final Locale RU    = new Locale("ru");
    public static final Locale CS    = new Locale("cs");
    public static final Locale IT    = new Locale("it");
    public static final Locale ES_DO = new Locale("es", "DO");

    public static final List<Locale> SUPPORTED = List.of(RU, CS, IT, ES_DO);

    private static final String BUNDLE_BASE = "org/shayd1/gui/localization/messages";

    private static final LocaleManager INSTANCE = new LocaleManager();

    private final ObjectProperty<Locale> currentLocale =
            new SimpleObjectProperty<>(RU);

    private ResourceBundle bundle;

    private LocaleManager() {
        setLocale(RU);
    }

    public static LocaleManager getInstance() { return INSTANCE; }

    public void setLocale(Locale locale) {
        currentLocale.set(locale);
        bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale);
    }

    public String get(String key) {
        try { return bundle.getString(key); }
        catch (Exception e) { return "?" + key + "?"; }
    }

    public Locale getCurrent()                        { return currentLocale.get(); }
    public ObjectProperty<Locale> localeProperty()    { return currentLocale; }

    public NumberFormat numberFormat() {
        return NumberFormat.getNumberInstance(currentLocale.get());
    }

    public DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(currentLocale.get());
    }

    public static String displayName(Locale locale) {
        if (locale.equals(RU))    return "Русский";
        if (locale.equals(CS))    return "Čeština";
        if (locale.equals(IT))    return "Italiano";
        if (locale.equals(ES_DO)) return "Español (DO)";
        return locale.toString();
    }
}