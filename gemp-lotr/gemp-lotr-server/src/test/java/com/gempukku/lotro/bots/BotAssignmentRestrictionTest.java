package com.gempukku.lotro.bots;

import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStarterBot;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrStartersRLGameStateFeatures;
import com.gempukku.lotro.bots.rl.fotrstarters.models.ModelRegistry;
import com.gempukku.lotro.bots.rl.fotrstarters.models.assignment.FpAssignmentTrainer;
import com.gempukku.lotro.bots.rl.semanticaction.AssignMinionsAction;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.BeforeClass;
import org.junit.Test;
import smile.classification.SoftClassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Issue #1097: the Shadow player used Uruk Guard's assignment ability ("Exert this minion and spot a companion to
 * prevent the opponent from assigning that companion to this minion") on Aragorn; the bot, as Free Peoples, then
 * answered the ASSIGN_MINIONS decision with Aragorn vs Uruk Guard, the engine rejected it, and the mediator conceded
 * the game for the bot ("~bot lost due to: Invalid decision").
 *
 * The bot is P1 (Free Peoples at site 1) throughout.
 */
public class BotAssignmentRestrictionTest {

    private static class Scenario extends VirtualTableScenario {
        Scenario(HashMap<String, String> cards) throws CardNotFoundException, DecisionResultInvalidException {
            super(cards, FellowshipSites, FOTRFrodo, RulingRing);
        }

        static void initBotCardFeatures() {
            CardFeatures.init(_cardLibrary);
        }
    }

    @BeforeClass
    public static void initCardFeatures() {
        Scenario.initBotCardFeatures();
    }

