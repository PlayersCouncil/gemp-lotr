package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sorrow", "13_7");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability, never used
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, vitality 4; a maneuver ability used only on purpose

					// Cards that hold culture tokens; each gets them only as it is played.
					put("palaces", "6_10");     // Suspended Palaces: [dwarven] condition, a fellowship ability only
					put("chamber", "15_3");     // Chamber of Records: [dwarven] condition, a fellowship ability only
					put("strands", "9_22");     // Strands of Elven Hair: [elven] possession with [elven] tokens, a fellowship ability only

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SorrowSharedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Sorrow Shared
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Remove a [dwarven] token to heal a Dwarf.
		*/

		var scn = GetScenario();

		var sorrow = scn.GetFreepsCard("sorrow");

		assertEquals("Sorrow Shared", sorrow.getBlueprint().getTitle());
		assertNull(sorrow.getBlueprint().getSubtitle());
		assertFalse(sorrow.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sorrow.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, sorrow.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, sorrow.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(sorrow, Timeword.MANEUVER));
		assertEquals(1, sorrow.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesADwarvenTokenToHealAWoundedDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorrow = scn.GetFreepsCard("sorrow");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palaces = scn.GetFreepsCard("palaces");
		var chamber = scn.GetFreepsCard("chamber");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(palaces, chamber, strands);
		scn.AddTokensToCard(palaces, 2);
		scn.AddTokensToCard(chamber, 1);
		scn.AddTokensToCard(strands, 2);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(sorrow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(sorrow));
		scn.FreepsPlayCard(sorrow);
		// The token: from a card with a [dwarven] token, not from one with [elven] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(palaces, chamber));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strands));
		scn.FreepsChooseCard(palaces);
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(2, scn.GetCultureTokensOn(strands));

		// The heal: a wounded Dwarf companion or ally, not a wounded companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(sorrow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithNoDwarvenTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorrow = scn.GetFreepsCard("sorrow");
		var gimli = scn.GetFreepsCard("gimli");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(strands);
		scn.AddTokensToCard(strands, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.MoveCardsToHand(sorrow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertInHand(sorrow);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Only [elven] tokens are in play.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sorrow));
		assertEquals(2, scn.GetCultureTokensOn(strands));
	}

	@Test
	public void WithNoWoundedDwarfItIsPlayedAndTheTokenPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sorrow = scn.GetFreepsCard("sorrow");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(palaces);
		scn.AddTokensToCard(palaces, 2);
		// Only a companion who is no Dwarf is wounded.
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(sorrow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(sorrow));
		scn.FreepsPlayCard(sorrow);

		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(sorrow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
