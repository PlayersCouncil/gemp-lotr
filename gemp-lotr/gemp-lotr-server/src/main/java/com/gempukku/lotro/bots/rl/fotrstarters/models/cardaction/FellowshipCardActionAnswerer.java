package com.gempukku.lotro.bots.rl.fotrstarters.models.cardaction;

public class FellowshipCardActionAnswerer extends AbstractCardActionAnswerer {
    private static final String TRIGGER = "Play Fellowship action or Pass";
    private final FellowshipPlayCardTrainer playTrainer = new FellowshipPlayCardTrainer();
    private final FellowshipUseCardTrainer useTrainer = new FellowshipUseCardTrainer();
    private final FellowshipHealTrainer healTrainer = new FellowshipHealTrainer();
    private final FellowshipTransferTrainer transferTrainer = new FellowshipTransferTrainer();

    @Override
    protected String getTextTrigger() {
        return TRIGGER;
    }

    @Override
    protected AbstractPlayUseCardTrainer getPlayTrainer() {
        return playTrainer;
    }

    @Override
    protected AbstractPlayUseCardTrainer getUseTrainer() {
        return useTrainer;
    }

    @Override
    protected AbstractPlayUseCardTrainer getHealTrainer() {
        return healTrainer;
    }

    @Override
    protected AbstractPlayUseCardTrainer getTransferTrainer() {
        return transferTrainer;
    }

    public static class FellowshipPlayCardTrainer extends AbstractPlayUseCardTrainer {

        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean isPlayTrainer() {
            return true;
        }

        @Override
        protected boolean isUseTrainer() {
            return false;
        }

        @Override
        protected boolean isTransferTrainer() {
            return false;
        }

        @Override
        protected boolean isHealTrainer() {
            return false;
        }
    }

    public static class FellowshipUseCardTrainer extends AbstractPlayUseCardTrainer {

        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean isPlayTrainer() {
            return false;
        }

        @Override
        protected boolean isUseTrainer() {
            return true;
        }

        @Override
        protected boolean isTransferTrainer() {
            return false;
        }

        @Override
        protected boolean isHealTrainer() {
            return false;
        }
    }

    public static class FellowshipHealTrainer extends AbstractPlayUseCardTrainer {

        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean isPlayTrainer() {
            return false;
        }

        @Override
        protected boolean isUseTrainer() {
            return false;
        }

        @Override
        protected boolean isTransferTrainer() {
            return true;
        }

        @Override
        protected boolean isHealTrainer() {
            return false;
        }
    }

    public static class FellowshipTransferTrainer extends AbstractPlayUseCardTrainer {

        @Override
        protected String getTextTrigger() {
            return TRIGGER;
        }

        @Override
        protected boolean isPlayTrainer() {
            return false;
        }

        @Override
        protected boolean isUseTrainer() {
            return false;
        }

        @Override
        protected boolean isTransferTrainer() {
            return false;
        }

        @Override
        protected boolean isHealTrainer() {
            return true;
        }
    }
}
