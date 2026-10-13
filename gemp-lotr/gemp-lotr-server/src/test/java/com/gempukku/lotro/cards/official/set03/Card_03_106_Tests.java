package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bill", "3_106");
					put("sam", "1_311");      // Sam, Son of Hamfast: [shire] Hobbit companion; Fellowship ability used only on purpose
					put("pippin", "103_31");  // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in past his own requirement)
					put("aragorn", "1_89");   // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("maggot", "1_288");   // Farmer Maggot, Chaser of Rascals: a Hobbit who is an ally, not a companion
					put("pan", "3_108");      // Frying Pan: a possession a Hobbit bears, with a Skirmish ability only
					put("sword", "1_299");    // Hobbit Sword: a Free Peoples possession of cost 1, no text, for a cost read in hand
					put("troop", "1_177");    // Goblin Patrol Troop: a minion of cost 6, no text, for a cost read in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BillthePonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Bill the Pony
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Possession
		 * Game Text: Bearer must be a Hobbit companion.
		 * The Shadow number of each site is -1.
		 * Discard Bill the Pony when at an underground site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bill");

		assertEquals("Bill the Pony", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnAHobbitCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(bill);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(bill));
		scn.FreepsPlayCard(bill);
		// The [shire] Ring-bearer and the [gondor] Hobbit; not a Man companion, nor a Hobbit ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, maggot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(bill, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LowersTheShadowNumberOfTheSiteMovedToAndStaysAwayFromUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, bill);

		scn.StartGame();
		assertAttachedTo(bill, sam);
		assertEquals(0, scn.GetTwilight());

		// To Ettenmoors, Shadow number 2, a plains site.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertEquals(3, scn.GetTwilight()); // 2 - 1 for the site + 2 companions
		assertAttachedTo(bill, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EverySiteIsOneLessOnceHeIsPlayedUntilAnUndergroundSiteDiscardsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		var pan = scn.GetFreepsCard("pan");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(sam, pan);
		scn.SetFreepsHand(bill, sword);
		scn.SetShadowHand(troop);
		scn.FreepsRefreshDecision();

		// Bag End 0 and Ettenmoors 2 behind the fellowship, Rivendell Valley 0 where it stands; cards that are no
		// sites, by what each would cost to play.
		var bagend = scn.GetSite(1);
		var ettenmoors = scn.GetSite(2);
		var rivendell = scn.GetSite(3);
		assertEquals(0, scn.GetTwilightCost(bagend));
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		assertEquals(0, scn.GetTwilightCost(rivendell));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.FreepsPlayCard(bill);
		scn.FreepsChooseCard(sam);
		assertAttachedTo(bill, sam);

		// A site behind the fellowship is one less; 0 - 1 counts as 0 (CR 4.2 "modifier").
		assertEquals(0, scn.GetTwilightCost(bagend));
		assertEquals(1, scn.GetTwilightCost(ettenmoors));
		assertEquals(0, scn.GetTwilightCost(rivendell));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(6, scn.GetTwilightCost(troop));

		// To Balin's Tomb, Shadow number 3, underground.  Decipher's ruling on 3_106 (Q-0133): "When the fellowship
		// moves with Bill the Pony to an underground site, the Shadow number of that site is reduced before Bill the
		// Pony is discarded."
		scn.SetTwilight(0);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilight()); // 3 - 1 for the site + 2 companions
		assertInDiscard(bill);
		assertAttachedTo(pan, sam);
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtAnUndergroundSiteHeIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(bill);
		scn.FreepsRefreshDecision();

		// Balin's Tomb is underground: being there is a state (Q-0133), so Bill is discarded as he comes into play.
		assertTrue(scn.FreepsPlayAvailable(bill));
		scn.FreepsPlayCard(bill);
		scn.FreepsChooseCard(sam);
		assertInDiscard(bill);
		assertEquals(0, scn.GetAttachedCards(sam).size());
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
