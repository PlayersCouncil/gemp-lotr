package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wake", "4_35");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, cost 2, strength 8, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man, not fierce.  His trigger
					// answers a move: he is laid after it.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man; strength +3 and fierce only at 2 burdens.
					put("axeman", "4_224");

					// For a [dunland] Man who comes into play after the event: A Shadow Fell Over Them ("Assignment:
					// Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile") plays Úlairë Nertëa, whose
					// "When you play" plays a minion from the discard pile with 5 companions in play.  Úlairë Lemenya
					// is the Nazgûl spotted; her text is about [wraith] conditions on companions.
					put("fell", "15_182");
					put("nertea", "1_234");
					put("lemenya", "1_232");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WakeofDestructionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wake of Destruction
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot 2 sites you control to make each of your [dunland] Men <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wake");

		assertEquals("Wake of Destruction", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void With2SitesControlledEachDunlandManIsMadeFierceAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wake = scn.GetShadowCard("wake");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.MoveCardsToHand(wake);
		assertInHand(wake);

		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage, saruman, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(wake));

		scn.ShadowPlayCard(wake);
		assertInDiscard(wake);
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		// A [dunland] minion who is no Man, and a Man of another culture.
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneSiteControlledAndAnotherControlledByTheOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wake = scn.GetShadowCard("wake");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		scn.MoveCardsToHand(wake);
		assertInHand(wake);

		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wake));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void TheManStaysFierceIntoHisFierceSkirmishAndIsNotFierceOnceTheRegroupPhaseBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wake = scn.GetShadowCard("wake");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(wake);
		assertInHand(wake);

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(wake);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 11 against 8: Aragorn loses, and the Savage goes on to the fierce round.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void ADunlandManWhoComesIntoPlayAfterTheEventIsNotMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, gimli, sam, guard);

		var wake = scn.GetShadowCard("wake");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var fell = scn.GetShadowCard("fell");
		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(wake);
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToDiscard(nertea, brigand);
		assertInHand(wake);
		assertInDiscard(nertea, brigand);
		scn.SetTwilight(20);

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(wake);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowUseCardAction(fell);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		// Five companions, one over 4: one minion from the discard pile.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
