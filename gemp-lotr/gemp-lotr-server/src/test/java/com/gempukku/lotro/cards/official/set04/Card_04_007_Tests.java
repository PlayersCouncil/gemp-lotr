package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "4_7");

					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1; no text
					put("warrior", "4_18");     // Dunlending Warrior: [dunland] Man, strength 9; an assignment ability that needs an ally, and there is none
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] minion who is no Man; may not be assigned to a skirmish
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6; +3 and fierce only at 2 burdens, and there are none

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dark Fury
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [dunland] Man strength +2 (or +4 if at a sanctuary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Dark Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndAwayFromASanctuaryMakesADunlandManPlusTwoForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(fury);
		assertInHand(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, warrior, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman goes down after the move, which his own text answers by exerting companions.
		scn.MoveMinionsToTable(saruman);
		// Ettenmoors, site 2: no sanctuary.
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fury));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, warrior});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(warrior));
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.ShadowPlayAvailable(fury));
		scn.ShadowPlayCard(fury);

		// A [dunland] Man in this skirmish or in one still to come; not the [dunland] minion who is no Man, the
		// [raider] Man or the [gondor] Man companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, warrior));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warrior);

		assertInDiscard(fury);
		// 9 + 2
		assertEquals(11, scn.GetStrength(warrior));
		assertEquals(11, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Savage beats Aragorn, 11 to 8; that skirmish was the phase the +2 lasted for.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, warrior));
		assertEquals(9, scn.GetStrength(warrior));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtRivendellValleyASanctuaryTheDunlandManIsPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(fury);
		assertInHand(fury);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		// Rivendell Valley, site 3: a sanctuary.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(11, scn.GetStrength(savage));
		scn.ShadowPlayCard(fury);

		assertInDiscard(fury);
		// 11 + 4, the +4 in place of the +2
		assertEquals(15, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtGaladrielsGladeASanctuaryTheDunlandManIsPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fury);
		assertInHand(fury);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		// Galadriel's Glade, site 6: a sanctuary.
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(11, scn.GetStrength(savage));
		scn.ShadowPlayCard(fury);

		assertInDiscard(fury);
		// 11 + 4
		assertEquals(15, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
