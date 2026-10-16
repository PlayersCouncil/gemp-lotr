package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alatar", "13_28");
					// Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; strength 6, +1 for each of
					// Hobbit, Dwarf, Elf and Man in the fellowship
					put("gandalf", "1_72");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					// Barliman Butterbur: a [gandalf] Man ally of cost 0; his only text is a fellowship ability
					put("barliman", "1_70");
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is an Ent and no Wizard; strength +2 only beside 3 Ents
					// Pallando, Far-travelling One: another [gandalf] follower, Aid (2); transferred to a [gandalf]
					// companion his own text asks nothing
					put("pallando", "13_37");
					// Fear and Great Wonder: [gandalf] support-area condition that carries [gandalf] tokens; its
					// maneuver ability is used only on purpose
					put("fear", "13_30");
					// Last Stand: [gandalf] support-area condition that carries [gandalf] tokens; its abilities are
					// used only on purpose
					put("laststand", "18_21");
					// A Path Appointed: [gondor] support-area condition that carries [gondor] tokens; its abilities
					// are used only on purpose
					put("path", "8_42");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1
					put("brigand", "4_10");       // Dunlending Brigand: [dunland] Man, strength 8, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlatarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Alatar, Final Envoy
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Follower
		 * Game Text: <b>Aid</b> – (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper>
		 * 	Each time you transfer this to a companion, you may reinforce a [gandalf] token.
		 * 	While you can spot a [gandalf] companion, each minion skirmishing bearer is strength -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alatar");

		assertEquals("Alatar", card.getBlueprint().getTitle());
		assertEquals("Final Envoy", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, scn.GetAidTwilightCost(card));
	}

	@Test
	public void EachTimeHisAidTransfersHimToACompanionAGandalfTokenMayBeReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetFreepsCard("alatar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		var laststand = scn.GetFreepsCard("laststand");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(alatar, fear, laststand, path);
		scn.AddTokensToCard(fear, 1);
		scn.AddTokensToCard(laststand, 2);
		scn.AddTokensToCard(path, 1);

		scn.StartGame();
		// Only there so that there is a maneuver phase; nobody is assigned to it.
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		// Aid - (2).
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(alatar, aragorn);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(2, scn.GetCultureTokensOn(laststand));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));

		scn.FreepsAcceptOptionalTrigger();

		// The cards with a [gandalf] token, and not the one with a [gondor] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, laststand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(fear);

		assertEquals(2, scn.GetCultureTokensOn(fear));
		assertEquals(2, scn.GetCultureTokensOn(laststand));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second maneuver phase in the same turn: the Aid taken again gives him to Gandalf, and that transfer
		// offers the reinforcement again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(alatar, gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(laststand);

		assertEquals(2, scn.GetCultureTokensOn(fear));
		assertEquals(3, scn.GetCultureTokensOn(laststand));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningTheReinforcementLeavesTheTokensAndAnotherFollowersTransferOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetFreepsCard("alatar");
		var pallando = scn.GetFreepsCard("pallando");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(alatar, pallando, fear);
		scn.AddTokensToCard(fear, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsChooseOptionalTrigger(alatar);
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(alatar, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar));
		assertEquals(1, scn.GetCultureTokensOn(fear));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertAttachedTo(alatar, aragorn);

		// Pallando's Aid is still to be answered.  His transfer is no transfer of Alatar.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsChooseOptionalTrigger(pallando);
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(pallando, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(alatar));
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGandalfTokenToReinforceTheTransferOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetFreepsCard("alatar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn);
		// A [gandalf] card with no token, and a token of another culture.
		scn.MoveCardsToSupportArea(alatar, fear, path);
		scn.AddTokensToCard(path, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(fear));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(alatar, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(alatar));
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithAGandalfCompanionInPlayEachMinionSkirmishingBearerIsStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetFreepsCard("alatar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, alatar);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		// The Aid is offered again for a follower already borne: declined, he stays on Aragorn.
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gandalf, brigand});
		// The Uruk Savage left over is the Shadow player's to assign: a second minion against Aragorn.
		scn.ShadowAssignToMinions(aragorn, savage);

		// Assigned to bearer, but his skirmish has not begun.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(brigand));

		scn.FreepsResolveSkirmish(gandalf);

		// A minion skirmishing another companion.
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(12, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AGandalfCompanionWhoIsNoWizardIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetFreepsCard("alatar");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.AttachCardsTo(aragorn, alatar);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, troop);
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(12, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionInPlayAMinionSkirmishingBearerKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alatar = scn.GetFreepsCard("alatar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var fear = scn.GetFreepsCard("fear");
		var barliman = scn.GetFreepsCard("barliman");
		var troop = scn.GetShadowCard("troop");
		// A [gandalf] ally and a [gandalf] condition, neither a companion, and companions of other cultures.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(fear);
		scn.AttachCardsTo(aragorn, alatar);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
