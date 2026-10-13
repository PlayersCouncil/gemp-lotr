package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_084_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sleep", "51_84");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: companion, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf, vitality 4
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, support area
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] Shadow condition, plays on Aragorn
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] Free Peoples condition, support area
					put("saga", "1_114");       // The Saga of Elendil: [gondor] Free Peoples condition on a [gondor] companion
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SleepCaradhrasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sleep, Caradhras
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spell.
		 * Exert Gandalf to discard a Shadow condition and then hinder all other conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sleep");

		assertEquals("Sleep, Caradhras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToDiscardAChosenShadowConditionAndThenHindersEveryOtherActiveCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("sleep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		// The Shadow player's own Free Peoples condition, which is not active on this player's turn.
		var shadowStairs = scn.GetShadowCard("stairs");
		var pinned = scn.GetShadowCard("pinned");
		var legacy = scn.GetShadowCard("legacy");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(stairs, shadowStairs, pinned);
		scn.AttachCardsTo(aragorn, saga, legacy);
		scn.AttachCardsTo(gandalf, poison);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(card));

		scn.FreepsPlayCard(card);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Shadow conditions of two cultures, in the support area and on a companion; no Free Peoples condition,
		// and no Shadow card that is not a condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, legacy));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stairs, saga, shadowStairs, poison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pinned);

		assertInDiscard(pinned, card);
		assertFalse(scn.IsHindered(pinned));
		assertTrue(scn.IsHindered(legacy));
		assertTrue(scn.IsHindered(stairs));
		assertTrue(scn.IsHindered(saga));
		assertTrue(scn.GetAttachedCards(aragorn).containsAll(java.util.List.of(legacy, saga)));
		assertFalse(scn.IsHindered(shadowStairs));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowConditionStillHindersEveryCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("sleep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(stairs);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(gandalf, poison);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(card));

		// The cost can be paid, so the event is played; it does as much of its effect as it can.
		scn.FreepsPlayCard(card);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(card);
		assertInZone(Zone.ATTACHED, poison);
		assertTrue(scn.IsHindered(stairs));
		assertTrue(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(poison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenGandalfIsExhaustedThoughAnotherCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("sleep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var stairs = scn.GetFreepsCard("stairs");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);
		scn.MoveCardsToSupportArea(stairs, pinned);
		scn.AddWoundsToChar(gandalf, 2);

		scn.StartGame();

		// Vitality 4 with 2 wounds: Gandalf can still exert.
		assertTrue(scn.FreepsPlayAvailable(card));

		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsRefreshDecision();
		// Vitality 4 with 3 wounds: exhausted.  Aragorn and Ent Avenger (a [gandalf] companion) could exert, but
		// neither is Gandalf.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertFalse(scn.FreepsPlayAvailable(card));
		assertFalse(scn.IsHindered(stairs));
		assertFalse(scn.IsHindered(pinned));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
