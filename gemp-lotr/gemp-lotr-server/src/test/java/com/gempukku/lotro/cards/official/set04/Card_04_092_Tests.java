package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tall", "4_92");

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability only ever used on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion to bear the cards below

					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] Free Peoples condition, support area
					put("saga", "1_114");       // The Saga of Elendil: [gondor] Free Peoples condition on a [gondor] companion
					put("arms", "7_252");       // Strong Arms: [rohan] Free Peoples condition that plays on a site
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, support area
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] Shadow condition, plays on Aragorn
					put("spies", "1_140");      // Spies of Saruman: [isengard] Shadow condition that plays on a site
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion
					put("coat", "2_105");       // Mithril-coat: [shire] artifact borne by Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrownSuddenlyTallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Grown Suddenly Tall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> Spot Gandalf to discard all conditions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tall");

		assertEquals("Grown Suddenly Tall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGandalfDiscardsEveryActiveConditionAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var arms = scn.GetFreepsCard("arms");
		var coat = scn.GetFreepsCard("coat");
		var pinned = scn.GetShadowCard("pinned");
		var legacy = scn.GetShadowCard("legacy");
		var spies = scn.GetShadowCard("spies");
		var poison = scn.GetShadowCard("poison");
		// Inactive conditions on the Free Peoples player's turn: the Shadow player's own Free Peoples condition,
		// and the Free Peoples player's own Shadow condition.
		var shadowStairs = scn.GetShadowCard("stairs");
		var freepsPinned = scn.GetFreepsCard("pinned");

		scn.MoveCardsToHand(tall);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(stairs, pinned, shadowStairs, freepsPinned);
		scn.AttachCardsTo(aragorn, saga, legacy, poison);
		scn.AttachCardsTo(frodo, coat);

		scn.StartGame();
		var site = scn.GetCurrentSite();
		scn.AttachCardsTo(site, arms, spies);

		assertInZone(Zone.SUPPORT, stairs, pinned, shadowStairs, freepsPinned);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(legacy, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(coat, frodo);
		assertAttachedTo(arms, site);
		assertAttachedTo(spies, site);

		assertTrue(scn.FreepsPlayAvailable(tall));
		scn.FreepsPlayCard(tall);

		// Conditions of both sides, in a support area, on a companion and on a site; the event itself is gone.
		assertInDiscard(tall, stairs, saga, arms, pinned, legacy, spies);
		// A Shadow possession and a Free Peoples artifact are no conditions.
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(coat, frodo);
		// Inactive conditions are not discarded.
		assertInZone(Zone.SUPPORT, shadowStairs, freepsPinned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenOnlyAGandalfCompanionWhoIsNotGandalfCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var avenger = scn.GetFreepsCard("avenger");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(tall);
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, pinned);
		assertFalse(scn.FreepsPlayAvailable(tall));
		assertInZone(Zone.HAND, tall);
		assertInZone(Zone.SUPPORT, pinned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
