package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("business", "2_44");
					put("uruk", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; damage +1 and nothing else
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc; discarded underground; a Shadow ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a [moria] minion; cheated in, so its text does nothing
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] condition in the support area; its text is about archery
					put("keyward", "12_105");   // Troll's Keyward: [orc] Orc, vitality 3. "Shadow: Exert Troll's Keyward and reveal your hand to add (1) for each [orc] Troll revealed."

					put("curse", "1_36");       // Curse Their Foul Feet!: an [elven] event, for the Free Peoples hand; it is never played
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; her text is about skirmishing a Nazgul
					put("mallorn", "1_54");     // Mallorn-trees: "Fellowship: Reveal an [elven] card from hand and place it beneath your draw deck."
				}},
				// Site 6 is Galadriel's Glade: "Sanctuary. Fellowship: Exert an Elf to look at an opponent's hand."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoBusinessofOursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: No Business of Ours
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.
		 * 		The Free Peoples player may not look at or reveal cards in any Shadow player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("business");

		assertEquals("No Business of Ours", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoBusinessSpotsAnIsengardMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(business, uruk);

		scn.StartGame();
		// A [moria] minion and an [isengard] condition: neither is an [isengard] minion.
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(armory);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, business);
		assertFalse(scn.ShadowPlayAvailable(business));

		scn.ShadowPlayCard(uruk);
		assertTrue(scn.ShadowPlayAvailable(business));

		// It costs nothing.
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(business);
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, business);
		assertNotAttachedTo(business, uruk);
		assertNotAttachedTo(business, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcMayBeSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCardsToHand(business);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(business));

		scn.ShadowPlayCard(business);
		assertInZone(Zone.SUPPORT, business);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsCannotLookAtTheShadowHandByASitesText() throws DecisionResultInvalidException, CardNotFoundException {
		// The one act stopped here.  Each definition word that looks at or reveals another player's hand is shown
		// stopped under the bar in mechanics/ABarOnLookingAtOrRevealingAHandTests (Ketura on Q-0264: B2).
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(business);

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(uruk, armory);

		var glade = scn.GetCurrentSite();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(glade));

		// Without the card, the site shows the whole hand. Arwen is the only Elf, so she pays.
		scn.FreepsUseCardAction(glade);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// With it, the Elf exerts and nothing is shown.
		assertTrue(scn.FreepsActionAvailable(glade));
		scn.FreepsUseCardAction(glade);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillRevealHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var keyward = scn.GetShadowCard("keyward");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(business);
		scn.MoveCardsToHand(uruk, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, business);
		assertEquals(0, scn.GetWoundsOn(keyward));
		assertTrue(scn.ShadowActionAvailable(keyward));
		int twilight = scn.GetTwilight();

		// The restriction is on the Free Peoples player: a hand its own owner reveals is shown to him.
		scn.ShadowUseCardAction(keyward);
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// No [orc] Troll among them, so nothing is added.
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayStillRevealCardsFromHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.MoveCardsToSupportArea(business);

		var mallorn = scn.GetFreepsCard("mallorn");
		var curse = scn.GetFreepsCard("curse");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(curse);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, business);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(mallorn));

		// The restriction is on a Shadow player's hand: the one [elven] card in his own is shown to the Shadow player.
		scn.FreepsUseCardAction(mallorn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(curse, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, curse);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
