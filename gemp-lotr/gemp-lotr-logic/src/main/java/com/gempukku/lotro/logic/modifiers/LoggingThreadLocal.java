package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class LoggingThreadLocal {
    private static final Logger LOG = LogManager.getLogger(LoggingThreadLocal.class);
//    private static ThreadLocal<StringBuilder> _logLocal = new ThreadLocal<StringBuilder>();
//    private static ThreadLocal<Long> _start = new ThreadLocal<Long>();
//    private static ThreadLocal<Integer> _level = new ThreadLocal<Integer>();
//
//    private static FileOutputStream _fos = null;

    private static synchronized void initializeOutputStream() {
//        if (_fos == null) {
//            try {
//                _fos = new FileOutputStream(new File(ApplicationRoot.getRoot(), "longActions.txt"));
//            } catch (IOException exp) {
//                throw new RuntimeException(exp);
//            }
//        }
    }

    public static void start() {
//        if (_fos == null)
//            initializeOutputStream();
//
//        _logLocal.set(new StringBuilder());
//        _start.set(System.currentTimeMillis());
//        _level.set(0);
    }

    public static void logMethodStart(PhysicalCard card, String desc) {
//        StringBuilder builder = _logLocal.get();
//        if (builder != null) {
//            int level = _level.get();
//            for (int i = 0; i < level; i++)
//                builder.append("\t");
//            builder.append(desc);
//            if (card != null)
//                builder.append(" ").append(card.getBlueprintId());
//            builder.append("\r\n");
//            _level.set(level + 1);
//        }
    }

    public static void logMethodEnd() {
//        StringBuilder builder = _logLocal.get();
//        if (builder != null) {
//            _level.set(_level.get() - 1);
//        }
    }

    public static void stop(boolean writeOperation) {
//        long start = _start.get();
//        long time = System.currentTimeMillis() - start;
//        if (time > 100) {
//            String str = "Processing took (" + writeOperation + ") " + time + "ms: \n" + _logLocal.get().toString();
//            try {
//                synchronized (_fos) {
//                    _fos.write(str.getBytes());
//                    _fos.flush();
//                }
//            } catch (IOException exp) {
//                throw new RuntimeException(exp);
//            }
//        }
//        _start.set(null);
//        _logLocal.set(null);
//        _level.set(null);
    }
}
