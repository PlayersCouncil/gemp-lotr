package com.gempukku.lotro.game;

import com.gempukku.lotro.common.DBDefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Player {

    public enum Type {
        ADMIN("a"),
        //wow this is such a better idea than a blank column
        //DEACTIVATED("d"),
        LEAGUE_ADMIN("l"),
        PLAY_TESTER("p"),
        //PLAY_TESTING_ADMIN("t"),
        COMMENTATOR("c"),
        //COMMENTATOR_ADMIN("m"),
        UNBANNED("n"),
        USER("u"),
        BOT("b");

        private final String _value;

        Type(String value) {
            _value = value;
        }

        public String getValue() {
            return _value;
        }

        public String toString() {
            return getValue();
        }

        public static List<Type> getTypes(String typeString) {
            List<Type> types = new ArrayList<>();
            for (Type type : values()) {
                if (typeString.contains(type.getValue())) {
                    types.add(type);
                }
            }
            return types;
        }

        public static String getTypeString(List<Type> types) {
            StringBuilder sb = new StringBuilder();
            for (Type type : values()) {
                if (types.contains(type)) {
                    sb.append(type.getValue());
                }
            }
            return sb.toString();
        }
    }

    private final int _id;
    private final String _name;
    private final String _password;
    private final String _type;
    private Integer _lastLoginReward;
    private final Date _bannedUntil;
    private final String _createIp;
    private final String _lastIp;
    private boolean _hasTrophies;

    public Player(DBDefs.Player def, boolean hasTrophies) {
        this(def.id, def.name, def.password, def.type, def.last_login_reward, def.GetBannedUntilDate(), def.create_ip, def.last_ip, hasTrophies);
    }

    public Player(int id, String name, String password, String type, Integer lastLoginReward,
            Date bannedUntil, String createIp, String lastIp, boolean hasTrophies) {
        _id = id;
        _name = name;
        _password = password;
        _type = type;
        _lastLoginReward = lastLoginReward;
        _bannedUntil = bannedUntil;
        _createIp = createIp;
        _lastIp = lastIp;
        _hasTrophies = hasTrophies;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getPassword() {
        return _password;
    }

    public String getType() {
        return _type;
    }

    public boolean hasType(Type type) {
        return Type.getTypes(_type).contains(type);
    }

    public Integer getLastLoginReward() {
        return _lastLoginReward;
    }

    public void setLastLoginReward(int lastLoginReward) {
        _lastLoginReward = lastLoginReward;
    }

    public Date getBannedUntil() {
        return _bannedUntil;
    }

    public String getCreateIp() {
        return _createIp;
    }

    public String getLastIp() {
        return _lastIp;
    }

    public boolean hasTrophies() { return _hasTrophies; }
    public void setHasTrophies(boolean value) { _hasTrophies = value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (!Objects.equals(_name, player._name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _name != null ? _name.hashCode() : 0;
    }

    public PlayerInfo GetUserInfo() {
        return new PlayerInfo(_name, _type, _hasTrophies);
    }

    public class PlayerInfo {
        public String name;
        public String type;
        public boolean hasTrophies;

        public PlayerInfo(String name, String info, boolean trophies) {
            this.name = name;
            type = info;
            hasTrophies = trophies;
        }
    }
}
