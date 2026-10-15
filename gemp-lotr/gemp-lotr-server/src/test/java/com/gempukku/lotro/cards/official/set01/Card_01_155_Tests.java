package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spy", "1_155");
					put("spy2", "1_155");       // a second copy: not unique, and "this minion" is the one that responds

					// Saruman's Reach: [isengard] Maneuver event, cost 0, "Exert an Uruk-hai to make the opponent choose to
					// either exert 2 companions or make the Ring-bearer put on The One Ring until the regroup phase".
					put("reach", "1_137");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; Damage +1 is its whole text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc; a Shadow ability used only on purpose
					put("hounding", "11_188");  // Hounding Uruk: [uruk-hai] Uruk-hai, no [isengard] minion; +3 only while a companion is exhausted
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] condition in the support area; its ability used only on purpose

					// Cards laid on top of the Shadow player's draw deck.
					put("decktroop", "1_177");  // a Shadow minion
					put("sword", "1_160");      // Uruk-hai Sword: a Shadow possession
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card
					put("hunt", "1_126");       // Hunt Them Down!: a Shadow event
					put("reach2", "1_137");     // a further Shadow card, below the ones discarded
					put("hunt2", "1_126");      // a Shadow card already in the discard pile

					put("sam", "1_311");        // Sam, Son of Hamfast: a second companion, so that two can exert for Saruman's Reach
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSpyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Spy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Response:</b> If the Ring-bearer puts on The One Ring, exert this minion. Discard a card from the top of your draw deck for each [isengard] minion you spot. Add a burden for each Shadow card discarded in this way.
		*/

		var scn = GetScenario();

		var spy = scn.GetFreepsCard("spy");

		assertEquals("Uruk Spy", spy.getBlueprint().getTitle());
		assertNull(spy.getBlueprint().getSubtitle());
		assertFalse(spy.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spy.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, spy.getBlueprint().getCulture());
		assertEquals(CardType.MINION, spy.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, spy.getBlueprint().getRace());
		assertEquals(2, spy.getBlueprint().getTwilightCost());
		assertEquals(7, spy.getBlueprint().getStrength());
		assertEquals(2, spy.getBlueprint().getVitality());
		assertEquals(5, spy.getBlueprint().getSiteNumber());
	}

	@Test
	public void PuttingOnTheRingLetsThisSpyExertToDiscardOneCardForEachIsengardMinionAndAddABurdenForEachShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var spy = scn.GetShadowCard("spy");
		var spy2 = scn.GetShadowCard("spy2");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var hounding = scn.GetShadowCard("hounding");
		var troop = scn.GetShadowCard("troop");
		var ambition = scn.GetShadowCard("ambition");
		var decktroop = scn.GetShadowCard("decktroop");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetShadowCard("aragorn");
		var hunt = scn.GetShadowCard("hunt");
		var reach2 = scn.GetShadowCard("reach2");
		var hunt2 = scn.GetShadowCard("hunt2");
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		// Four [isengard] minions (two Spies and Uruk Savage, Uruk-hai; Isengard Forger, an Orc), beside an [uruk-hai]
		// Uruk-hai, a [moria] Orc and an [isengard] condition, none of which is an [isengard] minion.
		scn.MoveMinionsToTable(spy, spy2, savage, forger, hounding, troop);
		scn.MoveCardsToSupportArea(ambition);
		scn.SetShadowDrawDeck(decktroop, sword, aragorn, hunt, reach2);
		scn.MoveCardsToDiscard(hunt2);
		assertEquals(5, scn.GetShadowDeckCount());
		assertInDiscard(hunt2);
		int freepsDeck = scn.GetFreepsDeckCount();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(spy));
		assertEquals(0, scn.GetWoundsOn(spy2));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(reach);
		scn.ShadowChooseCard(savage);
		// Frodo is the only companion, so there are no two to exert: the Ring goes on unasked.
		assertTrue(scn.RBWearingOneRing());

		// Each copy offers its own response.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spy));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spy2));
		scn.ShadowChooseOptionalTrigger(spy);
		assertEquals(1, scn.GetWoundsOn(spy));
		assertEquals(0, scn.GetWoundsOn(spy2));

		// The Shadow player names how many [isengard] minions he spots, from none to the four there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("4");

		// The top four cards of his own deck are discarded, and the fifth stays; the Free Peoples deck is untouched.
		assertInDiscard(decktroop, sword, aragorn, hunt);
		assertInZone(Zone.DECK, reach2);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetShadowDiscardCount());
		// A minion, a possession and an event are Shadow cards; Aragorn is not, nor is the Hunt Them Down! that was
		// already in the discard pile.
		assertEquals(3, scn.FreepsGetBurdens());

		// The other copy may still respond, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spy2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(spy2));
		assertInDiscard(reach);
		assertEquals(3, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMaySpotFewerIsengardMinionsThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		var hunt = scn.GetShadowCard("hunt");
		var decktroop = scn.GetShadowCard("decktroop");
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(spy, savage);
		scn.SetShadowDrawDeck(hunt, decktroop);
		assertEquals(2, scn.GetShadowDeckCount());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(spy));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPlayCard(reach);
		scn.ShadowChooseCard(savage);
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spy));
		scn.ShadowChooseOptionalTrigger(spy);
		assertEquals(1, scn.GetWoundsOn(spy));

		// Two [isengard] minions; he spots one.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertInDiscard(hunt);
		assertInZone(Zone.DECK, decktroop);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheFreePeoplesPlayerExertsTwoCompanionsInsteadOfPuttingOnTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var spy = scn.GetShadowCard("spy");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(spy, savage);
		int shadowDeck = scn.GetShadowDeckCount();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(spy));

		scn.ShadowPlayCard(reach);
		scn.ShadowChooseCard(savage);
		scn.FreepsChoose("Exert 2 companions");
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.RBWearingOneRing());

		// No Ring put on, so no response: the game goes on to the Free Peoples player's maneuver actions.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(spy));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
	}

	@Test
	public void AnExhaustedSpyCannotRespond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(spy, savage);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(spy, 1);
		assertEquals(1, scn.GetWoundsOn(spy));
		int shadowDeck = scn.GetShadowDeckCount();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPlayCard(reach);
		// The Savage is the one Uruk-hai able to exert, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.RBWearingOneRing());

		// The exertion is the response's price: the Ring is on, and the Spy is not offered.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(spy));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.FreepsGetBurdens());
	}
}
