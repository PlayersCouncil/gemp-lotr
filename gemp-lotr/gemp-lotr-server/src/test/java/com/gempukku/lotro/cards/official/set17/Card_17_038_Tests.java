package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "17_38");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Shelob, Eater of Light: fierce [gollum] Spider, strength 8, vitality 8; her play trigger is
					// never met (she is put on the table) and nothing is stacked for her strength text.
					put("shelob", "8_25");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally, vitality 2; his maneuver ability is not used
					put("thrarin", "1_27");

					// The White Council: "While you can spot Gandalf and 2 [elven] allies, the first sentence of
					// Saruman's game text does not apply."  Its regroup ability is not used.
					put("council", "101_18");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength by races; no other text
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] ally; a skirmish ability
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] ally; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Saruman, Servant of Sauron
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Follower
		 * Strength: +2
		 * Game Text: <b>Aid</b> – (1). <helper>(At the start of the maneuver phase, you may remove (1) to transfer this to a minion.)</helper>
		 * 	Each time bearer wins a skirmish, you may exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Servant of Sauron", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, scn.GetAidTwilightCost(card));
	}

	@Test
	public void HisAidRemovesOneToTransferHimToAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));

		// The Troop is the one minion, and takes him without a choice.
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(pool - 1, scn.GetTwilight());
		assertAttachedTo(saruman, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenBearerWinsASkirmishACompanionMayBeExertedButNotWhenAnotherMinionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.AttachCardsTo(troop, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		// His Aid is offered for him where he is borne already; he stays with the Troop.
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, brigand}, new PhysicalCardImpl[]{aragorn, troop});

		// The Brigand, who does not bear him, beats Gimli first (8 to 6): nothing is offered.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));

		// The Troop, his bearer, beats Aragorn (13 + 2 to 8) without killing him.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();

		// Any companion: the loser, a companion who fought elsewhere, and the Ring-bearer who did not fight.
		// Not an ally, and not a minion, though each could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrarin, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.ShadowChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheExertionWhenBearerWinsExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		// His Aid is offered for him where he is borne already; he stays with the Troop.
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedBearerWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Frodo and Aragorn (vitality 4 each) are exhausted.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		// His Aid is offered for him where he is borne already; he stays with the Troop.
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// An ally and the bearer himself could exert; no companion can.  (The win is answered before Aragorn's
		// wound for losing, engine pass 54; until then that wound was what exhausted him.)  The wound kills him.
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsACompanionMayBeExertedInTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var saruman = scn.GetShadowCard("saruman");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AttachCardsTo(shelob, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToAssignments();

		// Shelob, 8 + 2, beats Aragorn (8) without killing him.
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// She is fierce, and beats Aragorn a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gimli);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWhiteCouncilLeavesHisAidAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var golradir = scn.GetFreepsCard("golradir");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(golradir, celeborn);
		scn.MoveCardsToSupportArea(council);

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Gandalf and two [elven] allies: the Council's switch is thrown.  "Aid - (1)" opens his text, and he
		// is still offered his Aid, and transferred by it.
		assertInZone(Zone.SUPPORT, council, saruman);
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(pool - 1, scn.GetTwilight());
		assertAttachedTo(saruman, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