    /**
     * Frodo, Aragorn (str 8) and Sam in the fellowship, Uruk Guard (str 9) on the table (and optionally a second
     * one that does NOT use its ability). Shadow uses the first Guard's ability on Aragorn, then everyone passes,
     * leaving the Free Peoples ASSIGN_MINIONS decision pending for P1.
     */
    private static Scenario atFreePeoplesAssignment(boolean twinGuard) throws Exception {
        var scn = new Scenario(new HashMap<>() {{
            put("aragorn", "1_89");
            put("sam", "1_311");
            put("guard", "1_147");
            put("guard2", "1_147");
        }});
        var guard = scn.GetShadowCard("guard");
        scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));
        if (twinGuard)
            scn.MoveMinionsToTable(guard, scn.GetShadowCard("guard2"));
        else
            scn.MoveMinionsToTable(guard);

        scn.StartGame();
        scn.SkipToPhase(Phase.ASSIGNMENT);

        scn.FreepsPassCurrentPhaseAction();
        assertTrue(scn.ShadowActionAvailable(guard));
        scn.ShadowUseCardAction(guard);
        scn.ShadowChooseCard(scn.GetFreepsCard("aragorn"));
        assertEquals(1, scn.GetWoundsOn(guard));
        scn.PassCurrentPhaseActions();

        AwaitingDecision decision = scn.FreepsGetAwaitingDecision();
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, decision.getDecisionType());
        return scn;
    }

    private static String pair(PhysicalCardImpl fp, PhysicalCardImpl... minions) {
        StringBuilder sb = new StringBuilder().append(fp.getCardId());
        for (PhysicalCardImpl minion : minions)
            sb.append(' ').append(minion.getCardId());
        return sb.toString();
    }

    private static boolean pairs(String answer, PhysicalCardImpl fp, PhysicalCardImpl minion) {
        List<String> minions = AssignmentLegality.parse(answer).get(String.valueOf(fp.getCardId()));
        return minions != null && minions.contains(String.valueOf(minion.getCardId()));
    }

    private static boolean engineAccepts(Scenario scn, String answer) {
        try {
            scn.PlayerDecided(VirtualTableScenario.P1, answer);
            return true;
        } catch (RuntimeException exp) {
            if (exp.getCause() instanceof DecisionResultInvalidException)
                return false;
            throw exp;
        }
    }

    /**
     * A stand-in for the trained FpAssignmentTrainer model with the same taste the real one showed in #1097: put the
     * strongest minion on the strongest companion. Score = sum over the companion/ally slots of the assignment
     * features of (character strength x assigned minion strength).
     */
    private static ModelRegistry strongestOnStrongestModel() {
        SoftClassifier<double[]> model = new SoftClassifier<>() {
            @Override
            public int predict(double[] x, double[] posteriori) {
                int slots = 9 + 4, perSlot = 8;
                int start = x.length - 2 - slots * perSlot;
                double score = 0;
                for (int slot = 0; slot < slots; slot++)
                    score += x[start + slot * perSlot + 3] * x[start + slot * perSlot + 7];
                posteriori[1] = score / 1000.0;
                posteriori[0] = 1 - posteriori[1];
                return posteriori[1] > posteriori[0] ? 1 : 0;
            }

            @Override
            public int predict(double[] x) {
                return predict(x, new double[2]);
            }
        };
        ModelRegistry registry = new ModelRegistry();
        registry.registerModel(FpAssignmentTrainer.class, model);
        return registry;
    }

    private static FotrStarterBot modelBot(ModelRegistry registry) {
        return new FotrStarterBot(new FotrStartersRLGameStateFeatures(), VirtualTableScenario.P1, registry, null);
    }

    @Test
    public void theBotSeesTheSameRuleTheEngineEnforces() throws Exception {
        var scn = atFreePeoplesAssignment(false);
        var aragorn = scn.GetFreepsCard("aragorn");
        var sam = scn.GetFreepsCard("sam");
        var frodo = scn.GetRingBearer();
        var guard = scn.GetShadowCard("guard");
        var params = scn.FreepsGetAwaitingDecision().getDecisionParameters();
        String[] fps = params.get("freeCharacters");
        String[] minions = params.get("minions");
        // like the replay: all three companions and the Guard are offered; the restriction is only checked on answer
        assertEquals(3, fps.length);

        var legality = new AssignmentLegality(scn.game());
        assertFalse(legality.isLegal(Side.FREE_PEOPLE, fps, minions, AssignmentLegality.parse(pair(aragorn, guard))));
        assertTrue(legality.isLegal(Side.FREE_PEOPLE, fps, minions, AssignmentLegality.parse(pair(sam, guard))));
        assertTrue(legality.isLegal(Side.FREE_PEOPLE, fps, minions, AssignmentLegality.parse(pair(frodo, guard))));
        assertTrue(legality.isLegal(Side.FREE_PEOPLE, fps, minions, Map.of()));
        assertTrue(AssignmentLegality.UNCHECKED.isLegal(Side.FREE_PEOPLE, fps, minions, AssignmentLegality.parse(pair(aragorn, guard))));
        // structural rules: one minion per FP character without Defender, one skirmish per minion, only offered ids
        assertFalse(legality.isLegal(Side.FREE_PEOPLE, fps, minions, AssignmentLegality.parse(pair(sam, guard) + "," + pair(frodo, guard))));
        assertFalse(legality.isLegal(Side.FREE_PEOPLE, fps, minions, AssignmentLegality.parse(pair(guard, sam))));

        assertFalse(engineAccepts(scn, pair(aragorn, guard)));
        assertTrue(engineAccepts(scn, pair(sam, guard)));
        assertTrue(scn.IsCharAssignedAgainst(sam, guard));
    }

    @Test
    public void withoutTheRulesTheModelBotReproducesIssue1097() throws Exception {
        var scn = atFreePeoplesAssignment(false);
        var aragorn = scn.GetFreepsCard("aragorn");
        var guard = scn.GetShadowCard("guard");

        // what the live server did before the fix: the bot never saw the restriction
        String answer = modelBot(strongestOnStrongestModel()).chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision());

        assertTrue(answer, pairs(answer, aragorn, guard));
        assertFalse(engineAccepts(scn, answer));
    }

    @Test
    public void withTheRulesTheModelBotAssignsUrukGuardToSomeoneElse() throws Exception {
        var scn = atFreePeoplesAssignment(false);
        var aragorn = scn.GetFreepsCard("aragorn");
        var guard = scn.GetShadowCard("guard");

        String answer = modelBot(strongestOnStrongestModel())
                .chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision(), new AssignmentLegality(scn.game()));

        assertFalse(answer, pairs(answer, aragorn, guard));
        assertTrue(answer, engineAccepts(scn, answer));
        assertTrue("the model still wants the Guard in a skirmish", scn.IsCharAssigned(guard));
        assertFalse(scn.IsCharAssigned(aragorn));
    }

    @Test
    public void withTwoUrukGuardsTheBotKeepsTheCopyItChose() throws Exception {
        // Only the first Guard forbade Aragorn. Aragorn vs the second Guard is legal and the model's favourite; the
        // bot's answer used to be rebuilt from blueprint ids, which could swap the two identical Guards.
        for (int run = 0; run < 5; run++) {
            var scn = atFreePeoplesAssignment(true);
            var aragorn = scn.GetFreepsCard("aragorn");
            var guard = scn.GetShadowCard("guard");
            var guard2 = scn.GetShadowCard("guard2");

            String answer = modelBot(strongestOnStrongestModel())
                    .chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision(), new AssignmentLegality(scn.game()));

            assertTrue(answer, pairs(answer, aragorn, guard2));
            assertTrue(answer, engineAccepts(scn, answer));
            assertTrue(scn.IsCharAssignedAgainst(aragorn, guard2));
        }
    }

    @Test
    public void randomBotNeverOffersTheForbiddenPairing() throws Exception {
        var scn = atFreePeoplesAssignment(false);
        var aragorn = scn.GetFreepsCard("aragorn");
        var guard = scn.GetShadowCard("guard");
        var bot = new RandomDecisionBot(VirtualTableScenario.P1);
        var legality = new AssignmentLegality(scn.game());

        int unchecked = 0;
        String answer = null;
        for (int i = 0; i < 300; i++) {
            answer = bot.chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision(), legality);
            assertFalse(answer, pairs(answer, aragorn, guard));
            if (pairs(bot.chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision()), aragorn, guard))
                unchecked++;
        }
        assertTrue("without the rules the random bot does pick Aragorn sometimes", unchecked > 0);
        assertTrue(answer, engineAccepts(scn, answer));
    }

    @Test
    public void rejectedAssignmentFallsBackToItsLegalPartInsteadOfConceding() throws Exception {
        var scn = atFreePeoplesAssignment(true);
        var aragorn = scn.GetFreepsCard("aragorn");
        var sam = scn.GetFreepsCard("sam");
        var guard = scn.GetShadowCard("guard");
        var guard2 = scn.GetShadowCard("guard2");
        AwaitingDecision decision = scn.FreepsGetAwaitingDecision();

        // the mediator's botAnswered sequence
        scn.userFeedback().participantDecided(VirtualTableScenario.P1);
        String bad = pair(aragorn, guard) + "," + pair(sam, guard2);
        String accepted = BotDecisionFallback.decide(scn.game(), VirtualTableScenario.P1, decision, bad);
        scn.game().carryOutPendingActionsUntilDecisionNeeded();

        assertEquals(pair(sam, guard2), accepted);
        assertTrue(scn.IsCharAssignedAgainst(sam, guard2));
        assertFalse(scn.IsCharAssigned(aragorn));
        assertFalse(scn.IsCharAssigned(guard)); // left for the Shadow player to assign
        assertFalse(scn.game().isFinished());
    }

    @Test
    public void unusableOrMissingBotAnswerFallsBackToAssigningNothing() throws Exception {
        for (String answer : new String[]{null, "garbage", "999 998"}) {
            var scn = atFreePeoplesAssignment(false);
            AwaitingDecision decision = scn.FreepsGetAwaitingDecision();
            scn.userFeedback().participantDecided(VirtualTableScenario.P1);

            assertEquals("", BotDecisionFallback.decide(scn.game(), VirtualTableScenario.P1, decision, answer));
            scn.game().carryOutPendingActionsUntilDecisionNeeded();
            assertFalse(scn.IsCharAssigned(scn.GetShadowCard("guard")));
            // the Shadow player now gets to assign the Guard
            assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, scn.ShadowGetAwaitingDecision().getDecisionType());
        }
    }

    @Test
    public void rejectedCardActionChoicePasses() throws Exception {
        var scn = new Scenario(new HashMap<>() {{
            put("sam", "1_311");
        }});
        scn.MoveCardsToHand(scn.GetFreepsCard("sam"));
        scn.StartGame();
        AwaitingDecision decision = scn.FreepsGetAwaitingDecision();
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, decision.getDecisionType());
        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

        scn.userFeedback().participantDecided(VirtualTableScenario.P1);
        assertEquals("", BotDecisionFallback.decide(scn.game(), VirtualTableScenario.P1, decision, "not-an-action"));
        scn.game().carryOutPendingActionsUntilDecisionNeeded();
        assertNotEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
    }

    @Test
    public void validAnswerIsUsedAsIs() throws Exception {
        var scn = atFreePeoplesAssignment(false);
        var sam = scn.GetFreepsCard("sam");
        var guard = scn.GetShadowCard("guard");
        AwaitingDecision decision = scn.FreepsGetAwaitingDecision();
        scn.userFeedback().participantDecided(VirtualTableScenario.P1);

        assertEquals(pair(sam, guard), BotDecisionFallback.decide(scn.game(), VirtualTableScenario.P1, decision, pair(sam, guard)));
        scn.game().carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(scn.IsCharAssignedAgainst(sam, guard));
    }

    @Test
    public void assignNothingAnswerDoesNotCrashTheSemanticAction() throws Exception {
        var scn = atFreePeoplesAssignment(false);
        AwaitingDecision decision = scn.FreepsGetAwaitingDecision();
        // FotrStarterBot wraps every answer in an AssignMinionsAction; "" used to throw NumberFormatException there,
        // killing the bot's thread and leaving the game waiting forever
        var action = new AssignMinionsAction("", decision, scn.gameState(), true);
        assertTrue(action.getAssignmentMap().isEmpty());
        assertEquals("", action.toDecisionString(decision, scn.gameState()));
    }

    @Test
    public void shippedModelRespectsTheRestriction() throws Exception {
        File modelFile = new File("../bot-models/FpAssignmentTrainer.model");
        if (!modelFile.exists())
            modelFile = new File("bot-models/FpAssignmentTrainer.model");
        assumeTrue("trained model not found", modelFile.exists());
        SoftClassifier<double[]> model;
        try (var in = new ObjectInputStream(new FileInputStream(modelFile))) {
            //noinspection unchecked
            model = (SoftClassifier<double[]>) in.readObject();
        }
        ModelRegistry registry = new ModelRegistry();
        registry.registerModel(FpAssignmentTrainer.class, model);

        var scn = atFreePeoplesAssignment(false);
        var aragorn = scn.GetFreepsCard("aragorn");
        var guard = scn.GetShadowCard("guard");
        String unchecked = modelBot(registry).chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision());
        String checked = modelBot(registry).chooseAction(scn.gameState(), scn.FreepsGetAwaitingDecision(), new AssignmentLegality(scn.game()));
        System.out.println("#1097 shipped FP assignment model: unchecked=\"" + unchecked + "\" (Aragorn vs Guard: "
                + pairs(unchecked, aragorn, guard) + "), with rules=\"" + checked + "\"");

        assertFalse(checked, pairs(checked, aragorn, guard));
        assertTrue(checked, engineAccepts(scn, checked));
    }
}
