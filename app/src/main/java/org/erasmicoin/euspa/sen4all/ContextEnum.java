package org.erasmicoin.euspa.sen4all;

public enum ContextEnum {
    ATMOSPHERE(R.id.contextTextAtmo, R.id.contextIconAtmo, R.id.atmosphereContext, R.drawable.icons_atmo_map, "Atmosphere"),
    LAND(R.id.contextTextLand, R.id.contextIconLand, R.id.landContext, R.drawable.icons_land_map, "Land"),
    MARINE(R.id.contextTextMarine, R.id.contextIconMarine, R.id.marineContext, R.drawable.icons_marine_map, "Marine");

    final int textId;
    final int iconId;
    final int includeId;
    final int icon;
    final String name;

    ContextEnum(final int text, final int iconId, final int includeId, final int icon, final String name) {
        this.textId = text;
        this.iconId = iconId;
        this.includeId = includeId;
        this.icon = icon;
        this.name = name;
    }

    public String getName(){ return name;}

    public int getTextId(){
        return textId;
    }

    public int getIconId(){
        return iconId;
    }

    public int getIcon(){ return icon; }

    public int getIncludeId(){
        return includeId;
    }

    public static ContextEnum fromInteger(int x) {
        switch(x) {
            case 0:
                return ATMOSPHERE;
            case 1:
                return LAND;
            case 2:
                return MARINE;
        }
        return null;
    }

    public static int toInteger(ContextEnum x){
        if (x.equals(ATMOSPHERE)) {
            return 0;
        }else if(x.equals(LAND)){
            return 1;
        }else if(x.equals(MARINE)){
            return 2;
        }else{
            return 99;
        }
    }

}
