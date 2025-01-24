package com.example.massageapplication.language;

public class LanguageModel {

    private String languageName;
    private String xmlLanguageName;
    private String nativeName;
    private int flagIcon;

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public int getFlagIcon() {
        return flagIcon;
    }

    public void setFlagIcon(int flagIcon) {
        this.flagIcon = flagIcon;
    }

    public String getXmlLanguageName() {
        return xmlLanguageName;
    }

    public void setXmlLanguageName(String xmlLanguageName) {
        this.xmlLanguageName = xmlLanguageName;
    }

    public LanguageModel(String xmlLanguageName, String languageName, String nativeName, int flagIcon) {
        this.languageName = languageName;
        this.nativeName = nativeName;
        this.flagIcon = flagIcon;
        this.xmlLanguageName = xmlLanguageName;
    }
}
