package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "5_91");
					put("shield2", "5_91");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions or Eowyn can be
					// spotted (here two); his trigger answers a [rohan] Man exerting, which nobody does here
					put("merry", "103_85");
					// Legolas, Greenleaf: an archer; his archery ability is used only on purpose
					put("legolas", "1_50");

					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer minion; Archer is its whole text
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rohirrim Shield
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	The minion archery total is -1.
		 * 	<b>Regroup:</b> Discard this possession to heal bearer.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Rohirrim Shield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndMakesTheMinionArcheryTotalMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(guard, aragorn, merry, legolas);
		scn.MoveAlliesToTable(herdsman);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveCardsToHand(shield);
		assertInHand(shield);
		// The minions stand on the table later in the turn; laid now, the totals are read before and after the play.
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.FreepsRefreshDecision();

		// Two Shadow archers, one Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(shield);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, not a [rohan] Hobbit, not an Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		assertAttachedTo(shield, herdsman);
		// The minion archery total goes down by one; the fellowship's does not.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// And so it stands when the archery phase comes.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsThisShieldToHealItsOwnBearerOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var shield2 = scn.GetFreepsCard("shield2");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, shield);
		scn.AttachCardsTo(herdsman, shield2);

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertAttachedTo(shield, guard);
		assertAttachedTo(shield2, herdsman);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(shield));
		assertTrue(scn.FreepsActionAvailable(shield2));

		// The Guard's shield: it goes to the discard pile and heals one wound of his, and nobody else's.
		scn.FreepsUseCardAction(shield);
		assertInDiscard(shield);
		assertAttachedTo(shield2, herdsman);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The other copy, borne by an ally, heals the ally.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(shield2));
		scn.FreepsUseCardAction(shield2);
		assertInDiscard(shield, shield2);
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, shield);

		var marksman1 = scn.GetShadowCard("marksman1");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertAttachedTo(shield, guard);
		scn.FreepsPass();
		scn.MoveMinionsToTable(marksman1);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		assertAttachedTo(shield, guard);
	}
}
