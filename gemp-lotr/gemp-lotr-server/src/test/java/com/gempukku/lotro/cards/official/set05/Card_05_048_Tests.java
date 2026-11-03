package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crawling", "5_48");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("legion", "17_129");    // White Hand Legion: [uruk-hai] Uruk-hai, vitality 3; damage +1, to play remove 2 threats (cheated in)
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; "Shadow: Exert this minion to add (1)", used only on purpose

					// [isengard] weapons, each a hand weapon possession:
					put("sword", "1_160");      // Uruk-hai Sword: cost 1, bearer an Uruk-hai; a trigger on its bearer's win, never reached
					put("axe", "3_55");         // Isengard Axe: cost 0, bearer an [isengard] Orc; "When you play this weapon, add (1)"
					put("banner", "6_59");      // Banner of Isengard: cost 0, bearer an [isengard] minion; a trigger on its bearer's win, never reached
					// Spear of the White Hand: an [uruk-hai] hand weapon of cost 0 with no text, playable on the Legion
					put("spear", "17_117");
					// Scaffolding: an [isengard] possession of cost 1 that is no weapon, playable to the support area
					put("scaffolding", "6_73");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his maneuver ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackShapesCrawlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Black Shapes Crawling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot X Uruk-hai. The Free Peoples player may assign X wounds to your minions. Play X [isengard] weapons from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crawling");

		assertEquals("Black Shapes Crawling", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsTwoUrukHaiForTwoWoundsOnOneMinionAndTwoIsengardWeaponsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crawling = scn.GetShadowCard("crawling");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		var sword = scn.GetShadowCard("sword");
		var axe = scn.GetShadowCard("axe");
		var banner = scn.GetShadowCard("banner");
		var spear = scn.GetShadowCard("spear");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(crawling);
		scn.MoveCardsToDiscard(sword, axe, banner, spear, scaffolding);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, legion, forger);
		assertInHand(crawling);
		assertInDiscard(sword, axe, banner, spear, scaffolding);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(crawling));
		scn.ShadowPlayCard(crawling);
		// Two Uruk-hai, of two cultures, can be spotted; the [isengard] Orc is no Uruk-hai. Three of the weapons
		// could be played (one on each minion), so the Uruk-hai are what bound X.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// The Free Peoples player places the two wounds on any of the Shadow player's minions, both on one if he likes.
		assertEquals(0, scn.GetWoundsOn(legion));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, legion, forger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(legion);
		assertEquals(1, scn.GetWoundsOn(legion));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, legion, forger));
		scn.FreepsChooseCard(legion);
		assertEquals(2, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(forger));

		// Two [isengard] weapons, of any bearer; not the [uruk-hai] weapon, nor the [isengard] possession that is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, axe, banner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, scaffolding));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);
		assertAttachedTo(sword, legion);

		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, banner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, scaffolding));
		scn.ShadowChooseCard(axe);
		// The Isengard Forger is the one [isengard] Orc: the Axe goes on him unasked.
		assertAttachedTo(axe, forger);

		assertInDiscard(banner, spear, scaffolding, crawling);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedMinionWoundedIsKilledAndAWoundMayBeLeftUnplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crawling = scn.GetShadowCard("crawling");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		var sword = scn.GetShadowCard("sword");
		var banner = scn.GetShadowCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(crawling);
		scn.MoveCardsToDiscard(sword, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, legion, forger);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(forger, 1);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertInHand(crawling);
		assertInDiscard(sword, banner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(crawling);
		scn.ShadowChoose("2");

		// A wound, not an exertion: the exhausted Forger is offered and is killed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, legion, forger));
		scn.FreepsChooseCard(forger);
		assertInZone(Zone.DISCARD, forger);
		// Each of the X wounds is the Free Peoples player's to place or not (Ketura, Q-0397: "each wound should be
		// 0-1 up to X").
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(legion));

		// One of the two wounds forfeited, and still two weapons played (Ketura, Q-0397: forfeiting the rest of X
		// "doesn't change how many of X weapons Shadow gets to play from discard").
		scn.ShadowChooseCard(sword);
		scn.ShadowChooseCard(legion);
		assertAttachedTo(sword, legion);
		// The Banner goes on the one [isengard] minion left, unasked.
		assertAttachedTo(banner, savage);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayPlaceNoWoundsAndTheWeaponsAreStillPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crawling = scn.GetShadowCard("crawling");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var sword = scn.GetShadowCard("sword");
		var banner = scn.GetShadowCard("banner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(crawling);
		scn.MoveCardsToDiscard(sword, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, legion);
		assertInHand(crawling);
		assertInDiscard(sword, banner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(crawling);
		// One of the two Uruk-hai spotted: X is 1.
		scn.ShadowChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, legion));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(legion));

		// No wound placed, and the one weapon of X is still played (Q-0397); one only.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, banner));
		scn.ShadowChooseCard(banner);
		assertAttachedTo(banner, savage);
		assertInDiscard(sword, crawling);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crawling = scn.GetShadowCard("crawling");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(crawling);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInHand(crawling);
		assertInDiscard(sword);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(crawling));
	}

	@Test
	public void XIsHeldToTheIsengardWeaponsThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crawling = scn.GetShadowCard("crawling");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(crawling);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, legion);
		assertInHand(crawling);
		assertInDiscard(sword);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(crawling);
		// Two Uruk-hai, and one [isengard] weapon in the discard pile: the play of X weapons is a cost, and X stops
		// at what it can pay.
		assertTrue(scn.ShadowDecisionAvailable("how many Uruk-hai to spot"));
		assertEquals(1, scn.ShadowGetChoiceMax());
	}
}
