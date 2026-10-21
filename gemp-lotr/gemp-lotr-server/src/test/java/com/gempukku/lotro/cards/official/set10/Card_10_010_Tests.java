package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fleet", "10_10");
					put("fleet2", "10_10");
					// Lorien Elf: Elf companion, strength 4, vitality 2; "To play, spot an Elf", so it is cheated in
					put("elf", "1_53");
					// Silinde, Elf of Mirkwood: Elf ally, home site 3, strength 5, vitality 2; has the text of site 3
					// (Rivendell Valley: a fellowship action) while it can be spotted
					put("silinde", "1_60");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability used
					// only on purpose
					put("boromir", "1_97");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Footman of Dunland: [men] Man, strength 7, vitality 3; the site gains plains
					put("footman", "11_82");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");
					// The Mirror of Galadriel: [elven] possession in the support area; its ability is used only on purpose
					put("mirror", "1_55");
					// Burning of Westfold: [isengard] condition in the support area; its ability is used only on purpose
					put("burning", "4_144");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FleetfootedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Fleet-footed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a minion skirmishing an Elf strength -2. Spot a site in a support area to place this event on top of your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fleet");

		assertEquals("Fleet-footed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAMinionSkirmishingAnElfStrengthMinusTwoAndWithNoSiteInASupportAreaIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet = scn.GetFreepsCard("fleet");
		var fleet2 = scn.GetFreepsCard("fleet2");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var mirror = scn.GetFreepsCard("mirror");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		var burning = scn.GetShadowCard("burning");
		scn.MoveCompanionsToTable(elf, boromir);
		scn.MoveCardsToHand(fleet, fleet2);
		// A card in each player's support area, neither of them a site; every site is on the adventure path.
		scn.MoveCardsToSupportArea(mirror, burning);
		assertInZone(Zone.HAND, fleet, fleet2);
		assertInZone(Zone.SUPPORT, mirror, burning);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		// The minion left over: the Shadow player puts the Footman on the Elf as well.
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fleet));

		scn.FreepsPlayCard(fleet);

		// In Boromir's skirmish the Troop fights a companion who is no Elf, and the two minions assigned to the
		// Elf are not skirmishing yet: the event is played for nothing.  No site is in a support area, so it is
		// discarded.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertInDiscard(fleet);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, mirror, burning);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(elf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(fleet2);

		// Both minions in the Elf's skirmish; not the Troop, whose skirmish is over, and not the Elf, who is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, elf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		assertEquals(5, scn.GetStrength(footman));     // 7 - 2
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(fleet2);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithASiteInTheShadowSupportAreaAMinionSkirmishingAnElfAllyIsWeakenedForThatSkirmishAndTheEventGoesOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet = scn.GetFreepsCard("fleet");
		var silinde = scn.GetFreepsCard("silinde");
		var elf = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(fleet);
		assertInZone(Zone.HAND, fleet);

		scn.StartGame();
		scn.SkipToSite(2);
		// Both markers are at site 2: the Shadow player takes control of site 1, which goes to his support area.
		var site1 = scn.GetFreepsSite(1);
		assertFalse(scn.IsSiteControlled(site1));
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		assertInZone(Zone.SUPPORT, site1);
		scn.MoveMinionsToTable(savage, troop);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, savage},
				new PhysicalCardImpl[]{elf, troop}
		);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		scn.FreepsPlayCard(fleet);

		// The Savage is the one minion skirmishing an Elf (the ally), and is chosen unasked; the Troop, assigned to
		// the Lorien Elf and not yet skirmishing, keeps its strength.  The event is the new top card of the deck.
		assertEquals(3, scn.GetStrength(savage));      // 5 - 2
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertSame(fleet, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, fleet);
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Silinde (5) beats the Savage (3) without overwhelming it; it takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(elf);

		// The event's strength -2 lasts for the skirmish phase it was played in.
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASiteInTheFreePeoplesPlayersOwnSupportAreaPutsTheEventOnTopOfTheDrawDeckAndItIsNoAssignmentAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fleet = scn.GetFreepsCard("fleet");
		var elf = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(fleet);
		assertInZone(Zone.HAND, fleet);

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 in the Free Peoples player's own support area, as a site he took on his opponent's turn would be.
		var site1 = scn.GetFreepsSite(1);
		assertFalse(scn.IsSiteControlled(site1));
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		assertInZone(Zone.SUPPORT, site1);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The assignment phase, next to the skirmish phase: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fleet));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(fleet);

		assertEquals(3, scn.GetStrength(savage));      // 5 - 2
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertSame(fleet, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
