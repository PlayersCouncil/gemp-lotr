package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sleep", "1_84");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: companion, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf, vitality 4
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] Free Peoples condition, support area
					put("saga", "1_114");       // The Saga of Elendil: [gondor] Free Peoples condition on a [gondor] companion
					put("arms", "7_252");       // Strong Arms: [rohan] Free Peoples condition that plays on a site
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, support area
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] Shadow condition, plays on Aragorn
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion
					put("coat", "2_105");       // Mithril-coat: [shire] artifact borne by Frodo
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
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> Exert Gandalf to discard every condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sleep");

		assertEquals("Sleep, Caradhras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToDiscardEveryActiveConditionAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("sleep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var arms = scn.GetFreepsCard("arms");
		var coat = scn.GetFreepsCard("coat");
		var pinned = scn.GetShadowCard("pinned");
		var legacy = scn.GetShadowCard("legacy");
		var poison = scn.GetShadowCard("poison");
		// Inactive conditions on this player's turn: the Shadow player's own Free Peoples condition, and this
		// player's own Shadow condition.
		var shadowStairs = scn.GetShadowCard("stairs");
		var freepsPinned = scn.GetFreepsCard("pinned");

		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);
		scn.MoveCardsToSupportArea(stairs, pinned, shadowStairs, freepsPinned);
		scn.AttachCardsTo(aragorn, saga, legacy);
		scn.AttachCardsTo(gandalf, poison);
		scn.AttachCardsTo(frodo, coat);

		scn.StartGame();
		var site = scn.GetCurrentSite();
		scn.AttachCardsTo(site, arms);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(card));

		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Conditions of both sides, in a support area, on a companion and on a site.
		assertInDiscard(card, stairs, saga, arms, pinned, legacy);
		// A Shadow possession and a Free Peoples artifact are no conditions.
		assertInZone(Zone.ATTACHED, poison, coat);
		// Inactive conditions are not discarded.
		assertInZone(Zone.SUPPORT, shadowStairs, freepsPinned);
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
		assertInZone(Zone.SUPPORT, stairs, pinned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
